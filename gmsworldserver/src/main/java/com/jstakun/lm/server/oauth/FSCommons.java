package com.jstakun.lm.server.oauth;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;

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
	 
	 private FSCommons() {}
}
