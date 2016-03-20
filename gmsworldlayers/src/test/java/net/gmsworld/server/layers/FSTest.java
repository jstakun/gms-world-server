package net.gmsworld.server.layers;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.json.JSONException;
import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

import fi.foyt.foursquare.api.FoursquareApiException;

public class FSTest {
	
	static {
		LayerHelperFactory.setCacheProvider(new MockCacheProvider());
		LayerHelperFactory.setThreadProvider(new JvmThreadProvider());	
	}
	
	String token = Commons.FS_TEST_TOKEN_FULL_0;
	double lat = 52.25;
	double lng = 20.95;
	int limit = 30;
	Locale locale = Locale.GERMANY;

	private void printLandmarks(List<ExtendedLandmark> landmarks, String name) {
		if (landmarks != null) {
			System.out.println("Found " + landmarks.size() + " " + name);    
			for (ExtendedLandmark l : landmarks) {
				System.out.println(DateUtils.getFormattedDateTime(Locale.US, new Date(l.getCreationDate())) + " " + l.getName() + ": " + l.getLatitudeE6() + "," + l.getLongitudeE6() + "\n\nDescription: " + l.getDescription() + "\nThumbnail: " + l.getThumbnail() + "\nUrl: " + l.getUrl() + "\n");
			}
		}
	}
	
	@Test
	public void testExploreVenues() throws ParseException, JSONException, MalformedURLException, IOException, FoursquareApiException {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().exploreVenuesToLandmark(lat, lng, null, 10, limit, StringUtil.XLARGE, 1138, token, locale, false);
		printLandmarks(landmarks, "explored venues");
	}
	
	@Test
	public void testMyCheckins() throws UnsupportedEncodingException, ParseException, JSONException, FoursquareApiException {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().getFriendsCheckinsToLandmarks(lat, lng, limit, StringUtil.XLARGE, 1138, token, locale, false);
		printLandmarks(landmarks, "checkins");
	}
	
	@Test
	public void testPlaces() throws Exception {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getFoursquareUtils().processBinaryRequest(lat, lng, null, 10, 1138, limit, StringUtil.XLARGE, "checkin", null, locale, false);
		printLandmarks(landmarks, "places");
	}
}
