package com.jstakun.lm.server.utils;

import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.Commons;

public class TokenUtil {

	private static final String TOKEN_URL = "https://landmarks-gmsworld.rhcloud.com/actions/generateToken?scope=";      
    
	
	public static String generateToken(String scope, String user) throws Exception {
		if (scope != null) {
    		String tokenUrl = TOKEN_URL + scope;
    		if (user != null) {
    			tokenUrl += "&user=" + user;
    		}
    		String tokenJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(tokenUrl), Commons.RH_GMS_USER);		
			if (StringUtils.startsWith(tokenJson, "{")) {
				JSONObject root = new JSONObject(tokenJson);
				JSONObject output = root.getJSONObject("output");
				String key = output.getString("key");
				return key;
			} else {
				throw new Exception("Server error: " + tokenJson);
			}
    	} else {
    		throw new Exception("Scope is missing");
    	}
	}
}
