package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class FBTest {
	
	static {
		LayerHelperFactory.getInstance().setCacheProvider(new MockCacheProvider());
		LayerHelperFactory.getInstance().setThreadProvider(new JvmThreadProvider());	
	}
	
	String token = Commons.getProperty(Property.FB_TEST_TOKEN_FULL_0);
	double lat = 52.25;
	double lng = 20.95;
	int limit = 30;

	@Test
	public void testMyFriends() throws UnsupportedEncodingException {		
    	List<String> friends = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyFriends(token);
		System.out.println("Found " + friends.size() + " friends");
	}
	
	@Test
	public void testMyCheckins() throws UnsupportedEncodingException, ParseException {
		List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPlaces(1146, limit, StringUtil.XLARGE, token, Locale.UK, false);
		printLandmarks(landmarks, "checkins");
	}
	
	@Test
	public void testMyPhotos() throws UnsupportedEncodingException, ParseException {
		List<ExtendedLandmark> landmarks =	((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPhotos(1126, limit, StringUtil.XLARGE, token, Locale.UK, false);
	    printLandmarks(landmarks, "photos");
	}
	
	private void printLandmarks(List<ExtendedLandmark> landmarks, String name) {
		if (landmarks != null) {
			System.out.println("Found " + landmarks.size() + " " + name);    
			for (ExtendedLandmark l : landmarks) {
				System.out.println(DateUtils.getFormattedDateTime(Locale.US, new Date(l.getCreationDate())) + " " + l.getName() + ": " + l.getLatitudeE6() + "," + l.getLongitudeE6() + "\n\nDescription: " + l.getDescription() + "\nThumbnail: " + l.getThumbnail() + "\nUrl: " + l.getUrl() + "\n");
			}
		}
	}

	@Test
	public void testUserTaggedPlaces() throws UnsupportedEncodingException, ParseException {	
		List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyTaggedPlaces(1126, limit, StringUtil.XLARGE, token, Locale.UK, false);
		printLandmarks(landmarks, "tagged places");
	}
	
	@Test
	public void testPlaces() throws Exception {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).processBinaryRequest(lat, lng, null, 10, 1126, limit, StringUtil.XLARGE, null, null, Locale.UK, false);
		printLandmarks(landmarks, "places around");
	}
    
}
