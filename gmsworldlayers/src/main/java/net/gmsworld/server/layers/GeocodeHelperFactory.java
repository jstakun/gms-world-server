package net.gmsworld.server.layers;

import net.gmsworld.server.utils.memcache.CacheProvider;

public class GeocodeHelperFactory {

	private static final MapQuestUtils mapQuestUtils = new MapQuestUtils();
	
	private static final GoogleGeocodeUtils googleGeocodeUtils = new GoogleGeocodeUtils();
	
	private static CacheProvider cacheProvider;
	
	public static MapQuestUtils getMapQuestUtils() {
		mapQuestUtils.setCacheProvider(cacheProvider);
		return mapQuestUtils;
	}
	
	public static GoogleGeocodeUtils getGoogleGeocodeUtils() {
		googleGeocodeUtils.setCacheProvider(cacheProvider);
		return googleGeocodeUtils;
	}
	
	public static void setCacheProvider(CacheProvider cp) {
    	cacheProvider = cp;
    }
	
	public static CacheProvider getCacheProvider() {
		return cacheProvider;
	}
}
