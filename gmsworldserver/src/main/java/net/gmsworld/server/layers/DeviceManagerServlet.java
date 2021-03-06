package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceWebUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

/**
 * Servlet implementation class DeviceManagerServlet
 */
public final class DeviceManagerServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DeviceManagerServlet.class.getName());   
    public static final double CACHE_DEVICE_DISTANCE = 0.005;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public DeviceManagerServlet() {
        super();
    }

    @Override
	public void init(ServletConfig config) throws ServletException {
		 super.init(config);
		 GeocodeHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
		 LayerHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (!HttpUtils.isEmptyAny(request, "imei")) {
				 int status = DevicePersistenceUtils.isDeviceRegistered(request.getParameter("imei"));
		         if (status == 1) {
		        	  out.print("{\"status\":\"verified\"}");
		         } else {
		        	  out.print("{\"status\":\"unverified\"}");
		         }
			} else if (!HttpUtils.isEmptyAny(request, "username"))  {
				final String devices = DevicePersistenceUtils.getUserDevices(request.getParameter("username"));
				out.print("{\"devices\":" + devices + "}");
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (!HttpUtils.isEmptyAny(request, "imei")) {
				 final String imei = request.getParameter("imei").trim();
				 final String token = request.getParameter("token");
				 final String username = request.getParameter("username");
				 final String command = request.getParameter("command");
				 final String args = request.getParameter("args");
				 final String correlationId = request.getParameter("correlationId");
				 final String action = request.getParameter("action");
				 final String replyToCommand = request.getParameter("replyToCommand"); 
				 String flex = request.getParameter("flex"); 
				 
				 String deviceName = request.getParameter("name");
				 if (StringUtils.isEmpty(deviceName)) {
					 deviceName = request.getHeader(Commons.DEVICE_NAME_HEADER);
				 }
				 final String deviceId = request.getHeader(Commons.DEVICE_ID_HEADER);
				 final String accuracy = request.getHeader(Commons.ACC_HEADER);
				 
				 Double latitude = null, longitude = null;
				 if (request.getHeader(Commons.LAT_HEADER) != null) {
			   	   	latitude = GeocodeUtils.getLatitude(request.getHeader(Commons.LAT_HEADER));
			   	 }
			   	 if (request.getHeader(Commons.LNG_HEADER) != null) {
			   	    longitude = GeocodeUtils.getLongitude(request.getHeader(Commons.LNG_HEADER));
			   	 }
			   	 
		         try {
		        	 final int version = NumberUtils.getInt(request.getHeader(Commons.APP_VERSION_HEADER), -1);
		        	 final int appId = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), -1);
					 if (version >= 28) {
		        		 if (flex == null) {
		        			 flex = processHeadersV2(request, deviceId, latitude, longitude, accuracy, version, appId);
		        		 } else {
		        			 flex += "," + processHeadersV2(request, deviceId, latitude, longitude, accuracy, version, appId);
		        		 }
		        	 } else {
		        		 if (flex == null) {
		        			 flex = processHeaders(request, latitude, longitude);
		        		 } else {
		        			 flex += "," + processHeaders(request, latitude, longitude);
		        		 }
		        	 }
		         } catch (Exception e) {
		        	 logger.log(Level.SEVERE, e.getMessage(), e);
		         }
		         		        		 
		         if (StringUtils.equalsIgnoreCase(token, "BLACKLISTED")) {
		        	 logger.log(Level.SEVERE, "Imei: " + imei + ", token: " + token);
	        		 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		         } else {
		        	 int status;
		        	 final String pinStr = request.getParameter("pin");
		        	 int pin = -1;
		        	 if (StringUtils.isNotEmpty(pinStr)) {
		        		 pin = NumberUtils.getInt(pinStr.trim(), -1);
				     }
		        	 Long count = -1L;
		        	 if (StringUtils.isNotEmpty(command) && pin >= 0) {
			        	 final String commandKey = getCommandKey(correlationId, imei, username, deviceName, command);
			        	 count = CacheUtil.increment(commandKey);
			        	 if (StringUtils.equalsIgnoreCase(action, "reset_quota")) {
			        		 CacheUtil.put(commandKey, 0, CacheType.NORMAL);
			        		 MailUtils.sendQuotaResetMail(StringUtils.split(commandKey, "_")[0], imei, command);
			        		 logger.log(Level.INFO, "Command " + commandKey + " has been reset to 0");
			        		 status = 1;
			        		 persistDeviceLocation(deviceId, deviceName, username, latitude, longitude, accuracy);
			        	 } else if (count < 10 || ((StringUtils.equals(command, "messagedlapp") || isCommandForSameDevice(commandKey)) && count < 50) || 
			        	     DevicePersistenceUtils.isValidCommand(replyToCommand) || 
			        	     (StringUtils.equals(command, "messagedlapp") && StringUtils.isNotEmpty(request.getHeader(Commons.ROUTE_ID_HEADER)))) {
			        		 logger.log(Level.INFO, "Command " + commandKey + " has been sent " + count + " times");
			        		 status = DevicePersistenceUtils.sendCommand(imei, pin, deviceName, username, command, args, correlationId, flex);	 
			        		 if (status == 1) {
			        			 CacheUtil.cacheDeviceLocation(deviceId, latitude, longitude, accuracy);
			        		 }
			        	 } else {
			        		 logger.log(Level.SEVERE, "Command " + commandKey + " has been rejected after " + count + " attempts");
			        		 status = -3;
			        		 persistDeviceLocation(deviceId, deviceName, username, latitude, longitude, accuracy); 
					     }        		 
		        	 } else if (StringUtils.equalsIgnoreCase(action, "delete")) {
		        		 status = DevicePersistenceUtils.deleteDevice(imei);
		        		 logger.log(Level.INFO, "Deleted device " + imei + " with status " + status);
		        	 } else { 
		        		 status = DevicePersistenceUtils.setupDevice(imei, deviceName, username, token, flex);
		        		 logger.log(Level.INFO, "Saved device " + imei + " configuration with status " + status + "\nNew configuration - name:" + deviceName + ", username:" + username + ", " + flex);
		        		 if (status == 1) {
		        			 CacheUtil.cacheDeviceLocation(deviceId, latitude, longitude, accuracy);
		        		 }
		        	 }	 
		        	 JSONObject reply = new JSONObject();
		        	 if (status == 1) {
		        		 reply.put("status", "ok");
		        	 } else if (status == -2) {
		        		 reply.put("status", "failed");
		        		 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		        	 } else if (status == -3) {
		        		 reply.put("status", "failed");
		        		 response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		        	 } else if (status == -4) {
		        		 reply.put("status", "failed");
		        		 response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        	 } else if (status == -5) {
		        		 reply.put("status", "failed");
		        		 response.setStatus(HttpServletResponse.SC_GONE);
		        	 } else {
		        		 reply.put("status", "failed");
		        		 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		        	 }
		        	 reply.put("count", count).put("statusId", status);
		        	 out.print(reply.toString());
		         }
			} else if (!HttpUtils.isEmptyAny(request, "username", "action"))  {
				if (StringUtils.equalsIgnoreCase(request.getParameter("action"), "list")) {
					final String devices = DevicePersistenceUtils.getUserDevices(request.getParameter("username"));
					out.print("{\"devices\":" + devices + "}");
				} else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else { 
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}  
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}
 
	private String processHeaders(HttpServletRequest request, Double latitude, Double longitude) {
		String flex = null;
   	   	if (latitude != null && longitude != null) {
   	   		flex = "geo:" + latitude + "," + longitude;
   	   		if (StringUtils.isNotEmpty(request.getHeader(Commons.DEVICE_NAME_HEADER))) {
   				 flex += "," + request.getHeader(Commons.DEVICE_NAME_HEADER);
   			 }
   			 if (StringUtils.isNotEmpty(request.getHeader(Commons.ROUTE_ID_HEADER))) {
   				 flex += ",rid:" + request.getHeader(Commons.ROUTE_ID_HEADER);
   			 } 
   	   	}
   	   	return flex;
	}
	
	private String processHeadersV2(HttpServletRequest request, String deviceId, Double latitude, Double longitude, String accuracy, int version, int appId) {
		List<String> tokens = new ArrayList<>();
		final String speed = request.getHeader("X-GMS-Speed");
		
		if (latitude != null && longitude != null) {
   	   		String geo = "geo:" + StringUtil.formatCoordE6(latitude) + "+" + StringUtil.formatCoordE6(longitude);
   	   		if (version > 31 && StringUtils.isNotEmpty(accuracy)) {
   	   			geo += "+" + accuracy;
   	   			if (version > 52 && StringUtils.isNotEmpty(speed)) {
   	   				geo += "+" + speed;
   	   			}
   	   		}
   	   		tokens.add(geo);
   	   		if (StringUtils.isNotEmpty(request.getHeader(Commons.ROUTE_ID_HEADER))) {
   	   			tokens.add("routeId:" + request.getHeader(Commons.ROUTE_ID_HEADER));
   	   		} else if (StringUtils.isNotEmpty(deviceId)) {
   	   			List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(deviceId, Commons.MY_POS_CODE, 0, 1);
   	   			if (!landmarks.isEmpty()) {
   	   				Landmark landmark = landmarks.get(0);
   	   				landmark.setLatitude(latitude);
	   				landmark.setLongitude(longitude);
	   				final String address = GeocodeHelperFactory.getInstance().processReverseGeocode(latitude, longitude);
	   				if (StringUtils.isNotEmpty(address)) {
	   					landmark.setDescription(address);
	   				}
	   				LandmarkPersistenceWebUtils.setFlex(landmark, request);
	   				LandmarkPersistenceUtils.updateLandmark(landmark, GoogleCacheProvider.getInstance());
	   				if (landmark.getId() > 0) {
	   					LandmarkPersistenceWebUtils.notifyOnLandmarkCreation(landmark, request.getHeader("User-Agent"), null, null, null, appId);
	   				}
	   			} else {
	   				Landmark landmark = new Landmark();
   	   				landmark.setLatitude(latitude);
   	   				landmark.setLongitude(longitude);
   	   				landmark.setName(Commons.MY_POSITION_LAYER);
   	   				landmark.setLayer(Commons.MY_POS_CODE);
   	   				landmark.setUsername(deviceId);
   	   				if (!LandmarkPersistenceWebUtils.isSimilarToNewest(landmark, 10)) {
   	   					final String address = GeocodeHelperFactory.getInstance().processReverseGeocode(latitude, longitude);
   	   					if (StringUtils.isNotEmpty(address)) {
   	   						landmark.setDescription(address);
   	   					}
   	   					LandmarkPersistenceWebUtils.setFlex(landmark, request);
   	   					LandmarkPersistenceUtils.persistLandmark(landmark, GoogleCacheProvider.getInstance());
   	   					if (landmark.getId() > 0) {
   	   						LandmarkPersistenceWebUtils.notifyOnLandmarkCreation(landmark, request.getHeader("User-Agent"), null, null, null, appId);
   	   					}
   	   				}
   	   			}
   	   		}
   	   	}	
   	   	if (StringUtils.isNotEmpty(deviceId)) {
   	   		tokens.add("deviceId:" + deviceId);
   	   	}
   	    final String deviceName = request.getHeader(Commons.DEVICE_NAME_HEADER);
   	   	if (StringUtils.isNotEmpty(deviceName)) {
   	   		tokens.add("deviceName:" + deviceName);
   	   	}
   	   	final String replyToCommand = request.getParameter("replyToCommand");
   	   	if (StringUtils.isNotEmpty(replyToCommand)) {
   	   		tokens.add("command:" + replyToCommand);
   	   	}
   	   	final String language = request.getHeader("Accept-Language");
   	   	if (StringUtils.isNotEmpty(language)) {
   	   		tokens.add("language:" + language);
   	   	}
   	   	if (!tokens.isEmpty()) {
   	   		 return StringUtils.join(tokens, ",");
   	   	} else {
   	   		return null;
   	   	}
	}
	
	private void persistDeviceLocation(String deviceId, String username, String deviceName, Double latitude, Double longitude, String accuracy) throws Exception {
		if (StringUtils.isNotEmpty(deviceId) && latitude != null && longitude != null) {
			Double[] coords = CacheUtil.getDeviceLocation(deviceId);
			if (coords == null || (coords != null && NumberUtils.distanceInKilometer(latitude, longitude, coords[0], coords[1]) >= CACHE_DEVICE_DISTANCE)) {
				String geo = "geo:" + latitude + " " + longitude;
				if (StringUtils.isNotEmpty(accuracy)) {
					geo += " " + accuracy;
				}
				final int status = DevicePersistenceUtils.setupDevice(deviceId, deviceName, username, null, geo);
				logger.log(Level.INFO, "Saved device " + deviceId + " configuration with status " + status + "\nNew configuration - name:" + deviceName + ", username:" + username + ", " + geo);
				if (status == 1) {
					CacheUtil.cacheDeviceLocation(deviceId, latitude, longitude, accuracy);
				}
			}
		}
	}
	
	private String getCommandKey(final String correlationId, final String imei, final String username, final String name, final String command) {
		String commandKey = "";
   	 	String[] cid = StringUtils.split(correlationId, "=");
		if (cid != null && cid.length == 2) {
   	 		commandKey += cid[0].trim().replace("+", "") + "_";
   	 	}
   	 	if (imei != null) {
			 commandKey +=  imei  + "_";
		} else if (username != null && name != null) {
			 commandKey += username + "_" + name  + "_";
		}
   	 	commandKey += command;
   	 	return commandKey;
	}
	
	private boolean isCommandForSameDevice(final String commandKey) {
		final String[] tokens = StringUtils.split(commandKey, "_");
		return (tokens.length >= 2 && StringUtils.equals(tokens[0], tokens[1]));
	}
	
}
