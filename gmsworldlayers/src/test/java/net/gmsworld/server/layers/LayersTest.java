package net.gmsworld.server.layers;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
		
	   List<Object[]> data = new ArrayList<Object[]>();	
	   
	   LayerHelperFactory.setCacheProvider(new MockCacheProvider());
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
	   
	   //data.add(new Object[]{LayerHelperFactory.getPicasaUtils()});
	   //data.add(new Object[]{LayerHelperFactory.getExpediaUtils()});
	   
	   System.out.println("Found " + data.size() + " layers.");
	   
	   return data;
	}
	
	@Parameter
	public LayerHelper layer;
	
	@Test
	public void test() {
		try {
			//new york test
			String bbox = "-75.01,39.71,-73.01,41.71";
			//OSM List<ExtendedLandmark> landmarks = layer.processBinaryRequest(40.71, -74.01, null, 10000, 1115, limit, 1024, "atm", bbox, Locale.US);
			List<ExtendedLandmark> landmarks = layer.processBinaryRequest(40.71, -74.01, null, 10000, 1115, limit, 1024, bbox, "", Locale.US);
			int size = landmarks.size();
			System.out.println("Found " + size + " landmarks");
			assertNotNull(landmarks);
			//assertEquals("Found " + size + " landmarks", limit, size);
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
