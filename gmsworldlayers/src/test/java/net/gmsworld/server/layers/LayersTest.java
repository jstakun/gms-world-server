package net.gmsworld.server.layers;

import static org.junit.Assert.assertEquals;

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

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

@RunWith(Parameterized.class)
public class LayersTest {
	
	private static final int apiLevel = 1115;
	private static final int limit = 30; //max 1000
	private static final int radius = 50000; 
	
	private static CacheProvider cacheProvider;	
	private double lat, lng;
	private String bbox;
	private Locale locale;
	
	private static void initLayerHelper() {
		cacheProvider = new MockCacheProvider(); 		   
		LayerHelperFactory.setCacheProvider(cacheProvider);
	    LayerHelperFactory.setThreadProvider(new JvmThreadProvider());	   
	}
	
	@Before
	public void initialize() {
		//warsaw test
		//lat = 52.25;
		//lng = 20.95;
		//bbox = "20.96,52.24,20.97,52.25"; //"51.25,19.95,53.25,21.95";
		locale = new Locale("pl",""); //"PL");
		
		//new york test
		lat = 40.71;
		lng = -74.01;
		bbox = "-74.060000,40.660000,-74.010000,40.710000";//"-75.01,39.71,-73.01,41.71";
		//locale = Locale.US;
		
		//lat = 30.21;
		//lng = -97.77;
		//bbox = "-97.79,30.16,-97.74,30.26"; 
		
		//lat = 46.782499;
		//lng = 23.558828;
		
		//LayerHelperFactory.getHotelsBookingUtils().loadHotelsAsync(lat, lng, radius, limit);  
		//System.out.println("Hotels in radius: " + LayerHelperFactory.getHotelsBookingUtils().countNearbyHotels(lat, lng, radius));
		//System.out.println("Cheapest hotel price: " + LayerHelperFactory.getHotelsBookingUtils().findCheapestHotel(lat, lng, radius, 1));
	}
	
	
	@Parameters
	public static Collection<Object[]> staticLayers() {
	   initLayerHelper();
		
	   List<Object[]> data = new ArrayList<Object[]>();	
	  
	   //data.add(new Object[]{LayerHelperFactory.getPicasaUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getLastfmUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getOsmXapiUtils()});
	   
	   data.add(new Object[]{LayerHelperFactory.getHotelsBookingUtils()});
	   
	   //data.add(new Object[]{LayerHelperFactory.getFreebaseUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getInstagramUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getYoutubeUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getExpediaUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getMcOpenApiUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getGeonamesUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getGmsUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getTwitterUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getEventfulUtils()});
	   
	   //data.add(new Object[]{LayerHelperFactory.getGrouponUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getCouponsUtils()});
	   
	   //data.add(new Object[]{LayerHelperFactory.getFoursquareUtils()});   
	   //data.add(new Object[]{LayerHelperFactory.getYelpUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getGooglePlacesUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getFacebookUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getFoursquareMerchantUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getSearchUtils()});
	   
	   System.out.println("Found " + data.size() + " layers.");
	   
	   return data;
	}
	
	//@Parameters
	public static Collection<Object[]> dynamicLayers() {
	   initLayerHelper();
		
	   List<Object[]> data = new ArrayList<Object[]>();	
	   
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
	   
	   System.out.println("Found " + data.size() + " layers.");
	   
	   return data;
	}
	
	@Parameter
	public LayerHelper layer;
	
	@Test
	public void test() {
		try {
			List<ExtendedLandmark> landmarks = null;
			if (StringUtils.equals(layer.getLayerName(), Commons.OSM_ATM_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, "atm", bbox, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.FOURSQUARE_MERCHANT_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, Commons.getProperty(Property.FS_OAUTH_TOKEN), "1,2,3,4,5,6,7,8", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.FOURSQUARE_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, "checkin", "en", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.YELP_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, "false", "en", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.GOOGLE_PLACES_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, "en", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.COUPONS_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, null, "en", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.FACEBOOK_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, null, "", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.WIKIPEDIA_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, "en", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.LM_SERVER_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, 15, apiLevel, limit, 1024, Commons.LM_SERVER_LAYER, null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.PICASA_LAYER) || StringUtils.equals(layer.getLayerName(), Commons.PANORAMIO_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, bbox, "", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), "Search")) {
				landmarks = layer.processBinaryRequest(lat, lng, "restaurant", radius, apiLevel, limit, 1024, "0_0_50", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.HOTELS_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, "true", null, locale, true);
			} else {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, 1024, null, null, locale, true);
			}
			int size = landmarks.size();
			System.out.println("Found " + size + " landmarks in layer " + layer.getLayerName());
			//assertNotNull(landmarks);
			//assertEquals("Found " + size + " landmarks", limit, size);
			assertEquals("Layer " + layer.getLayerName() + " is empty!", landmarks.isEmpty(), false);
			
			//for (ExtendedLandmark landmark : landmarks) {
				//System.out.println(landmark.getName() + " :-> " + landmark.getDescription() + "---\n");
				//System.out.println(landmark.getThumbnail());
				//System.out.println(landmark.getUrl());
			//}
			
			String key = layer.cacheGeoJson(landmarks, lat, lng, layer.getLayerName(), locale);
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
