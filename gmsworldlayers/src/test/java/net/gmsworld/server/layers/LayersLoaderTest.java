package net.gmsworld.server.layers;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class LayersLoaderTest {

	final int limit = 30;
	
	@Test
	public void test() {
		
		LayerHelperFactory.setCacheProvider(new MockCacheProvider());
		LayerHelperFactory.setThreadProvider(new JvmThreadProvider());	
		
		List<String> layers = Arrays.asList(new String[]{Commons.FACEBOOK_LAYER, Commons.FOURSQUARE_LAYER, Commons.HOTELS_LAYER, 
				Commons.YELP_LAYER, Commons.WIKIPEDIA_LAYER, Commons.EVENTFUL_LAYER,
				Commons.FLICKR_LAYER, Commons.YOUTUBE_LAYER, Commons.FOURSQUARE_MERCHANT_LAYER,
				Commons.GOOGLE_PLACES_LAYER, Commons.LASTFM_LAYER, Commons.TWITTER_LAYER,
				Commons.INSTAGRAM_LAYER, Commons.MEETUP_LAYER, Commons.EXPEDIA_LAYER,
				Commons.WEBCAM_LAYER, Commons.MC_ATM_LAYER, Commons.FREEBASE_LAYER, Commons.LM_SERVER_LAYER,
				Commons.COUPONS_LAYER, Commons.GROUPON_LAYER,}); 
		        //Commons.PANORAMIO_LAYER, Commons.PICASA_LAYER, Commons.OSM_PARKING_LAYER, Commons.OSM_ATM_LAYER
		System.out.println("Testing " + layers.size() + " layers");
		
		LayersLoader loader = new LayersLoader(new JvmThreadProvider(), layers);
		
		//52.25, 20.95
		//40.71, -74.01
		List<List<ExtendedLandmark>> results = loader.loadLayers(40.71, -74.01, null, 10, 1130, limit, 1024, null, null, Locale.US, true);
	
	    for (List<ExtendedLandmark> landmarks : results) {
	    	System.out.print("Found " + landmarks.size() + " landmarks");
	    	if (! landmarks.isEmpty()) {
	    		System.out.println(" in layer " + landmarks.get(0).getLayer());
	    	} else {
	    		System.out.println("");
	    	}
	    }
	}

}
