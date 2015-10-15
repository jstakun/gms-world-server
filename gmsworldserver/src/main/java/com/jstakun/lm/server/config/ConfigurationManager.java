package com.jstakun.lm.server.config;

import com.jstakun.lm.server.persistence.Config;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.ConfigPersistenceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jstakun
 */
public final class ConfigurationManager {
    
    private static Map<String, String> configuration = new HashMap<String, String>();
    public static final String CONFIG = "config";
    public static final String GMS_WORLD_PAGE_TOKEN = "gmsWorldPageToken";
    public static final String GMS_WORLD_ACCESS_TOKEN = "gmsWorldAccessToken";
    public static final int LM_ID = 0;
    public static final int DA_ID = 1;
    public static final int BROWSER_ID = 10;
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
    	if (appId == LM_ID) {
    		return "Landmark Manager";
    	} else if (appId == DA_ID) {
    		return "Deals Anywhere";
    	} else if (appId == BROWSER_ID) {
    		return "Web Browser";
    	} else {	
    		return "Unknown application";
    	}
    }
    
    public static String getAppUrl(int appId) {
    	if (appId == LM_ID) {
    		return LM_GOOGLE_PLAY_URL;
    	} else if (appId == DA_ID) {
    		return DA_GOOGLE_PLAY_URL;
    	} else if (appId == BROWSER_ID) {
    		return BROWSER_URL;
    	} else {	
    		return LM_GOOGLE_PLAY_URL;
    	}
    }
}
