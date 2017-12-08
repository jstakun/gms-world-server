package com.jstakun.lm.server.utils.persistence;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;
import net.gmsworld.server.utils.persistence.Landmark;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

/**
 *
 * @author jstakun
 */
public class LandmarkPersistenceWebUtils {

    private static final Logger logger = Logger.getLogger(LandmarkPersistenceWebUtils.class.getName());
   
    public static boolean isSimilarToNewest(Landmark l) {
    	boolean isSimilarToNewest = false;
    	String name = l.getName();
    	String lat = StringUtil.formatCoordE2(l.getLatitude());
    	String lng = StringUtil.formatCoordE2(l.getLongitude());
        if (CacheUtil.containsKey(name + "_" + lat + "_" + lng)) {
        	isSimilarToNewest = true;
        	logger.log(Level.WARNING, "This landmark is similar to newest: " + name + "_" + lat + "_" + lng);
        } else {
        	CacheAction newestLandmarksAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
        		@Override
        		public Object executeAction() {
        			return net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils.selectNewestLandmarks();
        		}
        	});
        	
        	List<Landmark> landmarkList = newestLandmarksAction.getListFromCache(Landmark.class, "newestLandmarks", CacheType.FAST);
        	if (!landmarkList.isEmpty()) {
        		Landmark newestLandmark = landmarkList.get(0);
        		logger.log(Level.INFO, "Newest landmark: " + newestLandmark.getName() + ", " + newestLandmark.getLatitude() + ", " + newestLandmark.getLongitude());
        		if (l.compare(newestLandmark)) {
        			logger.log(Level.WARNING, "This landmark is similar to newest: " + name + ", " + lat + ", " + lng);
        			isSimilarToNewest = true;
        		} else {
        			logger.log(Level.INFO, "This landmark is not similar to newest: " + name + ", " + lat + ", " + lng);
        		}
        	}
        }
        
        if (!isSimilarToNewest) {
        	CacheUtil.put(name + "_" + lat + "_" + lng, "1", CacheType.LANDMARK);
        }
        
        return isSimilarToNewest;
    }
    
    public static void notifyOnLandmarkCreation(Landmark l, String userAgent, String socialIds) {
    	//load image
    	Queue queue = QueueFactory.getDefaultQueue();
    	queue.add(withUrl("/tasks/execute").param("action", "loadImage").param("latitude", Double.toString(l.getLatitude())).param("longitude", Double.toString(l.getLongitude())));
    	
    	//social notifications
    	String landmarkUrl = ConfigurationManager.SERVER_URL + "showLandmark/" + l.getId();
    	if (StringUtils.isNotEmpty(l.getHash())) {
    		landmarkUrl = UrlUtils.BITLY_URL + l.getHash();
    	} else {
    		landmarkUrl = UrlUtils.getShortUrl(landmarkUrl);
    	}
                        
    	String titleSuffix = "";
    	String[] tokens = StringUtils.split(userAgent, ",");
    	if (tokens != null) {
        	for (int i = 0; i < tokens.length; i++) {
            	String token = StringUtils.trimToEmpty(tokens[i]);
            	if (token.startsWith("Package:") || token.startsWith("Version:") || token.startsWith("Version Code:")) {
                	titleSuffix += " " + token;
            	}
        	}
    	}

    	String messageSuffix = "";
    	if (l.getUseCount() > 0) {
    		messageSuffix = " User has opened LM " + l.getUseCount() + " times.";
    	}
    	
    	String title = "New landmark";
    	if (StringUtils.isNotEmpty(titleSuffix)) {
        	title += titleSuffix;
    	}

    	String body = "Landmark: " + l.getName() + " has been created by user " + 
    			ConfigurationManager.SERVER_URL + "socialProfile?uid=" + l.getUsername() + "." + messageSuffix;
    
    	String userUrl = ConfigurationManager.SERVER_URL;
    	if (l.isSocial()) {
    		userUrl += "blogeo/" + l.getUsername();
    	} else {
    		userUrl += "showUser/" + l.getUsername();
    	}
    	
    	String imageUrl = ConfigurationManager.SERVER_URL + "image?lat=" + l.getLatitude() + "&lng=" + l.getLongitude();
    
    	Map<String, String> params = new ImmutableMap.Builder<String, String>().
            put("key", Integer.toString(l.getId())).
    		put("landmarkUrl", landmarkUrl).
    		put("email", l.getEmail()).
    		put("title", title).
    		put("userUrl", userUrl).
    		put("username", l.getUsername()).
    		put("name", l.getName()).
    		put("body", body).
    		put("latitude", Double.toString(l.getLatitude())).
    		put("longitude", Double.toString(l.getLongitude())).
    		put("layer", l.getLayer()).
    		put("desc", l.getDescription()).
    		put("socialIds", socialIds != null ? socialIds : l.getUsername()).
    		put("imageUrl", imageUrl).build();  
    	  
    	NotificationUtils.createLadmarkCreationNotificationTask(params);
    }
    
    public static void setFlex(Landmark l, HttpServletRequest request) {
    	//AddressInfo addressInfo = new AddressInfo();
    	
    	/*try {
    		//addressInfo = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(l.getLatitude(), l.getLongitude());
			addressInfo = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(l.getLatitude(), l.getLongitude());
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}*/
    	
    	int useCount = NumberUtils.getInt(request.getHeader(Commons.USE_COUNT_HEADER), 1);
		int appId = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), -1);
		int version = NumberUtils.getInt(request.getHeader(Commons.APP_VERSION_HEADER), -1);
		String deviceId = request.getHeader("X-GMS-DeviceId");
    	
    	JSONObject flex = new JSONObject();
		flex.put("useCount", useCount);
		if (appId > -1) {
			flex.put("appId", appId);
		}
		if (version > 0) {
			flex.put("version", version);
		}
		if (deviceId != null) {
			flex.put("deviceId", deviceId);
		}
		//flex.putOpt("cc", addressInfo.getField(AddressInfo.COUNTRY_CODE));
		//flex.putOpt("city", addressInfo.getField(AddressInfo.CITY));
		l.setFlex(flex.toString());
		
		//return addressInfo.getField(AddressInfo.EXTENSION); //formatted address
    }
}
