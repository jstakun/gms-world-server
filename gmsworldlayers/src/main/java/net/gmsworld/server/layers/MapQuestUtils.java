package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.openlapi.AddressInfo;

public class MapQuestUtils extends GeocodeHelper {

	private static final char[] delim = new char[]{',',' '};
	
	@Override
	public JSONObject processGeocode(String location, String email) {
		String urlString = "http://open.mapquestapi.com/geocoding/v1/address?key=" + Commons.getProperty(Property.MAPQUEST_APPKEY) + "&location=" + location;
		//System.out.println(urlString);
		JSONObject jsonResponse = null;

        try {
        	URL routeUrl = new URL(urlString);
        	String resp = HttpUtils.processFileRequest(routeUrl, "GET", null, null);
        	if (StringUtils.startsWith(resp, "{")) {
        		JSONObject root = new JSONObject(resp);
        		JSONArray results = root.getJSONArray("results");
        		if (results.length() > 0) {
        			JSONObject first = results.getJSONObject(0);
        			JSONArray locations = first.getJSONArray("locations");
        			if (locations.length() > 0) {
        				JSONObject locationJson = locations.getJSONObject(0);
        				JSONObject latLng = locationJson.getJSONObject("latLng");
            		
        				double lat = latLng.getDouble("lat");
        				double lng = latLng.getDouble("lng");
                	
        				try {
        					GeocodeCachePersistenceUtils.persistGeocode(location, 0, "", lat, lng);
        					//if (ConfigurationManager.getParam(ConfigurationManager.SAVE_GEOCODE_AS_LANDMARK, ConfigurationManager.OFF).equals(ConfigurationManager.ON)) {
        					String name = WordUtils.capitalize(location, delim);
        					LandmarkPersistenceUtils.persistLandmark(name, "", lat, lng, 0.0, "geocode", null, Commons.GEOCODES_LAYER, email);
        					//}
        				} catch (Exception ex) {
        					logger.log(Level.SEVERE, ex.getMessage(), ex);
        				}
                	
        				jsonResponse = new JSONObject();
        				jsonResponse.put("status", "OK");
        	            jsonResponse.put("lat", lat);
        	            jsonResponse.put("lng", lng);
        	            jsonResponse.put("type", "m");
        			}
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response " + resp);
        	}
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            try {
            	jsonResponse = new JSONObject().
                                put("status", "Error").
                                put("message", "Internal server error");
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        if (jsonResponse == null) {
        	jsonResponse = new JSONObject().
                    put("status", "Error").
                    put("message", "No geocode found");
        }
        
        return jsonResponse;
	}

	@Override
	public String processReverseGeocode(double lat, double lng) throws IOException {
		String normLat = StringUtil.formatCoordE6(lat);
		String normLng = StringUtil.formatCoordE6(lng);
		String key = getClass().getName() + "_" + normLat + "_" + normLng;
		String address = cacheProvider.getString(key);
		
		if (address == null) {
			String urlString = "http://open.mapquestapi.com/geocoding/v1/reverse?key=" + Commons.getProperty(Property.MAPQUEST_APPKEY) + "&location=" + normLat + "," + normLng;
			//System.out.println(urlString);
			URL routeUrl = new URL(urlString);
            String resp = HttpUtils.processFileRequest(routeUrl, "GET", null, null);
            if (StringUtils.startsWith(resp, "{")) {
                JSONObject root = new JSONObject(resp);
                JSONArray results = root.getJSONArray("results");
                if (results.length() > 0) {
                	JSONObject first = results.getJSONObject(0);
                	JSONArray locations = first.getJSONArray("locations");
                	if (locations.length() > 0) {
                		JSONObject location = locations.getJSONObject(0);
                		
                		//street 	Street address 	
                		//adminArea5 	City name 	
                		//adminArea4 	County name 	
                		//adminArea3 	State name 	
                		//adminArea1 	Country name 	
                		//postalCode 	Postal code
                		
                		AddressInfo addressInfo = new AddressInfo();
                		
                		if (location.has("street")) {
                			addressInfo.setField(AddressInfo.STREET, location.getString("street"));
                		}
                		if (location.has("adminArea5")) {
                			addressInfo.setField(AddressInfo.CITY, location.getString("adminArea5"));
                		}
                		if (location.has("adminArea4")) {
                			addressInfo.setField(AddressInfo.COUNTY, location.getString("adminArea4"));
                		}
                		if (location.has("adminArea3")) {
                			addressInfo.setField(AddressInfo.STATE, location.getString("adminArea3"));
                		}
                		if (location.has("adminArea1")) {
                			addressInfo.setField(AddressInfo.COUNTRY, location.getString("adminArea1"));
                		}
                		if (location.has("postalCode")) {
                			addressInfo.setField(AddressInfo.POSTAL_CODE, location.getString("postalCode"));
                		}
                		
                		address = JSONUtils.formatAddress(addressInfo);
                		
                		if (location.has("geocodeQuality")) {
                			logger.log(Level.INFO, "Found reverse geocode with quality {0}", location.getString("geocodeQuality"));
                		}	
                	}
                }
            } else {
           	 	logger.log(Level.SEVERE, "Received following server response " + resp);
            }
		} else {
			logger.log(Level.INFO, "Reading MapQuest reverse geocode from cache with key {0}", address);
		}
		
		return address;
	}

	@Override
	public JSONObject getRoute(double lat_start, double lng_start, double lat_end, double lng_end, String type, String username) throws IOException {
		JSONObject response = null; 
		String key = getRouteKey(MapQuestUtils.class, lat_start, lng_start, lat_end, lng_end, type, username);
		String output = cacheProvider.getString(key);
		
		//car/fastest
		//car/shortest
		//foot
				
		//fastest - Quickest drive time route.
		//shortest - Shortest driving distance route.
		//pedestrian - Walking route; Avoids limited access roads; Ignores turn restrictions.
		//multimodal - Combination of walking and (if available) Public Transit.
		//bicycle - Bike route; Avoids limited access roads; Avoids roads where bicycle access is false; Favors bike specific paths and lower maxspeed roads
		
		String normalizedType = "fastest";
		if (StringUtils.equals(type, "car/shortest")) {
			normalizedType = "shortest";
		} else if (StringUtils.equals(type, "foot")) {
			normalizedType = "pedestrian";
		}
		
		if (output == null) {
			 response = new JSONObject();
			 String routeUrlString = "http://open.mapquestapi.com/directions/v2/route?key=" + Commons.getProperty(Property.MAPQUEST_APPKEY)+ "&ambiguities=ignore&from=" + lat_start + "," + lng_start + "&to=" + lat_end + "," + lng_end + "&routeType=" + normalizedType + "&shapeFormat=raw&fullShape=true&narrativeType=none&unit=k&doReverseGeocode=false&generalize=0";
			 //System.out.print(routeUrlString);
			 URL routeUrl = new URL(routeUrlString);
             String resp = HttpUtils.processFileRequest(routeUrl, "GET", null, null);
             if (StringUtils.startsWith(resp, "{")) {
                 JSONObject root = new JSONObject(resp);
                 JSONObject route = root.getJSONObject("route");
                 if (route.has("shape")) {
                	 JSONObject shape = route.getJSONObject("shape");
                	 //logger.log(Level.INFO, "Shape: " + shape.toString());
                	 JSONArray shapePoints = shape.getJSONArray("shapePoints");
                	 if (shapePoints.length() > 0) {
                		 List<double[]> route_geometry = new ArrayList<double[]>(shapePoints.length()/2);
                		 for (int i=0;i<shapePoints.length();i+=2) {
                			 double[] point = new double[] {shapePoints.getDouble(i),shapePoints.getDouble(i+1)};
                			 route_geometry.add(point);
                		 }
                		 response.put("route_geometry", route_geometry);
                		 response.put("status", 0);
                		 JSONObject route_summary = new JSONObject();
                		 route_summary.put("total_distance", (int)(route.getDouble("distance")*1000));
                		 route_summary.put("total_time", route.getInt("time")); //seconds
                		 response.put("route_summary", route_summary);
                		 cacheProvider.put(key, response.toString());
                		 logger.log(Level.INFO, "Adding route to cache with key {0}", key);
                	 }
                 } else {
                	 logger.log(Level.SEVERE, "Received following response:\n{0}", resp);
                	 response.put("status", 1);
                	 response.put("status_message", "No route received");
                 }
             } else {
            	 logger.log(Level.SEVERE, "Received following server response " + resp);
             }
		} else {
			response = new JSONObject(output);
            logger.log(Level.INFO, "Reading route from cache with key {0}", key);
        }
		
		return response;
	}
   
}
