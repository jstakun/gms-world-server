package net.gmsworld.server.layers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.openlapi.AddressInfo;

import net.gmsworld.server.utils.memcache.CacheProvider;
import net.gmsworld.server.utils.persistence.GeocodeCache;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;

public class GeocodeHelperFactory {
	
	private static final Logger logger = Logger.getLogger(GeocodeHelperFactory.class.getName());

	private static final GeocodeHelperFactory instance = new GeocodeHelperFactory();
	
	private MapQuestUtils mapQuestUtils;
	
	private GoogleGeocodeUtils googleGeocodeUtils;
	
	private CacheProvider cacheProvider;
	
	private GeocodeHelperFactory() {
		mapQuestUtils = new MapQuestUtils();
		googleGeocodeUtils = new GoogleGeocodeUtils();
	}
	
	public static GeocodeHelperFactory getInstance() {
		return instance;
	}
	
	
	private MapQuestUtils getMapQuestUtils() {
		mapQuestUtils.setCacheProvider(cacheProvider);
		return mapQuestUtils;
	}
	
	private GoogleGeocodeUtils getGoogleGeocodeUtils() {
		googleGeocodeUtils.setCacheProvider(cacheProvider);
		return googleGeocodeUtils;
	}
	
	public void setCacheProvider(CacheProvider cp) {
    	cacheProvider = cp;
    }
	
	private AddressInfo processReverseGeocodeBackend(double latitude, double longitude) {
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
	
	public AddressInfo processReverseGeocode(double latitude, double longitude) {
		AddressInfo addressInfo = new AddressInfo();
		final GeocodeCache gc = GeocodeCachePersistenceUtils.selectGeocodeCache(latitude, longitude);
		
		if (gc != null && gc.getLocation() != null) {
			addressInfo.setField(AddressInfo.EXTENSION, gc.getLocation());
		} else {
			 addressInfo = processReverseGeocodeBackend(latitude, longitude);
		}
		return addressInfo;
	}
	
	public String processGeocode(String address, String email, int appId) {
    	JSONObject resp = getGoogleGeocodeUtils().processGeocode(address, email, appId, true);
        try {
            if (resp.getString("status").equals("Error")) {
                logger.log(Level.INFO, "Search geocode response {0}", resp.toString());
                resp = getMapQuestUtils().processGeocode(address, email, appId, true);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return resp.toString();
    }
	
	public  JSONObject getRoute(String lat_start, String lng_start, String lat_end, String lng_end, String type, String username) throws IOException {
		return getMapQuestUtils().getRoute(lat_start, lng_start, lat_end, lng_end, type, username);
	}
	
}
