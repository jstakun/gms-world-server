package net.gms_world.gmsworldserver;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;

import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class FlickrTest {

	@Test
	public void test() {
		try {
			//comment cache utils
			LayerHelperFactory.setCacheProvider(new MockCacheProvider());
			List<ExtendedLandmark> photos =  LayerHelperFactory.getFlickrUtils().processBinaryRequest(52.25, 20.95, null, 10000, 10, 30, 1024, null, null, Locale.US);
		    System.out.println("Found " + photos.size() + " photos");
			assertNotNull(photos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}