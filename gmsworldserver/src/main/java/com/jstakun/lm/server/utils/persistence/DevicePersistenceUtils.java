package com.jstakun.lm.server.utils.persistence;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class DevicePersistenceUtils {
	
	private static final Logger logger = Logger.getLogger(DevicePersistenceUtils.class.getName());
	
	private static final String[] commands = {"resume","start","stop","route","locate","mute","unmute","normal","call","message",
			"radius","gpshigh","gpsbalance","notify","audio","noaudio","photo", "ping","ring","ringoff",
			"lock","pin","about", "hello", "config", "perimeter", "reset"}; 
	
	public static int isDeviceRegistered(String imei) throws Exception {
		if (imei != null) {
		    final String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "getDevice?" + 
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
			   logger.log(Level.SEVERE, "Received server response {0}", deviceJson);
			   return -1;
		  }
	   } else {
		   logger.log(Level.SEVERE, "Imei and pin can't be null!");
		   return -1;
	   }
	}
	
	public static int setupDevice(String imei, String name, String username, String token, String flex) throws Exception {
		if (imei != null) {
		    final String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "setupDevice";
		    String params = "imei="+  imei;
		    if (StringUtils.isNotEmpty(username)) {
		    	params += "&username=" + username;
		    }
		    if (StringUtils.isNotEmpty(name)) {
		    	params += "&name=" + name;
		    }
		    if (StringUtils.isNotEmpty(token)) {
		    	params += "&token=" + token;
		    }
		    if (StringUtils.isNotEmpty(flex)) {
		    	params += "&flex=" + flex;
		    }
		    String deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));		
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
			   logger.log(Level.SEVERE, "Received server response " +  deviceJson + " from\n" + deviceUrl);
			   return -1;
		  }
	   } else {
		   logger.log(Level.SEVERE, "Imei and pin can't be null!");
		   return -1;
	   }
	}

	public static int sendCommand(String imei, Integer pin, String name, String username, String command, String args, String correlationId, String flex) throws Exception {
		if (pin != null && isValidCommand(command)) {
			final String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "commandDevice";
			String params = "command=" + command + "&pin=" + pin;
			if (imei != null) {
				params += "&imei="+  imei;
			} else if (username != null && name != null) {
				params += "&username=" + username + "&name=" + name ;
			} else if (imei == null && (username == null || name == null)) {
				logger.log(Level.SEVERE, "Imei and name or username can't be null!");
				return -1;
			}
		    if (StringUtils.isNotEmpty(args)) {
		    	params += "&args=" + URLEncoder.encode(args, "UTF-8");
		    }
		    if (StringUtils.isNotEmpty(correlationId)) {
		    	params += "&correlationId=" + correlationId; 
		    }
		    if (StringUtils.isNotEmpty(flex)) {
		    	params += "&flex=" + flex; 
		    }
		    String deviceJson = null;
		    try {
		    	//logger.log(Level.INFO, "Calling: " + deviceUrl);
			    deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));		
			    if (StringUtils.startsWith(deviceJson, "{")) {
			    	JSONObject root = new JSONObject(deviceJson);
			    	if (root.optString("name") != null ) {
			    		return 1;
			    	} else {
			    		logger.log(Level.SEVERE, "Received server response {0}", deviceJson);
			    		return -1;
			    	}
			    } else {
			    	Integer responseCode = HttpUtils.getResponseCode(deviceUrl);
			    	if (responseCode != null && responseCode == 400) {
			    		logger.log(Level.SEVERE, "Received server response 400: {0}", deviceJson);
			    		return -2;
			    	} else if (responseCode != null && responseCode == 404) {
			    		logger.log(Level.SEVERE, "Received server response 404 {0}", deviceJson);
			    		return -4;
			    	} else {
			    		logger.log(Level.SEVERE, "Received server response {0} {1}",  new Object[]{responseCode, deviceJson});
			    		return -1;
			    	}
			    }
		    } catch (Exception e) {
		    	logger.log(Level.SEVERE, "Received server response {0} {1} ", new Object[]{HttpUtils.getResponseCode(deviceUrl), deviceJson});
		    	return -1; 
		    }
	   } else {
		   logger.log(Level.SEVERE, "Command and/or pin are invalid!");
		   return -2;
	   }	
	}
	  
	public static boolean isValidCommand(String command) {
		if (StringUtils.isNotEmpty(command)) { 
			for (int i=0; i<commands.length; i++) {
				if (StringUtils.startsWithIgnoreCase(command, commands[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String getUserDevices(String username) throws Exception {
		if (username != null) {
		    String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "getUserDevices?" + 
	                 "username="+  username;
		    String deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
		    if (StringUtils.startsWith(deviceJson, "{")) {
			   JSONObject root = new JSONObject(deviceJson);
			   JSONArray output = root.optJSONArray("output");
			   if (output != null) {
				   return output.toString();   
			   } else {
				   logger.log(Level.SEVERE, "Received server response {0}", deviceJson);
				   return "[]";
			   }
		   } else {
			   logger.log(Level.SEVERE, "Received server response {0}", deviceJson);
			   return "[]";
		  }
	   } else {
		   logger.log(Level.SEVERE, "Username can't be null!");
		   return "[]";
	   }
	}
	
	public static int deleteDevice(String imei) throws Exception {
		if (imei != null) {
		    String deviceUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "deleteDevice?" + 
	                 "imei="+  imei;
		    String deviceJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(deviceUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
		    if (StringUtils.startsWith(StringUtils.trim(deviceJson), "{")) {
			   return 1;
		   } else {
			   logger.log(Level.SEVERE, "Received server response {0}", deviceJson);
			   return -1;
		  }
	   } else {
		   logger.log(Level.SEVERE, "Imei can't be null!");
		   return -2;
	   }
	}
	
	//command pin imei -p args 
	//command pin name username -p args 
	public static String sendCommand(final String commandString, final String socialId) {
		final String[] commandTokens = StringUtils.split(commandString, " ");
		String reply = "";
		if (commandTokens.length >= 3 && isValidCommand(commandTokens[0]) && StringUtils.isNumeric(commandTokens[1])) {
			try {
					String command = commandTokens[0];
					final Integer pin = Integer.valueOf(commandTokens[1]);	
					final String deviceId = commandTokens[2];
					
					int argsIndex = 4;
					String username = null;
					if (commandTokens.length > 3 && !StringUtils.equals(commandTokens[3], "-p")) {
						argsIndex = 5;
						username = commandTokens[3];
					}
					
					String args = null; 
					if (commandTokens.length == argsIndex+1) {
						 args = commandTokens[argsIndex];
					} else if (commandTokens.length > argsIndex+1) {
						 StringUtils.join(Arrays.copyOfRange(commandTokens, argsIndex, commandTokens.length-1), " ");
					}
					
					reply = "Command " +  command + " has been sent to the device " + deviceId + ".";
					
					if (StringUtils.startsWith(command, "/")) {
						command = command.substring(1);
					}
					
					if (StringUtils.endsWithIgnoreCase(command, "dl")) {
						command += "t";
					} else if (!StringUtils.endsWithIgnoreCase(command, "dlt")) {
						command += "dlt";
					}
					
					final String correlationId = RandomStringUtils.randomAlphabetic(16) + System.currentTimeMillis();
					final String commandName =  command.substring(0, command.length()-3);
				
					int status;
					if (username == null) {
						status = DevicePersistenceUtils.sendCommand(deviceId, pin, null, null, command, args, correlationId, null);
					} else {
						status = DevicePersistenceUtils.sendCommand(null, pin, deviceId, username, command, args, correlationId, null);
					}
					
					if (status == 1)  {
						CacheUtil.put(correlationId, socialId + "_+_" + deviceId + "_+_" + commandName, CacheType.LANDMARK);
					} else if (status == -2) { //400
						reply = "Invalid command " + commandName + " or pin";
					} else if (status == -4) { //404
						reply = "Device " + deviceId + " not found";
					} else  {
						reply = "Failed to send command " + commandName + " to the device " + deviceId;
					}  
			} catch (Exception e) {
				reply = "Failed to send command: " + e.getMessage();
			}
		} else {
			reply = "Oops! Your command seems to be invalid. Valid commands are:\n\ncommand security_pin device_imei\ncommand security_pin device_name user_name\n" +
		                  "\nIf command requires parameters append -p args at the end of the command.";
		}
		return reply;
	}
}
