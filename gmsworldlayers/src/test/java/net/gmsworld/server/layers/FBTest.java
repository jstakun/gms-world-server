package net.gmsworld.server.layers;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.json.JSONException;
import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class FBTest {

	@Test
	public void test() throws UnsupportedEncodingException {	
		String token = Commons.FB_TEST_TOKEN;
		double lat = 52.25;
		double lng = 20.95;
		
		LayerHelperFactory.setCacheProvider(new MockCacheProvider());
		LayerHelperFactory.setThreadProvider(new JvmThreadProvider());	
		
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getFriendsCheckinsToLandmarks(lat, lng, 1, 30, 1024, token, Locale.US);
		System.out.println("Found " + landmarks.size() + " checkins");
		
	    landmarks =	LayerHelperFactory.getFacebookUtils().getFriendsPhotosToLandmark(lat, lng, 1, 30, 1024, token, Locale.US);
	    System.out.println("Found " + landmarks.size() + " photos");
	}

}
