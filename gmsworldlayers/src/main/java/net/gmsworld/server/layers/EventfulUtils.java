package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
public class EventfulUtils extends LayerHelper {

	 //2011-08-20 03:59:59
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    @Override
    public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flex, String flexString2) throws MalformedURLException, IOException, JSONException, ParseException {
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, flex, flexString2);
        String output = cacheProvider.getString(key);
        JSONObject json = null;
        if (output == null) {
            String eventfulUrl = "http://api.eventful.com/json/events/search?"
                    + "location=" + latitude + "," + longitude + "&within=" + radius
                    + "&date=Future&units=km&format=json&page_size=" + limit + "&app_key=" + Commons.getProperty(Property.EVENTFUL_APP_KEY);
            if (StringUtils.isNotEmpty(query)) {
                eventfulUrl += "&keywords=" + URLEncoder.encode(query, "UTF-8");
            }

            String eventfulJson = HttpUtils.processFileRequest(new URL(eventfulUrl));
            json = createCustomJsonEventfulList(eventfulJson, version, stringLimit);

            if (json.getJSONArray("ResultSet").length() > 0) {
                cacheProvider.put(key, json.toString());
                logger.log(Level.INFO, "Adding EV landmark list to cache with key {0}", key);
            }
        } else {
            json = new JSONObject(output);
        }
        return json;
    }

    public String processRequest(String query, int version, int stringLimit, String queryString) throws IOException, JSONException, ParseException {
        String key = getCacheKey(EventfulUtils.class, "processRequest", 0, 0, query, 0, version, 0, stringLimit, queryString, null);

        String output = cacheProvider.getString(key);
        if (output == null) {
            String eventfulUrl = "http://api.eventful.com/json/events/search?" + queryString + "&app_key=" + Commons.getProperty(Property.EVENTFUL_APP_KEY);
            if (StringUtils.isNotEmpty(query)) {
                eventfulUrl += "&keywords=" + query;
            }

            //&date=2012042500-2012042700
            String eventfulJson = HttpUtils.processFileRequest(new URL(eventfulUrl));
            JSONObject json = createCustomJsonEventfulList(eventfulJson, version, stringLimit);
            output = json.toString();
            if (json.getJSONArray("ResultSet").length() > 0) {
                cacheProvider.put(key, output);
                logger.log(Level.INFO, "Adding EV landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading EV landmark list from cache with key {0}", key);
        }
        return output;
    }

    public String processRequest(String queryString) throws MalformedURLException, IOException {
        return HttpUtils.processFileRequest(new URL("http://api.eventful.com/json/events/search?" + queryString + "&app_key=" + Commons.getProperty(Property.EVENTFUL_APP_KEY)));
    }

    private static Map<String, Object> createEventfulJsonObject(JSONObject event, int version, int stringLimit) throws JSONException {
        Map<String, Object> jsonObject = new HashMap<String, Object>();
        if (version == 1) {
            jsonObject.put("name", event.getString("title"));
            jsonObject.put("lat", event.getString("latitude"));
            jsonObject.put("lng", event.getString("longitude"));
            jsonObject.put("desc", event.getString("url"));
        } else {
            jsonObject.put("name", event.getString("title"));
            jsonObject.put("lat", event.getString("latitude"));
            jsonObject.put("lng", event.getString("longitude"));
            jsonObject.put("url", event.getString("url"));

            Map<String, String> desc = new HashMap<String, String>();
            JSONUtils.putOptValue(desc, "description", event, "description", false, stringLimit, true);
            if (version >= 3) {
                JSONUtils.putOptDate(desc, "start_date", event, "start_time", formatter);
            } else {
                JSONUtils.putOptValue(desc, "start_date", event, "start_time", false, stringLimit, false);
            }
            JSONUtils.putOptValue(desc, "venue", event, "venue_name", true, stringLimit, false);
            JSONUtils.putOptValue(desc, "region", event, "region_name", true, stringLimit, false);
            JSONUtils.putOptValue(desc, "address", event, "venue_address", true, stringLimit, false);
            JSONUtils.putOptValue(desc, "city", event, "city_name", false, stringLimit, false);
            JSONUtils.putOptValue(desc, "country", event, "country_name", false, stringLimit, false);
            JSONUtils.putOptValue(desc, "zip", event, "postal_code", false, stringLimit, false);

            if (version >= 4) {
               JSONObject images = event.optJSONObject("image");
               if (images != null) {
                   JSONObject thumb = images.optJSONObject("thumb");
                   if (thumb != null) {
                       String icon = thumb.optString("url");
                       if (StringUtils.isNotEmpty(icon)){
                           desc.put("icon", icon);
                       }
                   }
               }
            }

            jsonObject.put("desc", desc);
        }

        return jsonObject;
    }

    private static JSONObject createCustomJsonEventfulList(String eventfulJson, int version, int stringLimit) throws JSONException, ParseException {
        List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        if (StringUtils.startsWith(eventfulJson, "{")) {
            try {
                JSONObject jsonRoot = new JSONObject(eventfulJson);
                int total_items = jsonRoot.getInt("total_items");

                if (total_items > 1) {
                    JSONObject e = jsonRoot.getJSONObject("events");
                    JSONArray events = e.getJSONArray("event");

                    for (int i = 0; i < events.length(); i++) {
                        JSONObject event = events.getJSONObject(i);
                        Map<String, Object> jsonObject = createEventfulJsonObject(event, version, stringLimit);
                        jsonArray.add(jsonObject);
                    }
                } else if (total_items == 1) {
                    JSONObject e = jsonRoot.getJSONObject("events");
                    JSONObject event = e.getJSONObject("event");
                    Map<String, Object> jsonObject = createEventfulJsonObject(event, version, stringLimit);
                    jsonArray.add(jsonObject);
                }
            } catch (JSONException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);

        return json;
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flex, String flexString2, Locale locale, boolean useCache)	throws Exception {
		String eventfulUrl = "http://api.eventful.com/json/events/search?"
                    + "location=" + lat + "," + lng + "&within=" + radius
                    + "&date=Future&units=km&format=json&page_size=" + limit + "&app_key=" + Commons.getProperty(Property.EVENTFUL_APP_KEY);
        if (StringUtils.isNotEmpty(query)) {
                eventfulUrl += "&keywords=" + URLEncoder.encode(query, "UTF-8");
        }
        String eventfulJson = HttpUtils.processFileRequest(new URL(eventfulUrl));
        return createCustomLandmarkEventfulList(eventfulJson, version, stringLimit, locale);
	}
	
	private static ExtendedLandmark createEventfulLandmark(JSONObject event, int stringLimit, Locale locale) throws JSONException {
		ExtendedLandmark landmark = null;
		try {
            	Double lat = NumberUtils.getDouble(event.get("latitude"));
            	Double lng = NumberUtils.getDouble(event.get("longitude"));
            	
            	if (lat != null && lng != null) {          	
            		String name = event.getString("title");
            		String url = event.getString("url");

            		Map<String, String> tokens = new HashMap<String, String>();
            		JSONUtils.putOptValue(tokens, "description", event, "description", false, stringLimit, true);
            		JSONUtils.putOptValue(tokens, "venue", event, "venue_name", true, stringLimit, false);
            		JSONUtils.putOptValue(tokens, "region", event, "region_name", true, stringLimit, false);
            
            		JSONUtils.putOptDate(tokens, "start_date", event, "start_time", formatter);
            		long creationDate = -1;
            		if (tokens.containsKey("start_date")) {
            			creationDate = Long.parseLong(tokens.get("start_date"));
            		}
            
            		AddressInfo address = new AddressInfo();
            
            		String val = event.optString("venue_address");
            		if (StringUtils.isNotEmpty(val)) {
            			address.setField(AddressInfo.STREET, val);
            		}                    
            		val = event.optString("city_name");
            		if (StringUtils.isNotEmpty(val)) {
            			address.setField(AddressInfo.CITY, val);
            		}
            		val = event.optString("country_name");
            		if (StringUtils.isNotEmpty(val)) {
            			address.setField(AddressInfo.COUNTRY, val);
            		}
            		val = event.optString("postal_code");
            		if (StringUtils.isNotEmpty(val)) {
            			address.setField(AddressInfo.POSTAL_CODE, val);
            		}
            
            		QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
            		landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.EVENTFUL_LAYER, address, creationDate, null);
            		landmark.setUrl(url);

            		JSONObject images = event.optJSONObject("image");
            		if (images != null) {
            			JSONObject thumb = images.optJSONObject("thumb");
            			if (thumb != null) {
            				String icon = thumb.optString("url");
            				if (StringUtils.isNotEmpty(icon)){
            					landmark.setThumbnail(icon);
            				}
            			}
            		}
          
            		String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
            		landmark.setDescription(description);
            	} else {
            		logger.log(Level.SEVERE, "Wrong coord format: " + event.get("latitude") + "," + event.get("longitude"));
            	}
        } catch (JSONException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
		return landmark;
    }

    private static List<ExtendedLandmark> createCustomLandmarkEventfulList(String eventfulJson, int version, int stringLimit, Locale locale) throws JSONException, ParseException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        if (StringUtils.startsWith(eventfulJson, "{")) {
            try {
                JSONObject jsonRoot = new JSONObject(eventfulJson);
                int total_items = jsonRoot.getInt("total_items");

                if (total_items > 1) {
                    JSONObject e = jsonRoot.getJSONObject("events");
                    JSONArray events = e.getJSONArray("event");

                    for (int i = 0; i < events.length(); i++) {
                        JSONObject event = events.getJSONObject(i);
                        ExtendedLandmark landmark = createEventfulLandmark(event, stringLimit, locale);
                        if (landmark != null) {		
                        	landmarks.add(landmark);
                        }
                    }
                } else if (total_items == 1) {
                    JSONObject e = jsonRoot.getJSONObject("events");
                    JSONObject event = e.getJSONObject("event");
                    ExtendedLandmark landmark = createEventfulLandmark(event, stringLimit, locale);
                    if (landmark != null) {		
                    	landmarks.add(landmark);
                    }
                }
            } catch (JSONException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return landmarks;
    }
    
    public String getLayerName() {
    	return Commons.EVENTFUL_LAYER;
    }
}
