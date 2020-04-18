package net.gmsworld.server.layers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;

/**
 *
 * @author jstakun
 */
public class WebcamUtils extends LayerHelper {

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {
		//normalize radius to max 250
		int normalizedRadius = radius;
		if (normalizedRadius > 1000) {
			normalizedRadius = normalizedRadius /1000;
		}
		if (normalizedRadius > 250) {
			normalizedRadius = 250;
		}
		String url = "https://webcamstravel.p.rapidapi.com/webcams/list/nearby=" + lat + "," + lng + "," + normalizedRadius + "/limit=" + limit + "?show=webcams:basic,image,location,url&lang=" + locale.getLanguage();
		URL webcamUrl = new URL(url);
	    String webcamResponse = HttpUtils.processFileRequest(webcamUrl, "x-rapidapi-key", Commons.getProperty(Property.RAPIDAPI_KEY));
	    return createLandmarksWebcamList(webcamResponse, stringLimit, locale);        
	}
	
	private static List<ExtendedLandmark> createLandmarksWebcamList(String webcamJson, int stringLimit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        if (StringUtils.startsWith(webcamJson,"{")) {
            JSONObject jsonRoot = new JSONObject(webcamJson);
            if (StringUtils.equals(jsonRoot.optString("status") ,"OK")) {
            	JSONObject result = jsonRoot.getJSONObject("result");
            	int total = result.getInt("total");
            	if (total > 0) {
            		JSONArray webcams  = result.getJSONArray("webcams");
            		int size = webcams.length();
            		for (int i = 0; i < size; i++) {
            			JSONObject webcam = webcams.getJSONObject(i);

            			String name = webcam.getString("title");
            			
            			JSONObject location = webcam.getJSONObject("location");
            			double lat = location.getDouble("latitude");
            			double lng = location.getDouble("longitude");
            			QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
            			
            			JSONObject jurl = webcam.getJSONObject("url");
            			String url = jurl.getJSONObject("current").getString("mobile");
            			
            			Map<String, String> tokens = new HashMap<String, String>();

            			AddressInfo address = new AddressInfo();
            			String val = location.optString("city");
            			if (val != null) {
            				address.setField(AddressInfo.CITY, val);	
            			}
            			val = location.optString("country");
            			if (val != null) {
            				address.setField(AddressInfo.COUNTRY, val);	
            			}
            			val = location.optString("region");
            			if (val != null) {
            				address.setField(AddressInfo.STATE, val);	
            			}
                    
            			JSONObject image = webcam.getJSONObject("image");
            			long creationDate = image.getLong("update") * 1000;
                    
            			ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.WEBCAM_LAYER, address, creationDate, null);
            			landmark.setUrl(url); 
                    
            			landmark.setThumbnail(image.getJSONObject("current").getString("thumbnail"));
                    
            			String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
            			landmark.setDescription(description);
    				
            			landmarks.add(landmark);
            		}
            	}
            } else {
            	 logger.log(Level.SEVERE, "Received following server response: " + webcamJson);
            }
        }

        return landmarks;
    }
	
	public String getLayerName() {
		return Commons.WEBCAM_LAYER;
	}
	
	public String getIcon() {
		return "webcam.png";
	}
	
	public String getURI() {
		return "webcamProvider";
	}
}
