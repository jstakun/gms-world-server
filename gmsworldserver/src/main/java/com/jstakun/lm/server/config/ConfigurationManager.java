package com.jstakun.lm.server.config;

import com.jstakun.lm.server.persistence.Config;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.ConfigPersistenceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.gmsworld.server.config.Commons;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public final class ConfigurationManager {
    
    private static Map<String, String> configuration = new HashMap<String, String>();
    public static final String CONFIG = "config";
    public static final String GMS_WORLD_PAGE_TOKEN = "gmsWorldPageToken";
    public static final String GMS_WORLD_ACCESS_TOKEN = "gmsWorldAccessToken";
    private static final String LM_GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui";
    private static final String DA_GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui.deals";
    private static final String BROWSER_URL = "http://www.gms-world.net/selectBrowserLandmark";
    public static String BOOKING_URL = "http://www.booking.com/city/%s/%s.html?aid=864525";
	
    public static void populateConfig()
    {
         List<Config> params = ConfigPersistenceUtils.selectAllConfigParams();
         for (Config param : params) {
             configuration.put(param.getKey(), param.getValue());
         }
         CacheUtil.put(CONFIG, configuration, CacheType.NORMAL);
    }

    private static void refreshConfig()
    {
        Object o = CacheUtil.getObject(CONFIG);
        if (o != null && o instanceof HashMap) {
            configuration = (HashMap<String, String>) o;
        } else {
            populateConfig();
        }
    }

    public static String getParam(String key, String defaultValue)
    {
    	try {
    		refreshConfig();
    		if (configuration.containsKey(key)) {
    			return (String)configuration.get(key);
    		} else {
    			return defaultValue;
    		}
    	} catch (Exception e) {
    		return defaultValue;
    	}
    }    
    
    public static Map<String, String> getConfiguration() {
    	return Collections.unmodifiableMap(configuration);
    }
    
    public static String getAppName(int appId) {
    	if (appId == Commons.LM_ID) {
    		return "Landmark Manager";
    	} else if (appId == Commons.DA_ID) {
    		return "Deals Anywhere";
    	} else if (appId == Commons.BROWSER_ID) {
    		return "Web Browser";
    	} else {	
    		return "Unknown application";
    	}
    }
    
    public static String getAppUrl(int appId) {
    	if (appId == Commons.LM_ID) {
    		return LM_GOOGLE_PLAY_URL;
    	} else if (appId == Commons.DA_ID) {
    		return DA_GOOGLE_PLAY_URL;
    	} else if (appId == Commons.BROWSER_ID) {
    		return BROWSER_URL;
    	} else {	
    		return LM_GOOGLE_PLAY_URL;
    	}
    }
    
    public static String getLayerIcon(String layer) {
 
    	if (StringUtils.equals(layer, Commons.FOURSQUARE_LAYER)) {
    		return "foursquare.png";
    	} else if (StringUtils.equals(layer, Commons.FACEBOOK_LAYER)) {
    		return "facebook.png";
    	} else if (StringUtils.equals(layer, Commons.YELP_LAYER)) {
    		return "yelp.png";
    	} else if (StringUtils.equals(layer, Commons.GOOGLE_PLACES_LAYER)) {
    		return "google_plus.png";
    	} else if (StringUtils.equals(layer, Commons.COUPONS_LAYER)) {
    		return "dollar.png";
    	} else if (StringUtils.equals(layer, Commons.GROUPON_LAYER)) {
    		return "dollar.png";
    	} else if (StringUtils.equals(layer, Commons.MC_ATM_LAYER)) {
    		return "mastercard.png";
    	} else if (StringUtils.equals(layer, Commons.FLICKR_LAYER)) {
    		return "flickr.png";
    	} else if (StringUtils.equals(layer, Commons.LM_SERVER_LAYER)) {
    		return "gmsworld.png";
    	} else if (StringUtils.equals(layer, Commons.PICASA_LAYER)) {
    		return "picasa.png";
    	} else if (StringUtils.equals(layer, Commons.MEETUP_LAYER)) {
    		return "meetup.png";
    	} else if (StringUtils.equals(layer, Commons.YOUTUBE_LAYER)) {
    		return "youtube.png";
    	} else if (StringUtils.equals(layer, Commons.EVENTFUL_LAYER)) {
    		return "event.png";
    	} else if (StringUtils.equals(layer, Commons.OSM_ATM_LAYER)) {
    		return "credit_cards.png";
    	} else if (StringUtils.equals(layer, Commons.OSM_PARKING_LAYER)) {
    		return "parking.png";
    	} else if (StringUtils.equals(layer, Commons.GEOCODES_LAYER)) {
    		return "wikipedia.png";
    	} else if (StringUtils.equals(layer, Commons.WIKIPEDIA_LAYER)) {
    		return "wikipedia.png";
    	} else if (StringUtils.equals(layer, Commons.LASTFM_LAYER)) {
    		return "lastfm.png";
    	} else if (StringUtils.equals(layer, Commons.WEBCAM_LAYER)) {
    		return "webcam.png";
    	} else if (StringUtils.equals(layer, Commons.PANORAMIO_LAYER)) {
    		return "panoramio.png";
    	} else if (StringUtils.equals(layer, Commons.FOURSQUARE_MERCHANT_LAYER)) {
    		return "gift.png";
    	} else if (StringUtils.equals(layer, Commons.EXPEDIA_LAYER)) {
    		return "expedia.png";
    	} else if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
    		return "hotel.png";
    	} else if (StringUtils.equals(layer, Commons.TWITTER_LAYER)) {
    		return "twitter.png";
    	} else if (StringUtils.equals(layer, Commons.INSTAGRAM_LAYER)) {
    		return "instagram.png";
    	} else if (StringUtils.equals(layer, Commons.FREEBASE_LAYER)) {
    		return "freebase.png";
    	} else { 
    		return null;
    	}
    }
}
