package com.jstakun.lm.server.utils.persistence;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class DevicePersistenceUtils {
	
	private static final Logger logger = Logger.getLogger(DevicePersistenceUtils.class.getName());
	
	private static final String[] commands = {"resume","start","stop","route","locate","mute","normal","call",
			"radius","gpshigh","gpsbalance","notify","audio","noaudio","photo","ping","ring","ringoff","lock","pin","about"}; 
	
	public static int isDeviceRegistered(String imei) throws Exception {
		if (imei != null) {
		    String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.RHCLOUD_SERVER_URL) + "getDevice?" + 
	                 "imei="+  imei;
		    String deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
		    if (StringUtils.startsWith(deviceJson, "{")) {
			   JSONObject root = new JSONObject(deviceJson);
			   JSONObject output = root.optJSONObject("output");
			   if (output != null && StringUtils.equals(output.getString("imei"), imei)) {
				   return 1;   
			   } else {
				   logger.log(Level.SEVERE, "Oops! wrong imei returned!");
				   return -1;
			   }
		   } else if (deviceJson == null || StringUtils.contains(deviceJson, "503 Service Temporarily Unavailable")) {
		       return -1;
		   } else {
			   logger.log(Level.SEVERE, "Received following server response {0}", deviceJson);
			   return -1;
		  }
	   } else {
		   logger.log(Level.SEVERE, "Imei and pin can't be null!");
		   return -1;
	   }
	}
	
	public static int setupDevice(String imei, String name, String username, String token) throws Exception {
		if (imei != null) {
		    String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.RHCLOUD_SERVER_URL) + "setupDevice?" + 
	                 "imei="+  imei;
		    if (StringUtils.isNotEmpty(username)) {
		    	deviceUrl += "&username=" + username;
		    }
		    if (StringUtils.isNotEmpty(name)) {
		    	deviceUrl += "&name=" + name;
		    }
		    if (StringUtils.isNotEmpty(token)) {
		    	deviceUrl += "&token=" + token;
		    }
		    String deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
		    if (StringUtils.startsWith(deviceJson, "{")) {
			   JSONObject root = new JSONObject(deviceJson);
			   JSONObject output = root.optJSONObject("output");
			   if (output != null && StringUtils.equals(output.getString("imei"), imei)) {
				   return 1;   
			   } else {
				   logger.log(Level.SEVERE, "Oops! wrong imei returned!");
				   return -1;
			   }
		   } else if (deviceJson == null || StringUtils.contains(deviceJson, "503 Service Temporarily Unavailable")) {
		       return -1;
		   } else {
			   logger.log(Level.SEVERE, "Received following server response " +  deviceJson + " from\n" + deviceUrl);
			   return -1;
		  }
	   } else {
		   logger.log(Level.SEVERE, "Imei and pin can't be null!");
		   return -1;
	   }
	}

	public static int sendCommand(String imei, Integer pin, String name, String username, String command, String args, String correlationId) throws Exception {
		if (pin != null && isValidCommand(command)) {
			String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.RHCLOUD_SERVER_URL) + "commandDevice?" + 
					"command=" + command + "&pin=" + pin;
			if (imei != null) {
				deviceUrl += "&imei="+  imei;
			} else if (username != null && name != null) {
				deviceUrl += "&username=" + username + "&name=" + name ;
			} else if (imei == null && (username == null || name == null)) {
				logger.log(Level.SEVERE, "Imei and name or username can't be null!");
				return -1;
			}
		    if (StringUtils.isNotEmpty(args)) {
		    	deviceUrl += "&args=" + args;
		    }
		    if (StringUtils.isNotEmpty(correlationId)) {
		    	deviceUrl += "&correlationId=" + correlationId; 
		    }
		    String deviceJson = null;
		    try {
			    deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
			    JSONObject root = new JSONObject(deviceJson);
			    if (root.optString("name") != null ) {
			       return 1;
			    } else {
				   logger.log(Level.SEVERE, "Received following server response {0}", deviceJson);
				   return -1;
			    }
		    } catch (Exception e) {
		    	logger.log(Level.SEVERE, "Received following server response {0} {1} ", new Object[]{HttpUtils.getResponseCode(deviceUrl), deviceJson});
		    	return -1; 
		    }
	   } else {
		   logger.log(Level.SEVERE, "Command and/or pin are invalid!");
		   return -1;
	   }	
	}
	  
	private static boolean isValidCommand(String command) {
		if (StringUtils.isNotEmpty(command)) { 
			for (int i=0; i<commands.length; i++) {
				if (StringUtils.startsWithIgnoreCase(command, commands[i])) {
					return true;
				}
			}
		}
		return false;
	}
}
