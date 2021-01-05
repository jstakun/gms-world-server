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
			"lock","pin","about", "hello", "config", "perimeter", "reset", "screen", "screenoff","screenshot"}; 
	
	public static int isDeviceRegistered(String imei) throws Exception {
		if (imei != null) {
		    final String deviceUrl = ConfigurationManager.getBackendUrl() + "/getDevice?imei="+  imei + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
		    final String deviceJson = HttpUtils.processFileRequest(new URL(deviceUrl));		
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
	
	public static int setupDevice(String imei, String deviceName, String username, String token, String flex) throws Exception {
		if (imei != null) {
		    final String deviceUrl = ConfigurationManager.getBackendUrl() + "/setupDevice";
		    String params = "imei="+  imei + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
		    if (StringUtils.isNotEmpty(username)) {
		    	params += "&username=" + username;
		    }
		    if (StringUtils.isNotEmpty(deviceName)) {
		    	params += "&name=" + deviceName;
		    }
		    if (StringUtils.isNotEmpty(token)) {
		    	params += "&token=" + token;
		    }
		    if (StringUtils.isNotEmpty(flex)) {
		    	params += "&flex=" + flex;
		    }
		    final String deviceJson = HttpUtils.processFileRequest(new URL(deviceUrl), "POST", null, params);		
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
			final String deviceUrl = ConfigurationManager.getBackendUrl() + "/commandDevice";
			String params = "command=" + command + "&pin=" + pin + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
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
		    	deviceJson = HttpUtils.processFileRequest(new URL(deviceUrl), "POST", null, params);		
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
			    	} else if (responseCode != null && responseCode == 410) {
			    		logger.log(Level.SEVERE, "Received server response 410 {0}", deviceJson);
			    		return -5;
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
		    final String deviceUrl = ConfigurationManager.getBackendUrl() + "/getUserDevices?username="+  URLEncoder.encode(username, "UTF-8")
		    									+ "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);	
		    final String deviceJson = HttpUtils.processFileRequest(new URL(deviceUrl));		
		    if (StringUtils.startsWith(deviceJson, "{")) {
			   JSONObject root = new JSONObject(deviceJson);
			   JSONArray devices = root.optJSONArray("output");
			   if (devices != null) {
				   for (int i =0;i<devices.length();i++) {
					   JSONObject device = devices.getJSONObject(i);
					   if (!device.has("token")) {
						   device.put("token", "");
					   }
				   }
				   return devices.toString();   
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
	
	public static int getUserDevicesCount(String username, String deviceName) {
		try {
			final String jsonArray = getUserDevices(username);
			JSONArray devicesArray = new JSONArray(jsonArray);
			final int length = devicesArray.length();
			if (StringUtils.isEmpty(deviceName) || (length == 0)) {
				return length;
			} else {
				for (int i=0;i<length;i++) {
					JSONObject device = devicesArray.getJSONObject(i);
					final String name  = device.getString("name");
					//logger.log(Level.INFO, "Comparing " + deviceName + " with " + name);
					if (StringUtils.equalsIgnoreCase(name, deviceName)) {
						return length;
					} 
				}
				return length+1;
			} 
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return 1;
		}
	}
	
	public static int deleteDevice(String imei) throws Exception {
		if (imei != null) {
		    final String deviceUrl = ConfigurationManager.getBackendUrl() + "/deleteDevice?imei=" +  imei + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
		    final String deviceJson = HttpUtils.processFileRequest(new URL(deviceUrl));		
		    if (StringUtils.startsWith(StringUtils.trim(deviceJson), "{")) {
		    	CacheUtil.cacheDeviceLocation(imei, null, null,null);
			   return 1;
		   } else {
			   Integer responseCode = HttpUtils.getResponseCode(deviceUrl);
			   if (responseCode != null && responseCode == 404)	 {
				   return -4;
			   } else {
				   if (deviceJson != null) {
					   logger.log(Level.SEVERE, "Received server response {0}", deviceJson);
				   } else {
					   logger.log(Level.SEVERE, "Received server response code {0}", responseCode);
				   }
				   return -1;
			   }
		  }
	   } else {
		   logger.log(Level.SEVERE, "Imei can't be null!");
		   return -2;
	   }
	}
	
	//command pin imei -p args 
	//command pin name username -p args 
	public static String sendCommand(final String commandString, final String socialId, final String socialNetwork) {
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
						status = sendCommand(deviceId, pin, null, null, command, args, correlationId, socialNetwork+":"+socialId);
					} else {
						status = sendCommand(null, pin, deviceId, username, command, args, correlationId, socialNetwork+":"+socialId);
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
			reply = "Oops! Your entered invalid command. Type /help for more details.";
		}
		return reply;
	}
	
	public static String getDevice(String imei) throws Exception {
		if (imei != null) {
		    final String deviceUrl = ConfigurationManager.getBackendUrl() + "/getDevice?imei=" +  imei + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
		    final String deviceJson = HttpUtils.processFileRequest(new URL(deviceUrl));		
		    if (StringUtils.startsWith(deviceJson, "{")) {
		    	return deviceJson;
		    } else {
		 	   logger.log(Level.SEVERE, "Received server response {0}", deviceJson);
			   return "{}";	
		    }
		} else {
			   logger.log(Level.SEVERE, "Imei can't be null!");
			   return "{}";				
		}
	}
}
