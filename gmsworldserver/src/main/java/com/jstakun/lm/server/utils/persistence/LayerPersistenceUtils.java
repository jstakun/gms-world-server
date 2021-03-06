package com.jstakun.lm.server.utils.persistence;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.util.LabelValueBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Layer;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

/**
 *
 * @author jstakun
 */
public class LayerPersistenceUtils {

    private static final Logger logger = Logger.getLogger(LayerPersistenceUtils.class.getName());

    public static List<LabelValueBean> selectAllLayers() {
        List<LabelValueBean> results = new ArrayList<LabelValueBean>();
        List<Layer> allLayers = getAllLayers();
		for (Layer l : allLayers) {
			results.add(new LabelValueBean(l.getFormatted(), l.getName()));
		}
        
        return results;
    }

    public static List<Layer> listAllLayers(final int version) {
    	final String key = LayerPersistenceUtils.class.getName() + "_listAllLayers_" + version;
    	CacheAction layersCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			public Object executeAction() {
				List<Layer> allLayers = getAllLayers();
				if (allLayers != null) {
					List<Layer> result = new ArrayList<Layer>();
					for (Layer l : allLayers) {
	    				if (l.geVersion() > 0 && l.geVersion() <= version) {
	    					result.add(l);
	    				}
	    			}
					return result;
				} else {
					return null;
				}
			}
		});
	    return layersCacheAction.getListFromCache(Layer.class, key, CacheType.NORMAL);
    }

    public static void persist(String name, String desc, boolean enabled, boolean manageable, boolean checkinable, String formatted) {
        try {
        	final String landmarksUrl = ConfigurationManager.getBackendUrl() + "/addItem";
        	final String params = "name=" + URLEncoder.encode(name, "UTF-8") + "&desc=" + URLEncoder.encode(desc, "UTF-8") + 
        			        "&formatted=" + URLEncoder.encode(formatted, "UTF-8") + "&type=layer" +
        			        "&e=" + enabled + "&m=" + manageable + "&c=" + checkinable;
        	final String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl + "?" + params));
        	logger.log(Level.INFO, "Received response: " + landmarksJson);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static String getLayerFormattedName(final String name) {
    	final String key = "layer_" + name;
    	CacheAction layersCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			public Object executeAction() {
				List<Layer> allLayers = getAllLayers();
	    		for (Layer l : allLayers) {
	    		    if (StringUtils.equals(l.getName(), name)) {
	    		    	return l.getFormatted();
	    		    }
	    		}
	    		return null;
			}
    	});	   	
    	
    	String resp = (String) layersCacheAction.getObjectFromCache(key, CacheType.NORMAL);
        if (StringUtils.isEmpty(resp)) {
        	resp = name;
        }
        
    	return resp;
    }
    
    private static List<Layer> getAllLayers() {
    	final String key = LayerPersistenceUtils.class.getName() + "_getAllLayers";
    	CacheAction layersCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			public Object executeAction() {
				try {
					List<Layer> layers = new ArrayList<Layer>();
					final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
					final String params = "type=layer&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
					final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
					if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
						JSONArray root = new JSONArray(gJson);
						for (int i=0;i<root.length();i++) {
							JSONObject layer = root.getJSONObject(i);
							Layer l = new Layer();
							Map<String, String> layerMap = new HashMap<String, String>();
							for(Iterator<String> iter = layer.keys();iter.hasNext();) {
								String key = iter.next();
								Object value = layer.get(key);
								layerMap.put(key, value.toString());
							}
        			
							BeanUtils.populate(l, layerMap);
							layers.add(l);
						}
						return layers;
					} else {
						logger.log(Level.SEVERE, "Received following server response: " + gJson);
						return null;
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					return null;
				}	
			}
		});
	    return layersCacheAction.getListFromCache(Layer.class, key, CacheType.NORMAL);
    }
    
    public static String createCustomJSonLayersList(List<Layer> layerList, double latitude, double longitude, int radius) throws JSONException {
    	ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
    	Map<String, Integer> count = LandmarkPersistenceUtils.countLandmarksByCoords(latitude, longitude, radius);
    	for (Layer layer : layerList) {
    		Map<String, Object> jsonObject = new HashMap<String, Object>();
    		String name = layer.getName();
    		jsonObject.put("name", name);
    		jsonObject.put("desc", layer.getDesc());
    		jsonObject.put("formatted", layer.getFormatted());
    		jsonObject.put("iconURI", net.gmsworld.server.config.ConfigurationManager.SERVER_URL + "images/" + layer.getName() + ".png");
    		jsonObject.put("manageable", layer.isManageable());
    		jsonObject.put("enabled", layer.isEnabled());
    		jsonObject.put("checkinable", layer.isCheckinable());
    		boolean isEmpty = (count.containsKey(name) ? (count.get(name) == 0) : true);
    		jsonObject.put("isEmpty", isEmpty);
    		jsonArray.add(jsonObject);
    	}
    	return JSONUtils.getJsonArrayObject(jsonArray);
    }
    	
    public static String createCustomJSonLayersList(List<Layer> layerList, double latitudeMin, double longitudeMin, double latitudeMax, double longitudeMax) throws JSONException {
    	List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
    	double latitude = (latitudeMin + latitudeMax) / 2;
    	double longitude = (longitudeMin + longitudeMax) / 2;
    	int radius = (int)(NumberUtils.distanceInKilometer(latitudeMin, latitudeMax, longitudeMin, longitudeMax) * 1000 / 2);
    	Map<String, Integer> count = LandmarkPersistenceUtils.countLandmarksByCoords(latitude, longitude, radius);
    	for (Layer layer : layerList) {
    		Map<String, Object> jsonObject = new HashMap<String, Object>();
    		String name = layer.getName();
    		jsonObject.put("name", name);
    		jsonObject.put("desc", layer.getDesc());
    		jsonObject.put("formatted", layer.getFormatted());
    		jsonObject.put("iconURI",  net.gmsworld.server.config.ConfigurationManager.SERVER_URL + "images/" + name + ".png");
    		jsonObject.put("manageable", layer.isManageable());
    		jsonObject.put("enabled", layer.isEnabled());
    		jsonObject.put("checkinable", layer.isCheckinable());
    		boolean isEmpty = (count.containsKey(name) ? (count.get(name) == 0) : true);
    		jsonObject.put("isEmpty", isEmpty);
    		jsonArray.add(jsonObject);
    	}
    	return JSONUtils.getJsonArrayObject(jsonArray);
    }	
}
