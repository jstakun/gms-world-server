package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.GeocodeCachePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import com.openlapi.AddressInfo;

/**
 *
 * @author jstakun
 */
public class CloudmadeUtils extends GeocodeHelper {

    private static final String API_version = "0.3";
    //private static final String API_latest = "latest";
    private static final Logger logger = Logger.getLogger(CloudmadeUtils.class.getName());
    private static final char[] delim = new char[]{',',' '};

    protected JSONObject getRoute(double lat_start, double lng_start, double lat_end, double lng_end, String type, String username) throws Exception {
    	String key = getRouteKey(CloudmadeUtils.class, lat_start, lng_start, lat_end, lng_end, type, username);		
        String output = cacheProvider.getString(key);     
        String token = cacheProvider.getString(Commons.getProperty(Property.CLOUDMADE_TOKEN_KEY) + "_" + username);
        JSONObject json = null; 
        
        if (output == null) {
            if (token == null && username != null) {
                URL url = new URL("http://auth.cloudmade.com/token/" + Commons.getProperty(Property.CLOUDMADE_APIKEY) + "?userid=" + username);
                token = HttpUtils.processFileRequest(url, "POST", null, null);
            }

            if (token != null) {
                String routeString = "http://navigation.cloudmade.com/" + Commons.getProperty(Property.CLOUDMADE_APIKEY) + "/api/" + API_version + "/" + lat_start + "," + lng_start + "," + lat_end + "," + lng_end + "/" + type + ".js?tId=" + System.currentTimeMillis() + "&token=" + token;
                //System.out.println("calling: " + routeString);
                URL routeUrl = new URL(routeString);
                String resp = HttpUtils.processFileRequest(routeUrl, "GET", null, null);
                if (resp != null) {
                    json = new JSONObject(resp);
                    cacheProvider.put(key, output);
                    logger.log(Level.INFO, "Adding route to cache with key {0}", key);
                }
            }
        } else {
            logger.log(Level.INFO, "Reading route from cache with key {0}", key);
        }

        return json;
    }
    
    private static String getReverseGeocodeUrlV2(double lat, double lng, String token) {
    	String coords = lat + "," + lng;
    	return "http://geocoding.cloudmade.com/" + Commons.getProperty(Property.CLOUDMADE_APIKEY) + "/geocoding/v2/find.js?object_type=address&around=" + coords + "&distance=closest&return_location=true&results=1&token=" + token;
    }
    
    /*private static String getReverseGeocodeUrlV3(double lat, double lng, String token) {
    	String coords = lat + ";" + lng;
    	return "http://beta.geocoding.cloudmade.com/v3/" + Commons.getProperty(Property.CLOUDMADE_APIKEY) + "/api/geo.location.search.2?format=json&source=OSM&enc=UTF-8&limit=1&q=" + coords + "&token=" + token;
    }*/
    
    private static AddressInfo processReverseGeocodeV2(String resp, String key) throws JSONException {
    	AddressInfo addressInfo = new AddressInfo();
    	String address = "";
    	JSONObject json = new JSONObject(resp);

        if (json.has("found") && json.getInt("found") > 0) {
            JSONArray features = json.getJSONArray("features");
            JSONObject feature = features.getJSONObject(0);
            JSONObject location = feature.getJSONObject("location");
            JSONObject properties = feature.getJSONObject("properties");

            if (location.has("country")) {
                String country = location.getString("country");
                addressInfo.setField(AddressInfo.COUNTRY, country); 
                address += country + ", ";
            }
            if (location.has("city")) {
                String city = location.getString("city");
                addressInfo.setField(AddressInfo.CITY, city); 
                if (StringUtils.isNotEmpty(city)) {
                    address += city + ", ";
                }
            }
            if (properties.has("addr:street")) {
                String street = properties.getString("addr:street");
                addressInfo.setField(AddressInfo.STREET, street); 
                if (StringUtils.isNotEmpty(street)) {
                    address += street + " ";
                }
            }
            if (properties.has("addr:housenumber")) {
                String house = properties.getString("addr:housenumber");
                if (StringUtils.isNotEmpty(house)) {
                    address += house;
                }
            }
        } else {
            logger.log(Level.WARNING, "Received following response from Cloudmade: {0}", json.toString());
        }
        
        addressInfo.setField(AddressInfo.EXTENSION, address); 
        
        return addressInfo;
    }
    
    /*private static String processReverseGeocodeV3(String resp, String key) throws JSONException {
    	String address = "";
    	JSONObject json = new JSONObject(resp);
    	
    	JSONArray places = json.optJSONArray("places");
    	if (places != null && places.length() > 0) {
    		JSONObject place = places.getJSONObject(0);
    		
    		if (place.has("country")) {
                String country = place.getString("country");
                address += country + ", ";
            }
    		if (place.has("zip")) {
                String zip = place.getString("zip");
                if (StringUtils.isNotEmpty(zip)) {
                    address += zip + " ";
                }
            }
            if (place.has("city")) {
                String city = place.getString("city");
                if (StringUtils.isNotEmpty(city)) {
                    address += city + ", ";
                }
            }
            if (place.has("street")) {
                String street = place.getString("street");
                if (StringUtils.isNotEmpty(street)) {
                    address += street + " ";
                }
            }
            if (place.has("houseNumber")) {
                String house = place.getString("houseNumber");
                if (StringUtils.isNotEmpty(house)) {
                    address += house;
                }
            }
    		
    		CacheUtil.put(key, address);
            logger.log(Level.INFO, "Adding geocode to cache with key {0}", key);
    	} else {
    		logger.log(Level.WARNING, "Received following response from Cloudmade: {0}", json.toString());
    	}   	
    	return address;
    }*/

    private static String getGeocodeUrlV2(String location, String token) throws UnsupportedEncodingException {
    	return "http://geocoding.cloudmade.com/" + Commons.getProperty(Property.CLOUDMADE_APIKEY) + "/geocoding/v2/find.js?query=" + URLEncoder.encode(location, "UTF-8") + "&results=1&token=" + token;
    }
    
    private static JSONObject processGeocodeV2(String resp) throws JSONException {
    	JSONObject json = new JSONObject(resp);
    	JSONObject jsonResponse = new JSONObject();

        if (json.has("found") && json.getInt("found") > 0) {
            JSONArray features = json.getJSONArray("features");
            JSONObject feature = features.getJSONObject(0);
            JSONObject centroid = feature.getJSONObject("centroid");
            JSONArray coords = centroid.getJSONArray("coordinates");
            //jsonResponse = "{\"status\":\"OK\",\"lat\":\"" + lat + "\",\"lng\":\"" + lng + "\",\"type\":\"g\"}";

            double lat = coords.getDouble(0);
            double lng = coords.getDouble(1);

            jsonResponse.put("status", "OK");
            jsonResponse.put("lat", lat);
            jsonResponse.put("lng", lng);
            jsonResponse.put("type", "c");
        } else {
            jsonResponse.put("status", "Error");
            jsonResponse.put("message", "No matching place found");
            logger.log(Level.WARNING, "No matching place found");
        }
        
        return jsonResponse;
    }
    
    protected AddressInfo processReverseGeocode(double lat, double lng) throws Exception {
        String key = getClass().getName() + "_" + lat + "_" + lng;
        AddressInfo address = (AddressInfo) cacheProvider.getObject(key);

        if (address == null) {
            String token = cacheProvider.getString(Commons.getProperty(Property.CLOUDMADE_TOKEN_KEY));

            if (token == null) {
                URL url = new URL("http://auth.cloudmade.com/token/" + Commons.getProperty(Property.CLOUDMADE_APIKEY) + "?userid=" + Commons.getProperty(Property.CLOUDMADE_USERNAME));
                token = HttpUtils.processFileRequest(url, "POST", null, null);
            }

            if (token != null) {
                cacheProvider.put(Commons.getProperty(Property.CLOUDMADE_TOKEN_KEY), token);
                //String geocodeString = getReverseGeocodeUrlV3(lat, lng, token);
                String geocodeString = getReverseGeocodeUrlV2(lat, lng, token);
                URL geocodeUrl = new URL(geocodeString);
                String resp = HttpUtils.processFileRequest(geocodeUrl, "GET", null, null);
                //System.out.println(resp);
                if (StringUtils.startsWith(resp, "{")) {
                	address = processReverseGeocodeV2(resp, key);
                	//address = processReverseGeocodeV3(resp, key);
                }
            }
        } else {
            logger.log(Level.INFO, "Reading Cloudmade geocode from cache with key {0}", address);
        }
        
        if (address != null) {
        	cacheProvider.put(key, address);
        	logger.log(Level.INFO, "Adding geocode to cache with key {0}", key);
        }       
        
        return address;
    }

    public JSONObject processGeocode(String location, String email, boolean persistAsLandmark) {

        String token = cacheProvider.getString(Commons.getProperty(Property.CLOUDMADE_TOKEN_KEY));
        JSONObject jsonResponse = null;

        try {
            if (token == null) {
                URL url = new URL("http://auth.cloudmade.com/token/" + Commons.getProperty(Property.CLOUDMADE_APIKEY) + "?userid=" + Commons.getProperty(Property.CLOUDMADE_USERNAME));
                token = HttpUtils.processFileRequest(url, "POST", null, null);
            }

            if (token != null) {
                cacheProvider.put(Commons.getProperty(Property.CLOUDMADE_TOKEN_KEY), token);
                String geocodeString = getGeocodeUrlV2(location, token);
                URL geocodeUrl = new URL(geocodeString);
                String resp = HttpUtils.processFileRequest(geocodeUrl, "GET", null, null);
                if (StringUtils.startsWith(resp, "{")) {
                    jsonResponse = processGeocodeV2(resp);                  
                    if (jsonResponse.has("lat") && jsonResponse.has("lng")) {
                    	double lat = jsonResponse.getDouble("lat");
                		double lng = jsonResponse.getDouble("lng");
                    	
                    	try {
                    		GeocodeCachePersistenceUtils.persistGeocode(location, 0, "", lat, lng);

                    		if (persistAsLandmark) {
                    		//if (com.jstakun.lm.server.config.ConfigurationManager.getParam(ConfigurationManager.SAVE_GEOCODE_AS_LANDMARK, ConfigurationManager.OFF).equals(ConfigurationManager.ON)) {
                                Landmark l = new Landmark();
                    			l.setLatitude(lat);
                    			l.setLongitude(lng);
                                l.setName(WordUtils.capitalize(location, delim));
                    			l.setLayer(Commons.GEOCODES_LAYER);
                    			l.setUsername("geocode");
                    			if (email != null) {
                    				l.setEmail(email);
                    			}
                    			//TODO use layerloader
                    			LandmarkPersistenceUtils.persistLandmark(l);
                    			if (l.getId() > 0) {
                    				try {
                    					List<ExtendedLandmark> landmarks = LayerHelperFactory.getHotelsCombinedUtils().processBinaryRequest(l.getLatitude(), l.getLongitude(), null, 15, 1024, 300, StringUtil.getStringLengthLimit("l"), "en", null, Locale.US, false);
                    					LayerHelperFactory.getHotelsCombinedUtils().cacheGeoJson(landmarks, l.getLatitude(), l.getLongitude(), Commons.HOTELS_LAYER);                          
                    				} catch (Exception e) {
                    					logger.log(Level.SEVERE, e.getMessage(), e);
                    				}
                    			}
                            }
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            //jsonResponse = "{\"status\":\"Error\",\"message\":\"Internal server error\"}";
            try {
            	jsonResponse = new JSONObject().
                                put("status", "Error").
                                put("message", "Internal server error");
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        return jsonResponse;
    }
}
