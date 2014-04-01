package com.jstakun.lm.server.social;

import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;

public class FacebookUtils {
	
	private static final Random random = new Random();
	private static final Logger logger = Logger.getLogger(FacebookUtils.class.getName());
	
	private static String sendMessage(FacebookClient facebookClient, String connection, Parameter[] params, boolean verifyPermission) {
        try {          
        	boolean hasPermission = false;
        	if (verifyPermission) {
        		//check if user has given messaging permission            
        		try {
        			JsonObject permissions = facebookClient.fetchObject("me/permissions", JsonObject.class);
        			JsonArray data = permissions.getJsonArray("data");
        			JsonObject d = data.getJsonObject(0);
        			if (d.optInt("publish_stream", 0) == 1) {
        				logger.log(Level.INFO, "User has granted publish permission");
        				hasPermission = true;
        			} else {
        				logger.log(Level.INFO, permissions.toString());
        			}	
        		} catch (Exception e) {
        			logger.log(Level.SEVERE, "FacebookUtils.sendMessage() exception", e);
        		}
        	}
            
        	if (!verifyPermission || hasPermission) {
        		FacebookType publishMessageResponse = (FacebookType) facebookClient.publish(connection, FacebookType.class, params);
        		String id = publishMessageResponse.getId();
        		logger.log(Level.INFO, "Published Facebook message ID: {0}", id);
        		return id;
        	} else {
        		return null;
        	}
        } catch (FacebookException ex) {
        	logger.log(Level.SEVERE, "FacebookUtils.sendMessage() exception", ex);
            return null;
        }
    }

    public static void sendMessageToUserFeed(String token, String key, int type) {
        if (token != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(token);
            Parameter params[] = null;
            String name = null;
            String link = null;
            if (key != null) {
            	Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
            	link = UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark));    
            	name = landmark.getName();
            }
            //message, picture, link, name, caption, description, source, place, tags
            
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
                      
            if (type == Commons.BLOGEO) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.blogeo")),
                            Parameter.with("name", name),
                            Parameter.with("description", rb.getString("Social.fb.desc.blogeo")),
                            Parameter.with("link", link),
                            Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/blogeo_j.png")
                        };
            } else if (type == Commons.LANDMARK) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.landmark")),
                            Parameter.with("name", name),
                            Parameter.with("description", rb.getString("Social.fb.desc.landmark")),
                            Parameter.with("link", link),
                            Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/poi_j.png")
                        };
            } else if (type == Commons.LOGIN) {
            	 params = new Parameter[]{
            			 Parameter.with("message", rb.getString("Social.login")),
            			 Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/3globe_80.png"),
            			 Parameter.with("description", rb.getString("Social.login.desc")),
            			 Parameter.with("link", ConfigurationManager.SERVER_URL),
             			Parameter.with("name", "Message from GMS World"),
                     }; 
            } else if (type == Commons.MY_POS) {
            	params = new Parameter[]{
            			Parameter.with("message", rb.getString("Social.fb.message.mypos")),
            			Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/location.png"),
            			Parameter.with("description", rb.getString("Social.fb.desc.mypos")),
            			Parameter.with("link", link),
            			Parameter.with("name", name),
            	};		
            }
            sendMessage(facebookClient, "me/feed", params, true);
        } else {
            logger.log(Level.SEVERE, "Landmark or token is null! Key: {0}, token: {1}", new Object[]{key, token});
        }
    }

    //login with manage_pages permission
    public static void sendMessageToPageFeed(String key, String landmarkUrl) {
        final String[] images = {"blogeo_j.png", "blogeo_a.png", "poi_j.png", "poi_a.png"};
        int imageId = 2;
        try {
            imageId = random.nextInt(4);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        //logger.log(Level.INFO, "Image id: {0}", imageId);
        if (imageId > 3 || imageId < 0) {
            imageId = 2;
        }
        Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
        if (landmark != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(Commons.fb_page_token);
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            Parameter params[] = null;
            //message, picture, link, name, caption, description, source, place, tags
            String userMask = UrlUtils.createUsernameMask(landmark.getUsername());
            //logger.log(Level.INFO, "FB message link is: {0}", link);
            params = new Parameter[]{
                        Parameter.with("message", String.format(rb.getString("Social.fb.message.server"), userMask)),
                        Parameter.with("name", landmark.getName()),
                        Parameter.with("description", rb.getString("Social.fb.desc.server")),
                        Parameter.with("link", landmarkUrl),
                        Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/" + images[imageId])
                    };
            sendMessage(facebookClient, Commons.FB_GMS_WORLD_FEED, params, false);
        } else {
            logger.log(Level.SEVERE, "Landmark key is wrong! Key: {0}", key);
        }
    }
    
    public static void sendImageMessage(String imageUrl, String showImageUrl, String username) {
        if (imageUrl != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(Commons.fb_page_token);
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            String userMask = UrlUtils.createUsernameMask(username);
            //logger.log(Level.INFO, "FB message link is: {0}", link);
            Parameter[] params = new Parameter[]{
                Parameter.with("message", String.format(rb.getString("Social.fb.message.screenshot"),userMask)),
                Parameter.with("name", "GMS World"),
                Parameter.with("description", rb.getString("Social.fb.desc.screenshot")),
                Parameter.with("link", showImageUrl),
                Parameter.with("picture", imageUrl + "=s128")
            };

            sendMessage(facebookClient, Commons.FB_GMS_WORLD_FEED, params, false);
        } else {
            logger.log(Level.SEVERE, "Image url is null!");
        }
    }
    
    public static int checkin(String token, String place, String name) {
    	FacebookClient facebookClient = new DefaultFacebookClient(token);
    	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
    	Parameter[] params = new Parameter[]{
    			Parameter.with("message", String.format(rb.getString("Social.checkin"), name)),
                Parameter.with("place", place),
    	};
    	String id = sendMessage(facebookClient, "me/feed", params, true);
    	if (id != null) {
    		return 200;
    	} else {
    		return 200; //TODO change to 500;
    	}
    }
    
    public static int sendComment(String token, String place, String message, String name) {
    	FacebookClient facebookClient = new DefaultFacebookClient(token);
    	Parameter[] params = new Parameter[]{
    			Parameter.with("message", message),
                Parameter.with("link", place),
                Parameter.with("name", name),
    	};
    	String id = sendMessage(facebookClient, "me/feed", params, true);
    	if (id != null) {
    		return 200;
    	} else {
    		return 500;
    	}
    }
}
