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
public final class FBCommons {
    
    private static final String redirect_uri = ConfigurationManager.SSL_SERVER_URL + "fbauth";
    private static final String SCOPE = "publish_stream,offline_access,user_birthday,email,friends_status,user_status,friends_photos,user_photos"; //manage_pages
 
    public static String getLoginRedirectURL() {
        //display=touch
        return "https://graph.facebook.com/oauth/authorize?client_id=" + Commons.fb_client_id + "&display=wap&redirect_uri=" + redirect_uri + "&scope=" + SCOPE;

    }

    public static String getAuthURL(String authCode) {
        return "https://graph.facebook.com/oauth/access_token?client_id=" + Commons.fb_client_id + "&redirect_uri=" + redirect_uri + "&client_secret=" + Commons.fb_secret + "&code=" + authCode;
    }
    
    private FBCommons() {}
}
