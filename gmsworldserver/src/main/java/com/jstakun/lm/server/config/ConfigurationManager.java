package com.jstakun.lm.server.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import net.gmsworld.server.config.Commons;

import com.jstakun.lm.server.persistence.Config;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.ConfigPersistenceUtils;

/**
 *
 * @author jstakun
 */
public final class ConfigurationManager {
    
    private static Map<String, String> configuration;
    public static final String CONFIG = "config";
    public static final String GMS_WORLD_PAGE_TOKEN = "gmsWorldPageToken";
    public static final String GMS_WORLD_ACCESS_TOKEN = "gmsWorldAccessToken";
    public static final String GMS_LANDMARK_URL = "gmsLandmarkUrl";
    private static final String LM_GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui";
    private static final String DA_GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=com.jstakun.gms.android.ui.deals";
    private static final String DL_GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=net.gmsworld.devicelocator";
    private static final String BROWSER_URL = "https://www.gms-world.net/landmarks";
    public static final String BOOKING_URL = "http://www.booking.com/city/%s/%s.html?aid=864525";
    public static final String HOTELS_URL = "http://www.hotelsonmap.net/";
    public static final String RHCLOUD_SERVER_URL = "https://openapi-landmarks.b9ad.pro-us-east-1.openshiftapps.com/actions/";
    
    public static void populateConfig()
    {
         List<Config> params = ConfigPersistenceUtils.selectAllConfigParams();
         configuration = new HashMap<String, String>();
         for (Config param : params) {
             configuration.put(param.getKey(), param.getValue());
             Logger.getLogger("com.jstakun.lm.server.config.ConfigurationManager").log(Level.INFO, "Setting {0}: {1}", new Object[]{param.getKey(), param.getValue()});
         }
         CacheUtil.put(CONFIG, configuration, CacheType.NORMAL);
    }

    private static void refreshConfig()
    {
    	configuration = (Map<String, String>)CacheUtil.getObject(CONFIG);
        if (configuration == null) {
        	Logger.getLogger("com.jstakun.lm.server.config.ConfigurationManager").log(Level.WARNING, "Loading configuration from datastore...");
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
    
    public static String[] getArray(String key) {
    	String listStr = getParam(key, "");
		return StringUtils.split(listStr, "|");
    }
    
    public static boolean listContainsValue(String key, String value) {
    	return (StringUtils.indexOfAny(value, getArray(key)) >= 0);
    }
    
    public static void setParam(String key, String value) {
    	if (StringUtils.isNotEmpty(key)) {
    			ConfigPersistenceUtils.persist(key, value);
    			populateConfig();
    	}
    	refreshConfig();
    }
    
    public static Map<String, String> getConfiguration() {
    	refreshConfig();
    	return Collections.unmodifiableMap(configuration);
    }
    
    public static String getAppName(int appId) {
    	if (appId == Commons.LM_ID) {
    		return "Landmark Manager";
    	} else if (appId == Commons.DA_ID) {
    		return "Deals Anywhere";
    	} else if (appId == Commons.BROWSER_ID) {
    		return "Web Browser";
    	} else if (appId == Commons.DL_ID) {
    		return "Device Locator";
    	} else {	
    		return "Unknown application";
    	}
    }
    
    public static String getAppUrl(int appId) {
    	if (appId == Commons.LM_ID) {
    		return LM_GOOGLE_PLAY_URL;
    	} else if (appId == Commons.DA_ID) {
    		return DA_GOOGLE_PLAY_URL;
    	} else if (appId == Commons.DL_ID) {
    		return DL_GOOGLE_PLAY_URL;
    	}else if (appId == Commons.BROWSER_ID) {
    		return BROWSER_URL;
    	} else {	
    		return LM_GOOGLE_PLAY_URL;
    	}
    }
}
