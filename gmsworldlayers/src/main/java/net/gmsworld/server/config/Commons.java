package net.gmsworld.server.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

public class Commons {
	public static final String PANORAMIO_LAYER = "Panoramio";
	public static final String WIKIPEDIA_LAYER = "Wikipedia";
	public static final String EVENTFUL_LAYER = "Eventful";
	public static final String FLICKR_LAYER = "Flickr";
	public static final String YOUTUBE_LAYER = "YouTube";
	public static final String FACEBOOK_LAYER = "Facebook";
	public static final String FOURSQUARE_LAYER = "Foursquare";
	public static final String FOURSQUARE_MERCHANT_LAYER = "Foursquare Merchant";
	public static final String YELP_LAYER = "Yelp";
	public static final String GROUPON_LAYER = "Groupon";
	public static final String HOTELS_LAYER = "Hotels";
	public static final String GOOGLE_PLACES_LAYER = "Google Places";
	public static final String GEOCODES_LAYER = "Geocodes";
	public static final String TWITTER_LAYER = "Twitter";
	public static final String MEETUP_LAYER = "Meetup";
	public static final String WEBCAM_LAYER = "Travel Webcams";
	public static final String OSM_ATM_LAYER = "ATMs";
	public static final String OSM_PARKING_LAYER = "Parkings";
	public static final String OSM_TAXI_LAYER = "Taxi";
	public static final String MC_ATM_LAYER = "MasterCard ATMs";
	public static final String LM_SERVER_LAYER = "Public";
	public static final String LOCAL_LAYER = "Phone Landmarks";
	public static final String MY_POS_CODE = "MyPos";
	public static final String MY_POSITION_LAYER = "My Location";

	public static final String COUPONS_LAYER = "Coupons";
	public static final String INSTAGRAM_LAYER = "Instagram";
	public static final String LASTFM_LAYER = "LastFM";
	public static final String PICASA_LAYER = "Picasa";
	public static final String EXPEDIA_LAYER = "Expedia";
	//public static final String FREEBASE_LAYER = "Freebase";
	// public static final String QYPE_LAYER = "Qype";
	// public static final String HOTWIRE_LAYER = "Hotwire";
	// public static final String UPCOMING_LAYER = "Upcoming";
	// public static final String GOWALLA_LAYER = Gowalla";
	public static final String SEARCH_LAYER = "Search";
	public static final String HOTELS_COMBINED_LAYER = "Hotels Combined";

	public static final String APP_HEADER = "X-GMS-AppId";
	public static final String TOKEN_HEADER = "X-GMS-Token";
	public static final String SCOPE_HEADER = "X-GMS-Scope";
	public static final String APP_VERSION_HEADER = "X-GMS-AppVersionId";
	public static final String USE_COUNT_HEADER = "X-GMS-UseCount";
	public static final String MYPOS_KEY_HEADER = "X-GMS-MyPos-Key";
	public static final String LAT_HEADER = "X-GMS-Lat";
	public static final String LNG_HEADER = "X-GMS-Lng";
	public static final String ACC_HEADER = "X-GMS-Acc";
	public static final String DEVICE_NAME_HEADER  = "X-GMS-DeviceName";
	public static final String DEVICE_ID_HEADER  = "X-GMS-DeviceId";
	public static final String ROUTE_ID_HEADER = "X-GMS-RouteId";
	public static final String AUTH_HEADER = "X-GMS-AuthStatus";
	public static final String SILENT_HEADER = "X-GMS-Silent";
	public static final String BUCKET_NAME_HEADER = "X-GMS-BucketName";
	
	public static final String SOCIAL = "Social";
	public static final int BLOGEO = 0;
	public static final int LANDMARK = 1;
	public static final int MY_POS = 2;
	public static final int LOGIN = 3;
	public static final int SERVER = 4;
	public static final int CHECKIN = 5;
	public static final int ROUTE = 6;
	public static final int SCREENSHOT = 7;
	public static final int HOTELS = 8;

	public static final String GOOGLE_PLUS = "gg";
	public static final String GOOGLE = "gl";
	public static final String LINKEDIN = "ln";
	public static final String TWITTER = "tw";
	public static final String FACEBOOK = "fb";
	public static final String FOURSQUARE = "fs";
	public static final String GMS_WORLD = "gms";

	public static final String MAIL = "m";
	public static final long FIVE_MINS = 0x493e0L;
	public static final int LM_ID = 0;
	public static final int DA_ID = 1;
	public static final int DL_ID = 2;
	public static final int BROWSER_ID = 10;

	public enum Property {
		BITLY_USERNAME, BITLY_APIKEY, BITLY_GUID, //
		APP_USER, MYPOS_USER, //
		LN_API_KEY, LN_API_SECRET, LN_STATE, //
		bc_salt, bc_password, bc_algorithm, //
		CLOUDMADE_APIKEY, CLOUDMADE_TOKEN_KEY, CLOUDMADE_USERNAME, //
		COUPONS_KEY, //
		EVENTFUL_APP_KEY, //
		EXPEDIA_KEY, //
		fb_app_token, fb_page_token, fb_secret, fb_client_id, FB_GMS_WORLD_FEED, //
		FLICKR_APIKEY, FLICKR_sharedSecret, //
		FS_CLIENT_ID, FS_CLIENT_SECRET, FS_OAUTH_TOKEN, //
		GL_PLUS_KEY, GL_PLUS_SECRET, gl_plus_token, gl_plus_refresh, GOOGLE_API_KEY, GOOGLE_API_WEB_KEY, //
		GEONAMES_USERNAME, //
		GROUPON_CLIENT_ID, //
		INSTAGRAM_ACCESS_TOKEN, //
		LASTFM_API_KEY, //
		HOTWIRE_KEY, //
		RAPIDAPI_KEY, //
		mcopenapi_ksPwd, mcopenapi_keyAlias, mcopenapi_prodConsumerKey, 
		MEETUP_API_KEY, //
		TW_TOKEN, TW_SECRET, TW_CONSUMER_KEY, TW_CONSUMER_SECRET, //
		WEBCAM_API_KEY, //
		YELP_API_KEY, //
		YOUTUBE_API_KEY, //
		CB_API_KEY, CB_API_SECRET, //
		GROUPON_AFFILIATE_ID, MAPQUEST_APPKEY, //
		AWS_ACCESS_KEY, AWS_ACCESS_SECRET, //
		TELEGRAM_TOKEN,
		REFERER_KEY , 
		RECAPTCHA_PRIVATE_KEY , RECAPTCHA_PUBLIC_KEY ,
		DEFAULT_USERNAME, DEFAULT_PASSWORD, 
	    FB_GMS_WORLD_PAGE_TOKEN, 
	    RH_MAILER_PWD, RH_TEST_TOKEN, RH_TEST_SCOPE, 
		RH_ROUTES_API_KEY, RH_HOTELS_API_KEY, RH_LANDMARKS_API_KEY,
		FCM_LM_WEB_API_KEY, FCM_PROJECT, FCM_APP_KEY,
		DL_PAGE_ACCESS_TOKEN, DL_PAGE_ID, DL_VERIFY_TOKEN
	};

	private static Properties props = new Properties();
	
	private static final Logger logger = Logger.getLogger(Commons.class.getName());
	
	static {
		try {
			InputStream input = null;
			
			final String configLocation = System.getenv("CONFIG_LOCATION");
			if (StringUtils.isNotEmpty(configLocation)) {
				logger.log(Level.INFO, "Loading application config from " + configLocation + "...");
				if (configLocation.startsWith("classpath:/")) {
					input = Commons.class.getClassLoader().getResourceAsStream(configLocation);
				} else {
					input = new FileInputStream(configLocation);
				}
			}
			
			if (input == null) {
				logger.log(Level.INFO, "Loading application.properties from classpath...");
				input = Commons.class.getClassLoader().getResourceAsStream("application.properties");
			}
			
			if (input == null) {
				logger.log(Level.SEVERE, "Unable to find application.properties");
			} else {
				props.load(input);
				logger.log(Level.INFO, "Loaded " + props.size() + " properties.");
			}
        
		} catch (Exception ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}
	
	public static String getProperty(Property property) {
		//logger.log(Level.INFO, "Reading property " + property.name());
		String val = props.getProperty(property.name());
		if (val == null) {
			val = System.getenv(property.name());
		} 
		if (val != null) {
			val = val.trim();
		}
		return val;
	}
}
