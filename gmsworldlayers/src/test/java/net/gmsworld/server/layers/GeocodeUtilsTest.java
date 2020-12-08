package net.gmsworld.server.layers;

import java.io.IOException;

import org.junit.Test;

import net.gmsworld.server.utils.memcache.MockCacheProvider;

public class GeocodeUtilsTest {
	
	final String address = "UK, London, 64 Baker Street";
	final double lat = 10.76819, lng = 106.644512;
	
	@Test
	public void test() throws IOException {
	
		GoogleGeocodeUtils googleGeocodeUtils = new GoogleGeocodeUtils();
		googleGeocodeUtils.setCacheProvider(new MockCacheProvider());
		
		System.out.println("Test 1 ---------------");
		System.out.println(googleGeocodeUtils.processGeocode(address, null, 2, false));	
	
		System.out.println("Test 2 ---------------");
		System.out.println(googleGeocodeUtils.processReverseGeocode(lat, lng));
		
		MapQuestUtils mapQuestGeocodeUtils = new MapQuestUtils();
		mapQuestGeocodeUtils.setCacheProvider(new MockCacheProvider());
		
		System.out.println("Test 3 ---------------");
		System.out.println(mapQuestGeocodeUtils.processGeocode(address, null, 2, false));	
	
		System.out.println("Test 4 ---------------");
		System.out.println(mapQuestGeocodeUtils.processReverseGeocode(lat, lng));
	
	}
}
