package com.jstakun.lm.server.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.common.collect.ImmutableMap;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;

import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.persistence.TokenPersistenceUtils;

/**
 *
 * @author jstakun
 */
public final class GlCommons {
	
    private static final String SCOPE = "https://www.googleapis.com/auth/blogger https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
    protected static final String CALLBACK_URI = ConfigurationManager.SSL_SERVER_URL + "s/glauth";
    private static final Logger logger = Logger.getLogger(GlCommons.class.getName());
    
    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth?"
            + "scope=%s&redirect_uri=%s&response_type=code&client_id=%s&access_type=offline"
            + "&request_visible_actions=%s";
    
    protected static String getAuthorizationUrl() throws UnsupportedEncodingException {
        return String.format(AUTHORIZE_URL, URLEncoder.encode(SCOPE, "UTF-8"), URLEncoder.encode(CALLBACK_URI, "UTF-8"), Commons.getProperty(Property.GL_PLUS_KEY), URLEncoder.encode("https://schemas.google.com/AddActivity", "UTF-8"));
    }
    
    protected static Map<String, String> authorize(String code) throws Exception {
        URL url = new URL("https://accounts.google.com/o/oauth2/token");

        Map<String, String> userData = null;
        
        String result = HttpUtils.processFileRequest(url, "POST", null, "code=" + code + "&client_id=" + Commons.getProperty(Property.GL_PLUS_KEY) + "&client_secret=" + Commons.getProperty(Property.GL_PLUS_SECRET) + "&redirect_uri=" + GlCommons.CALLBACK_URI + "&grant_type=authorization_code");
        String accessToken = null, refreshToken = null;
        long expires_in = -1;
        
        if (StringUtils.startsWith(result, "{")) {
            JSONObject resp = new JSONObject(result);
            accessToken = resp.optString("access_token");
            refreshToken = resp.optString("refresh_token");
            expires_in = resp.optInt("expires_in", -1);
        }

        if (StringUtils.isNotEmpty(accessToken)) {

            userData = getUserData(accessToken, refreshToken);
            
            if (StringUtils.isNotEmpty(refreshToken)) {
                userData.put("refresh_token", refreshToken);
            } else {
            	logger.log(Level.INFO, "Refresh token is empty!");
            }
            
            userData.put("token", accessToken);
            if (expires_in > -1) {
            	userData.put(ConfigurationManager.GL_EXPIRES_IN, Long.toString(expires_in));
            }
           
            String key = TokenPersistenceUtils.generateToken("lm", userData.get(ConfigurationManager.GL_USERNAME) + "@" + Commons.GOOGLE);
            userData.put("gmsToken", key); 
            
            Map<String, String> params = new ImmutableMap.Builder<String, String>().
               		put("service", Commons.GOOGLE).
            		put("accessToken", accessToken).
            		put("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
            		put("username", userData.get(ConfigurationManager.GL_USERNAME)).
            		put("name", userData.get(ConfigurationManager.GL_NAME)).build();
            NotificationUtils.createNotificationTask(params);
        } else {
    		throw new Exception("Access token is empty");
    	}
        
        return userData;
    }
    
    private GlCommons() {}

	private static Map<String,String> getUserData(String accessToken, String refreshToken) {
	    Map<String, String> userData = new HashMap<String, String>();
	
	    try {
	        HttpTransport httpTransport = new UrlFetchTransport();
	        JsonFactory jsonFactory = new JacksonFactory();
	
	        GoogleCredential requestInitializer = new GoogleCredential.Builder().
	                setClientSecrets(Commons.getProperty(Property.GL_PLUS_KEY), Commons.getProperty(Property.GL_PLUS_SECRET)).
	                setJsonFactory(jsonFactory).
	                setTransport(httpTransport).build();
	
	        requestInitializer.setAccessToken(accessToken).setRefreshToken(refreshToken);
	
	        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v1/userinfo?alt=json");
	        HttpRequest request = httpTransport.createRequestFactory(requestInitializer).buildGetRequest(url);
	
	        String response = request.execute().parseAsString();
	        
	        JSONObject json = new JSONObject(response);
	        if (json.has("id")) {
	        	userData.put(ConfigurationManager.GL_USERNAME,json.getString("id"));
	        }
	        if (json.has("name")) {
	        	userData.put(ConfigurationManager.GL_NAME, json.getString("name"));
	        }
	        if (json.has("gender")) {
	        	userData.put(ConfigurationManager.GL_GENDER,  json.getString("gender"));
	        }
	        if (json.has("birthday")) {
	        	userData.put(ConfigurationManager.GL_BIRTHDAY, json.getString("birthday"));
	        }
	        if (json.has("email")) {
	        	userData.put(ConfigurationManager.USER_EMAIL, json.getString("email"));
	        }
	    } catch (Exception ex) {
	        logger.log(Level.SEVERE, "GooglePlusUtils.getUserId() exception: ", ex);
	    }
	
	    return userData;
	}
}
