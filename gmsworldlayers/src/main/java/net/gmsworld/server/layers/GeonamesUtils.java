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
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.NumberUtils;

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
public class GeonamesUtils extends LayerHelper {

    private static final int MAXROWS = 30;

    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String lang, String flexString2) throws Exception {
    	JSONObject json = null;
    	if (isEnabled()) {
    		int r = NumberUtils.normalizeNumber(radius, 1, 20);
    		String key = getCacheKey(getClass(), "processRequest", lat, lng, query, r, version, limit, stringLimit, lang, flexString2);
    		String output = cacheProvider.getString(key);
        	if (output == null) {
            	URL geonamesUrl = new URL("http://api.geonames.org/findNearbyWikipediaJSON?lat=" + lat + "&lng=" + lng + "&maxRows=" + MAXROWS + "&radius=" + r + "&username=" + Commons.getProperty(Property.GEONAMES_USERNAME) + "&lang=" + lang);
            	String geonamesResponse = HttpUtils.processFileRequest(geonamesUrl);

            	json =  createCustomJSonGeonamesList(geonamesResponse, version, limit, stringLimit);

            	if (json.getJSONArray("ResultSet").length() > 0) {
                	cacheProvider.put(key, json.toString());
                	logger.log(Level.INFO, "Adding GN landmark list to cache with key {0}", key);
            	}
            } else {
            	logger.log(Level.INFO, "Reading GN landmark list from cache with key {0}", key);
            	json = new JSONObject(output);
        	}
    	} else {
    		json = new JSONObject().put("ResultSet", new JSONArray());
    	}
    	return json;
    }

    private static JSONObject createCustomJSonGeonamesList(String jsonGeonames, int version, int limit, int stringLimit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        JSONArray geonames = JSONUtils.getJSonArray(jsonGeonames, "geonames");
        if (geonames != null) {
            for (int i = 0; i < geonames.length(); i++) {
                try {
                    JSONObject geoname = geonames.getJSONObject(i);
                    Map<String, Object> jsonObject = new HashMap<String, Object>();
                    jsonObject.put("name", geoname.getString("title"));
                    jsonObject.put("lat", MathUtils.normalizeE6(geoname.getDouble("lat")));
                    jsonObject.put("lng", MathUtils.normalizeE6(geoname.getDouble("lng")));

                    if (version >= 2) {
                        Map<String, String> desc = new HashMap<String, String>();
                        JSONUtils.putOptValue(desc, "description", geoname, "summary", false, stringLimit, false);
                        if (version >= 3) {
                            String icon = geoname.optString("thumbnailImg");
                            if (StringUtils.isNotEmpty(icon)) {
                                desc.put("icon", icon);
                            }
                        }
                        if (!desc.isEmpty()) {
                            jsonObject.put("desc", desc);
                        }
                        jsonObject.put("url", geoname.getString("wikipediaUrl"));
                    } else {
                        jsonObject.put("desc", geoname.getString("wikipediaUrl"));
                    }

                    jsonArray.add(jsonObject);
                } catch (JSONException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                if (i > limit) {
                    break;
                }
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String lang, String flexString2, Locale locale, boolean useCache) throws Exception {
		if (lang == null) {
			lang = locale.getLanguage();
		}
		int r = NumberUtils.normalizeNumber(radius, 1, 20);
        List<ExtendedLandmark> output = new ArrayList<ExtendedLandmark>();

        URL geonamesUrl = new URL("http://api.geonames.org/findNearbyWikipediaJSON?lat=" + lat + "&lng=" + lng + "&maxRows=" + MAXROWS + "&radius=" + r + "&username=" + Commons.getProperty(Property.GEONAMES_USERNAME) + "&lang=" + lang);

        String geonamesResponse = HttpUtils.processFileRequest(geonamesUrl);
            
        if (StringUtils.startsWith(geonamesResponse, "{")) {
            //System.out.println("Response: " + geonamesResponse + " from " + geonamesUrl.toString());
            output =  createLandmarksGeonamesList(geonamesResponse, limit, stringLimit, locale);
        } else {
            logger.log(Level.WARNING, "Received following response " + geonamesResponse);
            	
        }
        return output;
	}
	
	private static List<ExtendedLandmark> createLandmarksGeonamesList(String jsonGeonames, int limit, int stringLimit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        JSONArray geonames = JSONUtils.getJSonArray(jsonGeonames, "geonames");
        if (geonames != null) {
            for (int i = 0; i < geonames.length(); i++) {
                try {
                    JSONObject geoname = geonames.getJSONObject(i);
                    
                    String name = geoname.getString("title");
                    double lat = geoname.getDouble("lat");
                    double lng = geoname.getDouble("lng");
                    String url = geoname.getString("wikipediaUrl");
                    if (!StringUtils.startsWith(url, "http")) {
                    	url = "http://" + url;
                    }
                    
                    QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                    ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.WIKIPEDIA_LAYER, new AddressInfo(), -1, null);
                    landmark.setUrl(url); 
                    
                    Map<String, String> tokens = new HashMap<String, String>();
                    JSONUtils.putOptValue(tokens, "description", geoname, "summary", false, stringLimit, false);
                    
                    String icon = geoname.optString("thumbnailImg");
                    if (StringUtils.isNotEmpty(icon)) {
                        landmark.setThumbnail(icon);
                    }
                    
                    String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                    landmark.setDescription(description);
					
                    landmarks.add(landmark);
                } catch (JSONException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                if (i > limit) {
                    break;
                }
            }
        }

        return landmarks;
    }
	
	public String getLayerName() {
		 return Commons.WIKIPEDIA_LAYER;
	}
	
	public String getURI() {
		return "geonamesProvider";
	}
}
