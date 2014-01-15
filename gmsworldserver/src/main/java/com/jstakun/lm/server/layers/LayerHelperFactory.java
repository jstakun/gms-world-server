/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.layers;

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

    private static final GooglePlacesUtils googlePlacesUtils = new GooglePlacesUtils();

    private static final GrouponUtils grouponUtils = new GrouponUtils();

    private static final MeetupUtils meetupUtils = new MeetupUtils();

    private static final PicasaUtils picasaUtils = new PicasaUtils();

    private static final QypeUtils qypeUtils = new QypeUtils();

    private static final TwitterUtils twitterUtils = new TwitterUtils();

    private static final UpcomingUtils upcomingUtils = new UpcomingUtils();

    private static final YelpUtils yelpUtils = new YelpUtils();

    private static final YoutubeUtils youtubeUtils = new YoutubeUtils();

    private static final GMSUtils gmsUtils = new GMSUtils();

    private static final EventfulUtils eventfulUtils = new EventfulUtils();

    private static final OsmXapiUtils osmXapiUtils = new OsmXapiUtils();

    private static final GeonamesUtils geonamesUtils = new GeonamesUtils();

    private static final LastfmUtils lastfmUtils = new LastfmUtils();

    private static final WebcamUtils webcamUtils = new WebcamUtils();

    private static final HotwireUtils hotwireUtils = new HotwireUtils();

    private static final PanoramioUtils panoramioUtils = new PanoramioUtils();

    private static final ExpediaUtils expediaUtils = new ExpediaUtils();

    private static final HotelsCombinedUtils hotelsCombinedUtils = new HotelsCombinedUtils();
    
    private static final InstagramUtils instagramUtils = new InstagramUtils();
    
    private static final FreebaseUtils freebaseUtils = new FreebaseUtils();
    
    private static final SearchUtils searchUtils = new SearchUtils();
    /**
     * @return the googlePlacesUtils
     */
    public static GooglePlacesUtils getGooglePlacesUtils() {
        return googlePlacesUtils;
    }

    /**
     * @return the grouponUtils
     */
    public static GrouponUtils getGrouponUtils() {
        return grouponUtils;
    }

    /**
     * @return the meetupUtils
     */
    public static MeetupUtils getMeetupUtils() {
        return meetupUtils;
    }

    /**
     * @return the picasaUtils
     */
    public static PicasaUtils getPicasaUtils() {
        return picasaUtils;
    }

    /**
     * @return the qypeUtils
     */
    public static QypeUtils getQypeUtils() {
        return qypeUtils;
    }

    /**
     * @return the twitterUtils
     */
    public static TwitterUtils getTwitterUtils() {
        return twitterUtils;
    }

    /**
     * @return the upcomingUtils
     */
    public static UpcomingUtils getUpcomingUtils() {
        return upcomingUtils;
    }

    /**
     * @return the yelpUtils
     */
    public static YelpUtils getYelpUtils() {
        return yelpUtils;
    }

    /**
     * @return the youtubeUtils
     */
    public static YoutubeUtils getYoutubeUtils() {
        return youtubeUtils;
    }

    /**
     * @return the gmsUtils
     */
    public static GMSUtils getGmsUtils() {
        return gmsUtils;
    }

    /**
     * @return the eventfulUtils
     */
    public static EventfulUtils getEventfulUtils() {
        return eventfulUtils;
    }

    /**
     * @return the couponsUtils
     */
    public static CouponsUtils getCouponsUtils() {
        return couponsUtils;
    }

    /**
     * @return the mcOpenApiUtils
     */
    public static McOpenApiUtils getMcOpenApiUtils() {
        return mcOpenApiUtils;
    }

    /**
     * @return the facebookUtils
     */
    public static FacebookUtils getFacebookUtils() {
        return facebookUtils;
    }

    /**
     * @return the flickrUtils
     */
    public static FlickrUtils getFlickrUtils() {
        return flickrUtils;
    }

    /**
     * @return the foursquareUtils
     */
    public static FoursquareUtils getFoursquareUtils() {
        return foursquareUtils;
    }

    /**
     * @return the osmXapiUtils
     */
    public static OsmXapiUtils getOsmXapiUtils() {
        return osmXapiUtils;
    }

    /**
     * @return the geonamesUtils
     */
    public static GeonamesUtils getGeonamesUtils() {
        return geonamesUtils;
    }

    /**
     * @return the lastfmUtils
     */
    public static LastfmUtils getLastfmUtils() {
        return lastfmUtils;
    }

    /**
     * @return the webcamUtils
     */
    public static WebcamUtils getWebcamUtils() {
        return webcamUtils;
    }

    /**
     * @return the hotwireUtils
     */
    public static HotwireUtils getHotwireUtils() {
        return hotwireUtils;
    }

    /**
     * @return the panoramioUtils
     */
    public static PanoramioUtils getPanoramioUtils() {
        return panoramioUtils;
    }

    /**
     * @return the expediaUtils
     */
    public static ExpediaUtils getExpediaUtils() {
        return expediaUtils;
    }

    /**
     * @return the hotelsCombinedUtils
     */
    public static HotelsCombinedUtils getHotelsCombinedUtils() {
        return hotelsCombinedUtils;
    }
    
    /**
     * @return the instagramUtils
     */
    public static InstagramUtils getInstagramUtils() {
        return instagramUtils;
    }
    
    /**
     * @return the feebaseUtils
     */
    public static FreebaseUtils getFreebaseUtils() {
        return freebaseUtils;
    }
    
    /**
     * @return the searchUtils
     */
    public static SearchUtils getSearchUtils() {
        return searchUtils;
    }

}
