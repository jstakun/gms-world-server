package net.gmsworld.server.layers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.utils.memcache.CacheProvider;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 *
 * @author jstakun
 */
public class LayerHelperFactory {
	
	private static final Logger logger = Logger.getLogger(LayerHelperFactory.class.getName());
	
	private static List<String> enabledLayers = new ArrayList<String>();
	
	private static Map<String, LayerHelper> allLayers = new HashMap<String, LayerHelper>();
	
	private static final LayerHelperFactory instance = new LayerHelperFactory();
	
	private LayerHelperFactory() {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
    		.setUrls(
    				ClasspathHelper.forClass(LayerHelper.class)
    		)
    		.setScanners(
    				new SubTypesScanner().filterResultsBy(
    						new FilterBuilder()
    							.include(LayerHelper.class.getName())
    				)
    		)
    		.filterInputsBy(
    			new FilterBuilder()
                	.includePackage("net.gmsworld.server.layers")
    		)
		);

		Set<Class<? extends LayerHelper>> matchingClasses = reflections.getSubTypesOf(LayerHelper.class);
		
		for (Class<? extends LayerHelper> matchingClass : matchingClasses) {
			if (!Modifier.isAbstract(matchingClass.getModifiers())) {
				try {
					LayerHelper layer = matchingClass.newInstance();
					logger.info("Found layer " + layer.getLayerName() + " class " + matchingClass.getName());
					if (layer.isEnabled()) {
						enabledLayers.add(layer.getLayerName());
					}
					allLayers.put(layer.getLayerName(), layer);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Failed to create new instance for class " + matchingClass.getName(), e);
				}			
			}
		}		
	}
	
	public void setCacheProvider(CacheProvider cp) {
    	for (LayerHelper layer : allLayers.values()) {
    		layer.setCacheProvider(cp);
    	}
    }
    
    public void setThreadProvider(ThreadFactory tp) {
    	for (LayerHelper layer : allLayers.values()) {
    		layer.setThreadProvider(tp);
    	}
    }
    
    public static LayerHelperFactory getInstance() {
    	return instance;
    }
    
	/* remove
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
    
    public static GooglePlacesUtils getGooglePlacesUtils() {
    	googlePlacesUtils.setCacheProvider(cacheProvider);
    	googlePlacesUtils.setThreadProvider(threadProvider);
        return googlePlacesUtils;
    }

    public static GrouponUtils getGrouponUtils() {
    	grouponUtils.setCacheProvider(cacheProvider);
    	return grouponUtils;
    }

    public static MeetupUtils getMeetupUtils() {
    	 meetupUtils.setCacheProvider(cacheProvider);
    	return meetupUtils;
    }

    public static PicasaUtils getPicasaUtils() {
    	picasaUtils.setCacheProvider(cacheProvider);
    	return picasaUtils;
    }

    public static TwitterUtils getTwitterUtils() {
    	twitterUtils.setCacheProvider(cacheProvider);
    	return twitterUtils;
    }

    public static YelpUtils getYelpUtils() {
    	yelpUtils.setCacheProvider(cacheProvider);
    	yelpUtils.setThreadProvider(threadProvider);
    	return yelpUtils;
    }

    public static YoutubeUtils getYoutubeUtils() {
    	youtubeUtils.setCacheProvider(cacheProvider);
    	return youtubeUtils;
    }

    public static GMSUtils getGmsUtils() {
    	gmsUtils.setCacheProvider(cacheProvider);
    	return gmsUtils;
    }

    public static EventfulUtils getEventfulUtils() {
    	eventfulUtils.setCacheProvider(cacheProvider);
    	return eventfulUtils;
    }

    public static CouponsUtils getCouponsUtils() {
    	couponsUtils.setCacheProvider(cacheProvider);
    	return couponsUtils;
    }

    public static McOpenApiUtils getMcOpenApiUtils() {
    	mcOpenApiUtils.setCacheProvider(cacheProvider);
    	return mcOpenApiUtils;
    }

    public static FacebookUtils getFacebookUtils() {
    	facebookUtils.setCacheProvider(cacheProvider);
    	facebookUtils.setThreadProvider(threadProvider);
    	return facebookUtils;
    }

    public static FlickrUtils getFlickrUtils() {
    	flickrUtils.setCacheProvider(cacheProvider);
    	return flickrUtils;
    }

    public static FoursquareUtils getFoursquareUtils() {
    	foursquareUtils.setCacheProvider(cacheProvider);
    	foursquareUtils.setThreadProvider(threadProvider);
    	return foursquareUtils;
    }

    public static OsmAtmUtils getOsmAtmUtils() {
    	osmAtmUtils.setCacheProvider(cacheProvider);
    	return osmAtmUtils;
    }
    
    public static OsmParkingsUtils getOsmParkingsUtils() {
    	osmParkingUtils.setCacheProvider(cacheProvider);
    	return osmParkingUtils;
    }
    
    public static OsmTaxiUtils getOsmTaxiUtils() {
    	osmTaxiUtils.setCacheProvider(cacheProvider);
    	return osmTaxiUtils;
    }

    public static GeonamesUtils getGeonamesUtils() {
    	geonamesUtils.setCacheProvider(cacheProvider);
    	return geonamesUtils;
    }

    public static LastfmUtils getLastfmUtils() {
    	lastfmUtils.setCacheProvider(cacheProvider);
    	return lastfmUtils;
    }

    public static WebcamUtils getWebcamUtils() {
    	webcamUtils.setCacheProvider(cacheProvider);
    	return webcamUtils;
    }

    public static PanoramioUtils getPanoramioUtils() {
    	panoramioUtils.setCacheProvider(cacheProvider);
    	return panoramioUtils;
    }

    public static ExpediaUtils getExpediaUtils() {
    	expediaUtils.setCacheProvider(cacheProvider);
    	return expediaUtils;
    }

    public static HotelsBookingUtils getHotelsBookingUtils() {
    	hotelsBookingUtils.setCacheProvider(cacheProvider);
    	hotelsBookingUtils.setThreadProvider(threadProvider);
    	return hotelsBookingUtils;
    }
    
    public static InstagramUtils getInstagramUtils() {
    	instagramUtils.setCacheProvider(cacheProvider);
    	return instagramUtils;
    }
    
    public static FreebaseUtils getFreebaseUtils() {
    	freebaseUtils.setCacheProvider(cacheProvider);
    	return freebaseUtils;
    }
    
    public static SearchUtils getSearchUtils() {
    	searchUtils.setCacheProvider(cacheProvider);
    	searchUtils.setThreadProvider(threadProvider);
    	return searchUtils;
    }
    
    public static FoursquareMerchantUtils getFoursquareMerchantUtils() {
    	foursquareMerchantUtils.setCacheProvider(cacheProvider);
    	foursquareMerchantUtils.setThreadProvider(threadProvider);
    	return foursquareMerchantUtils;
    }*/
    //end of remove
    
    public LayerHelper getByName(String name) {
    	return allLayers.get(name);
    }
    
    public List<String> getEnabledLayers() {
    	return enabledLayers;
    }
    
    public String getIcon(String name) {
    	LayerHelper layer = getByName(name);
    	if (layer != null) {
    		return layer.getIcon();
    	} else {
    		logger.log(Level.WARNING, "No icon found for layer " + name);
    		return null;
    	}
    }
    
    protected Map<String, LayerHelper> getAllLayers() {
    	return allLayers;
    }
}
