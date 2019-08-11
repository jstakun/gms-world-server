package com.jstakun.lm.server.oauth;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.persistence.TokenPersistenceUtils;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompleteUser;
import fi.foyt.foursquare.api.entities.Contact;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

public final class FSCommons {
	 private static final String AUTHORIZE_URL = "https://foursquare.com/oauth2/authenticate?client_id=%s&response_type=code&redirect_uri=%s";
	 private static final String CALLBACK_URL= ConfigurationManager.SSL_SERVER_URL + "s/fsauth";
	 private static final String TOKEN_URL = "https://foursquare.com/oauth2/access_token?client_id=%s" +
       "&client_secret=%s&grant_type=authorization_code&redirect_uri=%s&code=%s";  
	 private static final Logger logger = Logger.getLogger(FSCommons.class.getName());
	 
	 protected static String getAuthorizationUrl() {
	        return String.format(AUTHORIZE_URL, Commons.getProperty(Property.FS_CLIENT_ID), CALLBACK_URL);
	 }       		
	 
	 protected static String getAccessTokenUrl(String code) {
		 	return String.format(TOKEN_URL, Commons.getProperty(Property.FS_CLIENT_ID), Commons.getProperty(Property.FS_CLIENT_SECRET), CALLBACK_URL, code); 
	 }
	 
	 protected static Map<String, String> authorize(String code) throws Exception {
		 	Map<String, String> userData = null;
		 	if (StringUtils.isNotEmpty(code)) {
		 		URL tokenUrl = new URL(getAccessTokenUrl(code));

		 		String result = HttpUtils.processFileRequest(tokenUrl, "POST", null, null);
		 		String accessToken = null;

		 		if (StringUtils.startsWith(result, "{")) {
		 			JSONObject resp = new JSONObject(result);
		 			accessToken = resp.getString("access_token");
		 		}

		 		if (accessToken != null) {

		 			userData = getUserData(accessToken);
				
		 			if (!userData.isEmpty()) {
		 				userData.put("token", accessToken);
					
		 				String key = TokenPersistenceUtils.generateToken("lm", userData.get(ConfigurationManager.FS_USERNAME) + "@" + Commons.FOURSQUARE);
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
		 	} else {
        		throw new Exception("Code is empty");
        	}
			
			return userData;
	 }
	 
	 private FSCommons() {}

	private static Map<String, String> getUserData(String accessToken) {
		Map<String, String> userData = new HashMap<String, String>();
		
		try {
			FoursquareApi api = new FoursquareApi(Commons.getProperty(Property.FS_CLIENT_ID), Commons.getProperty(Property.FS_CLIENT_SECRET), null, accessToken, new DefaultIOHandler());
		
			Result<CompleteUser> result = api.user("self");
		
			CompleteUser user = result.getResult();
		
			if (user != null) {
				userData.put(ConfigurationManager.FS_USERNAME, user.getId());
			
				String name = StringUtil.getFormattedUsername(user.getFirstName(), user.getLastName(), ""); 
			
				if (StringUtils.isNotEmpty(name)) {
					userData.put(ConfigurationManager.FS_NAME, name);
				}
		
				Contact contact = user.getContact();
				if (contact != null) {
					String email = contact.getEmail();
					if (email != null) {
						userData.put(ConfigurationManager.USER_EMAIL, email);
					}
				}
			}
		} catch (Exception ex) {
	        logger.log(Level.SEVERE, "FSCommons.getUserData() exception:", ex);
	    }
		
		return userData;
	}
}
