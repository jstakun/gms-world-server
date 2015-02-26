package net.gmsworld.server.layers;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.memcache.CacheProvider;
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
	private static CacheProvider cacheProvider;
	
	@Parameters
	public static Collection<Object[]> data() {
		
	   List<Object[]> data = new ArrayList<Object[]>();	
	   
	   cacheProvider = new MockCacheProvider(); 
	   
	   LayerHelperFactory.setCacheProvider(cacheProvider);
	   LayerHelperFactory.setThreadProvider(new JvmThreadProvider());	
	   
	   List<Method> methods = getStaticGetMethods(LayerHelperFactory.class);
	   for (Method m : methods) {
		   try {
			   data.add(new Object[]{ m.invoke(null,(Object[])null) });
		   } catch (IllegalAccessException e) {
			   e.printStackTrace();
		   } catch (IllegalArgumentException e) {
			   e.printStackTrace();
		   } catch (InvocationTargetException e) {
			   e.printStackTrace();
		   }
	   }
	   
	   //data.add(new Object[]{LayerHelperFactory.getCouponsUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getFoursquareMerchantUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getPicasaUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getExpediaUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getYelpUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getMcOpenApiUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getGooglePlacesUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getFoursquareUtils()});
	   
	   System.out.println("Found " + data.size() + " layers.");
	   
	   return data;
	}
	
	@Parameter
	public LayerHelper layer;
	
	@Test
	public void test() {
		try {
			//warsaw test
			//double lat = 52.25;
			//double lng = 20.95;
			//String bbox = "20.96,52.24,20.97,52.25"; //"51.25,19.95,53.25,21.95";
			//new york test
			double lat = 40.71;
			double lng = -74.01;
			String bbox = "-74.06,40.66,-74.01,40.71";//"-75.01,39.71,-73.01,41.71";
			//OSM List<ExtendedLandmark> landmarks = layer.processBinaryRequest(lat, lng, null, 10000, 1115, limit, 1024, "atm", bbox, Locale.US);
			//Foursquare List<ExtendedLandmark> landmarks = layer.processBinaryRequest(lat, lng, null, 10000, 1115, limit, 1024, Commons.getProperty(Property.FS_OAUTH_TOKEN), "1,2,3,4,5,6,7,8", Locale.US);
			List<ExtendedLandmark> landmarks = layer.processBinaryRequest(lat, lng, null, 10000, 1115, limit, 1024, bbox, "", Locale.US, true);
			int size = landmarks.size();
			System.out.println("Found " + size + " landmarks");
			assertNotNull(landmarks);
			//assertEquals("Found " + size + " landmarks", limit, size);
			for (ExtendedLandmark landmark : landmarks) {
				System.out.println(landmark.getName() + " :-> " + landmark.getDescription() + "---\n");
				//System.out.println(landmark.getThumbnail());
			}
			String layerName = "Anything";
			String key = layer.cacheGeoJson(landmarks, lat, lng, layerName);
			System.out.println(cacheProvider.getString(key));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<Method> getStaticGetMethods(Class<?> clazz) {
	    List<Method> methods = new ArrayList<Method>();
	    for (Method method : clazz.getMethods()) {
	        if (Modifier.isStatic(method.getModifiers()) && method.getName().startsWith("get")) {
	            methods.add(method);
	        }
	    }
	    return methods;
	}
}
