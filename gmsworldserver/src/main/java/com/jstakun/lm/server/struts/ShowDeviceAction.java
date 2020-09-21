package com.jstakun.lm.server.struts;

import java.io.IOException;
import java.util.Date;
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

import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;

import net.gmsworld.server.utils.persistence.Landmark;

public class ShowDeviceAction extends Action {

	private static final Logger logger = Logger.getLogger(ShowDeviceAction.class.getName());
	
	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,  final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		final String imei =  (String) request.getParameter("imei");
		
		if (StringUtils.isNotEmpty(imei)) {
			try {
				final String deviceJsonString = DevicePersistenceUtils.getDevice(imei);
				//{"output":{"creationDate":"2020-08-19T08:14:39","geo":"52.268144 20.952876 15.063 1597817679363","imei":"359044061052655","name":"Tablet-Natalii","token":"tokenString","username":"jaroslaw.stakun@gmail.com"}}
				JSONObject root = new JSONObject(deviceJsonString);
				if (root.has("output")) {
					 JSONObject deviceJson = root.getJSONObject("output");
					 String geo = deviceJson.getString("geo");
					 String[] tokens = StringUtils.split(geo, " ");
					 if (tokens.length > 1) {
						 Landmark landmark = new Landmark();
						 landmark.setLatitude(Double.parseDouble(tokens[0]));
						 landmark.setLongitude(Double.parseDouble(tokens[1]));
						 landmark.setName("Device " + deviceJson.getString("name"));
						 final String username = deviceJson.getString("username") ;
						 if (StringUtils.isNotEmpty(username)) {
							 landmark.setLayer(username + " devices");
						 } else {
							 landmark.setLayer("Devices");
						 }
						 if (tokens.length == 3) {
							 landmark.setCreationDate(new Date(Long.parseLong(tokens[2])));
						 } else if (tokens.length > 3) {
							 landmark.setAltitude(Double.parseDouble(tokens[2]));
							 landmark.setCreationDate(new Date(Long.parseLong(tokens[3])));
						 }
						 String description = "<a href=\"https://maps.google.com/maps?q=" + landmark.getLatitude() + "," + landmark.getLongitude() + "\">Open in Google Maps</a>";
						 landmark.setDescription(description);
						 request.setAttribute("landmark", landmark);
					 }
				}
			} catch (Exception e) {
				 logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return mapping.findForward("success");
	}
}
