package net.gmsworld.server.layers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.utils.memcache.CacheProvider;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class LayerHelperFactory {
	
	private static final Logger logger = Logger.getLogger(LayerHelperFactory.class.getName());
	
	private static CacheProvider cacheProvider;
	
	private static ThreadFactory threadProvider;
	
	private static List<String> enabledLayers = new ArrayList<String>();
	
	private static final CouponsUtils couponsUtils = new CouponsUtils();

    private static final McOpenApiUtils mcOpenApiUtils = new McOpenApiUtils();

    private static final FacebookUtils facebookUtils = new FacebookUtils();

    private static final FlickrUtils flickrUtils = new FlickrUtils();

    private static final FoursquareUtils foursquareUtils = new FoursquareUtils();

    private static final FoursquareMerchantUtils foursquareMerchantUtils = new FoursquareMerchantUtils();

    private static final GooglePlacesUtils googlePlacesUtils = new GooglePlacesUtils();

    private static final GrouponUtils grouponUtils = new GrouponUtils();
    
    private static final MeetupUtils meetupUtils = new MeetupUtils();

    private static final PicasaUtils picasaUtils = new PicasaUtils();

    private static final TwitterUtils twitterUtils = new TwitterUtils();

    private static final YelpUtils yelpUtils = new YelpUtils();

    private static final YoutubeUtils youtubeUtils = new YoutubeUtils();

    private static final GMSUtils gmsUtils = new GMSUtils();

    private static final EventfulUtils eventfulUtils = new EventfulUtils();

    private static final OsmParkingsUtils osmParkingUtils = new OsmParkingsUtils();

    private static final OsmAtmUtils osmAtmUtils = new OsmAtmUtils();

    private static final GeonamesUtils geonamesUtils = new GeonamesUtils();

    private static final LastfmUtils lastfmUtils = new LastfmUtils();

    private static final WebcamUtils webcamUtils = new WebcamUtils();

    private static final PanoramioUtils panoramioUtils = new PanoramioUtils();

    private static final ExpediaUtils expediaUtils = new ExpediaUtils();

    private static final OsmTaxiUtils osmTaxiUtils = new OsmTaxiUtils();
    
    private static final HotelsBookingUtils hotelsBookingUtils = new HotelsBookingUtils();
    
    private static final InstagramUtils instagramUtils = new InstagramUtils();
    
    private static final FreebaseUtils freebaseUtils = new FreebaseUtils();
    
    private static final SearchUtils searchUtils = new SearchUtils();
    
    public static void setCacheProvider(CacheProvider cp) {
    	cacheProvider = cp;
    }
    
    public static void setThreadProvider(ThreadFactory tp) {
    	threadProvider = tp;
    }
    
    public static CacheProvider getCacheProvider() {
		return cacheProvider;
	}
    
    /**
     * @return the googlePlacesUtils
     */
    public static GooglePlacesUtils getGooglePlacesUtils() {
    	googlePlacesUtils.setCacheProvider(cacheProvider);
    	googlePlacesUtils.setThreadProvider(threadProvider);
        return googlePlacesUtils;
    }

    /**
     * @return the grouponUtils
     */
    public static GrouponUtils getGrouponUtils() {
    	grouponUtils.setCacheProvider(cacheProvider);
    	return grouponUtils;
    }

    /**
     * @return the meetupUtils
     */
    public static MeetupUtils getMeetupUtils() {
    	 meetupUtils.setCacheProvider(cacheProvider);
    	return meetupUtils;
    }

    /**
     * @return the picasaUtils
     */
    public static PicasaUtils getPicasaUtils() {
    	picasaUtils.setCacheProvider(cacheProvider);
    	return picasaUtils;
    }

    /**
     * @return the twitterUtils
     */
    public static TwitterUtils getTwitterUtils() {
    	twitterUtils.setCacheProvider(cacheProvider);
    	return twitterUtils;
    }

    /**
     * @return the yelpUtils
     */
    public static YelpUtils getYelpUtils() {
    	yelpUtils.setCacheProvider(cacheProvider);
    	yelpUtils.setThreadProvider(threadProvider);
    	return yelpUtils;
    }

    /**
     * @return the youtubeUtils
     */
    public static YoutubeUtils getYoutubeUtils() {
    	youtubeUtils.setCacheProvider(cacheProvider);
    	return youtubeUtils;
    }

    /**
     * @return the gmsUtils
     */
    public static GMSUtils getGmsUtils() {
    	gmsUtils.setCacheProvider(cacheProvider);
    	return gmsUtils;
    }

    /**
     * @return the eventfulUtils
     */
    public static EventfulUtils getEventfulUtils() {
    	eventfulUtils.setCacheProvider(cacheProvider);
    	return eventfulUtils;
    }

    /**
     * @return the couponsUtils
     */
    public static CouponsUtils getCouponsUtils() {
    	couponsUtils.setCacheProvider(cacheProvider);
    	return couponsUtils;
    }

    /**
     * @return the mcOpenApiUtils
     */
    public static McOpenApiUtils getMcOpenApiUtils() {
    	mcOpenApiUtils.setCacheProvider(cacheProvider);
    	return mcOpenApiUtils;
    }

    /**
     * @return the facebookUtils
     */
    public static FacebookUtils getFacebookUtils() {
    	facebookUtils.setCacheProvider(cacheProvider);
    	facebookUtils.setThreadProvider(threadProvider);
    	return facebookUtils;
    }

    /**
     * @return the flickrUtils
     */
    public static FlickrUtils getFlickrUtils() {
    	flickrUtils.setCacheProvider(cacheProvider);
    	return flickrUtils;
    }

    /**
     * @return the foursquareUtils
     */
    public static FoursquareUtils getFoursquareUtils() {
    	foursquareUtils.setCacheProvider(cacheProvider);
    	foursquareUtils.setThreadProvider(threadProvider);
    	return foursquareUtils;
    }

    /**
     * @return the osmAtmUtils
     */
    public static OsmAtmUtils getOsmAtmUtils() {
    	osmAtmUtils.setCacheProvider(cacheProvider);
    	return osmAtmUtils;
    }
    
    /**
     * @return the osmParkingsUtils
     */
    public static OsmParkingsUtils getOsmParkingsUtils() {
    	osmParkingUtils.setCacheProvider(cacheProvider);
    	return osmParkingUtils;
    }
    
    /**
     * @return the osmTaxiUtils
     */
    public static OsmTaxiUtils getOsmTaxiUtils() {
    	osmTaxiUtils.setCacheProvider(cacheProvider);
    	return osmTaxiUtils;
    }

    /**
     * @return the geonamesUtils
     */
    public static GeonamesUtils getGeonamesUtils() {
    	geonamesUtils.setCacheProvider(cacheProvider);
    	return geonamesUtils;
    }

    /**
     * @return the lastfmUtils
     */
    public static LastfmUtils getLastfmUtils() {
    	lastfmUtils.setCacheProvider(cacheProvider);
    	return lastfmUtils;
    }

    /**
     * @return the webcamUtils
     */
    public static WebcamUtils getWebcamUtils() {
    	webcamUtils.setCacheProvider(cacheProvider);
    	return webcamUtils;
    }

    /**
     * @return the panoramioUtils
     */
    public static PanoramioUtils getPanoramioUtils() {
    	panoramioUtils.setCacheProvider(cacheProvider);
    	return panoramioUtils;
    }

    /**
     * @return the expediaUtils
     */
    public static ExpediaUtils getExpediaUtils() {
    	expediaUtils.setCacheProvider(cacheProvider);
    	return expediaUtils;
    }

    /**
     * @return the hotelsBookingUtils
     */
    public static HotelsBookingUtils getHotelsBookingUtils() {
    	hotelsBookingUtils.setCacheProvider(cacheProvider);
    	hotelsBookingUtils.setThreadProvider(threadProvider);
    	return hotelsBookingUtils;
    }
    
    /**
     * @return the instagramUtils
     */
    public static InstagramUtils getInstagramUtils() {
    	instagramUtils.setCacheProvider(cacheProvider);
    	return instagramUtils;
    }
    
    /**
     * @return the feebaseUtils
     */
    public static FreebaseUtils getFreebaseUtils() {
    	freebaseUtils.setCacheProvider(cacheProvider);
    	return freebaseUtils;
    }
    
    /**
     * @return the searchUtils
     */
    public static SearchUtils getSearchUtils() {
    	searchUtils.setCacheProvider(cacheProvider);
    	searchUtils.setThreadProvider(threadProvider);
    	return searchUtils;
    }
    
    /**
     * @return the foursquareMerchantUtils
     */
    public static FoursquareMerchantUtils getFoursquareMerchantUtils() {
    	foursquareMerchantUtils.setCacheProvider(cacheProvider);
    	foursquareMerchantUtils.setThreadProvider(threadProvider);
    	return foursquareMerchantUtils;
    }
    
    public static LayerHelper getByName(String name) {
    	/*if (StringUtils.equals(name, Commons.COUPONS_LAYER)) {
    		return getCouponsUtils();
    	} else if (StringUtils.equals(name, Commons.MC_ATM_LAYER)) {
    		return getMcOpenApiUtils();
    	} else if (StringUtils.equals(name, Commons.FACEBOOK_LAYER)) {
    		return getFacebookUtils();
    	} else if (StringUtils.equals(name, Commons.FLICKR_LAYER)) {
    		return getFlickrUtils();
    	} else if (StringUtils.equals(name, Commons.FOURSQUARE_LAYER)) {
    		return getFoursquareUtils();
    	} else if (StringUtils.equals(name, Commons.FOURSQUARE_MERCHANT_LAYER)) {
            return getFoursquareMerchantUtils();
    	} else if (StringUtils.equals(name, Commons.GOOGLE_PLACES_LAYER)) {
    		return  getGooglePlacesUtils();
    	} else if (StringUtils.equals(name, Commons.GROUPON_LAYER)) {
        	return getGrouponUtils();
    	} else if (StringUtils.equals(name, Commons.MEETUP_LAYER)) {
    		return getMeetupUtils();
    	} else if (StringUtils.equals(name, Commons.PICASA_LAYER)) {
        	return getPicasaUtils();
    	} else if (StringUtils.equals(name, Commons.TWITTER_LAYER)) {
        	return getTwitterUtils();
    	} else if (StringUtils.equals(name, Commons.YELP_LAYER)) {
        	return getYelpUtils();
    	} else if (StringUtils.equals(name, Commons.YOUTUBE_LAYER)) {
        	return getYoutubeUtils();
    	} else if (StringUtils.equals(name, Commons.LM_SERVER_LAYER)) {
            return getGmsUtils();
    	} else if (StringUtils.equals(name, Commons.EVENTFUL_LAYER)) {
            return getEventfulUtils();
    	} else if (StringUtils.equals(name, Commons.OSM_ATM_LAYER)) { 
            return getOsmAtmUtils();
    	} else if (StringUtils.equals(name, Commons.OSM_PARKING_LAYER)) {
    		return getOsmParkingsUtils();
    	} else if (StringUtils.equals(name, Commons.OSM_TAXI_LAYER)) {
    		return getOsmTaxiUtils();
    	} else if (StringUtils.equals(name, Commons.WIKIPEDIA_LAYER)) {
            return getGeonamesUtils();
    	} else if (StringUtils.equals(name, Commons.LASTFM_LAYER)) {
            return getLastfmUtils();
    	} else if (StringUtils.equals(name, Commons.WEBCAM_LAYER)) {
            return getWebcamUtils();
    	} else if (StringUtils.equals(name, Commons.PANORAMIO_LAYER)) {
            return getPanoramioUtils();
    	} else if (StringUtils.equals(name, Commons.EXPEDIA_LAYER)) {
            return getExpediaUtils();
    	} else if (StringUtils.equals(name, Commons.HOTELS_LAYER)) {
            return getHotelsBookingUtils(); 
    	} else if (StringUtils.equals(name, Commons.INSTAGRAM_LAYER)) {
            return getInstagramUtils();
    	} else if (StringUtils.equals(name, Commons.FREEBASE_LAYER)) {
            return getFreebaseUtils();
    	} else {
    		return null;
    	} */
    	
    	try {
    		//TODO read layer from cache
			List<Method> methods = getStaticGetMethods(LayerHelperFactory.class);
			for (Method m : methods) {
				if (!StringUtils.endsWithAny(m.getName(), new String[]{"getCacheProvider", "getByName", "getEnabledLayers", "getSearchUtils", "getIcon"})) {
					Object o = m.invoke(null,(Object[])null);
					if (o instanceof LayerHelper) {
						LayerHelper layer = (LayerHelper) o;
						if (StringUtils.equals(layer.getLayerName(), name)) {
							return layer;
						}
						//TODO save layers to cache
					}
				}
			}
    	} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    	return null;
    }
    
    public static List<String> getEnabledLayers() {
    	if (enabledLayers.isEmpty()) {
    		try {
    			List<Method> methods = getStaticGetMethods(LayerHelperFactory.class);
    			for (Method m : methods) {
    				if (!StringUtils.endsWithAny(m.getName(), new String[]{"getCacheProvider", "getByName", "getEnabledLayers", "getSearchUtils", "getIcon"})) {
    					LayerHelper layer = (LayerHelper)m.invoke(null,(Object[])null);
    					if (layer.isEnabled()) {
    						enabledLayers.add(layer.getLayerName());
    					}
    					//TODO save layers to cache
    				}
    			}
    			
    			/*for (Class<?> clazz : findLayerHelperSubclasses()) {
    				if (!clazz.getName().equals("net.gmsworld.server.layers.SearchUtils") && !Modifier.isAbstract(clazz.getModifiers())) {
    					logger.info("Processing class " + clazz.getName());
    					Object instance = clazz.newInstance(); //use singleton
    					Method isEnabled = clazz.getMethod("isEnabled");
    					Boolean response = (Boolean) isEnabled.invoke(instance, (Object[])null);
    					if (response) {
    						Method layerName = clazz.getMethod("getLayerName");
    						String name = (String) layerName.invoke(instance, (Object[])null);
    						enabledLayers.add(name);
    					}
    				}
    			}*/
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
    		}
    	}
    	return enabledLayers;
    }
    
    public static String getIcon(String name) {
    	LayerHelper layer = getByName(name);
    	if (layer != null) {
    		return layer.getIcon();
    	} else {
    		logger.log(Level.WARNING, "No icon found for layer " + name);
    		return null;
    	}
    }
    
    private static List<Method> getStaticGetMethods(Class<?> clazz) {
	    List<Method> methods = new ArrayList<Method>();
	    for (Method method : clazz.getMethods()) {
	        if (Modifier.isStatic(method.getModifiers()) && method.getName().startsWith("get")) {
	            methods.add(method);
	        }
	    }
	    return methods;
	}

}
