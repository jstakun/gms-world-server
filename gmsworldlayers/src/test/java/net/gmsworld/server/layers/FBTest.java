package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class FBTest {
	
	static {
		LayerHelperFactory.setCacheProvider(new MockCacheProvider());
		LayerHelperFactory.setThreadProvider(new JvmThreadProvider());	
	}
	
	String token = Commons.FB_TEST_TOKEN_FULL_1;
	double lat = 52.25;
	double lng = 20.95;
	int limit = 93;

	@Test
	public void testMyFriends() throws UnsupportedEncodingException {		
    	List<String> friends = LayerHelperFactory.getFacebookUtils().getMyFriends(token);
		System.out.println("Found " + friends.size() + " friends");
	}
	
	@Test
	public void testMyCheckins() throws UnsupportedEncodingException, ParseException {
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getMyPlaces(1126, limit, 1024, token, Locale.UK);
		printLandmarks(landmarks, "checkins");
	}
	
	@Test
	public void testMyPhotos() throws UnsupportedEncodingException, ParseException {
		List<ExtendedLandmark> landmarks =	LayerHelperFactory.getFacebookUtils().getMyPhotos(1126, limit, 1024, token, Locale.UK);
	    printLandmarks(landmarks, "photos");
	}
	
	private void printLandmarks(List<ExtendedLandmark> landmarks, String name) {
		if (landmarks != null) {
			System.out.println("Found " + landmarks.size() + " " + name);    
			for (ExtendedLandmark l : landmarks) {
				System.out.println(DateUtils.getFormattedDateTime(Locale.US, new Date(l.getCreationDate())) + " " + l.getName() + ": " + l.getLatitudeE6() + "," + l.getLongitudeE6() + "\n\nDescription: " + l.getDescription() + "\nThumbnail: " + l.getThumbnail() + "\n");
			}
		}
	}

	@Test
	public void testUserTaggedPlaces() throws UnsupportedEncodingException, ParseException {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getMyTaggedPlaces(1126, limit, 1024, token, Locale.UK);
		printLandmarks(landmarks, "tagged places");
	}
	
	@Test
	public void testPlaces() throws Exception {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().processBinaryRequest(lat, lng, null, 10, 1126, 30, 1024, null, null, Locale.UK, false);
		printLandmarks(landmarks, "places around");
	}
    
}
