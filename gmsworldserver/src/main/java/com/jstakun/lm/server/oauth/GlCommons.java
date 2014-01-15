/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;

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
    
    private GlCommons() {}
}
