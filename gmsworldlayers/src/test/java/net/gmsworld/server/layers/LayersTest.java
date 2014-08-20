package net.gmsworld.server.layers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.memcache.MockCacheProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

@RunWith(Parameterized.class)
public class LayersTest {
	
	final int limit = 30;
	
	@Parameters
	public static Collection<Object[]> data() {
	   LayerHelperFactory.setCacheProvider(new MockCacheProvider());
	   LayerHelperFactory.setThreadProvider(new JvmThreadProvider());	
	   Object[][] data = new Object[][] { {LayerHelperFactory.getFacebookUtils()}, {LayerHelperFactory.getFlickrUtils()} };
	   return Arrays.asList(data);
	}
	
	@Parameter
	public LayerHelper layer;
	
	@Test
	public void test() {
		try {
			//new york test
			List<ExtendedLandmark> landmarks = layer.processBinaryRequest(40.71, -74.01, null, 10000, 10, limit, 1024, null, null, Locale.US);
			int size = landmarks.size();
			System.out.println("Found " + size + " landmarks");
			assertNotNull(landmarks);
			assertEquals("Found " + size + " landmarks", limit, size);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
