package com.jstakun.lm.server.struts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONObject;
import org.json.JSONArray;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Notification;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.OtpUtils;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

import net.gmsworld.server.utils.persistence.Landmark;

public class ShowUserDevicesAction extends Action {
	
private static final Logger logger = Logger.getLogger(ShowUserDevicesAction.class.getName());
	
	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,  final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		final String secret = request.getParameter("secret");
		
		if (StringUtils.isNotEmpty(secret)) {
			Notification n = NotificationPersistenceUtils.findBySecret(secret);
			String username = null;
			if (n != null) {
				username = n.getId();
			}	else {
				User u =  UserPersistenceUtils.selectUserByLogin(null, secret);
				if (u != null) {
					username = u.getLogin();
				}
			}	
			if (username != null) {
				request.setAttribute("username", username);
				request.setAttribute("secret", secret);
				Double centerLat = 0.0;
	            Double centerLon = 0.0;
	            List<Landmark> devices = new ArrayList<Landmark>();
				
				final String userDevicesJsonString = DevicePersistenceUtils.getUserDevices(username);
				if (StringUtils.startsWith(userDevicesJsonString, "[")) {
					JSONArray userDevicesJson = new JSONArray(userDevicesJsonString);
					for (int i=0;i<userDevicesJson.length();i++) {
						 Landmark device = jsonToLandmark(userDevicesJson.getJSONObject(i));
						 if (device != null) {
							 devices.add(device);
							 centerLat += device.getLatitude();
			                 centerLon += device.getLongitude();
						 }
					}
				} else {
					logger.log(Level.WARNING, "Received follwing response: " + userDevicesJsonString);
				}
				
				if (! devices.isEmpty())  {
					centerLat /= devices.size();
		            centerLon /= devices.size();
		            request.setAttribute("centerLat", centerLat);
		            request.setAttribute("centerLon", centerLon);
		            request.setAttribute("collectionAttributeName", "devices");
		            request.setAttribute("devices", devices);
				}
			}
				 
			request.setAttribute("type", "device");	
		}
		
		return mapping.findForward("success");
	}
	
	protected static Landmark jsonToLandmark(JSONObject deviceJson) {
		 Landmark landmark = null;
		 final String imei = deviceJson.getString("imei");
		 final String geo = deviceJson.optString("geo");
		 if (StringUtils.isNotEmpty(geo)) {
			 String[] tokens = StringUtils.split(geo, " ");
			 if (tokens.length > 1) {
				 landmark = new Landmark();
				 landmark.setLatitude(Double.parseDouble(tokens[0]));
				 landmark.setLongitude(Double.parseDouble(tokens[1]));
				 if (tokens.length > 3) { //this is accuracy!
					 landmark.setAltitude(Double.parseDouble(tokens[2]));
				 } 
				 String deviceName = deviceJson.optString("name");
				 if (StringUtils.isEmpty(deviceName)) {
					 deviceName = "Unknown";
				 }
				 landmark.setName("Device " + deviceName);
				 //final String username = deviceJson.optString("username"); don't use it
				 landmark.setUsername(imei);
				 landmark.setLayer("Device Locator devices");
				 if (tokens.length == 3 && StringUtils.isNumeric(tokens[2])) {
					 long creationTimestamp = Long.parseLong(tokens[2]);
					 landmark.setCreationDate(new Date(creationTimestamp));
					 if (System.currentTimeMillis() - creationTimestamp > (1000 * 60 * 60 * 24)) {
						 sendLocationCommand(imei);
					 }
				 } else if (tokens.length > 3 && StringUtils.isNumeric(tokens[3])) {
					 landmark.setAltitude(Double.parseDouble(tokens[2]));
					 long creationTimestamp = Long.parseLong(tokens[3]);
					 landmark.setCreationDate(new Date(creationTimestamp));
					 if (System.currentTimeMillis() - creationTimestamp > (1000 * 60 * 60 * 24)) {
						 sendLocationCommand(imei);
					 }
				 }
				 landmark.setDescription("<a href=\"https://maps.google.com/maps?q=" + landmark.getLatitude() + "," + landmark.getLongitude() + "\">Open in Google Maps</a>");					 
			 }
		 } else {
			 sendLocationCommand(imei);
			 logger.log(Level.WARNING, "Device location not found");
		 }
		 return landmark;
	}
	
	private static void sendLocationCommand(final String imei) {
		try {
			final String token = OtpUtils.generateOtpToken(imei, null);	
			final String reply = DevicePersistenceUtils.sendCommand("locatedladmindlt " + token + " " + imei, ConfigurationManager.TELEGRAM_BOT_ID, "telegram"); 
		    logger.log(Level.INFO, "Command status: " + reply);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
