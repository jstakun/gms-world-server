/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class WebcamUtils extends LayerHelper {

    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
        JSONObject json = null;
        String output = cacheProvider.getString(key);

        if (output == null) {
            URL webcamUrl = new URL("http://api.webcams.travel/rest?"
                    + "method=wct.webcams.list_nearby&devid=" + Commons.getProperty(Property.WEBCAM_API_KEY)
                    + "&lat=" + lat + "&lng=" + lng + "&radius=" + radius
                    + "&unit=km&format=json&per_page=" + limit);

            String webcamResponse = HttpUtils.processFileRequest(webcamUrl);

            json = createCustomJsonWebcamList(webcamResponse, stringLimit, version);
            if (json.getJSONArray("ResultSet").length() > 0) {
                cacheProvider.put(key, json.toString());
                logger.log(Level.INFO, "Adding WC landmark list to cache with key {0}", key);
            }

        } else {
            logger.log(Level.INFO, "Reading WC landmark list from cache with key {0}", key);
            json = new JSONObject(output);
        }

        return json;
    }

    private static JSONObject createCustomJsonWebcamList(String webcamJson, int stringLimit, int version) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        if (StringUtils.startsWith(webcamJson,"{")) {
            JSONObject jsonRoot = new JSONObject(webcamJson);
            JSONObject webcams = jsonRoot.getJSONObject("webcams");
            int count = webcams.getInt("count");
            if (count > 0) {
                JSONArray items = webcams.getJSONArray("webcam");
                int size = items.length();
                for (int i = 0; i < size; i++) {
                    JSONObject webcam = items.getJSONObject(i);

                    Map<String, Object> jsonObject = new HashMap<String, Object>();

                    jsonObject.put("name", webcam.getString("title"));
                    jsonObject.put("lat", webcam.getString("latitude"));
                    jsonObject.put("lng", webcam.getString("longitude"));
                    jsonObject.put("url", webcam.getString("url"));

                    Map<String, String> desc = new HashMap<String, String>();

                    JSONUtils.putOptValue(desc, "city", webcam, "city", false, stringLimit, false);
                    JSONUtils.putOptValue(desc, "country", webcam, "country", false, stringLimit, false);
                    desc.put("creationDate", Long.toString(webcam.getLong("last_update") * 1000));
                    if (version > 1) {
                        desc.put("icon", webcam.getString("thumbnail_url"));
                    }
                    jsonObject.put("desc", desc);

                    jsonArray.add(jsonObject);
                }
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {
		 String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
		 List<ExtendedLandmark> output = (List<ExtendedLandmark>)cacheProvider.getObject(key);

	     if (output == null) {
	            URL webcamUrl = new URL("http://api.webcams.travel/rest?"
	                    + "method=wct.webcams.list_nearby&devid=" + Commons.getProperty(Property.WEBCAM_API_KEY)
	                    + "&lat=" + lat + "&lng=" + lng + "&radius=" + radius
	                    + "&unit=km&format=json&per_page=" + limit);

	            String webcamResponse = HttpUtils.processFileRequest(webcamUrl);

	            output = createLandmarksWebcamList(webcamResponse, stringLimit, locale);
	            if (!output.isEmpty()) {
	                cacheProvider.put(key, output);
	                logger.log(Level.INFO, "Adding WC landmark list to cache with key {0}", key);
	            }

	     } else {
	        logger.log(Level.INFO, "Reading WC landmark list from cache with key {0}", key);
	     }
	     logger.log(Level.INFO, "Returning " + output.size() + " landmarks ...");

	     return output;
	}
	
	private static List<ExtendedLandmark> createLandmarksWebcamList(String webcamJson, int stringLimit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        if (StringUtils.startsWith(webcamJson,"{")) {
            JSONObject jsonRoot = new JSONObject(webcamJson);
            JSONObject webcams = jsonRoot.getJSONObject("webcams");
            int count = webcams.getInt("count");
            if (count > 0) {
                JSONArray items = webcams.getJSONArray("webcam");
                int size = items.length();
                for (int i = 0; i < size; i++) {
                    JSONObject webcam = items.getJSONObject(i);

                    String name = webcam.getString("title");
                    double lat = webcam.getDouble("latitude");
                    double lng = webcam.getDouble("longitude");
                    String url = webcam.getString("url");

                    Map<String, String> tokens = new HashMap<String, String>();

                    AddressInfo address = new AddressInfo();
                    String val = webcam.optString("city");
                    if (val != null) {
                    	address.setField(AddressInfo.CITY, val);	
                    }
                    val = webcam.optString("country");
                    if (val != null) {
                    	address.setField(AddressInfo.COUNTRY, val);	
                    }
                    
                    long creationDate = webcam.getLong("last_update") * 1000;
                    
                    QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                    ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.WEBCAM_LAYER, address, creationDate, null);
                    landmark.setUrl(url); 
                    
                    landmark.setThumbnail(webcam.getString("thumbnail_url"));
                    
                    String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                    landmark.setDescription(description);
    				
                    landmarks.add(landmark);
                }
            }
        }

        return landmarks;
    }
}
