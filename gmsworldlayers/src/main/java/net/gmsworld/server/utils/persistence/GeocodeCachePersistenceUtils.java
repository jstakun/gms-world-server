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

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

/**
 *
 * @author jstakun
 */
public class GeocodeCachePersistenceUtils {

    private static final Logger logger = Logger.getLogger(GeocodeCachePersistenceUtils.class.getName());
    private static final String BACKEND_SERVER_URL = "https://openapi-landmarks.b9ad.pro-us-east-1.openshiftapps.com/actions/";//"https://landmarks-gmsworld.rhcloud.com/actions/";//
    
    public static void persistGeocode(final String location, final double latitude, final double longitude) {
        if (StringUtils.isNotEmpty(location)) {
        	try {
        		String gUrl = BACKEND_SERVER_URL + "addItem";
        		String params = "type=geocode&latitude=" + StringUtil.formatCoordE6(latitude) + "&longitude=" + StringUtil.formatCoordE6(longitude) + 
        			"&address=" + URLEncoder.encode(location, "UTF-8");			 
        		String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        		logger.log(Level.INFO, "Received response: " + gJson);
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        	}
        }
    }

    public static GeocodeCache checkIfGeocodeExists(final String address) {
    	GeocodeCache gc = null;
       	try {
        	String gUrl = BACKEND_SERVER_URL + "itemProvider";
        	String params = "type=geocode&address=" + URLEncoder.encode(address, "UTF-8");			 
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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
    		String limit = "10";
        	String gUrl = BACKEND_SERVER_URL + "itemProvider";
        	String params = "type=geocode&limit=" + limit;			 
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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

    public static GeocodeCache selectGeocodeCache(String k) {
        GeocodeCache gc = null;
        try {
        	String gUrl = BACKEND_SERVER_URL + "itemProvider";
        	String params = "type=geocode&id=" + k;			 
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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
    
    public static GeocodeCache selectGeocodeCache(double lat, double lng) {
        GeocodeCache gc = null;
         try {
        	String gUrl = BACKEND_SERVER_URL + "itemProvider";
        	String params = "type=geocode&lat=" + StringUtil.formatCoordE6(lat) + "&lng=" + StringUtil.formatCoordE6(lng);			 
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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
}
