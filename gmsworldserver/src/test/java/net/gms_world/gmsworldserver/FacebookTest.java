package net.gms_world.gmsworldserver;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;

import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class FacebookTest {

	@Test
	public void test() {
		List<ExtendedLandmark> landmarks;
		try {
			LayerHelperFactory.setCacheProvider(new MockCacheProvider());
			LayerHelperFactory.setThreadProvider(new JvmThreadProvider());
			landmarks = LayerHelperFactory.getFacebookUtils().processBinaryRequest(52.25, 20.95, null, 10000, 10, 30, 1024, null, null, Locale.US);
			System.out.println("Found " + landmarks.size() + " landmarks");
			assertNotNull(landmarks);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
