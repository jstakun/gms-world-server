package com.jstakun.lm.server.social;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.Commons.Property;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

public class NotificationUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationUtils.class.getName());
	
	public static void createNotificationTask(Map<String, String> params) {
		Queue queue = QueueFactory.getQueue("notifications");
		TaskOptions options = withUrl("/tasks/notificationTask");
		for (Map.Entry<String, String> entry : params.entrySet()) {
			options.param(entry.getKey(), entry.getValue());	
		}
		//logger.log(Level.INFO, "Creating new notification task {0}...", options.toString());
		queue.add(options);   		
	}

	protected static Map<String, String> getNotificationParams(String key) {
		Map<String, String> params = new HashMap<String, String>();
		
		if (StringUtils.isNotEmpty(key)) {
			params.put("key", key);
			Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
			if (landmark != null) {
				params.put("url", UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark)));
				if (landmark.getLayer().equals("Social")) {
					params.put("type", Integer.toString(Commons.BLOGEO));
					params.put("title", landmark.getName());
				} else if (landmark.getLayer().equals(Commons.MY_POS_CODE)) {
					params.put("type", Integer.toString(Commons.MY_POS));
					params.put("title", Commons.MY_POSITION_LAYER);
				} else {
					params.put("type", Integer.toString(Commons.LANDMARK));
					params.put("title", landmark.getName());
				}
			}
		} else {
			params.put("type", Integer.toString(Commons.LOGIN));
			params.put("url", ConfigurationManager.SERVER_URL);
			params.put("title", "Message from GMS World");
		}
		
		return params;
	}
	
	public static void createLadmarkCreationNotificationTask(Map<String, String> params) {
		
		Map<String, String> newParams = new HashMap<String, String>(params);
		
		//FacebookUtils
		newParams.put("service", Commons.FACEBOOK);
		createNotificationTask(newParams);
		
		//TwitterUtils
		newParams.put("service", Commons.TWITTER);
        createNotificationTask(newParams);
		
        //GoogleBloggerUtils
        newParams.put("service", Commons.GOOGLE_BLOGGER);
        createNotificationTask(newParams);
		
        //GooglePlusUtils
        newParams.put("service", Commons.GOOGLE_PLUS);
        createNotificationTask(newParams);
        
        //email
        newParams.put("service", Commons.MAIL);
        createNotificationTask(newParams);
	}
	
	public static void createImageCreationNotificationTask(Map<String, String> params) {
		
		Map<String, String> newParams = new HashMap<String, String>(params);
		
		//FacebookUtils
		newParams.put("service", Commons.FACEBOOK);
		createNotificationTask(newParams);
		
		//TwitterUtils
		newParams.put("service", Commons.TWITTER);
        createNotificationTask(newParams);
		
        //GoogleBloggerUtils
        newParams.put("service", Commons.GOOGLE_BLOGGER);
        createNotificationTask(newParams);
		
        //GooglePlusUtils
        newParams.put("service", Commons.GOOGLE_PLUS);
        createNotificationTask(newParams);
	}
	
	public static void sendImageCreationNotification(Map<String, String[]> params) {
		String imageUrl = params.get("imageUrl")[0];
    	String username = params.get("username")[0];
    	String service = params.get("service")[0];
    	String showImageUrl = params.get("showImageUrl")[0];
        double lat = NumberUtils.getDouble(params.get("lat")[0], 0d);
    	double lng = NumberUtils.getDouble(params.get("lng")[0], 0d);
    	
    	if (StringUtils.isNotEmpty(imageUrl) && StringUtils.isNotEmpty(service) && StringUtils.isNotEmpty(showImageUrl)) {
    	
    		logger.log(Level.INFO, "Sending image creation notification to {0}...", service);
    	
    		if (StringUtils.equals(service, Commons.FACEBOOK)) {
    			FacebookUtils.sendImageMessage(imageUrl, showImageUrl, username);
    		} else if (StringUtils.equals(service, Commons.TWITTER)) {
    			TwitterUtils.sendImageMessage(showImageUrl, username, lat, lng);
    		} else if (StringUtils.equals(service, Commons.GOOGLE_BLOGGER)) {
    			GoogleBloggerUtils.sendImageMessage(showImageUrl, username, imageUrl);
    		} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    			GooglePlusUtils.sendImageMessage(showImageUrl, username, imageUrl);
    		}
    	
    	} else {
    		logger.log(Level.SEVERE, "Wrong parameters imageUrl={0}, service={1}, showImageUrl={2}", new Object[]{imageUrl, service, showImageUrl});	
    	}
	}
	
	public static void sendLandmarkCreationNotification(Map<String, String[]> params, ServletContext context) {
		String service = params.get("service")[0];
    	String key = params.get("key")[0];
    	String landmarkUrl = params.get("landmarkUrl")[0];
    	String email = params.get("email")[0];
    	String title = params.get("title")[0];
    	String body = params.get("body")[0];   
    	String userUrl = params.get("userUrl")[0];
    	String username = params.get("username")[0];
    	
    	logger.log(Level.INFO, "Sending landmark creation notification to service {0}...", service);
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK)) {
    		FacebookUtils.sendMessageToPageFeed(key, landmarkUrl);
    	} else if (StringUtils.equals(service, Commons.TWITTER)) {
    		TwitterUtils.sendMessage(key, landmarkUrl, Commons.getProperty(Property.TW_TOKEN), Commons.getProperty(Property.TW_SECRET), Commons.SERVER);
    	} else if (StringUtils.equals(service, Commons.GOOGLE_BLOGGER)) {
    		GoogleBloggerUtils.sendMessage(key, landmarkUrl, Commons.getProperty(Property.gl_plus_token), Commons.getProperty(Property.gl_plus_refresh), true);
    	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		GooglePlusUtils.sendMessage(Commons.getProperty(Property.gl_plus_token), Commons.getProperty(Property.gl_plus_refresh), key, landmarkUrl, Commons.SERVER);
    	} else if (StringUtils.equals(service, Commons.MAIL)) {
    		MailUtils.sendLandmarkCreationNotification(title, body);
    		//send landmark creation notification email to user
    		if (StringUtils.isNotEmpty(email)) {
    			String userMask = UrlUtils.createUsernameMask(username);
    			MailUtils.sendLandmarkNotification(email, userUrl, userMask, landmarkUrl, key, context);
    		}			
    	}
	}
	
	public static void sendUserLoginNotification(Map<String, String[]> params, ServletContext context) {
		String service = params.get("service")[0];
    	String accessToken = params.get("accessToken")[0];
    	String username = params.get("username")[0];
    	String name = params.get("name")[0];
    	String email = null;
    	if (params.containsKey("email")) {
    		email = params.get("email")[0];
    	}
    	String layer = null;
    	
    	logger.log(Level.INFO, "Sending user login notification to {0}...", service);
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK)) {
    		FacebookUtils.sendMessageToUserFeed(accessToken, ConfigurationManager.SERVER_URL, "Message from GMS World", Commons.LOGIN);
            layer = Commons.FACEBOOK_LAYER;
    	} else if (StringUtils.equals(service, Commons.FOURSQUARE)) {
    		layer = Commons.FOURSQUARE_LAYER;
    	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		String refreshToken = params.get("refreshToken")[0];
        	GooglePlusUtils.sendMessage(accessToken, refreshToken, null, ConfigurationManager.SERVER_URL, Commons.LOGIN);
        	layer = "Google";
    	} else if (StringUtils.equals(service, Commons.LINKEDIN)) {
    		LinkedInUtils.sendPost(ConfigurationManager.SERVER_URL, "GMS World", Commons.LOGIN, accessToken);
    		layer = "LinkedIn";
    	} else if (StringUtils.equals(service, Commons.TWITTER)) {
    		String tokenSecret = params.get("tokenSecret")[0];
        	TwitterUtils.sendMessage(null, ConfigurationManager.SERVER_URL, accessToken, tokenSecret, Commons.LOGIN);
        	layer = Commons.TWITTER_LAYER;
    	}
    	
    	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
        MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, service));
        if (StringUtils.isNotEmpty(email) && layer != null) {
        	MailUtils.sendLoginNotification(email, name, layer, context);
        }
	}
	
	public static void sendUserProfileNotification(Map<String, String[]> params) {
		String service = params.get("service")[0];
    	String url = params.get("url")[0];
    	int type = NumberUtils.getInt(params.get("type")[0],-1);
    	String title = params.get("title")[0];
    	String key = null;
    	if (params.containsKey("key")) {
    		key = params.get("key")[0];
    	}
    	String token = null;
    	if (params.containsKey("token")) {
    		token = params.get("token")[0];
    	}
    	
    	logger.log(Level.INFO, "Sending notification to {0} user social profile...", service);
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK)) {
        	FacebookUtils.sendMessageToUserFeed(token, url, title, type);
    	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		String refreshToken = params.get("refresh_token")[0];
    	    GooglePlusUtils.sendMessage(token, refreshToken, key, url, type);
    	} else if (StringUtils.equals(service, Commons.LINKEDIN)) {
    		LinkedInUtils.sendPost(url, title, type, token);
    	} else if (StringUtils.equals(service, Commons.TWITTER)) {
    		String secret = params.get("secret")[0];
    		TwitterUtils.sendMessage(key, url, token, secret, type);
    	}
	}
}
