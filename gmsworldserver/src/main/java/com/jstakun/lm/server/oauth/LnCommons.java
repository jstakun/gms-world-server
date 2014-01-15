/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.oauth;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;

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
    
    private LnCommons() {}
}
