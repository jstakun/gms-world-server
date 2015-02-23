package com.jstakun.lm.server.social;

import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.NumberUtils;

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
        	
        	//remove
        	//logger.log(Level.INFO, "Sending message with params:");
        	//for (Parameter param : params) {
        	//	logger.log(Level.INFO, param.name + "=" + param.value);
        	//}
        	//
            
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

	protected static void sendMessageToUserFeed(String token, String url, String title, int type) {
        if (token != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(token);
            Parameter params[] = null;
            //message, picture, link, name, caption, description, source, place, tags
            
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
                      
            if (type == Commons.BLOGEO) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.blogeo")),
                            Parameter.with("name", title),
                            Parameter.with("description", rb.getString("Social.fb.desc.blogeo")),
                            Parameter.with("link", url),
                            Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/blogeo_j.png")
                        };
            } else if (type == Commons.LANDMARK) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.landmark")),
                            Parameter.with("name", title),
                            Parameter.with("description", rb.getString("Social.fb.desc.landmark")),
                            Parameter.with("link", url),
                            Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/poi_j.png")
                        };
            } else if (type == Commons.LOGIN) {
            	 params = new Parameter[]{
            			 Parameter.with("message", rb.getString("Social.login")),
            			 Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/3globe_80.png"),
            			 Parameter.with("description", rb.getString("Social.login.desc")),
            			 Parameter.with("link", url),
             			Parameter.with("name", title),
                     }; 
            } else if (type == Commons.MY_POS) {
            	params = new Parameter[]{
            			Parameter.with("message", rb.getString("Social.fb.message.mypos")),
            			Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/location.png"),
            			Parameter.with("description", rb.getString("Social.fb.desc.mypos")),
            			Parameter.with("link", url),
            			Parameter.with("name", title),
            	};		
            }
            sendMessage(facebookClient, "me/feed", params, true);
        } else {
            logger.log(Level.SEVERE, "Token is empty!");
        }
    }

    //login with manage_pages permission
    protected static void sendMessageToPageFeed(String key, String url, String user, String name, int type) {
        final String[] images = {"blogeo_j.png", "blogeo_a.png", "poi_j.png", "poi_a.png"};
        ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
        Parameter params[] = null;
        
        if (type == Commons.SERVER) {
        	Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
        	if (landmark != null) {
        		//message, picture, link, name, caption, description, source, place, tags
        		String userMask = UrlUtils.createUsernameMask(landmark.getUsername());
        		int imageId = NumberUtils.normalizeNumber(random.nextInt(4), 0, 3);
                //logger.log(Level.INFO, "FB message link is: {0}", link);
        		params = new Parameter[]{
                        Parameter.with("message", String.format(rb.getString("Social.fb.message.server"), userMask)),
                        Parameter.with("name", landmark.getName()),
                        Parameter.with("description", rb.getString("Social.fb.desc.server")),
                        Parameter.with("link", url),
                        Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/" + images[imageId])
                    };
        	} else {
        		logger.log(Level.SEVERE, "Landmark with key {0} is empty!", key);
        	}
        } else if (type == Commons.CHECKIN) {
        	params = new Parameter[]{
                    Parameter.with("message", String.format(rb.getString("Social.fb.message.checkin"), user, name)),
                    Parameter.with("name", name),
                    Parameter.with("description", rb.getString("Social.fb.desc.checkin")),
                    Parameter.with("link", url),
                    Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/checkin.png")
                };   
        }
        
        if (params != null) {
        	FacebookClient facebookClient = new DefaultFacebookClient(Commons.getProperty(Property.fb_page_token));
            sendMessage(facebookClient, Commons.getProperty(Property.FB_GMS_WORLD_FEED), params, false);
        }
    }
    
    protected static void sendImageMessage(String imageUrl, String showImageUrl, String username) {
        if (imageUrl != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(Commons.getProperty(Property.fb_page_token));
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            String userMask = UrlUtils.createUsernameMask(username);
            //logger.log(Level.INFO, "FB message link is: {0}", link);
            Parameter[] params = new Parameter[]{
                Parameter.with("message", String.format(rb.getString("Social.fb.message.screenshot"),userMask)),
                Parameter.with("name", "GMS World"),
                Parameter.with("description", rb.getString("Social.fb.desc.screenshot")),
                Parameter.with("link", showImageUrl),
                Parameter.with("picture", imageUrl)
            };

            sendMessage(facebookClient, Commons.getProperty(Property.FB_GMS_WORLD_FEED), params, false);
        } else {
            logger.log(Level.SEVERE, "Image url is null!");
        }
    }
    
    protected static int checkin(String token, String place, String name) {
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
    
    protected static int sendComment(String token, String place, String message, String name) {
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
