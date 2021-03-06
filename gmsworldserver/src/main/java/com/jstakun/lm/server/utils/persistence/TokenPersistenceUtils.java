package com.jstakun.lm.server.utils.persistence;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class TokenPersistenceUtils {

	private static final Logger logger = Logger.getLogger(TokenPersistenceUtils.class.getName());
	
	public static String generateToken(String scope, String user) throws Exception {
		if (scope != null) {
    		String tokenUrl = ConfigurationManager.getBackendUrl() + "/generateToken?scope="+ scope + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
    		if (user != null) {
    			tokenUrl += "&user=" + user;
    		}
    		final String tokenJson = HttpUtils.processFileRequest(new URL(tokenUrl));		
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
		final String tokenKey = scope + "/" + token;
		final String tokenKeyLock = tokenKey + "/lock";
		
		String lock = null;
		if (CacheUtil.containsKey(tokenKeyLock)) {
			 lock = CacheUtil.getString(tokenKeyLock);
		} else {
			 lock = "1";
			 CacheUtil.put(tokenKeyLock, lock, CacheType.FAST);
		}

		if (lock != null) {
			synchronized (lock) {
				if (CacheUtil.containsKey(tokenKey)) {
					if (StringUtils.equals(CacheUtil.getString(tokenKey),"1")) {
						return 1;
					} else {
						return 0;
					}
				} else {
					final String tokenUrl = ConfigurationManager.getBackendUrl() + "/isValidToken?scope=" + scope + "&key=" + token + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
					final String tokenJson = HttpUtils.processFileRequest(new URL(tokenUrl));		
					if (StringUtils.startsWith(tokenJson, "{")) {
						JSONObject root = new JSONObject(tokenJson);
						boolean isValid = root.optBoolean("output", false);
						if (isValid) {
							CacheUtil.put(tokenKey, "1", CacheType.FAST);
							return 1;
						} else {
							CacheUtil.put(tokenKey, "0", CacheType.FAST);
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
		} else {
			return -1;
		}
	}
}
