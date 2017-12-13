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
	
	public static int isDeviceRegistered(Long imei, Integer pin) throws Exception {
		if (imei != null && pin != null) {
		    String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.RHCLOUD_SERVER_URL) + "getDevice?" + 
	                 "imei="+  imei + "&pin=" + pin;
		    String deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
		    if (StringUtils.startsWith(deviceJson, "{")) {
			   JSONObject root = new JSONObject(deviceJson);
			   JSONObject output = root.optJSONObject("output");
			   if (output != null && output.getLong("imei") == imei) {
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
	
	public static int setupDevice(Long imei, Integer pin, String username, String token) throws Exception {
		if (imei != null && pin != null) {
		    String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.RHCLOUD_SERVER_URL) + "setupDevice?" + 
	                 "imei="+  imei + "&pin=" + pin;
		    if (StringUtils.isNotEmpty(username)) {
		    	deviceUrl += "&username=" + username;
		    }
		    if (StringUtils.isNotEmpty(token)) {
		    	deviceUrl += "&token=" + token;
		    }
		    String deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
		    if (StringUtils.startsWith(deviceJson, "{")) {
			   JSONObject root = new JSONObject(deviceJson);
			   JSONObject output = root.optJSONObject("output");
			   if (output != null && output.getLong("imei") == imei) {
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

}
