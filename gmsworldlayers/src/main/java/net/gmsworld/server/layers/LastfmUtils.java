package net.gmsworld.server.layers;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
public class LastfmUtils extends LayerHelper {
	
    private static final SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US); //Fri, 26 Aug 2011 12:33:01
    private static final String SERVER_ERROR_MARKER = "LastFmServerErrorMarker";

    @Override
	public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
    	JSONObject json = null;
    	if  (isEnabled()) {
	    	String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, flexString, flexString2);
	        String output = cacheProvider.getString(key);
	
	        if (output == null) {
	            URL lastfmUrl = new URL("http://ws.audioscrobbler.com/2.0/?method=geo.getevents&lat=" + latitude
	                    + "&long=" + longitude + "&distance=" + radius + "&limit=" + limit + "&format=json&api_key=" + Commons.getProperty(Property.LASTFM_API_KEY));
	            String lastfmResponse = HttpUtils.processFileRequest(lastfmUrl);
	
	            json = createCustomJsonLastfmList(lastfmResponse, version, stringLimit);
	
	            if (json.getJSONArray("ResultSet").length() > 0) {
	                cacheProvider.put(key, json.toString());
	                logger.log(Level.INFO, "Adding LFM landmark list to cache with key {0}", key);
	            }
	        } else {
	            logger.log(Level.INFO, "Reading LFM landmark list from cache with key {0}", key);
	            json = new JSONObject(output);
	        }
    	} else {
    		json = new JSONObject().put("ResultSet", new JSONArray());
    	}
        return json;
    }

    private static JSONObject createCustomJsonLastfmList(String lastfmJson, int version, int stringLimit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        if (StringUtils.startsWith(lastfmJson, "{")) {
            try {
                JSONObject jsonRoot = new JSONObject(lastfmJson);
                if (jsonRoot.has("events")) {
                    JSONObject events = jsonRoot.getJSONObject("events");
                    JSONArray event = events.optJSONArray("event");
                    if (event != null) {
                        for (int i = 0; i < event.length(); i++) {
                            JSONObject e = event.getJSONObject(i);
                            Map<String, Object> jsonObject = createLastfmJsonObject(e, version, stringLimit);
                            if (jsonObject != null) {
                                jsonArray.add(jsonObject);
                            }
                        }
                    } else {
                        JSONObject e = events.optJSONObject("event");
                        if (e != null) {
                            Map<String, Object> jsonObject = createLastfmJsonObject(e, version, stringLimit);
                            if (jsonObject != null) {
                                jsonArray.add(jsonObject);
                            }
                        }
                    }

                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

    private static Map<String, Object> createLastfmJsonObject(JSONObject e, int version, int stringLimit) throws JSONException, ParseException {
        Map<String, Object> jsonObject = new HashMap<String, Object>();

        JSONObject artists = e.getJSONObject("artists");

        JSONObject venue = e.getJSONObject("venue");
        JSONObject location = venue.getJSONObject("location");
        JSONObject geo = location.getJSONObject("geo:point");

        jsonObject.put("name", e.getString("title"));
        jsonObject.put("lat", geo.getString("geo:lat"));
        jsonObject.put("lng", geo.getString("geo:long"));
        jsonObject.put("url", e.getString("url"));

        Map<String, String> desc = new HashMap<String, String>();

        JSONUtils.putOptValue(desc, "description", e, "description", false, stringLimit, true);
        JSONUtils.putOptValue(desc, "venue", venue, "name", false, stringLimit, false);
        JSONUtils.putOptValue(desc, "phone", venue, "phonenumber", false, stringLimit, false);
        JSONUtils.putOptValue(desc, "city", location, "city", false, stringLimit, false);
        JSONUtils.putOptValue(desc, "country", location, "country", false, stringLimit, false);
        JSONUtils.putOptValue(desc, "street", location, "street", false, stringLimit, false);
        JSONUtils.putOptValue(desc, "zip", location, "postalcode", false, stringLimit, false);

        if (version >= 3) {
           JSONUtils.putOptDate(desc, "start_date", e, "startDate", formatter);
           JSONUtils.putOptDate(desc, "end_date", e, "endDate", formatter);
        } else {
            JSONUtils.putOptValue(desc, "start_date", e, "startDate", false, stringLimit, false);
            JSONUtils.putOptValue(desc, "end_date", e, "endDate", false, stringLimit, false);
        }

        JSONArray artist = artists.optJSONArray("artist");
        if (artist == null) {
            JSONUtils.putOptValue(desc, "artist", artists, "headliner", false, stringLimit, false);
        } else {
            String art = "";
            for (int j = 0; j < artist.length(); j++) {
                art += artist.getString(j);
                if (j < artist.length() - 1) {
                    art += ", ";
                }
            }
            desc.put("artist", art);
        }

        JSONArray image = e.optJSONArray("image");
        if (image != null && image.length() > 1) {
            JSONObject imageSmall = image.getJSONObject(1);
            if (StringUtils.equals(imageSmall.getString("size"), "medium")) {
                String icon = imageSmall.getString("#text");
                if (StringUtils.isNotEmpty(icon)) {
                    desc.put("icon", icon);
                }
            }
        }

        jsonObject.put("desc", desc);

        return jsonObject;
    }
    
    private static List<ExtendedLandmark> createCustomLandmarkLastfmList(String lastfmJson, int stringLimit, Locale locale) throws JSONException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
    	
    	if (StringUtils.startsWith(lastfmJson, "{")) {
            try {
                JSONObject jsonRoot = new JSONObject(lastfmJson);
                if (jsonRoot.has("events")) {
                    JSONObject events = jsonRoot.getJSONObject("events");
                    JSONArray event = events.optJSONArray("event");
                    if (event != null) {
                        for (int i = 0; i < event.length(); i++) {
                            JSONObject e = event.getJSONObject(i);
                            try {
                            	ExtendedLandmark landmark = createLastfmLandmark(e, stringLimit, locale);	
                                if (landmark != null) {
                                   landmarks.add(landmark);
                                }
                            } catch (JSONException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        JSONObject e = events.optJSONObject("event");
                        if (e != null) {
                        	try {
                            	ExtendedLandmark landmark = createLastfmLandmark(e, stringLimit, locale);	
                                if (landmark != null) {
                                   landmarks.add(landmark);
                                }
                            } catch (JSONException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        }
                    }

                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return landmarks;
    }
    
    private static ExtendedLandmark createLastfmLandmark(JSONObject e, int stringLimit, Locale locale) throws JSONException, ParseException {
    	ExtendedLandmark landmark = null;
    	JSONObject artists = e.getJSONObject("artists");
        JSONObject venue = e.optJSONObject("venue");
        if (venue != null) {
        	JSONObject location = venue.getJSONObject("location");
        	JSONObject geo = location.getJSONObject("geo:point");
        	
        	String name = e.getString("title");
        	double lat = geo.getDouble("geo:lat");
        	double lng = geo.getDouble("geo:long");
        	String url = e.getString("url");

        	Map<String, String> tokens = new HashMap<String, String>();

        	JSONUtils.putOptValue(tokens, "description", e, "description", false, stringLimit, true);
        	JSONUtils.putOptValue(tokens, "venue", venue, "name", false, stringLimit, false);
        
        	AddressInfo address = new AddressInfo();
        	String val = venue.optString("phonenumber");
        	if (StringUtils.isNotEmpty(val) && val.matches(".*\\d+.*")) {
        		address.setField(AddressInfo.PHONE_NUMBER, val);	
        	}
        	val = venue.optString("city");
        	if (StringUtils.isNotEmpty(val)) {
        		address.setField(AddressInfo.CITY, val);	
        	}	
        	val = venue.optString("country");
        	if (StringUtils.isNotEmpty(val)) {
        		address.setField(AddressInfo.COUNTRY, val);	
        	}
        	val = venue.optString("street");
        	if (StringUtils.isNotEmpty(val)) {
        		address.setField(AddressInfo.STREET, val);	
        	}
        	val = venue.optString("postalcode");
        	if (StringUtils.isNotEmpty(val)) {
        		address.setField(AddressInfo.POSTAL_CODE, val);	
        	}
        
        	JSONUtils.putOptDate(tokens, "start_date", e, "startDate", formatter);
        
        	long creationDate = -1;
        	if (tokens.containsKey("start_date")) {
        		creationDate = Long.parseLong(tokens.get("start_date"));
        	}
        
        	JSONUtils.putOptDate(tokens, "end_date", e, "endDate", formatter);
        
        	JSONArray artist = artists.optJSONArray("artist");
        	if (artist == null) {
        		JSONUtils.putOptValue(tokens, "artist", artists, "headliner", false, stringLimit, false);
        	} else {
        		String art = "";
        		for (int j = 0; j < artist.length(); j++) {
        			art += artist.getString(j);
        			if (j < artist.length() - 1) {
        				art += ", ";
        			}
        		}
        		tokens.put("artist", art);
        	}
        
        	QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
        	landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.LASTFM_LAYER, address, creationDate, null);
        	landmark.setUrl(url);
        	
        	String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
        	landmark.setDescription(description);
        
        	JSONArray image = e.optJSONArray("image");
        	if (image != null && image.length() > 1) {
            	JSONObject imageSmall = image.getJSONObject(1);
            	if (StringUtils.equals(imageSmall.getString("size"), "medium")) {
                	String icon = imageSmall.getString("#text");
                	if (StringUtils.isNotEmpty(icon)) {
                		landmark.setThumbnail(icon);
                	}
            	}
        	}
        }

        return landmark;
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {		
		String lastfmResponse = null;
		
		if (!cacheProvider.containsKey(SERVER_ERROR_MARKER)) {
			URL lastfmUrl = new URL("http://ws.audioscrobbler.com/2.0/?method=geo.getevents&lat=" + lat
                    + "&long=" + lng + "&distance=" + radius + "&limit=" + limit + "&format=json&api_key=" + Commons.getProperty(Property.LASTFM_API_KEY));
			lastfmResponse = HttpUtils.processFileRequest(lastfmUrl);
			Integer responseCode = HttpUtils.getResponseCode(lastfmUrl.toString());
			if (responseCode != null && responseCode == 400) {
				cacheProvider.put(SERVER_ERROR_MARKER, "1");
			}
		}	
		
        return createCustomLandmarkLastfmList(lastfmResponse, stringLimit, locale);           
	}
	
	public String getLayerName() {
    	return Commons.LASTFM_LAYER;
    }
	
	public boolean isEnabled() {
   		return false;
    }
}
