package net.gmsworld.server.layers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.openlapi.AddressInfo;

import net.gmsworld.server.utils.memcache.CacheProvider;
import net.gmsworld.server.utils.persistence.GeocodeCache;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;

public class GeocodeHelperFactory {
	
	private static final Logger logger = Logger.getLogger(GeocodeHelperFactory.class.getName());

	private static final MapQuestUtils mapQuestUtils = new MapQuestUtils();
	
	private static final GoogleGeocodeUtils googleGeocodeUtils = new GoogleGeocodeUtils();
	
	private static CacheProvider cacheProvider;
	
	public static MapQuestUtils getMapQuestUtils() {
		mapQuestUtils.setCacheProvider(cacheProvider);
		return mapQuestUtils;
	}
	
	protected static GoogleGeocodeUtils getGoogleGeocodeUtils() {
		googleGeocodeUtils.setCacheProvider(cacheProvider);
		return googleGeocodeUtils;
	}
	
	public static void setCacheProvider(CacheProvider cp) {
    	cacheProvider = cp;
    }
	
	public static CacheProvider getCacheProvider() {
    	return cacheProvider;
    }
	
	public static AddressInfo processReverseGeocode(double latitude, double longitude) {
		 AddressInfo addressInfo = null;
		 try {
			 addressInfo = getGoogleGeocodeUtils().processReverseGeocode(latitude, longitude);
			 if (addressInfo == null) {
			 	 addressInfo = getMapQuestUtils().processReverseGeocode(latitude, longitude);
			 }
			 if (addressInfo == null) {
				 addressInfo = new AddressInfo();
			 }
		 } catch (Exception e) {
	         logger.log(Level.SEVERE, e.getMessage(), e);
	     } 
		 return addressInfo;
	}
	
	public static AddressInfo processReverseGeocodeAddress(double latitude, double longitude) {
		GeocodeCache gc = GeocodeCachePersistenceUtils.selectGeocodeCache(latitude, longitude);
		AddressInfo addressInfo = new AddressInfo();
		if (gc != null && gc.getLocation() != null) {
			addressInfo.setField(AddressInfo.EXTENSION, gc.getLocation());
		} else {
			 addressInfo = processReverseGeocode(latitude, longitude);
		}
		return addressInfo;
	}
}
