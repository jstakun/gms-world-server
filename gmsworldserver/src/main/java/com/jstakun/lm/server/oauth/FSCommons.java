package com.jstakun.lm.server.oauth;

import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.layers.FoursquareUtils;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.TokenUtil;

public final class FSCommons {
	 private static final String AUTHORIZE_URL = "https://foursquare.com/oauth2/authenticate?client_id=%s&response_type=code&redirect_uri=%s";
	 private static final String CALLBACK_URI = ConfigurationManager.SSL_SERVER_URL + "fsauth";
	 private static final String TOKEN_URL = "https://foursquare.com/oauth2/access_token?client_id=%s" +
       "&client_secret=%s&grant_type=authorization_code&redirect_uri=%s&code=%s";  
	 
	 protected static String getAuthorizationUrl() {
	        return String.format(AUTHORIZE_URL, Commons.FS_CLIENT_ID, CALLBACK_URI);
	 }       		
	 
	 protected static String getAccessTokenUrl(String code) {
		 	return String.format(TOKEN_URL, Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, CALLBACK_URI, code); 
	 }
	 
	 protected static Map<String, String> authorize(String code) throws Exception {
		 	URL tokenUrl = new URL(FSCommons.getAccessTokenUrl(code));

			String result = HttpUtils.processFileRequest(tokenUrl, "POST", null, null);
			String accessToken = null;

			if (StringUtils.startsWith(result, "{")) {
				JSONObject resp = new JSONObject(result);
				accessToken = resp.getString("access_token");
			}

			Map<String, String> userData = null;
			
			if (accessToken != null) {

				userData = FoursquareUtils.getUserData(accessToken);
				
				if (!userData.isEmpty()) {
					userData.put("token", accessToken);
					
					String key = TokenUtil.generateToken("lm", userData.get(ConfigurationManager.FS_USERNAME) + "@" + Commons.FOURSQUARE);
					userData.put("gmsToken", key); 

					Map<String, String> params = new ImmutableMap.Builder<String, String>().
                    	put("service", Commons.FOURSQUARE).
             		put("accessToken", accessToken).
             		put("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
             		put("username", userData.get(ConfigurationManager.FS_USERNAME)).
             		put("name", userData.containsKey(ConfigurationManager.FS_NAME) ? userData.get(ConfigurationManager.FS_NAME) : "noname").build();
					
					NotificationUtils.createNotificationTask(params);	
				
             	} 
			} else {
        		throw new Exception("AccessToken is empty");
        	}
			
			return userData;
	 }
	 
	 private FSCommons() {}
}
