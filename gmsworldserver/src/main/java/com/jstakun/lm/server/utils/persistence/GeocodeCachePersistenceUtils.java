/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

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

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.GeocodeCache;
import com.jstakun.lm.server.utils.DateUtils;
import com.jstakun.lm.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class GeocodeCachePersistenceUtils {

    private static final Logger logger = Logger.getLogger(GeocodeCachePersistenceUtils.class.getName());
    
    public static void persistGeocode(String location, int status, String message, double latitude, double longitude) {
        /*String loc = StringUtils.replace(location, "\n", " ");
        
        GeocodeCache gc = new GeocodeCache(loc, status, message, latitude, longitude);

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(gc);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	try {
        	String gUrl = "http://landmarks-gmsworld.rhcloud.com/actions/addItem";
        	String params = "type=geocode&latitude=" + latitude + "&longitude=" + longitude + 
        			"&address=" + URLEncoder.encode(location, "UTF-8");			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequest(new URL(gUrl), "POST", null, params);
        	logger.log(Level.INFO, "Received response: " + gJson);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static GeocodeCache checkIfGeocodeExists(String address) {
    	GeocodeCache gc = null;
        /*PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Query cacheQuery = pm.newQuery(GeocodeCache.class);
            cacheQuery.setFilter("location == address && status == 0");
            cacheQuery.declareParameters("String address");
            List<GeocodeCache> gcl = (List<GeocodeCache>) cacheQuery.execute(address);

            if (!gcl.isEmpty()) {
                gc = gcl.get(0);
                gc.setCreationDate(new Date(System.currentTimeMillis()));
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	try {
        	String gUrl = "http://landmarks-gmsworld.rhcloud.com/actions/itemProvider";
        	String params = "type=geocode&address=" + URLEncoder.encode(address, "UTF-8");			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequest(new URL(gUrl), "POST", null, params);
        	//logger.log(Level.INFO, "Received response: " + gJson);
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

    public static List<GeocodeCache> selectNewestGeocodes() {
    	List<GeocodeCache> gcl = new ArrayList<GeocodeCache>();
        /*PersistenceManager pm = PMF.get().getPersistenceManager();
        
        try {
            String lastStr = ConfigurationManager.getParam(ConfigurationManager.NUM_OF_GEOCODES, "5");
            int last = Integer.parseInt(lastStr);
            if (last > 0) {
                Query cacheQuery = pm.newQuery(GeocodeCache.class);
                cacheQuery.setFilter("status == 0");
                cacheQuery.setRange(0, last);
                cacheQuery.setOrdering("creationDate desc");
                gcl = (List<GeocodeCache>) cacheQuery.execute();
                pm.retrieveAll(gcl);
                gcl = (List<GeocodeCache>) pm.detachCopyAll(gcl);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	
    	try {
    		String limit = ConfigurationManager.getParam(ConfigurationManager.NUM_OF_GEOCODES, "10");
        	String gUrl = "http://landmarks-gmsworld.rhcloud.com/actions/itemProvider";
        	String params = "type=geocode&limit=" + limit;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequest(new URL(gUrl), "POST", null, params);
        	//logger.log(Level.INFO, "Received response: " + gJson);
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
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Key key = KeyFactory.stringToKey(k);
            geocodeCache = pm.getObjectById(GeocodeCache.class, key);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
        try {
        	String gUrl = "http://landmarks-gmsworld.rhcloud.com/actions/itemProvider";
        	String params = "type=geocode&id=" + k;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequest(new URL(gUrl), "POST", null, params);
        	//logger.log(Level.INFO, "Received response: " + gJson);
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
		   
		return gc;
	}
}
