package com.jstakun.lm.server.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.ImageUtils;
import net.gmsworld.server.utils.NumberUtils;

public class RoutesUtils {
                
    	private static final int LIMIT = 64;
				
    	private static final Logger logger = Logger.getLogger(RoutesUtils.class.getName());
    			
    	private static final String BACKEND_URL =  "https://routes-api.b9ad.pro-us-east-1.openshiftapps.com";//"http://openapi-hotels.b9ad.pro-us-east-1.openshiftapps.com";
    			
    	private static final String ROUTES_URL = BACKEND_URL + "/camel/v1/cache/features/routes";
	
    	private static final String ROUTE_URL_NAME = BACKEND_URL + "/camel/v1/one/routes/name/";
    			
    	private static final String ROUTE_URL_ID = BACKEND_URL + "/camel/v1/getById/routes/"; //"/camel/v1/one/routes/_id/" ;
    							
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
                	FileUtils.saveFileV2(null, key + ".jpg", pathImage, lat, lng);
               }	
         }
	            
	     public static String[] cache(String route) {
	           String[] resp = new String[2];
	           try {
	        	    URL cacheUrl = new URL(ROUTES_URL + "?user_key=" + Commons.getProperty(Property.RH_ROUTES_API_KEY));
	            	resp[0] = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "POST", null, route, "application/json; charset=utf-8", Commons.getProperty(Property.RH_GMS_USER));
	            	Integer responseCode= HttpUtils.getResponseCode(cacheUrl.toString());
	            	if (responseCode != null) {
	            		resp[1] = responseCode.toString(); 
	            	}
	            	logger.log(Level.INFO, "Cache response: " + resp[1] + ": " + resp[0]);
	           } catch (Exception e) {
	           		logger.log(Level.SEVERE, e.getMessage(), e);
	           }
	           return resp;
         }
	            
	     public static String loadFromCache(String routeId) {
	           //check first if route is cached
	           String reply = null;
	           if (CacheUtil.containsKey(routeId)) {
	        	   FeatureCollection fc = (FeatureCollection) CacheUtil.getObject(routeId);
	        	   try {
	        		   reply = new ObjectMapper().writeValueAsString(fc);
	        	   } catch (Exception e) {
	        		   logger.log(Level.SEVERE, e.getMessage(), e);
	        	   }
	           } else {
	        	   try {
	        		   URL cacheUrl = new URL(ROUTE_URL_NAME + routeId + "?user_key=" + Commons.getProperty(Property.RH_ROUTES_API_KEY));
	        		   reply = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "GET", null, null, "application/json; charset=utf-8", Commons.getProperty(Property.RH_GMS_USER));
	        		   Integer responseCode = HttpUtils.getResponseCode(cacheUrl.toString());
	        		   if (responseCode == null || responseCode != 200 || !StringUtils.startsWith(reply, "{")) {
	        			   logger.log(Level.SEVERE, "Received following response from " + cacheUrl.toString() + ": -" + reply + "-");
	        			   cacheUrl = new URL(ROUTE_URL_ID  + routeId + "?user_key=" + Commons.getProperty(Property.RH_ROUTES_API_KEY));
	        			   reply = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "GET", null, null, "application/json; charset=utf-8", Commons.getProperty(Property.RH_GMS_USER));
	        			   responseCode = HttpUtils.getResponseCode(cacheUrl.toString());
	        	           if (responseCode == null || responseCode != 200 || !StringUtils.startsWith(reply, "{")) {
	        				   logger.log(Level.SEVERE, "Received following response from " + cacheUrl.toString() + ": -" + reply + "-");
	        			   }	
	        		   }
	        	   } catch (Exception e) {
	        		   logger.log(Level.SEVERE, e.getMessage() + " from response: " +  reply, e);
	        	   }
	           }
	           return reply;
	     }       
	     
	     public static void addRoutePointToCache(String routeId, double latitude, double longitude) {
	    	 FeatureCollection fc = null;
	    	 Feature f = null;
	    	 if (CacheUtil.containsKey(routeId)) {
	    		 fc = (FeatureCollection) CacheUtil.getObject(routeId);
	    	     f = fc.getFeatures().get(0);
	    	 } else {
	    		 fc = new FeatureCollection();
	    		 f = new Feature();
	    		 f.setGeometry(new LineString());
	    		 f.setProperty("description", "Currently recorder route...");
	    		 f.setProperty("name", routeId);
	    		 f.setProperty("creationTime", System.currentTimeMillis());
	    		 fc.add(f);
	    	 }
	    	 LineString ls = (LineString) f.getGeometry();
    		 ls.add(new LngLatAlt(longitude, latitude));
    		 int lsSize = ls.getCoordinates().size();
    		 if (lsSize > 1) {
    			 try {
    				 Map<String, Object> props = f.getProperties();
    				 f.setProperty("time", System.currentTimeMillis() - (long)props.get("creationTime"));
    				 double distance = 0d;
    				 if (props.containsKey("distance")) {
    					 distance = (double)props.get("distance");
    				 }
    				 LngLatAlt coord1 = ls.getCoordinates().get(lsSize-2);
    				 LngLatAlt coord2 = ls.getCoordinates().get(lsSize-1);
    			     distance += NumberUtils.distanceInKilometer(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude())*1000d;
    				 f.setProperty("distance", distance);
    			 } catch (Exception e) {
    				 logger.log(Level.SEVERE, e.getMessage(), e);
    			 }
    		 }
    		 //logger.log(Level.INFO, "Adding to cache new route: " + routeId + ":" + fc.toString());
	    	 CacheUtil.put(routeId, fc, CacheType.LONG);
	     }
}
