package net.gmsworld.server.layers;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

public class FBTest {
	
	static {
		LayerHelperFactory.getInstance().setCacheProvider(new MockCacheProvider());
		LayerHelperFactory.getInstance().setThreadProvider(new JvmThreadProvider());	
	}
	
	final String PSID = "";
	final String mytoken = "PUT_YOUR_TOKEN_HERE";
	
	final double lat = 52.25;
	final double lng = 20.95;
	final int limit = 30;

	@Test
	public void testMyFriends() throws Exception {		
    	List<String> friends = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyFriends(mytoken);
		System.out.println("Found " + friends.size() + " friends");
	}
	
	@Test
	public void testMyCheckins() throws Exception {
		List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPlaces(1146, limit, StringUtil.XLARGE, mytoken, Locale.UK, false);
		printLandmarks(landmarks, "checkins");
	}
	
	@Test
	public void testMyPhotos() throws Exception {
		List<ExtendedLandmark> landmarks =	((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyPhotos(1126, limit, StringUtil.XLARGE, mytoken, Locale.UK, false);
	    printLandmarks(landmarks, "photos");
	}
	
	@Test
	public void testMyTaggedPlaces() throws Exception {	
		List<ExtendedLandmark> landmarks = ((FacebookUtils)LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)).getMyTaggedPlaces(1126, limit, StringUtil.XLARGE, mytoken, Locale.UK, false);
		printLandmarks(landmarks, "tagged places");
	}
	
	@Test
	public void testPlaces() throws Exception {	
		List<ExtendedLandmark> landmarks = LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).processBinaryRequest(lat, lng, null, 10, 1126, limit, StringUtil.XLARGE, null, null, Locale.UK, false);
		//LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).processRequest(lat, lng, null, 10, 1126, limit, StringUtil.XLARGE, null, null);
		printLandmarks(landmarks, "places around");
	}
    
	//@Test
	public void testMessenger() {
		MessengerUtils.sendMessage(PSID, MessengerUtils.ACTION_MARK_SEEN, null);
		MessengerUtils.sendMessage(PSID, null, "Hello");
	}
	
	private void printLandmarks(List<ExtendedLandmark> landmarks, String name) {
		if (landmarks != null) {
			System.out.println("Found " + landmarks.size() + " " + name);    
			for (ExtendedLandmark l : landmarks) {
				System.out.println(DateUtils.getFormattedDateTime(Locale.US, new Date(l.getCreationDate())) + " " + l.getName() + ": " + l.getLatitudeE6() + "," + l.getLongitudeE6() + "\n\nDescription: " + l.getDescription() + "\nThumbnail: " + l.getThumbnail() + "\nUrl: " + l.getUrl() + "\n");
			}
		}
	}
}
