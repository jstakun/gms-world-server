package com.jstakun.lm.server.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.ImageUtils;

public class RoutesUtils {
                
    			private static final int LIMIT = 64;
				
    			private static final Logger logger = Logger.getLogger(RoutesUtils.class.getName());
	
	            public static JSONObject getFromServer(String lat_start, String lng_start, String lat_end, String lng_end, String type, String username) {
                	JSONObject route = null;
                	try {
                		route = GeocodeHelperFactory.getMapQuestUtils().getRoute(lat_start, lng_start, lat_end, lng_end, type, username);
                		if (route != null) {
                			route.put("source", "MapQuest");
                			route.put("creationDate", System.currentTimeMillis());
                		}
                	} catch (Exception e) {
                         logger.log(Level.SEVERE, e.getMessage(), e); 		
                	}
                	return route;
                }
                
                public static void saveImage(String key, JSONObject route, boolean isSecure, double lat, double lng) throws IOException {
                		JSONArray route_geometry = route.optJSONArray("route_geometry");
                		if (route_geometry != null && route_geometry.length() > 1) {
                			int filter = route_geometry.length() / LIMIT + 1;
                			List<Double[]> path = new ArrayList<Double[]>(LIMIT);
                			
                			//remove every nth points to have up to LIMIT points
                			for (int i=0;i<route_geometry.length();i++) {
                	            if (i != 0 && i != (route_geometry.length()-1) && i % filter == 0) { 
                	            	JSONArray point = route_geometry.getJSONArray(i);	
                	            	path.add(new Double[]{point.getDouble(0), point.getDouble(1)});
                	            }
                			}
                			logger.log(Level.INFO, "Path has " + path.size() + " points");
                    
                			byte[] pathImage = ImageUtils.loadPath(path, "640x256", isSecure);
                			FileUtils.saveFileV2(key + ".jpg", pathImage, lat, lng);
                		}	
                }
	            
	            public static void cache(JSONObject route) {
	            	try {
	            		URL cacheUrl = new URL("http://hotels-gmsworldatoso.rhcloud.com/camel/v1/cache/routes");
	            		String resp = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "POST", null, route.toString(), "application/json; charset=utf-8", Commons.getProperty(Property.RH_GMS_USER));
	            		logger.log(Level.INFO, "Cache response: " + resp);
	            	} catch (Exception e) {
	            		logger.log(Level.SEVERE, e.getMessage(), e);
	            	}
                }
	            
	            public static JSONObject loadFromCache(String routeId) {
	            	JSONObject response = null;
	            	String reply = null;
	            	try {
	            		URL cacheUrl = new URL("http://hotels-gmsworldatoso.rhcloud.com/camel/v1/one/routes/_id/" + routeId);
	            		reply = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "GET", null, null, "application/json; charset=utf-8", Commons.getProperty(Property.RH_GMS_USER));
	            		if (HttpUtils.getResponseCode(cacheUrl.toString()) == 200 && StringUtils.startsWith(reply, "{")) {
	            			response = new JSONObject(reply);
	            		} else {
	            			logger.log(Level.SEVERE, "Cache response: -" + reply + "-");	
	            		}
	            	} catch (Exception e) {
	            		logger.log(Level.SEVERE, e.getMessage() + " from response: " +  reply, e);
	            	}
	            	return response;
	            }
                
                
}