package com.jstakun.lm.server.social;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.HtmlUtils;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import com.openlapi.AddressInfo;

public class NotificationUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationUtils.class.getName());
	
	private static final int HOTELS_RADIUS = 50;
	
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
				params.put("url", UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark.getHash(), landmark.getId(), landmark.getCreationDate())));
				if (landmark.isSocial()) {
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
	
	public static void createSocialCheckinNotificationTask(Map<String, String> params) {
		Map<String, String> newParams = new HashMap<String, String>(params);
		
		//FacebookUtils
		newParams.put("service", Commons.FACEBOOK);
		createNotificationTask(newParams);
		
		//TwitterUtils
		newParams.put("service", Commons.TWITTER);
        createNotificationTask(newParams);
		
        //GoogleBloggerUtils
        newParams.put("service", Commons.GOOGLE);
        createNotificationTask(newParams);
		
        //GooglePlusUtils
        //newParams.put("service", Commons.GOOGLE_PLUS);
        //createNotificationTask(newParams);
       
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
        newParams.put("service", Commons.GOOGLE);
        createNotificationTask(newParams);
		
        //GooglePlusUtils
        //newParams.put("service", Commons.GOOGLE_PLUS);
        //createNotificationTask(newParams);
        
        //email
        newParams.put("service", Commons.MAIL);
        createNotificationTask(newParams);
        
        //Hotels notification
        newParams.put("type", "Hotels");
        
        newParams.put("service", Commons.FACEBOOK);
		createNotificationTask(newParams);
		
		newParams.put("service", Commons.TWITTER);
        createNotificationTask(newParams);
	}
	
	public static void createRouteCreationNotificationTask(Map<String, String> params) {
		Map<String, String> newParams = new HashMap<String, String>(params);
		//FacebookUtils
		newParams.put("service", Commons.FACEBOOK);
		createNotificationTask(newParams);
		
		//TwitterUtils
		newParams.put("service", Commons.TWITTER);
        createNotificationTask(newParams);
		
        //GoogleBloggerUtils
        newParams.put("service", Commons.GOOGLE);
        createNotificationTask(newParams);
		
        //GooglePlusUtils
        //newParams.put("service", Commons.GOOGLE_PLUS);
        //createNotificationTask(newParams);
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
        newParams.put("service", Commons.GOOGLE);
        createNotificationTask(newParams);
		
        //GooglePlusUtils
        //newParams.put("service", Commons.GOOGLE_PLUS);
        //createNotificationTask(newParams);
	}
	
	public static String sendImageCreationNotification(Map<String, String[]> params) {
		String imageUrl = params.get("imageUrl")[0];
    	String username = params.get("username")[0];
    	String service = params.get("service")[0];
    	String showImageUrl = params.get("showImageUrl")[0];
        Double lat = NumberUtils.getDouble(params.get("lat")[0], 0d);
    	Double lng = NumberUtils.getDouble(params.get("lng")[0], 0d);
    	
    	if (StringUtils.isNotEmpty(imageUrl) && StringUtils.isNotEmpty(service) && StringUtils.isNotEmpty(showImageUrl)) {  	
    		logger.log(Level.INFO, "Sending image creation notification to {0}...", service);    	
    		if (StringUtils.equals(service, Commons.FACEBOOK)) {
    			return FacebookSocialUtils.sendImageMessage(imageUrl, showImageUrl, username, null, Commons.SCREENSHOT);
    		} else if (StringUtils.equals(service, Commons.TWITTER)) {
    			return TwitterUtils.sendImageMessage(showImageUrl, imageUrl, username, lat, lng, null, Commons.SCREENSHOT);
    		} else if (StringUtils.equals(service, Commons.GOOGLE)) {
    			return GoogleBloggerUtils.sendImageMessage(showImageUrl, username, imageUrl, null, lat, lng, Commons.SCREENSHOT);
    		} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    			return GooglePlusUtils.sendImageMessage(showImageUrl, username, imageUrl, null, Commons.SCREENSHOT);
    		} else {
    			return null;
    		}   	
    	} else {
    		logger.log(Level.SEVERE, "Wrong parameters imageUrl={0}, service={1}, showImageUrl={2}", new Object[]{imageUrl, service, showImageUrl});
    		return null;
    	}
	}
	
	public static String sendLandmarkCreationNotification(Map<String, String[]> params, ServletContext context) {
		String service = params.get("service")[0];
    	//String key = params.get("key")[0];
    	String landmarkUrl = params.get("landmarkUrl")[0];
    	String email = params.get("email")[0];
    	String title = params.get("title")[0];
    	String body = params.get("body")[0];   
    	String userUrl = params.get("userUrl")[0];
    	String user = params.get("username")[0];
    	String imageUrl = params.get("imageUrl")[0];
    	String socialIds = params.get("socialIds")[0];
    	String name = params.get("name")[0];
    	Double latitude = Double.parseDouble(params.get("latitude")[0]);
    	Double longitude = Double.parseDouble(params.get("longitude")[0]);
    	String desc = params.get("desc")[0];
    	String layer = params.get("layer")[0];
    	String type = null;
    	if (params.containsKey("type")) {
    		type = params.get("type")[0];
    	}
    	
    	Map<String, String> socialIdsMap = new HashMap<String, String>();
    	if (socialIds != null) {
    		String[] ids = StringUtils.split(socialIds, ",");
    		for (int i=0;i<ids.length;i++) {
    			String[] id = StringUtils.split(ids[i], "@");
    			if (id.length == 2) {
    				socialIdsMap.put(id[1], id[0]);
    			} else if (id.length == 1) {
    				socialIdsMap.put(Commons.GMS_WORLD, id[0]);
    			}
    		}
    	} else {
    		String[] id = StringUtils.split(user, "@");
			if (id.length == 2) {
				socialIdsMap.put(id[1], id[0]);
			} else if (id.length == 1) {
				socialIdsMap.put(Commons.GMS_WORLD, id[0]);
			}
    	}
    	
    	logger.log(Level.INFO, "Sending landmark creation notification to service {0}...", service);
    	
    	String userMask = null;   	
    	AddressInfo addressInfo = null;
    	String cheapestPrice = null;
    	int hotelsCount = -1;
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK) || StringUtils.equals(service, Commons.TWITTER)) {
    		 try {
    			 addressInfo = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(latitude, longitude);
    		 } catch (Exception e) {
    	         logger.log(Level.SEVERE, e.getMessage(), e);
    	     } 
    		 if (StringUtils.equals(type, "Hotels")) {
    			 hotelsCount = LayerHelperFactory.getHotelsBookingUtils().countNearbyHotels(latitude, longitude, HOTELS_RADIUS);
    			 if (hotelsCount > 0) {	
    				 cheapestPrice = LayerHelperFactory.getHotelsBookingUtils().findCheapestHotel(latitude, longitude, HOTELS_RADIUS, 1);
    			 }
    		 }
    	}
    	
        if (StringUtils.equals(service, Commons.FACEBOOK)) {
    		if (StringUtils.equals(type, "Hotels")) {
    			String fbTitle = name;
    			if (addressInfo != null) {
    				fbTitle = "around ";
    				String city = addressInfo.getField(AddressInfo.CITY);
    				if (StringUtils.isNotEmpty(city)) {
    					fbTitle += city + ", ";
    				} else {
    					fbTitle = "somewhere in ";
    				}
    				String cc = addressInfo.getField(AddressInfo.COUNTRY_CODE);
    				if (StringUtils.isNotEmpty(cc)) {
    					Locale l = new Locale("", cc);
    					String country = l.getDisplayCountry();
    					if (StringUtils.isNotEmpty(country)) {
    						fbTitle += country;
    					} else {
    						fbTitle += "...";
    					}
    				} else {
    					fbTitle += "...";
    				}
    			}  if (cheapestPrice != null) {
                	userMask = " " + fbTitle;
                	fbTitle = "From " + cheapestPrice + " per night";
                } else {
                	userMask = " around!";
                }
                if (hotelsCount > 0) {
                	String hotelsUrl = UrlUtils.getShortUrl(com.jstakun.lm.server.config.ConfigurationManager.HOTELS_URL + "hotelLandmark/" + HtmlUtils.encodeDouble(latitude) + "/" + HtmlUtils.encodeDouble(longitude));
                	return FacebookSocialUtils.sendMessageToPageFeed(hotelsUrl, userMask, fbTitle, imageUrl, Commons.HOTELS, null);
                } else {
                	return null;
                }
    		} else {
    			if (socialIdsMap.containsKey(Commons.FACEBOOK)) {
    				userMask = UrlUtils.createUsernameMask(socialIdsMap.get(Commons.FACEBOOK) + "@" + Commons.FACEBOOK);
    			} else {
    				userMask = UrlUtils.createUsernameMask(user);
    			}
    			logger.log(Level.INFO, "Using user mask " + userMask);
    			String fbTitle = name;
    			if (addressInfo != null) {
    				fbTitle = "In ";
    				String city = addressInfo.getField(AddressInfo.CITY);
    				if (StringUtils.isNotEmpty(city)) {
    					fbTitle += city + ", ";
    				} else {
    					fbTitle = "Somewhere in ";
    				}
    				String cc = addressInfo.getField(AddressInfo.COUNTRY_CODE);
    				if (StringUtils.isNotEmpty(cc)) {
    					Locale l = new Locale("", cc);
    					String country = l.getDisplayCountry();
    					if (StringUtils.isNotEmpty(country)) {
    						fbTitle += country;
    					} else {
    						fbTitle += "...";
    					}
    				} else {
    					fbTitle += "...";
    				}
    			}    
    			return FacebookSocialUtils.sendMessageToPageFeed(landmarkUrl, userMask, fbTitle, imageUrl, Commons.SERVER, null);
    		}
    	} else if (StringUtils.equals(service, Commons.TWITTER)) {
    		if (StringUtils.equals(type, "Hotels")) {
    			if (latitude != null && longitude != null) {
        			name = "";
        			if (addressInfo != null) {
                    	String city = addressInfo.getField(AddressInfo.CITY);
                    	if (StringUtils.isNotEmpty(city)) {
                    		name += city + ", ";
                    	} else {
                    		name = "somewhere in ";
                    	}
                    	String cc = addressInfo.getField(AddressInfo.COUNTRY_CODE);
                    	if (StringUtils.isNotEmpty(cc)) {
                    		Locale l = new Locale("", cc);
                    		String country = l.getDisplayCountry();
                    		if (StringUtils.isNotEmpty(country)) {
                    			name += country;
                    		} else {
                    			name += "...";
                    		}
                    	} else {
                    		name += "...";
                    	}
                    }
        			if (cheapestPrice != null) {
        				if (StringUtils.isNotEmpty(name)) {
        					name += " ";
        				}
        				name += "from " + cheapestPrice + " per night";
        			}
        			if (hotelsCount > 0) {
        				String hotelsUrl = UrlUtils.getShortUrl(com.jstakun.lm.server.config.ConfigurationManager.HOTELS_URL + "hotelLandmark/" + HtmlUtils.encodeDouble(latitude) + "/" + HtmlUtils.encodeDouble(longitude));
            			return TwitterUtils.sendMessage(hotelsUrl, Commons.getProperty(Property.TW_TOKEN), Commons.getProperty(Property.TW_SECRET), userMask, name, imageUrl, latitude, longitude, Commons.HOTELS);
        			} else {
        				return null;
        			}
        		} else {
        			return null;
        		}
    		} else {
    			if (socialIdsMap.containsKey(Commons.TWITTER)) {
    				userMask = "@" + socialIdsMap.get(Commons.TWITTER);
    			} else {
    				userMask = UrlUtils.createUsernameMask(user);
    			}
    			logger.log(Level.INFO, "Using user mask " + userMask);
    			return TwitterUtils.sendMessage(landmarkUrl, Commons.getProperty(Property.TW_TOKEN), Commons.getProperty(Property.TW_SECRET), userMask, name, imageUrl, latitude, longitude, Commons.SERVER);
    		}
    	} else if (StringUtils.equals(service, Commons.GOOGLE)) {
    		//TODO add hotels notification
    		if (socialIdsMap.containsKey(Commons.GOOGLE)) {
    			userMask = socialIdsMap.get(Commons.GOOGLE) + "@" + Commons.GOOGLE;
    		} else if (socialIdsMap.containsKey(Commons.GOOGLE_PLUS)) {
    			userMask = socialIdsMap.get(Commons.GOOGLE_PLUS) + "@" + Commons.GOOGLE_PLUS;
    		} else {
    			userMask = user;
    		}
    		logger.log(Level.INFO, "Using user mask " + userMask);
    		return GoogleBloggerUtils.sendMessage(landmarkUrl, Commons.getProperty(Property.gl_plus_token), Commons.getProperty(Property.gl_plus_refresh), userMask, name, imageUrl, layer, latitude, longitude, desc, Commons.SERVER);
    	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		userMask = UrlUtils.createUsernameMask(user);
    		logger.log(Level.INFO, "Using user mask " + userMask);
    		return GooglePlusUtils.sendMessage(Commons.getProperty(Property.gl_plus_token), Commons.getProperty(Property.gl_plus_refresh), landmarkUrl, userMask, name, latitude, longitude, Commons.SERVER);
    	} else if (StringUtils.equals(service, Commons.MAIL)) {
    		MailUtils.sendLandmarkCreationNotification(title, body);
    		//send landmark creation notification email to user
    		if (StringUtils.isNotEmpty(email)) {
    			userMask = UrlUtils.createUsernameMask(user);
    			logger.log(Level.INFO, "Using user mask " + userMask);
        		return MailUtils.sendLandmarkNotification(email, userUrl, userMask, landmarkUrl, context);
    		} else {
    			return null;
    		}
    	} else {
    		return null;
    	}
	}
	
	public static String sendUserLoginNotification(Map<String, String[]> params, ServletContext context) {
		String service = params.get("service")[0];
    	String accessToken = params.get("accessToken")[0];
    	String username = params.get("username")[0];
    	String name = params.get("name")[0];
    	String email = null;
    	if (params.containsKey("email")) {
    		email = params.get("email")[0];
    	}
    	String layer = null;
    	String status = null;
    	
    	logger.log(Level.INFO, "Sending user login notification to {0}...", service);
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK)) {
    		status = FacebookSocialUtils.sendMessageToUserFeed(accessToken, ConfigurationManager.SERVER_URL, "Message from GMS World", Commons.LOGIN);
            layer = Commons.FACEBOOK_LAYER;
    	} else if (StringUtils.equals(service, Commons.FOURSQUARE)) {
    		layer = Commons.FOURSQUARE_LAYER;
    		status = "ok";
    	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		String refreshToken = null;
    		if (params.containsKey("refreshToken")) {
    			refreshToken = params.get("refreshToken")[0];
    		}
    		status = GooglePlusUtils.sendMessage(accessToken, refreshToken, ConfigurationManager.SERVER_URL, username, name, null, null, Commons.LOGIN);
        	layer = "Google";
    	} else if (StringUtils.equals(service, Commons.LINKEDIN)) {
    		status = LinkedInUtils.sendPost(ConfigurationManager.SERVER_URL, "GMS World", Commons.LOGIN, accessToken);
    		layer = "LinkedIn";
    	} else if (StringUtils.equals(service, Commons.TWITTER)) {
    		String tokenSecret = params.get("tokenSecret")[0];
    		status = TwitterUtils.sendMessage(ConfigurationManager.SERVER_URL, accessToken, tokenSecret, username, name, null, null, null, Commons.LOGIN);
        	layer = Commons.TWITTER_LAYER;
    	} 
    	
    	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
        MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, service));
        if (StringUtils.isNotEmpty(email) && layer != null) {
        	MailUtils.sendLoginNotification(email, name, layer, context);
        }
        
        return status;
	}
	
	public static String sendUserProfileNotification(Map<String, String[]> params) {
		String service = params.get("service")[0];
    	String url = params.get("url")[0];
    	int type = NumberUtils.getInt(params.get("type")[0],-1);
    	String title = params.get("title")[0];
    	//String key = null;
    	//if (params.containsKey("key")) {
    	//	key = params.get("key")[0];
    	//}
    	String token = null;
    	if (params.containsKey("token")) {
    		token = params.get("token")[0];
    	}
    	
    	logger.log(Level.INFO, "Sending notification to {0} user social profile...", service);
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK)) {
        	return FacebookSocialUtils.sendMessageToUserFeed(token, url, title, type);
    	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		String refreshToken = params.get("refresh_token")[0];
    	    return GooglePlusUtils.sendMessage(token, refreshToken, url, null, null, null, null, type);
    	} else if (StringUtils.equals(service, Commons.LINKEDIN)) {
    		return LinkedInUtils.sendPost(url, title, type, token);
    	} else if (StringUtils.equals(service, Commons.TWITTER)) {
    		String secret = params.get("secret")[0];
    		return TwitterUtils.sendMessage(url, token, secret, null, null, null, null, null, type);
    	} else {
    		return null;
    	}
	}
	
	public static String sendCheckinNotification(Map<String, String[]> params) {
		String service = params.get("service")[0];
    	String url = params.get("url")[0];
    	String user = params.get("user")[0];
    	String name = params.get("name")[0];
    	String imageUrl = params.get("imageUrl")[0];
    	
    	Double latitude = null;
    	Double longitude = null;
    	try {
    		if (params.containsKey("lat")) {
    			latitude = Double.parseDouble(params.get("lat")[0]);
    		}
    		if (params.containsKey("lng")) {
    			longitude = Double.parseDouble(params.get("lng")[0]);
    		}
    	} catch (Exception e) {
    		
    	}
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK)) {
    		return FacebookSocialUtils.sendMessageToPageFeed(url, user, name, imageUrl, Commons.CHECKIN, null);
    	} else if (StringUtils.equals(service, Commons.TWITTER)) {
    		return TwitterUtils.sendMessage(url, Commons.getProperty(Property.TW_TOKEN), Commons.getProperty(Property.TW_SECRET), user, name, imageUrl, latitude, longitude, Commons.CHECKIN);
    	} else if (StringUtils.equals(service, Commons.GOOGLE)) {
    		return GoogleBloggerUtils.sendMessage(url, Commons.getProperty(Property.gl_plus_token), Commons.getProperty(Property.gl_plus_refresh), user, name, imageUrl, null, latitude, longitude, null,  Commons.CHECKIN);
    	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		return GooglePlusUtils.sendMessage(Commons.getProperty(Property.gl_plus_token), Commons.getProperty(Property.gl_plus_refresh), url, user, name, latitude, longitude, Commons.CHECKIN);
    	} else {
    		return null;
    	}
	}
	
	public static String sendRouteCreationNotification(Map<String, String[]> params) {
		String service = params.get("service")[0];
    	String imageUrl = params.get("imageUrl")[0];
    	String routeType = params.get("routeType")[0];
    	String username = params.get("username")[0];
    	String routeUrl = params.get("showRouteUrl")[0];
    	Double lat = NumberUtils.getDouble(params.get("lat")[0], 0d);
    	Double lng = NumberUtils.getDouble(params.get("lng")[0], 0d);
    	
    	if (StringUtils.equals(service, Commons.FACEBOOK)) {
			return FacebookSocialUtils.sendImageMessage(imageUrl, routeUrl, username, routeType, Commons.ROUTE);
		} else if (StringUtils.equals(service, Commons.TWITTER)) {
			return TwitterUtils.sendImageMessage(routeUrl, imageUrl, username, lat, lng, routeType, Commons.ROUTE);
	    } else if (StringUtils.equals(service, Commons.GOOGLE)) {
			return GoogleBloggerUtils.sendImageMessage(routeUrl, username, imageUrl, routeType, lat, lng, Commons.ROUTE);
		} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
			return GooglePlusUtils.sendImageMessage(routeUrl, username, imageUrl, routeType, Commons.ROUTE);
		} else {
			return null;
		}
	}
	
}
