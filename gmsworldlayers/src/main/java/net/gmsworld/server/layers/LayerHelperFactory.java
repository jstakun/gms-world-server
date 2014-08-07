/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.gmsworld.server.layers;

/**
 *
 * @author jstakun
 */
public class LayerHelperFactory {
    
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
    /**
     * @return the googlePlacesUtils
     */
    protected static GooglePlacesUtils getGooglePlacesUtils() {
        return googlePlacesUtils;
    }

    /**
     * @return the grouponUtils
     */
    protected static GrouponUtils getGrouponUtils() {
        return grouponUtils;
    }

    /**
     * @return the meetupUtils
     */
    protected static MeetupUtils getMeetupUtils() {
        return meetupUtils;
    }

    /**
     * @return the picasaUtils
     */
    protected static PicasaUtils getPicasaUtils() {
        return picasaUtils;
    }

    /**
     * @return the twitterUtils
     */
    protected static TwitterUtils getTwitterUtils() {
        return twitterUtils;
    }

    /**
     * @return the yelpUtils
     */
    protected static YelpUtils getYelpUtils() {
        return yelpUtils;
    }

    /**
     * @return the youtubeUtils
     */
    protected static YoutubeUtils getYoutubeUtils() {
        return youtubeUtils;
    }

    /**
     * @return the gmsUtils
     */
    protected static GMSUtils getGmsUtils() {
        return gmsUtils;
    }

    /**
     * @return the eventfulUtils
     */
    protected static EventfulUtils getEventfulUtils() {
        return eventfulUtils;
    }

    /**
     * @return the couponsUtils
     */
    protected static CouponsUtils getCouponsUtils() {
        return couponsUtils;
    }

    /**
     * @return the mcOpenApiUtils
     */
    protected static McOpenApiUtils getMcOpenApiUtils() {
        return mcOpenApiUtils;
    }

    /**
     * @return the facebookUtils
     */
    protected static FacebookUtils getFacebookUtils() {
        return facebookUtils;
    }

    /**
     * @return the flickrUtils
     */
    protected static FlickrUtils getFlickrUtils() {
        return flickrUtils;
    }

    /**
     * @return the foursquareUtils
     */
    protected static FoursquareUtils getFoursquareUtils() {
        return foursquareUtils;
    }

    /**
     * @return the osmXapiUtils
     */
    protected static OsmXapiUtils getOsmXapiUtils() {
        return osmXapiUtils;
    }

    /**
     * @return the geonamesUtils
     */
    protected static GeonamesUtils getGeonamesUtils() {
        return geonamesUtils;
    }

    /**
     * @return the lastfmUtils
     */
    protected static LastfmUtils getLastfmUtils() {
        return lastfmUtils;
    }

    /**
     * @return the webcamUtils
     */
    protected static WebcamUtils getWebcamUtils() {
        return webcamUtils;
    }

    /**
     * @return the panoramioUtils
     */
    protected static PanoramioUtils getPanoramioUtils() {
        return panoramioUtils;
    }

    /**
     * @return the expediaUtils
     */
    protected static ExpediaUtils getExpediaUtils() {
        return expediaUtils;
    }

    /**
     * @return the hotelsCombinedUtils
     */
    protected static HotelsCombinedUtils getHotelsCombinedUtils() {
        return hotelsCombinedUtils;
    }
    
    /**
     * @return the instagramUtils
     */
    protected static InstagramUtils getInstagramUtils() {
        return instagramUtils;
    }
    
    /**
     * @return the feebaseUtils
     */
    protected static FreebaseUtils getFreebaseUtils() {
        return freebaseUtils;
    }
    
    /**
     * @return the searchUtils
     */
    protected static SearchUtils getSearchUtils() {
        return searchUtils;
    }
    
    /**
     * @return the foursquareMerchantUtils
     */
    protected static FoursquareMerchantUtils getFoursquareMerchantUtils() {
    	return foursquareMerchantUtils;
    }

}
