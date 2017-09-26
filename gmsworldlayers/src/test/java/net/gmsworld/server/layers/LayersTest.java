package net.gmsworld.server.layers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.JvmThreadProvider;
import net.gmsworld.server.utils.StringUtil;
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
	private static final int limit = 99; //min 30, max 1000
	private static final int radius = 50000; 
	private static final int stringLength = StringUtil.XLARGE;
	
	private static CacheProvider cacheProvider;	
	private double lat, lng;
	private String bbox;
	private Locale locale;
	
	private static void initLayerHelper() {
		cacheProvider = new MockCacheProvider(); 		   
		LayerHelperFactory.getInstance().setCacheProvider(cacheProvider);
	    LayerHelperFactory.getInstance().setThreadProvider(new JvmThreadProvider());	   
	}
	
	@Before
	public void initialize() {
		//warsaw test
		lat = 52.25;
		lng = 20.95;
		bbox = "20.96,52.24,20.97,52.25"; //"51.25,19.95,53.25,21.95"; //
		//locale = new Locale("pl",""); //"PL");
		
		//new york test
		//lat = 40.71;
		//lng = -74.01;
		//bbox = "-74.060000,40.660000,-74.010000,40.710000";//"-75.01,39.71,-73.01,41.71";
		locale = Locale.US;
		
		//lat = 30.21;
		//lng = -97.77;
		//bbox = "-97.79,30.16,-97.74,30.26"; 
		
		//lat = 46.782499;
		//lng = 23.558828;	
	}
	
	
	@Parameters
	public static Collection<Object[]> staticLayers() {
	   initLayerHelper();
		
	   List<Object[]> data = new ArrayList<Object[]>();	
	  
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.WEBCAM_LAYER)});
	   
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.OSM_TAXI_LAYER)});
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.OSM_ATM_LAYER)});
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.OSM_PARKING_LAYER)});
	   
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.COUPONS_LAYER)});
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.GROUPON_LAYER)});
	   
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.MC_ATM_LAYER)});
	   
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.YOUTUBE_LAYER)});
	   
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER)});
	   
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER)});
	   
	   //data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER)});
	   
	   data.add(new Object[]{LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER)});
	   
	   System.out.println("Found " + data.size() + " layers.");
	   
	   return data;
	}
	
	//@Parameters
	public static Collection<LayerHelper> dynamicLayers() {
	   initLayerHelper();
	   List<LayerHelper> layerHelpers = new ArrayList<LayerHelper>();
	   List<String> enabledLayers = LayerHelperFactory.getInstance().getEnabledLayers();
	   for (String layer : enabledLayers) {
		   layerHelpers.add(LayerHelperFactory.getInstance().getByName(layer));
	   }
	   return layerHelpers;
	   //return LayerHelperFactory.getInstance().getAllLayers().values();	
	}
	
	@Parameter
	public LayerHelper layer;
	
	//@Test
	public void test2()  {
		List<String> enabledLayers = LayerHelperFactory.getInstance().getEnabledLayers();
		System.out.println("Enabled layers count: " +	enabledLayers.size());
		
		for (String layerName : enabledLayers) {
			System.out.println(layerName + " layer is enabled, icon: " + LayerHelperFactory.getInstance().getIcon(layerName));
		}		
	}
	
	@Test
	public void test() {
		try {
			List<ExtendedLandmark> landmarks = null;
			if (StringUtils.equals(layer.getLayerName(), Commons.OSM_ATM_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "atm", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.OSM_PARKING_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "parking", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.OSM_TAXI_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "taxi", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.FOURSQUARE_MERCHANT_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, Commons.getProperty(Property.FS_OAUTH_TOKEN), "1,2,3,4,5,6,7,8", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.FOURSQUARE_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "checkin", "en", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.YELP_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "false", "en", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.GOOGLE_PLACES_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "en", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.COUPONS_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, null, "en", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.FACEBOOK_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, null, "", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.WIKIPEDIA_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "en", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.LM_SERVER_LAYER)) {
			    landmarks = layer.processBinaryRequest(lat, lng, null, 15, apiLevel, limit, stringLength, Commons.LM_SERVER_LAYER, null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.PICASA_LAYER) || StringUtils.equals(layer.getLayerName(), Commons.PANORAMIO_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, bbox, "", locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), "Search")) {
				landmarks = layer.processBinaryRequest(lat, lng, "restaurant", radius, apiLevel, limit, stringLength, "0_0_50", null, locale, true);
			} else if (StringUtils.equals(layer.getLayerName(), Commons.HOTELS_LAYER)) {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, "true", null, locale, true);
			} else {
				landmarks = layer.processBinaryRequest(lat, lng, null, radius, apiLevel, limit, stringLength, null, null, locale, true);
			}
			
			int size = landmarks.size();
			System.out.println("Found " + size + " landmarks in layer " + layer.getLayerName());
			//assertNotNull(landmarks);
			//assertEquals("Found " + size + " landmarks", limit, size);
			
			for (ExtendedLandmark landmark : landmarks) {
				System.out.println(landmark.getName() + " :-> " + landmark.getDescription() + "---\n");
				System.out.println(landmark.getThumbnail() + " " + landmark.getCategoryId() + "," + landmark.getSubCategoryId());
				System.out.println(landmark.getUrl());
				System.out.println(landmark.getLayer());
			}
			
			String key = layer.cacheGeoJson(landmarks, lat, lng, layer.getLayerName(), locale, null);
			System.out.println(cacheProvider.getString(key));
			
			assertEquals("Layer " + layer.getLayerName() + " is empty!", false, landmarks.isEmpty());
			assertEquals("Layer " + layer.getLayerName() + " size is " + landmarks.size(), limit, landmarks.size());		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//@Test
	public void hotelsTest() {
		//LayerHelperFactory.getHotelsBookingUtils().loadHotelsAsync(lat, lng, radius, limit);  
		try {
			String hotels = ((HotelsBookingUtils)LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER)).extendFeatureCollection(lat, lng, radius, limit, "stars", locale);
			//System.out.println(hotels);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("Hotels in radius: " + LayerHelperFactory.getHotelsBookingUtils().countNearbyHotels(lat, lng, radius));
		//System.out.println("Cheapest hotel price: " + LayerHelperFactory.getHotelsBookingUtils().findCheapestHotel(lat, lng, radius, 1));			
	}
	
	/*private static List<Method> getStaticGetMethods(Class<?> clazz) {
	    List<Method> methods = new ArrayList<Method>();
	    for (Method method : clazz.getMethods()) {
	        if (Modifier.isStatic(method.getModifiers()) && method.getName().startsWith("get")) {
	            methods.add(method);
	        }
	    }
	    return methods;
	}*/
}
