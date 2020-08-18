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
import com.jstakun.lm.server.config.ConfigurationManager;
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
    			
    	public static JSONObject getFromServer(String lat_start, String lng_start, String lat_end, String lng_end, String type, String username) {
                JSONObject route = null;
                try {
                	route = GeocodeHelperFactory.getInstance().getRoute(lat_start, lng_start, lat_end, lng_end, type, username);
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
	            
	     public static String[] cache(String route, String name) {
	           String[] resp = new String[2];
	           try {
	        	    final String content = "type=route&name=" + name + "&route=" + route + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
	        	    final URL routesUrl = new URL(ConfigurationManager.getBackendUrl() + "/addItem");
		        	resp[0] = HttpUtils.processFileRequest(routesUrl, "POST", null, content, "application/x-www-form-urlencoded");
		        	//
		        	final Integer responseCode= HttpUtils.getResponseCode(routesUrl.toString()); 
		            if (responseCode != null) {
	            		resp[1] = responseCode.toString(); 
	            	}
		            logger.log(Level.INFO, "Cache response: " + resp[1] + ": " + resp[0]);
	           } catch (Exception e) {
	           		logger.log(Level.SEVERE, e.getMessage(), e);
	           }
	           return resp;
         }
	            
	     public static String loadFromCache(final String routeId, final String live) {
	           //check first if route is cached
	           String reply = null;
	           if (CacheUtil.containsKey(routeId)) {
	        	   FeatureCollection fc = (FeatureCollection) CacheUtil.getObject(routeId);
	        	   try {
	        		   byte[] routeBytes = new ObjectMapper().writeValueAsBytes(fc);
	        		   reply = new String(routeBytes);
	        	   } catch (Throwable e) {
	        		   logger.log(Level.SEVERE, e.getMessage(), e);
	        	   }
	           } else if (!StringUtils.equalsIgnoreCase(live, "true")) {
	        	   try {
	        		   final String routesUrl = ConfigurationManager.getBackendUrl() + "/itemProvider?type=route&name=" + routeId + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
	        		   reply = HttpUtils.processFileRequest(new URL(routesUrl));
	        		   final Integer responseCode = HttpUtils.getResponseCode(routesUrl.toString());
	        		   if (responseCode == null || responseCode != 200 || !StringUtils.startsWith(reply, "{") || !StringUtils.contains(reply, "features")) {
	        			   logger.log(Level.SEVERE, "Received following response from " + routesUrl + ": -" + reply + "-");
	        		   }
	        	   } catch (Exception e) {
	        		   logger.log(Level.SEVERE, e.getMessage() + " from response: " +  reply, e);
	        	   }
	           }
	           return reply;
	     }       
	     
	     public static boolean addRoutePointToCache(String routeId, double latitude, double longitude) {
	    	 FeatureCollection fc = null;
	    	 Feature f = null;
	    	 if (CacheUtil.containsKey(routeId)) {
	    		 fc = (FeatureCollection) CacheUtil.getObject(routeId);
	    	 }
	    	 if (fc != null) {
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
	    	 //testing
	    	 final int lsSize = ls.getCoordinates().size();
	    	 final LngLatAlt coord2 = new LngLatAlt(longitude, latitude);
    		 if (lsSize > 0) {
    			 final LngLatAlt coord1 = ls.getCoordinates().get(lsSize-1);
			     final double pointsDistance = NumberUtils.distanceInKilometer(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude())*1000d;
			     logger.log(Level.INFO, "Distance between points is " + pointsDistance + " meters");
			     if (pointsDistance >= 1d) {
			    	 ls.add(coord2);
			    	 Map<String, Object> props = f.getProperties();
    				 f.setProperty("time", System.currentTimeMillis() - (Long)props.get("creationTime"));
    				 double distance = 0d;
    				 if (props.containsKey("distance")) {
    					 try {
    						 distance = Double.parseDouble((String)props.get("distance"));
    					 } catch (Exception e) { 
    						 logger.log(Level.SEVERE, "Invalid distance value: " + props.get("distance"));
    					 }
    				 }
    				 distance += pointsDistance;
    				 f.setProperty("distance", distance);
    				 CacheUtil.put(routeId, fc, CacheType.LONG);
    				 return true;
			     } else {
			    	 return false;
			     }
    		 } else {
    			 ls.add(coord2);
    			 CacheUtil.put(routeId, fc, CacheType.LONG);
    			 return true;
    		 }
	    	 /*ls.add(new LngLatAlt(longitude, latitude));
    		 int lsSize = ls.getCoordinates().size();
    		 if (lsSize > 1) {
    			 try {
    				 Map<String, Object> props = f.getProperties();
    				 f.setProperty("time", System.currentTimeMillis() - (Long)props.get("creationTime"));
    				 double distance = 0d;
    				 if (props.containsKey("distance")) {
    					 distance = (Double)props.get("distance");
    				 }
    				 LngLatAlt coord1 = ls.getCoordinates().get(lsSize-2);
    				 LngLatAlt coord2 = ls.getCoordinates().get(lsSize-1);
    			     distance += NumberUtils.distanceInKilometer(coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude())*1000d;
    				 f.setProperty("distance", distance);
    			 } catch (Exception e) {
    				 logger.log(Level.SEVERE, e.getMessage(), e);
    			 }
    		 }
    		 //logger.log(Level.INFO, "Adding to cache route " + routeId + ":" + fc.toString());
	    	 CacheUtil.put(routeId, fc, CacheType.LONG);*/
	    	 
	     }
}
