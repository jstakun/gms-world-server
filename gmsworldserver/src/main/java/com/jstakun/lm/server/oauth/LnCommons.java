/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.oauth;

import java.net.URL;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.social.LinkedInUtils;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.TokenUtil;

/**
 *
 * @author jstakun
 */
public final class LnCommons {
    private static final String CALLBACK_URI = ConfigurationManager.SSL_SERVER_URL + "lnauth";
    private static final String SCOPE = "r_basicprofile%20r_emailaddress%20r_contactinfo%20rw_nus";
    private static final String AUTHORIZE_URL = "https://www.linkedin.com/uas/oauth2/authorization?response_type=code&client_id=%s" +
                                "&scope=%s&state=%s&redirect_uri=%s";
    
    private static final String TOKEN_URL = "https://www.linkedin.com/uas/oauth2/accessToken?grant_type=authorization_code" +
    		"&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s";
                                                                                  
    
    protected static String getAuthorizationUrl() {
    	return String.format(AUTHORIZE_URL, Commons.LN_API_KEY, SCOPE, Commons.LN_STATE, CALLBACK_URI);
    }
    
    protected static String getAccessTokenUrl(String code) {
	 	return String.format(TOKEN_URL, code, CALLBACK_URI, Commons.LN_API_KEY, Commons.LN_API_SECRET); 
    }
    
    protected static Map<String, String> authorize(String code, String state) throws Exception {
    	Map<String, String> userData = null;
    	if (code != null && StringUtils.equals(state, Commons.LN_STATE)) {
        	
        	URL tokenUrl = new URL(LnCommons.getAccessTokenUrl(code));
	
        	String result = HttpUtils.processFileRequest(tokenUrl, "POST", null, null);
        	String accessToken = null;
        	long expires_in = -1;
    
        	if (StringUtils.startsWith(result, "{")) {
        		JSONObject resp = new JSONObject(result);
        		accessToken = resp.optString("access_token");
        		expires_in = resp.optInt("expires_in", -1);
        	}
        
        	if (StringUtils.isNotEmpty(accessToken))
        	{
        		userData = LinkedInUtils.getUserDate(accessToken);
        	
        		String key = TokenUtil.generateToken("lm", userData.get(ConfigurationManager.LN_USERNAME) + "@" + Commons.LINKEDIN);
        		userData.put("gmsToken", key); 
        	
        		userData.put("token", accessToken);
            
        		if (expires_in > -1) {
        			userData.put(ConfigurationManager.LN_EXPIRES_IN, Long.toString(expires_in));
        		}

        		Map<String, String> params = new ImmutableMap.Builder<String, String>().
                   	put("service", Commons.LINKEDIN).
            		put("accessToken", accessToken).
            		put("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
            		put("username", userData.get(ConfigurationManager.LN_USERNAME)).
            		put("name", userData.get(ConfigurationManager.LN_NAME)).build();  
        		NotificationUtils.createNotificationTask(params);          
        	}
    	}	
    	
    	return userData;
    }
    
    private LnCommons() {}
}
