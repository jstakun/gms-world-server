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
		
		List<String> layers = Arrays.asList(new String[]{Commons.FACEBOOK_LAYER, Commons.FOURSQUARE_LAYER, Commons.HOTELS_LAYER});
		
		LayersLoader loader = new LayersLoader(layers);
		
		List<List<ExtendedLandmark>> results = loader.loadLayers(52.25, 20.95, null, 10, 1130, limit, 1024, null, null, Locale.US, true);
	
	    for (List<ExtendedLandmark> landmarks : results) {
	    	System.out.print("Found " + landmarks.size() + " landmarks");
	    	if (! landmarks.isEmpty()) {
	    		System.out.println(" in layer " + landmarks.get(0).getLayer());
	    	}
	    }
	}

}
