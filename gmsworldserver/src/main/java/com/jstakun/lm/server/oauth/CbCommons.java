package com.jstakun.lm.server.oauth;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class CbCommons {

	private static final String CALLBACK_URI = ConfigurationManager.SSL_SERVER_URL + "cbauth";
	
	private static final String AUTHORIZE_URL = "https://www.coinbase.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=%s&state=%s";
	
	private static final String TOKEN_URL = "https://www.coinbase.com/oauth/token?grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s";
	
	private static final String SCOPE = "user+balance";
	
	private static final Logger logger = Logger.getLogger(CbCommons.class.getName());
    
	//refresh token after 2 hours 
	//POST   https://www.coinbase.com/oauth/token?refresh_token=
	
	private CbCommons() {}
	
	protected static String getAuthorizationUrl() {
    	return String.format(AUTHORIZE_URL, Commons.getProperty(Property.CB_API_KEY), CALLBACK_URI, SCOPE, Commons.getProperty(Property.LN_STATE));
    }
	
	protected static String getAccessTokenUrl(String code) {
	 	return String.format(TOKEN_URL, code, CALLBACK_URI, Commons.getProperty(Property.CB_API_KEY), Commons.getProperty(Property.CB_API_SECRET)); 
    }
	
	protected static Map<String, String> authorize(String code, String state) throws Exception {
		Map<String, String> userData = new HashMap<String, String>();
		
		if (code != null && StringUtils.equals(state, Commons.getProperty(Property.LN_STATE))) {
			URL tokenUrl = new URL(getAccessTokenUrl(code));

			String result = HttpUtils.processFileRequest(tokenUrl, "POST", null, null);
			String accessToken = null, refreshToken = null;
        	long expires_in = -1;

			if (StringUtils.startsWith(result, "{")) {
				JSONObject resp = new JSONObject(result);
				accessToken = resp.getString("access_token");
				refreshToken = resp.optString("refresh_token");
            	expires_in = resp.optInt("expires_in", -1);
			}
		
		
			if (accessToken != null) {
				if (StringUtils.isNotEmpty(refreshToken)) {
                	userData.put("refresh_token", refreshToken);
            	} else {
            		logger.log(Level.INFO, "Refresh token is empty!");
            	}
            
            	userData.put("token", accessToken);
            	if (expires_in > -1) {
            		userData.put(ConfigurationManager.CB_EXPIRES_IN, Long.toString(expires_in));
            	}
            
            	//load user data https://github.com/coinbase/coinbase-java        
            	//GET https://api.coinbase.com/v1/users/self?access_token=
            
            	URL userUrl = new URL("https://api.coinbase.com/v1/users/self?access_token=" + accessToken);
            	String response = HttpUtils.processFileRequest(userUrl, "GET", null, null);
            
            	JSONObject json = new JSONObject(response);
            	JSONObject user = json.getJSONObject("user");
            
            	String id = user.optString("id");
		    	if (id != null) {
		    		userData.put(ConfigurationManager.CB_USERNAME, id);
		    	}
            
            	String name = user.optString("name");
            	if (StringUtils.isNotEmpty(name)) {
		    		userData.put(ConfigurationManager.CB_NAME, name);
		    	}
            
            	String email = user.optString("email"); 
		    	if (StringUtils.isNotEmpty(email)) {
		        	userData.put(ConfigurationManager.USER_EMAIL, email);
		    	}
			} else {
    			throw new Exception("AccessToken is empty");
    		}
		} else {
    		throw new Exception("Wrong code ro state");
    	}
		
		return userData;
	}
}
