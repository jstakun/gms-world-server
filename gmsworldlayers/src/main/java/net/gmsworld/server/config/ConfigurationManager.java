package net.gmsworld.server.config;

public class ConfigurationManager {

	public static final String SERVER_URL = "http://www.gms-world.net/";
    public static final String SSL_SERVER_URL = "https://www.gms-world.net/";//;"https://gms-world.appspot.com/";
    public static final String RHCLOUD_SERVER_URL = "https://landmarks-gmsworld.rhcloud.com/actions/";
    public static final String HOTELS_PROVIDER_URL = "http://hotels-gmsworldatoso.rhcloud.com/";
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
    public static final String DL_VERSION = "ddVersion"; //DL app version
    public static final String EXCLUDED = "excluded";//list of email addresses excluded from engagement message
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
    
    public static final String CB_USERNAME = "cbUsername";
    public static final String CB_NAME = "cbName";
    public static final String CB_EXPIRES_IN = "cbExpiresIn";
    
    public static final String GL_EXPIRES_IN = "glExpiresIn";
    public static final String GL_USERNAME = "glUsername";
    public static final String GL_NAME = "glName";
    public static final String GL_GENDER = "glGender";
    public static final String GL_BIRTHDAY = "glBirthday";
    
    public static final String GMS_NAME = "gmsName";
    public static final String GMS_TOKEN = "gmsToken";
    
    public static final String USER_EMAIL = "userEmail";

	public static final String SUPPORT_MAIL = "support@gms-world.net";
	public static final String ADMIN_NICK = "GMS World Administrator";
    public static final String DL_MAIL = "device-locator@gms-world.net";
    public static final String LM_MAIL = "landmark-manager@gms-world.net";
    public static final String DL_NICK = "Device Locator Administrator";
    public static final String LM_NICK = "Landmark Manager Administrator";
    
	public static enum MAP_PROVIDER {OSM_MAPS, GOOGLE_MAPS};
}
