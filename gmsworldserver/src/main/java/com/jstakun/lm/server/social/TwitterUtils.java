package com.jstakun.lm.server.social;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringUtils;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

public class TwitterUtils {
	
	private static final Logger logger = Logger.getLogger(TwitterUtils.class.getName());
	
	private static Twitter getTwitter(String token, String secret) {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Commons.getProperty(Property.TW_CONSUMER_KEY), Commons.getProperty(Property.TW_CONSUMER_SECRET));
        AccessToken accessToken;
        if (token != null && secret != null) {
            accessToken = new AccessToken(token, secret);
        } else {
            accessToken = new AccessToken(Commons.getProperty(Property.TW_TOKEN), Commons.getProperty(Property.TW_SECRET));
        }
        twitter.setOAuthAccessToken(accessToken);
        return twitter;
    }
	
	protected static void sendMessage(String key, String url, String token, String secret, String user, String name, String imageUrl, int type) {
		String message = null;
		try {
        	    Landmark landmark = null; 
        	    
        	    if (key != null) {
        	    	landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
                }
            
        	    ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
                if (type == Commons.SERVER && landmark != null) {
                    String username = landmark.getUsername();
                    String userMask;
                    if (StringUtils.endsWith(username, "@tw")) {
                        userMask = "@" + username.substring(0, username.length() - 3);
                    } else {
                        userMask = UrlUtils.createUsernameMask(username);
                    }
                    message = String.format(rb.getString("Social.tw.server"), userMask, landmark.getName(), url);
                } else if (type == Commons.BLOGEO && landmark != null) {
                    message = String.format(rb.getString("Social.tw.status"), "new geo message to #GMSWorldBlogeo", landmark.getName(), url);
                } else if (type == Commons.LANDMARK && landmark != null) {
                    message = String.format(rb.getString("Social.tw.status"), "new point of interest to #GMSWorld", landmark.getName(), url);
                } else if (type == Commons.MY_POS) {
                    message = String.format(rb.getString("Social.tw.myloc"),  url);
                } else if (type == Commons.LOGIN) {
                    message = String.format(rb.getString("Social.tw.login"), url);
                } else if (type == Commons.CHECKIN) { 
                	message = String.format(rb.getString("Social.tw.checkin"), user, name, url);
                }

                if (message != null) {
                	//message length must be < 140
                    if (message.length() > 140 && url != null) {
                		message = String.format(rb.getString("Social.tw.short"), url);
                	}
                	StatusUpdate update = new StatusUpdate(message);
                    if (landmark != null) {
                    	update.setDisplayCoordinates(true);
                        update.setLocation(new GeoLocation(landmark.getLatitude(), landmark.getLongitude()));
                        if (imageUrl != null) {
                        	try {
                            	InputStream is  = new URL(imageUrl).openStream();
                            	if (is != null) {
                            		update.media("landmark.jpg", is);
                            	}
                            } catch (Exception e) {
                            	logger.log(Level.SEVERE, "Failed to load landmark.", e);
                            }
                        }
                    }
                    Status s = getTwitter(token, secret).updateStatus(update);
                    logger.log(Level.INFO, "Sent twitter update id: {0}", s.getId());
                } 
        } catch (Exception ex) {
            if (message != null) {
            	logger.log(Level.SEVERE, "Failed to send status: {0}", message);
            }
        	logger.log(Level.SEVERE, ex.getMessage(), ex);
            
        }
    }

    protected static void sendImageMessage(String flex, String imageUrl, String username, Double latitude, Double longitude, int type) {
    	String message = null;
    	try {
            String userMask;

            if (StringUtils.endsWith(username, "@tw")) {
                userMask = "@" + username.substring(0, username.length() - 3);
            } else {
                userMask = UrlUtils.createUsernameMask(username);
            }
            
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            if (type == Commons.SCREENSHOT) {
            	message = String.format(rb.getString("Social.tw.screenshot"), userMask, flex);
            } else if (type == Commons.ROUTE) {
            	message = String.format(rb.getString("Social.tw.route"), userMask, flex, ConfigurationManager.SERVER_URL); 
            }

            StatusUpdate update = new StatusUpdate(message);
            if (latitude != null && longitude != null) {
            	update.setDisplayCoordinates(true);
            	update.setLocation(new GeoLocation(latitude, longitude));
            }
            
            InputStream is = null;
            try {
            	if (type == Commons.SCREENSHOT) {
            		is  = new URL(imageUrl + "?thumbnail=false").openStream();
            	} else if (type == Commons.ROUTE) {
            		is  = new URL(imageUrl + "&thumbnail=false").openStream();
            	}
            	if (is != null) {
            		update.media("image.jpg", is);
            	}
            } catch (Exception e) {
            	logger.log(Level.SEVERE, "Failed to load image " + imageUrl, e);
            } finally {
            	if (is != null) {
            		is.close();
            	}
            }
            Status s = getTwitter(null, null).updateStatus(update);
            logger.log(Level.INFO, "Sent twitter update id: {0}", s.getId());
        } catch (Exception ex) {
        	if (message != null) {
            	logger.log(Level.SEVERE, "Failed to send status: {0}", message);
            }
        	logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
