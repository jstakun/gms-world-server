package net.gmsworld.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.openlapi.AddressInfo;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.memcache.CacheProvider;

/**
 *
 * @author jstakun
 */
public class GeocodeCachePersistenceUtils {

    private static final Logger logger = Logger.getLogger(GeocodeCachePersistenceUtils.class.getName());
    private static final String BACKEND_SERVER_URL = "https://landmarks-api.b9ad.pro-us-east-1.openshiftapps.com/api/v1"; 
    
    public static void persistGeocode(final AddressInfo addressInfo, final double latitude, final double longitude, CacheProvider cacheProvider) {
        if (addressInfo != null && StringUtils.isNotEmpty(addressInfo.getField(AddressInfo.EXTENSION))) {
        	try {
        		JSONObject flex = new JSONObject();
        		if (StringUtils.isNotEmpty(addressInfo.getField(AddressInfo.COUNTRY_CODE))) {
        			flex.put("cc", addressInfo.getField(AddressInfo.COUNTRY_CODE));
        		}
        		if (StringUtils.isNotEmpty(addressInfo.getField(AddressInfo.CITY))) {
        			flex.put("city", addressInfo.getField(AddressInfo.CITY));
        		}
        		if (StringUtils.isNotEmpty(addressInfo.getField(AddressInfo.STATE))) {
        			flex.put("state", addressInfo.getField(AddressInfo.STATE));
        		}
        		if (StringUtils.isNotEmpty(addressInfo.getField(AddressInfo.COUNTY))) {
        			flex.put("county", addressInfo.getField(AddressInfo.COUNTY));
        		}
        		
        		final String gUrl = BACKEND_SERVER_URL + "/addItem";
        		final String content = "type=geocode&latitude=" + StringUtil.formatCoordE6(latitude) + "&longitude=" + StringUtil.formatCoordE6(longitude) + 
        			"&address=" + URLEncoder.encode(addressInfo.getField(AddressInfo.EXTENSION), "UTF-8") + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY) + "&flex=" + URLEncoder.encode(flex.toString(), "UTF-8");
        		final String gJson = HttpUtils.processFileRequest(new URL(gUrl), "POST", null, content);
        		logger.log(Level.INFO, "Received response: " + gJson);
        		if (cacheProvider != null) {
        			cacheProvider.remove("NewestGeocodes");
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        	}
        }
    }

    public static GeocodeCache checkIfGeocodeExists(final String address) {
    	GeocodeCache gc = null;
       	try {
        	final String gUrl = BACKEND_SERVER_URL + "/itemProvider?type=geocode&address=" + URLEncoder.encode(address, "UTF-8") + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		JSONObject root = new JSONObject(gJson);
        		if (root.has("latitude") && root.has("longitude")) {
        			gc = jsonToGeocode(root);
        		} else if (root.has("error")) {
        			logger.log(Level.SEVERE, "Received following server error: " + root.getString("error"));
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return gc;
    }

    public static List<GeocodeCache> selectNewestGeocodes() {
    	List<GeocodeCache> gcl = new ArrayList<GeocodeCache>();    	
    	try {
    		final String limit = "10";
        	final String gUrl = BACKEND_SERVER_URL + "/itemProvider?type=geocode&limit=" + limit + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray root = new JSONArray(gJson);
        		for (int i=0;i<root.length();i++) {
        			JSONObject geocode = root.getJSONObject(i);
        			if (geocode.has("latitude") && geocode.has("longitude")) {
        				try {
        					GeocodeCache gc = jsonToGeocode(geocode);      				   
            				gcl.add(gc);
             			} catch (Exception e) {
             	        	logger.log(Level.SEVERE, e.getMessage(), e);
             	        }  
        			}
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return gcl;
    }

    public static GeocodeCache selectGeocodeCacheById(String id) {
        GeocodeCache gc = null;
        try {
        	final String gUrl = BACKEND_SERVER_URL + "/itemProvider?type=geocode&id=" + id + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		JSONObject root = new JSONObject(gJson);
        		if (root.has("latitude") && root.has("longitude")) {
        			gc = jsonToGeocode(root);
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return gc;
    }
    
    public static GeocodeCache selectGeocodeCacheByCoords(double lat, double lng) {
        GeocodeCache gc = null;
         try {
        	final String gUrl = BACKEND_SERVER_URL + "/itemProvider?type=geocode&lat=" + StringUtil.formatCoordE6(lat) + "&lng=" + StringUtil.formatCoordE6(lng) + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		JSONObject root = new JSONObject(gJson);
        		if (root.has("latitude") && root.has("longitude")) {
        			gc = jsonToGeocode(root);
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return gc;
    }
    
    private static GeocodeCache jsonToGeocode(JSONObject geocode) throws IllegalAccessException, InvocationTargetException {
		GeocodeCache gc = new GeocodeCache();
		   
		Map<String, String> geocodeMap = new HashMap<String, String>();
		for(Iterator<String> iter = geocode.keys();iter.hasNext();) {
				String key = iter.next();
				Object value = geocode.get(key);
				geocodeMap.put(key, value.toString());
		}
		   
		ConvertUtils.register(DateUtils.getRHCloudDateConverter(), Date.class);
		BeanUtils.populate(gc, geocodeMap);
		
		try {
			Date d = new Date(Long.parseLong(geocodeMap.get("creationDateLong")));
			gc.setCreationDate(d);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		   
		return gc;
	}
    
    public static void removeGeocode(int id) {
    	try {
        	final String gUrl = BACKEND_SERVER_URL + "/deleteItem";
        	final String params = "type=geocode&id=" + id + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		logger.log(Level.INFO, "Geocode " + id + " remove status: " + gJson);
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
