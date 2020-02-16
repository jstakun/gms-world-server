package com.jstakun.lm.server.social;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.layers.FacebookUtils;
import net.gmsworld.server.utils.UrlUtils;

public class FacebookSocialUtils {
	
	//private static final Random random = new Random();
	private static final Logger logger = Logger.getLogger(FacebookSocialUtils.class.getName());
	
	private static String sendMessage(FacebookClient facebookClient, String connection, Parameter[] params, boolean verifyPermission) {
        try {          
        	boolean hasPermission = false;
        	if (verifyPermission) {
        		try {
        			JsonObject permissions = facebookClient.fetchObject("me/permissions", JsonObject.class);
        			JsonArray data = permissions.get("data").asArray();
        			for (int i=0;i<data.size();i++) {
        				JsonObject p = data.get(i).asObject();
        				if (StringUtils.equals(p.get("permission").asString(), "publish_actions") && 
        						StringUtils.equals(p.get("status").asString(), "granted")) {
        					hasPermission = true;
        					break;
        				}	
        			} 
        			if (!hasPermission) {
        				logger.log(Level.WARNING, "Access token has no publish_actions permission: " + permissions.toString());	
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

	protected static String sendMessageToUserFeed(String token, String url, String title, int type) {
        if (token != null) {
            FacebookClient facebookClient = FacebookUtils.getFacebookClient(token);
            Parameter params[] = null;
            //message, picture, link, name, caption, description, source, place, tags
            
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
                      
            if (type == Commons.BLOGEO) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.blogeo")),
                            //Parameter.with("name", title),
                            //Parameter.with("description", rb.getString("Social.fb.desc.blogeo")),
                            Parameter.with("link", url),
                            //Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/blogeo_j.png")
                        };
            } else if (type == Commons.LANDMARK) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.landmark")),
                            //Parameter.with("name", title),
                            //Parameter.with("description", rb.getString("Social.fb.desc.landmark")),
                            Parameter.with("link", url),
                            //Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/poi_j.png")
                        };
            } else if (type == Commons.LOGIN) {
            	 params = new Parameter[]{
            			 Parameter.with("message", rb.getString("Social.login")),
            			 //Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/3globe_80.png"),
            			 //Parameter.with("description", rb.getString("Social.login.desc")),
            			 Parameter.with("link", url),
             			//Parameter.with("name", title),
                     }; 
            } else if (type == Commons.MY_POS) {
            	params = new Parameter[]{
            			Parameter.with("message", rb.getString("Social.fb.message.mypos")),
            			//Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/location.png"),
            			//Parameter.with("description", rb.getString("Social.fb.desc.mypos")),
            			Parameter.with("link", url),
            			//Parameter.with("name", title),
            	};		
            }
            return sendMessage(facebookClient, "me/feed", params, true);
        } else {
            logger.log(Level.SEVERE, "Token is empty!");
            return null;
        }
    }

    protected static String sendMessageToPageFeed(String url, String user, String name, String imageUrl, int type, String token) {
        ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
        Parameter params[] = null;
        
        if (type == Commons.SERVER) {
           //message, picture, link, name, caption, description, source, place, tags
           //if (imageUrl == null) {
           //	   final String[] images = {"blogeo_j.png", "blogeo_a.png", "poi_j.png", "poi_a.png"};
           //      final int imageId = NumberUtils.normalizeNumber(random.nextInt(4), 0, 3);
           //	   imageUrl = ConfigurationManager.SERVER_URL + "images/" + images[imageId];
           //}
           params = new Parameter[]{
                   Parameter.with("message", String.format(rb.getString("Social.fb.message.server"), name)),
                   //Parameter.with("name", name),
                   //Parameter.with("description", rb.getString("Social.fb.desc.server")),
                   Parameter.with("link", url),
                   //Parameter.with("picture", imageUrl)
           };      	
        } else if (type == Commons.CHECKIN) {
        	params = new Parameter[]{
                    Parameter.with("message", String.format(rb.getString("Social.fb.message.checkin"), user, name)),
                    //Parameter.with("name", name),
                    //Parameter.with("description", rb.getString("Social.fb.desc.checkin")),
                    Parameter.with("link", url),
                    //Parameter.with("picture", imageUrl)
            };   
        } else if (type == Commons.HOTELS) {
        	if (imageUrl == null) {
         	   imageUrl = ConfigurationManager.SERVER_URL + "images/hotel_search_128.png";
            }
        	params = new Parameter[]{
                    Parameter.with("message", String.format(rb.getString("Social.fb.message.hotels"), user, name)),
                    //Parameter.with("name", name),
                    //Parameter.with("description", rb.getString("Social.fb.desc.hotels")),
                    Parameter.with("link", url),
                    //Parameter.with("picture", imageUrl)
            };	
        }
        
        if (params != null) {      	
        	if (token == null) {
        		token = com.jstakun.lm.server.config.ConfigurationManager.getParam(com.jstakun.lm.server.config.ConfigurationManager.GMS_WORLD_PAGE_TOKEN, null);
        	}
        	FacebookClient facebookClient = FacebookUtils.getFacebookClient(token);
        	return sendMessage(facebookClient, Commons.getProperty(Property.FB_GMS_WORLD_FEED), params, false);
        } else {
        	logger.log(Level.SEVERE, "Params are null!");
        	return null;
        }
    }
    
    protected static String sendImageMessage(String imageUrl, String showImageUrl, String username, String flex, int type) {
        if (imageUrl != null) {
            FacebookClient facebookClient = FacebookUtils.getFacebookClient(com.jstakun.lm.server.config.ConfigurationManager.getParam(com.jstakun.lm.server.config.ConfigurationManager.GMS_WORLD_PAGE_TOKEN, null));
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            String userMask = UrlUtils.createUsernameMask(username);
            Parameter[] params = null;
            if (type == Commons.SCREENSHOT) {
            	params = new Parameter[]{
            			Parameter.with("message", String.format(rb.getString("Social.fb.message.screenshot"),userMask)),
            			//Parameter.with("name", "GMS World"),
            			//Parameter.with("description", rb.getString("Social.fb.desc.image")),
            			Parameter.with("link", showImageUrl),
            			//Parameter.with("picture", imageUrl + "?thumbnail=false")
            	};
            } else if (type == Commons.ROUTE) {
            	params = new Parameter[]{
            			Parameter.with("message", String.format(rb.getString("Social.fb.message.route"), userMask, flex)),
            			//Parameter.with("name", "GMS World"),
            			//Parameter.with("description", rb.getString("Social.fb.desc.image")),
            			Parameter.with("link", showImageUrl),
            			//Parameter.with("picture", imageUrl + "&thumbnail=false")
            	};
            }

            if (params != null) {
            	return sendMessage(facebookClient, Commons.getProperty(Property.FB_GMS_WORLD_FEED), params, false);
            } else {
            	logger.log(Level.SEVERE, "Params are null!");
            	return null;
            }
        } else {
            logger.log(Level.SEVERE, "Image url is null!");
            return null;
        }
    }
    
    protected static int checkin(String token, String place, String name) {
    	FacebookClient facebookClient = FacebookUtils.getFacebookClient(token);
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
    	FacebookClient facebookClient = FacebookUtils.getFacebookClient(token);
    	Parameter[] params = new Parameter[]{
    			Parameter.with("message", message),
                Parameter.with("link", place),
                //Parameter.with("name", name),
    	};
    	String id = sendMessage(facebookClient, "me/feed", params, true);
    	if (id != null) {
    		return 200;
    	} else {
    		return 500;
    	}
    }
}
