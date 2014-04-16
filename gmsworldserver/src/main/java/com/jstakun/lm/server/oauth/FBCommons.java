/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.layers.FacebookUtils;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.TokenUtil;

/**
 *
 * @author jstakun
 */
public final class FBCommons {
    
    private static final String redirect_uri = ConfigurationManager.SSL_SERVER_URL + "fbauth";
    private static final String SCOPE = "publish_stream,offline_access,user_birthday,email,friends_status,user_status,friends_photos,user_photos"; //manage_pages
 
    protected static String getLoginRedirectURL() {
        //display=touch
        return "https://graph.facebook.com/oauth/authorize?client_id=" + Commons.fb_client_id + "&display=wap&redirect_uri=" + redirect_uri + "&scope=" + SCOPE;

    }

    protected static String getAuthURL(String authCode) {
        return "https://graph.facebook.com/oauth/access_token?client_id=" + Commons.fb_client_id + "&redirect_uri=" + redirect_uri + "&client_secret=" + Commons.fb_secret + "&code=" + authCode;
    }
    
    protected static Map<String, String> authorize(String code) throws Exception {
    	Map<String, String> userData = null;
    	
    	if (StringUtils.isNotEmpty(code)) {
            
        	String authURL = FBCommons.getAuthURL(code);
            
            URL url = new URL(authURL);

            String result = readURL(url);
            String accessToken = null;
            int expires = -1;
            String[] pairs = result.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length != 2) {
                    throw new RuntimeException("Unexpected auth response");
                } else {
                    if (kv[0].equals("access_token")) {
                        accessToken = kv[1];
                    }
                    if (kv[0].equals("expires")) {
                        expires = Integer.parseInt(kv[1]);
                    }
                }
            }
            if (accessToken != null) {
                userData = FacebookUtils.getMyData(accessToken);
                userData.put("token", accessToken);
                if (expires > 0) {
                	userData.put(ConfigurationManager.FB_EXPIRES_IN, Integer.toString(expires));
                }                 
                
                String key = TokenUtil.generateToken("lm", userData.get(ConfigurationManager.FB_USERNAME) + "@" + Commons.FACEBOOK);
                userData.put("gmsToken", key); 
                
                Map<String, String> params = new ImmutableMap.Builder<String, String>().
                        put("service", Commons.FACEBOOK).
                		put("accessToken", accessToken).
                		put("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
                		put("username", userData.get(ConfigurationManager.FB_USERNAME)).
                		put("name", userData.get(ConfigurationManager.FB_NAME)).build();                  
                NotificationUtils.createNotificationTask(params);
                
            } else {
        		throw new Exception("AccessToken is empty");
        	}
        } else {
    		throw new Exception("Code is empty");
    	}
    	
    	return userData;
    }
    
    private static String readURL(URL url) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        int r;
        while ((r = is.read()) != -1) {
            baos.write(r);
        }
        is.close();
        return new String(baos.toByteArray());
    }
    
    private FBCommons() {}
}
