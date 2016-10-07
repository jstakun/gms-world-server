package net.gmsworld.server.layers;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

public abstract class OverpassUtils extends LayerHelper {

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static final String API_ENDPOINT = "http://overpass-api.de/api/interpreter?data="; 
	//bbox South-West-North-East  minimum latitude, minimum longitude, maximum latitude, maximum longitude
	
	@Override
	protected synchronized List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String amenity, String bbox, Locale locale, boolean useCache) throws Exception {
		List<ExtendedLandmark> output = new ArrayList<ExtendedLandmark>();
        
        if (bbox == null) {
        	double[] dcoords = new double[]{lat, lng, lat, lng};
        
        	if ((dcoords[3] - dcoords[1]) < 0.1) {
        		if (dcoords[3] < 180.0) {
        			dcoords[3] += 0.1;
        		}
        		if (dcoords[1] > -180.0) {
        			dcoords[1] -= 0.1;
        		}
        	}
        
        	if ((dcoords[2] - dcoords[0]) < 0.1) {
        		if (dcoords[2] < 90.0) {
        			dcoords[2] += 0.1;
        		}
        		if (dcoords[0] > -90.0) {
        			dcoords[0] -= 0.1;
        		}
        	}
        
        	bbox = String.format("%.2f", MathUtils.normalizeE2(dcoords[0])) + "," +
        		String.format("%.2f", MathUtils.normalizeE2(dcoords[1])) + "," +
        		String.format("%.2f", MathUtils.normalizeE2(dcoords[2])) + "," +
        		String.format("%.2f", MathUtils.normalizeE2(dcoords[3]));  
        }
        
        if (amenity != null && bbox != null) {
        	
            final String endpoint = API_ENDPOINT + URLEncoder.encode(String.format("[out:json][timeout:30];node[amenity=%1$s](%2$s);out meta qt %3$d;", amenity, bbox, limit), "UTF-8");
        	String response = HttpUtils.processFileRequest(new URL(endpoint));
        	
        	output = createCustomLandmarkList(response, locale, amenity);
        	
        } else {
            logger.log(Level.WARNING, "Parameters can't be null! Amenity: " + amenity + " , bbox: " + bbox);
        }
        return output;
	}

	@Override
	protected String getLayerName() {
		return null; //this needs to be implemented by super class
	}
	
	private List<ExtendedLandmark> createCustomLandmarkList(String responseJson, Locale locale, String amenity) throws JSONException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
    	
    	 if (StringUtils.startsWith(responseJson, "{")) {
             JSONObject jsonRoot = new JSONObject(responseJson);
             
             JSONArray elements = jsonRoot.optJSONArray("elements");
             
             if (elements != null) {
            	 for (int i = 0; i < elements.length(); i++) {
                	 JSONObject node = elements.getJSONObject(i);
                	 
                	 double lat = node.getDouble("lat");
                	 double lng = node.getDouble("lon");
                	 
                	 long creationDate = -1; //"timestamp": "2010-10-25T15:46:46Z",
                	 try {
                		 String timestamp = node.getString("timestamp").replace("T", " ").replace("Z", "");
                		 creationDate = DateUtils.parseDate(dateFormat, timestamp).getTime();
                	 } catch (Exception e) {              		 
                	 }
                	 
                	 JSONObject tags = node.getJSONObject("tags");
                	 
                	 String name = StringUtils.capitalize(amenity);
                	 if (tags.has("name")) {
                		 name = tags.getString("name");
                	 }
                	              	 
                	 AddressInfo address = new AddressInfo();
                     String val = tags.optString("addr:street");
                     if (StringUtils.isNotBlank(val)) {
                    	 String number = tags.optString("addr:housenumber");
                         if (StringUtils.isNotBlank(number)) {
                             val += " " + number;
                         }
                     	 address.setField(AddressInfo.STREET, val);
                     }	
                     val = tags.optString("addr:city");
                     if (StringUtils.isNotBlank(val)) {
                     	 address.setField(AddressInfo.CITY, val);
                     }
                     
                     Map<String, String> tokens = new HashMap<String, String>();
                     for (String key : tags.keySet()) {
                    	 if (!key.startsWith("addr:") && !key.equals("name") && !key.equals("amenity")) {
                    		 tokens.put(key, tags.getString(key));
                    	 }
                     }
                	 
                	 QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                     ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, getLayerName(), address, creationDate, null);
                     
                     String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                     landmark.setDescription(description);
         			
                     landmarks.add(landmark);
                 }
             }    
    	 
    	 } 
    	
    	 return landmarks;
	}	 
}
