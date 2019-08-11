/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.oauth;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.google.common.collect.ImmutableMap;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;

import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.TokenPersistenceUtils;

/**
 *
 * @author jstakun
 */
public final class TwCommons {
    protected static final String CALLBACK_URL = ConfigurationManager.SSL_SERVER_URL + "s/twauth";
    
    protected static String getAuthorizationUrl() throws TwitterException {
    	Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(Commons.getProperty(Property.TW_CONSUMER_KEY), Commons.getProperty(Property.TW_CONSUMER_SECRET));
		RequestToken requestToken = twitter.getOAuthRequestToken(TwCommons.CALLBACK_URL);
		if (requestToken != null) {
		CacheUtil.put("twRequestToken_" + requestToken.getToken(), requestToken, CacheType.NORMAL);
			return requestToken.getAuthenticationURL();
		} else {
			throw new TwitterException("RequestToken is empty");
		}
    }
    
    protected static Map<String, String> authorize(String token, String verifier) throws Exception {
    	Map<String, String> userData = null;
    	if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(verifier)) {
    		Twitter twitter = new TwitterFactory().getInstance();
    		twitter.setOAuthConsumer(Commons.getProperty(Property.TW_CONSUMER_KEY), Commons.getProperty(Property.TW_CONSUMER_SECRET));
    		RequestToken requestToken = CacheUtil.getObject(RequestToken.class, "twRequestToken_" + token);
		
    		userData = new HashMap<String, String>();
		
    		if (requestToken != null) {
    			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
			
    			User me = twitter.showUser(twitter.getId());
			
    			userData.put("token", accessToken.getToken());
    			userData.put("secret", accessToken.getTokenSecret());
    			userData.put(ConfigurationManager.TWEET_USERNAME, me.getScreenName());
    			userData.put(ConfigurationManager.TWEET_NAME, me.getName());

    			String key = TokenPersistenceUtils.generateToken("lm", me.getScreenName() + "@" + Commons.TWITTER);
    			userData.put("gmsToken", key); 
			
    			Map<String, String> params = new ImmutableMap.Builder<String, String>().
    					put("service", Commons.TWITTER).
    					put("accessToken", accessToken.getToken()).
    					put("tokenSecret", accessToken.getTokenSecret()).
    					put("username", userData.get(ConfigurationManager.TWEET_USERNAME)).
    					put("name", userData.get(ConfigurationManager.TWEET_NAME)).build();
    			NotificationUtils.createNotificationTask(params);    
    		} else {
    			throw new Exception("AccessToken is empty");
    		}
    	} else {
    		throw new Exception("OauthToken or OauthVerifier is empty");
    	}
		
		return userData;
    }
    
    private TwCommons() {}
}
