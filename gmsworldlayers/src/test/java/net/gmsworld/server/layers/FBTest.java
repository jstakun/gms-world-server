package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
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
	
	String token = Commons.FB_TEST_TOKEN;
	double lat = 52.25;
	double lng = 20.95;

	@Test
	public void testMyFriends() throws UnsupportedEncodingException {		
    	List<String> friends = LayerHelperFactory.getFacebookUtils().getMyFriends(token);
		System.out.println("Found " + friends.size() + " friends");
	}
	
	public void testFriendsCheckins() throws UnsupportedEncodingException {
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getFriendsCheckinsToLandmarks(lat, lng, 1, 30, 1024, token, Locale.US);
		printLandmarks(landmarks, "checkins");
	}
	
	public void testFriendsPhotos() throws UnsupportedEncodingException {
		List<ExtendedLandmark> landmarks =	LayerHelperFactory.getFacebookUtils().getFriendsPhotosToLandmark(lat, lng, 1, 30, 1024, token, Locale.US);
	    printLandmarks(landmarks, "photos");
	}
	
	private void printLandmarks(List<ExtendedLandmark> landmarks, String name) {
		System.out.println("Found " + landmarks.size() + " " + name);    
	    for (ExtendedLandmark l : landmarks) {
			System.out.println(DateUtils.getFormattedDateTime(Locale.US, new Date(l.getCreationDate())) + " " + l.getName() + ": " + l.getLatitudeE6() + "," + l.getLongitudeE6());
		}
	}

	@Test
	public void testUserTaggedPlaces() throws UnsupportedEncodingException {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFacebookUtils().getUserTaggedPlaces(1126, 30, 1024, token, Locale.UK);
		printLandmarks(landmarks, "places");
	}
    
}
