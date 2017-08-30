package com.jstakun.lm.server.utils;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class TokenUtil {

	private static final Logger logger = Logger.getLogger(TokenUtil.class.getName());
	
	public static String generateToken(String scope, String user) throws Exception {
		if (scope != null) {
    		String tokenUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.RHCLOUD_SERVER_URL) + "generateToken?scope="+ scope;
    		if (user != null) {
    			tokenUrl += "&user=" + user;
    		}
    		String tokenJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(tokenUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
			if (StringUtils.startsWith(tokenJson, "{")) {
				JSONObject root = new JSONObject(tokenJson);
				JSONObject output = root.getJSONObject("output");
				String key = output.getString("key");
				return key;
			} else {
				throw new Exception("Received following server response: " + tokenJson);
			}
    	} else {
    		throw new Exception("Scope is missing");
    	}
	}
	
	public static int isTokenValid(String token, String scope) throws Exception {
		String tokenUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.RHCLOUD_SERVER_URL) + "isValidToken?scope=" + scope + "&key=" + token;
		String tokenJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(tokenUrl), Commons.getProperty(Property.RH_GMS_USER), false);		
		if (StringUtils.startsWith(tokenJson, "{")) {
			JSONObject root = new JSONObject(tokenJson);
			if (root.optBoolean("output", false)) {
				return 1;
			} else {
				return 0;
			}
		} else if (tokenJson == null || StringUtils.contains(tokenJson, "503 Service Temporarily Unavailable")) {
		    return -1;
		} else {
			logger.log(Level.SEVERE, "Received following server response {0}", tokenJson);
			return -1;
		}
	}
}
