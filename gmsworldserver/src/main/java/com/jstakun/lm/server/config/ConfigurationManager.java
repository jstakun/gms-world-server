/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.config;

import com.jstakun.lm.server.persistence.Config;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
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
    
	public static final String SERVER_URL = "http://www.gms-world.net/";
    public static final String SSL_SERVER_URL = "https://gms-world.appspot.com/";
    public static final String RHCLOUD_SERVER_URL = "https://landmarks-gmsworld.rhcloud.com/actions/";
    public static final String ON = "1";
    public static final String OFF = "0";

    public static final String NUM_OF_LANDMARKS = "numOfLandmarks"; //max number of landmarks on index.jsp
    public static final String NUM_OF_GEOCODES = "numOfGeocodes"; //max number of geocodes on sidebar.jsp
    public static final String SAVE_GEOCODE_AS_LANDMARK = "saveGeocodeAsLandmark"; //save geocode as landmark
    public static final String LOG_OLDER_THAN_DAYS = "logOlderThanDays"; //purge log
    public static final String SCREENSHOT_OLDER_THAN_DAYS = "screenshotOlderThanDays"; //purge screenshot
    public static final String NOTIFICATIONS_INTERVAL = "notificationsInterval"; //notifications interval
    public static final String LM_VERSION = "lmVersion"; //LM app version
    public static final String DA_VERSION = "daVersion"; //DA app version
    public static final String EXCLUDED = "excluded";//list of excluded from engagement email
    public static final String CLOSED_URLS = "closed";//temporary closed urls
    public static final String IP_TOTAL_LIMIT = "totalLimit"; //total call limit from ip
    public static final String IP_URI_LIMIT = "uriLimit"; //total call limit from ip to uri
    
    public static final String FB_USERNAME = "fbUsername";
    public static final String FB_GENDER = "fbGender";
    public static final String FB_BIRTHDAY = "fbBirthday";
    public static final String FB_NAME = "fbName";
    public static final String FB_EXPIRES_IN = "fbExpiresIn";
    
    public static final String TWEET_USERNAME = "twUsername";
    public static final String TWEET_NAME = "twName";
    
    public static final String LN_USERNAME = "lnUsername";
    public static final String LN_NAME = "lnName";
    public static final String LN_EXPIRES_IN = "lnExpiresIn";
    
    public static final String FS_USERNAME = "fsUsername";
    public static final String FS_NAME = "fsName";
    
    public static final String GL_EXPIRES_IN = "glExpiresIn";
    public static final String GL_USERNAME = "glUsername";
    public static final String GL_NAME = "glName";
    public static final String GL_GENDER = "glGender";
    public static final String GL_BIRTHDAY = "glBirthday";
    
    public static final String GMS_NAME = "gmsName";
    
    public static final String USER_EMAIL = "userEmail";

	public static final String SUPPORT_MAIL = "support@gms-world.net";
	public static final String ADMIN_MAIL = "jstakun.appspot@gmail.com";
	public static final String ADMIN_NICK = "GMS World Administrator";
    
    private static Map<String, String> configuration = new HashMap<String, String>();
    public static final String CONFIG = "config";
    
    public static void populateConfig()
    {
         List<Config> params = ConfigPersistenceUtils.selectAllConfigParams();
         for (Config param : params) {
             configuration.put(param.getKey(), param.getValue());
         }
         CacheUtil.put(CONFIG, configuration);
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
        refreshConfig();
        if (configuration.containsKey(key)) {
            return (String)configuration.get(key);
        } else {
            return defaultValue;
        }
    }    
    
    public static Map<String, String> getConfiguration() {
    	return Collections.unmodifiableMap(configuration);
    }
}
