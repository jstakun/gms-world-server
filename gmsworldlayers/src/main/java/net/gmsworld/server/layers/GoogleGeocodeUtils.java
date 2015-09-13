package net.gmsworld.server.layers;

import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.openlapi.AddressInfo;

public class GoogleGeocodeUtils extends GeocodeHelper {

	@Override
	protected JSONObject processGeocode(String addressIn, String email, boolean persistAsLandmark) {
        JSONObject jsonResponse = new JSONObject();
        try {
            logger.log(Level.INFO, "Calling Google geocode: {0}", addressIn);
            URL geocodeUrl = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(addressIn, "UTF-8") + "&sensor=false");
            String geocodeResponse = HttpUtils.processFileRequest(geocodeUrl);
            if (geocodeResponse != null) {
                JSONObject json = new JSONObject(geocodeResponse);
                String status = json.getString("status");
                if (status.equals("OK")) {
                    JSONArray results = json.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject item = results.getJSONObject(0);
                        String address = item.getString("formatted_address");
                        JSONObject geometry = item.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        
                        String location_type = geometry.getString("location_type");
                        
                        logger.log(Level.INFO, "Geocode precision is " + location_type);
                        
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");

                        //jsonResponse = "{\"status\":\"OK\",\"lat\":\"" + lat + "\",\"lng\":\"" + lng + "\",\"type\":\"g\"}";
                        jsonResponse.put("status", "OK");
                        jsonResponse.put("lat", lat);
                        jsonResponse.put("lng", lng);
                        jsonResponse.put("type", "g");

                        try {
                           GeocodeCachePersistenceUtils.persistGeocode(addressIn, 0, null, lat, lng);

                           if (persistAsLandmark) {
                           //if (ConfigurationManager.getParam(ConfigurationManager.SAVE_GEOCODE_AS_LANDMARK, ConfigurationManager.OFF).equals(ConfigurationManager.ON)) {
                        	   LandmarkPersistenceUtils.persistLandmark(address, "", lat, lng, 0.0, "geocode", null, Commons.GEOCODES_LAYER, email);
                           }
                        } catch (Exception ex) {
                               logger.log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    } else {
                        jsonResponse.put("status", "Error");
                        jsonResponse.put("message", "No matching place found");
                        logger.log(Level.WARNING, "No matching place found");
                    }
                } else {
                    jsonResponse.put("status", "Error");
                    jsonResponse.put("message", status);
                    logger.log(Level.WARNING, "Error: {0}", status);
                }
            } else {
                jsonResponse.put("status", "Error");
                jsonResponse.put("message", "No response from geocode server");
                logger.log(Level.WARNING, "No response from geocode server");
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            //jsonResponse = "{\"status\":\"Error\",\"message\":\"Internal server error\"}";
            try {
                jsonResponse.put("status", "Error");
                jsonResponse.put("message", "Internal server error");
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return jsonResponse;
    }


	@Override
	protected AddressInfo processReverseGeocode(double lat, double lng) {
		String coords = StringUtil.formatCoordE6(lat) + "," + StringUtil.formatCoordE6(lng);
		final String key = getClass().getName() + coords;
		AddressInfo addressInfo = (AddressInfo) cacheProvider.getObject(key);

        if (addressInfo == null) {
        	addressInfo = new AddressInfo();
            try {
                URL geocodeUrl = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + coords + "&language=en");
                String geocodeResponse = HttpUtils.processFileRequest(geocodeUrl);
                if (geocodeResponse != null) {
                    JSONObject json = new JSONObject(geocodeResponse);
                    String status = json.getString("status");
                    if (status.equals("OK")) {
                        JSONArray results = json.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject item = results.getJSONObject(0);
                            addressInfo.setField(AddressInfo.EXTENSION, item.getString("formatted_address"));
                            
                            JSONArray address_components = item.getJSONArray("address_components");
                            for (int i = 0;i < address_components.length(); i++) {
                            	JSONObject address_component = address_components.getJSONObject(i);
                            	JSONArray types = address_component.getJSONArray("types");
                            	for (int j=0;j<types.length();j++) {
                            		String type = types.getString(j);
                            		if (StringUtils.equals(type, "country")) {
                            			addressInfo.setField(AddressInfo.COUNTRY, address_component.getString("short_name")); //long_name
                            		} else if (StringUtils.equals(type, "locality")) {
                            			addressInfo.setField(AddressInfo.CITY, address_component.getString("long_name"));
                            		} else if (StringUtils.equals(type, "route")) {
                            			addressInfo.setField(AddressInfo.STREET, address_component.getString("long_name"));
                            		} //else if (StringUtils.equals(type, "street_address")) {
                            		//} 
                            	}
                            }
                        }
                    } else {
                    	logger.log(Level.WARNING, "Received following Google reverse geocode response: " + status);
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

            if (StringUtils.isNotEmpty(addressInfo.getField(AddressInfo.EXTENSION))) {
            	cacheProvider.put(key, addressInfo);
            }
        } else {
            logger.log(Level.INFO, "Reading GRG geocode from cache with key {0}", addressInfo.getField(AddressInfo.EXTENSION));
        }

        return addressInfo;
	}

	@Override
	protected JSONObject getRoute(double lat_start, double lng_start, double lat_end, double lng_end, String type, String username) throws Exception {
		throw new Exception("Service not implemented");
	}

}
