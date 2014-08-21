/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.gmsworld.server.layers;

import net.gmsworld.server.utils.ThreadProvider;
import net.gmsworld.server.utils.memcache.CacheProvider;

/**
 *
 * @author jstakun
 */
public class LayerHelperFactory {
	
	private static CacheProvider cacheProvider;
	
	private static ThreadProvider threadProvider;
	
	private static final CouponsUtils couponsUtils = new CouponsUtils();

    private static final McOpenApiUtils mcOpenApiUtils = new McOpenApiUtils();

    private static final FacebookUtils facebookUtils = new FacebookUtils();

    private static final FlickrUtils flickrUtils = new FlickrUtils();

    private static final FoursquareUtils foursquareUtils = new FoursquareUtils();

    private static final FoursquareMerchantUtils foursquareMerchantUtils = new FoursquareMerchantUtils();

    private static final GooglePlacesUtils googlePlacesUtils = new GooglePlacesUtils();

    private static final GrouponUtils grouponUtils = new GrouponUtils();
    
    private static final HotwireUtils hotwireUtils = new HotwireUtils();

    private static final MeetupUtils meetupUtils = new MeetupUtils();

    private static final PicasaUtils picasaUtils = new PicasaUtils();

    private static final TwitterUtils twitterUtils = new TwitterUtils();

    private static final YelpUtils yelpUtils = new YelpUtils();

    private static final YoutubeUtils youtubeUtils = new YoutubeUtils();

    private static final GMSUtils gmsUtils = new GMSUtils();

    private static final EventfulUtils eventfulUtils = new EventfulUtils();

    private static final OsmXapiUtils osmXapiUtils = new OsmXapiUtils();

    private static final GeonamesUtils geonamesUtils = new GeonamesUtils();

    private static final LastfmUtils lastfmUtils = new LastfmUtils();

    private static final WebcamUtils webcamUtils = new WebcamUtils();

    private static final PanoramioUtils panoramioUtils = new PanoramioUtils();

    private static final ExpediaUtils expediaUtils = new ExpediaUtils();

    private static final HotelsCombinedUtils hotelsCombinedUtils = new HotelsCombinedUtils();
    
    private static final InstagramUtils instagramUtils = new InstagramUtils();
    
    private static final FreebaseUtils freebaseUtils = new FreebaseUtils();
    
    private static final SearchUtils searchUtils = new SearchUtils();
    
    public static void setCacheProvider(CacheProvider cp) {
    	cacheProvider = cp;
    }
    
    public static void setThreadProvider(ThreadProvider tp) {
    	threadProvider = tp;
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
     * @return the osmXapiUtils
     */
    public static OsmXapiUtils getOsmXapiUtils() {
    	osmXapiUtils.setCacheProvider(cacheProvider);
    	return osmXapiUtils;
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
     * @return the hotelsCombinedUtils
     */
    public static HotelsCombinedUtils getHotelsCombinedUtils() {
    	hotelsCombinedUtils.setCacheProvider(cacheProvider);
    	return hotelsCombinedUtils;
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
    
    /**
     * @return the hotwireUtils
     */
    public static HotwireUtils getHotwireUtils() {
    	hotwireUtils.setCacheProvider(cacheProvider);
    	return hotwireUtils;
    }

}
