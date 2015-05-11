package com.jstakun.lm.server.oauth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.layers.FacebookUtils;
import net.gmsworld.server.utils.DateUtils;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.TokenUtil;
import com.restfb.FacebookClient;
import com.restfb.types.User;

/**
 *
 * @author jstakun
 */
public final class FBCommons {
    
	private static final String outf = "yyyyMMdd";
    private static final String redirect_uri = ConfigurationManager.SSL_SERVER_URL + "fbauth";
    private static final String SCOPE = "email,publish_actions,user_tagged_places";//,user_posts,user_photos"; //user permission
    //private static final String SCOPE = "manage_pages,publish_pages,publish_actions"; //server permissions
    
    protected static String getLoginRedirectURL() {
        //display=touch
        return "https://graph.facebook.com/oauth/authorize?client_id=" + Commons.getProperty(Property.fb_client_id) + "&display=touch&redirect_uri=" + redirect_uri + "&scope=" + SCOPE;

    }

    protected static String getAuthURL(String authCode) {
        return "https://graph.facebook.com/oauth/access_token?client_id=" + Commons.getProperty(Property.fb_client_id) + "&redirect_uri=" + redirect_uri + "&client_secret=" + Commons.getProperty(Property.fb_secret) + "&code=" + authCode;
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
                userData = getMyData(accessToken);
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

	private static Map<String, String> getMyData(String token) {
		Map<String, String> userData = new HashMap<String, String>();
		
		FacebookClient facebookClient = FacebookUtils.getFacebookClient(token);
	    User me = facebookClient.fetchObject("me", User.class);
	    
	    userData.put(ConfigurationManager.FB_USERNAME, me.getId());
	    String name = me.getName();
	    if (name != null) {
	        userData.put(ConfigurationManager.FB_NAME, name);
	    } else {
	    	userData.put(ConfigurationManager.FB_NAME, me.getId());
	    }
	    String gender = me.getGender();
	    if (gender != null) {
	       userData.put(ConfigurationManager.FB_GENDER, gender);
	    }
	    Date birthday = me.getBirthdayAsDate();
	    if (birthday != null) {
	    	String outd = DateUtils.formatDate(outf, birthday);
			userData.put(ConfigurationManager.FB_BIRTHDAY, outd);
	    } 
		
	    String email = me.getEmail();
	    if (StringUtils.isNotEmpty(email)) {
	    	userData.put(ConfigurationManager.USER_EMAIL, email);
	    }
	    
		return userData;
	}
}
