/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.social.GooglePlusUtils;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.TokenUtil;

/**
 *
 * @author jstakun
 */
public final class GlCommons {
	
    private static final String SCOPE = "https://www.googleapis.com/auth/blogger https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/userinfo.email";
    protected static final String CALLBACK_URI = ConfigurationManager.SSL_SERVER_URL + "glauth";
    //public static final String BLOGGER_SCOPE = "http://www.blogger.com/feeds/";
    //public static final String POSTS_FEED_URI_SUFFIX = "/posts/default";
    //public static final String METAFEED_URL = "http://www.blogger.com/feeds/default/blogs";
    
    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth?"
            + "scope=%s&redirect_uri=%s&response_type=code&client_id=%s&access_type=offline"
            + "&request_visible_actions=%s";
    
    protected static String getAuthorizationUrl() throws UnsupportedEncodingException {
        return String.format(AUTHORIZE_URL, URLEncoder.encode(SCOPE, "UTF-8"), URLEncoder.encode(CALLBACK_URI, "UTF-8"), Commons.GL_PLUS_KEY, URLEncoder.encode("https://schemas.google.com/AddActivity", "UTF-8"));
    }
    
    protected static Map<String, String> authorize(String code) throws Exception {
        URL url = new URL("https://accounts.google.com/o/oauth2/token");

        Map<String, String> userData = null;
        
        String result = HttpUtils.processFileRequest(url, "POST", null, "code=" + code + "&client_id=" + Commons.GL_PLUS_KEY + "&client_secret=" + Commons.GL_PLUS_SECRET + "&redirect_uri=" + GlCommons.CALLBACK_URI + "&grant_type=authorization_code");
        String accessToken = null, refreshToken = null;
        long expires_in = -1;
        
        if (StringUtils.startsWith(result, "{")) {
            JSONObject resp = new JSONObject(result);
            accessToken = resp.optString("access_token");
            refreshToken = resp.optString("refresh_token");
            expires_in = resp.optInt("expires_in", -1);
        }

        if (accessToken != null && refreshToken != null) {

            userData = GooglePlusUtils.getUserData(accessToken, refreshToken);
            
            String token = accessToken;
            if (refreshToken != null) {
                token = token + " " + refreshToken;
                userData.put("refresh_token", refreshToken);
            }
            
            userData.put("token", accessToken);
            if (expires_in > -1) {
            	userData.put(ConfigurationManager.GL_EXPIRES_IN, Long.toString(expires_in));
            }
           
            String key = TokenUtil.generateToken("lm", userData.get(ConfigurationManager.GL_USERNAME) + "@" + Commons.GOOGLE_PLUS);
            userData.put("gmsToken", key); 
            
            Map<String, String> params = new ImmutableMap.Builder<String, String>().
               		put("service", Commons.GOOGLE_PLUS).
            		put("accessToken", accessToken).
            		put("refreshToken", refreshToken).
            		put("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
            		put("username", userData.get(ConfigurationManager.GL_USERNAME)).
            		put("name", userData.get(ConfigurationManager.GL_NAME)).build();
            NotificationUtils.createNotificationTask(params);
        }
        
        return userData;
    }
    
    private GlCommons() {}
}
