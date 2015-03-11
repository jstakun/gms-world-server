/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
public class MeetupUtils extends LayerHelper {

    @Override
	public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flex, String flexString2) throws MalformedURLException, IOException, JSONException {
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, flex, flexString2);
        JSONObject json = null;
        String output = cacheProvider.getString(key);

        if (output == null) {
            String meetupUrl = "https://api.meetup.com/2/open_events?key=" + Commons.getProperty(Property.MEETUP_API_KEY) + "&lon=" + longitude + "&lat="
                    + latitude + "&radius=" + radius + "&page=" + limit + "&order=time&text_format=plain";
            //order=time,distance,trending
            if (StringUtils.isNotEmpty(query)) {
                meetupUrl += "&text=" + URLEncoder.encode(query, "UTF-8");
            }

            String meetupResponse = HttpUtils.processFileRequest(new URL(meetupUrl));

            json = createCustomJsonMeetupList(meetupResponse, stringLimit);

            if (json.getJSONArray("ResultSet").length() > 0) {
                cacheProvider.put(key, json.toString());
                logger.log(Level.INFO, "Adding MTU landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading MTU landmark list from cache with key {0}", key);
            json = new JSONObject(output);
        }

        return json;
    }

    private static JSONObject createCustomJsonMeetupList(String meetupJson, int stringLimit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        if (StringUtils.startsWith(meetupJson, "{")) {
            JSONObject jsonRoot = new JSONObject(meetupJson);
            JSONArray events = jsonRoot.optJSONArray("results");

            if (events != null) {
                for (int i = 0; i < events.length(); i++) {

                    Map<String, Object> jsonObject = new HashMap<String, Object>();

                    JSONObject event = events.getJSONObject(i);
                    JSONObject venue = event.optJSONObject("venue");

                    if (venue != null) {
                        jsonObject.put("name", event.getString("name"));
                        jsonObject.put("lat", venue.getDouble("lat"));
                        jsonObject.put("lng", venue.getDouble("lon"));
                        jsonObject.put("url", event.getString("event_url"));

                        Map<String, String> desc = new HashMap<String, String>();

                        JSONUtils.putOptValue(desc, "description", event, "description", true, stringLimit, false);

                        JSONUtils.putOptValue(desc, "venue", venue, "venue_name", true, stringLimit, false);
                        JSONUtils.putOptValue(desc, "address", venue, "address_1", false, stringLimit, false);
                        JSONUtils.putOptValue(desc, "city", venue, "city", false, stringLimit, false);
                        JSONUtils.putOptValue(desc, "country", venue, "country", false, stringLimit, false);
                        JSONUtils.putOptValue(desc, "phone", venue, "phone", false, stringLimit, false);
                        if (venue.has("zip") && !venue.isNull("zip")) {
                            Object zip = venue.get("zip");
                            if (StringUtils.isNotEmpty(zip.toString())) {
                                desc.put("zip", zip.toString());
                            }
                        }

                        desc.put("start_date", Long.toString(event.getLong("time")));
                        //desc.put("creationDate", Long.toString(event.getLong("updated")));

                        String photo = event.optString("photo_url");
                        if (StringUtils.isNotEmpty(photo)) {
                            desc.put("icon", photo);
                        }

                        jsonObject.put("desc", desc);

                        jsonArray.add(jsonObject);
                    }
                }
            }
        }
        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
    
    private static List<ExtendedLandmark> createCustomLandmarkMeetupList(String meetupJson, int stringLimit, Locale locale) throws JSONException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        if (StringUtils.startsWith(meetupJson, "{")) {
            JSONObject jsonRoot = new JSONObject(meetupJson);
            JSONArray events = jsonRoot.optJSONArray("results");

            if (events != null) {
                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.getJSONObject(i);
                    JSONObject venue = event.optJSONObject("venue");

                    if (venue != null) {
                        String name = event.getString("name");
                        double lat = venue.getDouble("lat");
                        double lng = venue.getDouble("lon");
                        String url = event.getString("event_url");

                        Map<String, String> tokens = new HashMap<String, String>();

                        JSONUtils.putOptValue(tokens, "description", event, "description", true, stringLimit, false);
                        JSONUtils.putOptValue(tokens, "venue", venue, "venue_name", true, stringLimit, false);
                        
                        AddressInfo address = new AddressInfo();
                        
                        String val = venue.optString("address_1");
                        if (StringUtils.isNotEmpty(val)) {
                        	address.setField(AddressInfo.STREET, val);
                        }                    
                        val = venue.optString("city");
                        if (StringUtils.isNotEmpty(val)) {
                        	address.setField(AddressInfo.CITY, val);
                        }
                        val = venue.optString("country");
                        if (StringUtils.isNotEmpty(val)) {
                        	address.setField(AddressInfo.COUNTRY, val);
                        }
                        val = venue.optString("phone");
                        if (StringUtils.isNotEmpty(val)) {
                        	address.setField(AddressInfo.PHONE_NUMBER, val);
                        }
                        if (venue.has("zip") && !venue.isNull("zip")) {
                            Object zip = venue.get("zip");
                            if (StringUtils.isNotEmpty(zip.toString())) {
                            	address.setField(AddressInfo.POSTAL_CODE, zip.toString());
                            }
                        }

                        long creationDate = event.getLong("time"); //event.getLong("updated");
                        tokens.put("start_date", Long.toString(creationDate));
                        
                        QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                        ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.MEETUP_LAYER, address, creationDate, null);
                        landmark.setUrl(url);
                        
                        String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                        landmark.setDescription(description);
                        
                        String photo = event.optString("photo_url");
                        if (StringUtils.isNotEmpty(photo)) {
                        	landmark.setThumbnail(photo);                            
                        }
                        
                        landmarks.add(landmark);
                    }
                }
            }
        }
        return landmarks;
    }

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {
		String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
		List<ExtendedLandmark> output = (List<ExtendedLandmark>)cacheProvider.getObject(key);

        if (output == null) {
            String meetupUrl = "https://api.meetup.com/2/open_events?key=" + Commons.getProperty(Property.MEETUP_API_KEY) + "&lon=" + lng + "&lat="
                    + lat + "&radius=" + radius + "&page=" + limit + "&order=time&text_format=plain";
            //order=time,distance,trending
            if (StringUtils.isNotEmpty(query)) {
                meetupUrl += "&text=" + URLEncoder.encode(query, "UTF-8");
            }

            String meetupResponse = HttpUtils.processFileRequest(new URL(meetupUrl));

            output = createCustomLandmarkMeetupList(meetupResponse, stringLimit, locale);

            if (!output.isEmpty()) {
                cacheProvider.put(key, output);
                logger.log(Level.INFO, "Adding MTU landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading MTU landmark list from cache with key {0}", key);
        }
        logger.log(Level.INFO, "Found {0} landmarks", output.size()); 

        return output;
	}
	
	public String getLayerName() {
    	return Commons.MEETUP_LAYER;
    }
}
