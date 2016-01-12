package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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
import net.gmsworld.server.utils.ThreadManager;

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
 *    
 *  
 */
public class GooglePlacesUtils extends LayerHelper {

	/*private static final String[] TYPES = {
		"accounting",
	    "airport",
	    "amusement_park",
	    "aquarium",
	    "art_gallery",
	    "atm",
	    "bakery",
	    "bank",
	    "bar",
	    "beauty_salon",
	    "bicycle_store",
	    "book_store",
	    "bowling_alley",
	    "bus_station",
	    "cafe",
	    "campground",
	    "car_dealer",
	    "car_rental",
	    "car_repair",
	    "car_wash",
	    "casino",
	    "cemetery",
	    "church",
	    "city_hall",
	    "clothing_store",
	    "convenience_store",
	    "courthouse",
	    "dentist",
	    "department_store",
	    "doctor",
	    "electrician",
	    "electronics_store",
	    "embassy",
	    "establishment",
	    "finance",
	    "fire_station",
	    "florist",
	    "food",
	    "funeral_home",
	    "furniture_store",
	    "gas_station",
	    "general_contractor",
	    "grocery_or_supermarket",
	    "gym",
	    "hair_care",
	    "hardware_store",
	    "health",
	    "hindu_temple",
	    "home_goods_store",
	    "hospital",
	    "insurance_agency",
	    "jewelry_store",
	    "laundry",
	    "lawyer",
	    "library",
	    "liquor_store",
	    "local_government_office",
	    "locksmith",
	    "lodging",
	    "meal_delivery",
	    "meal_takeaway",
	    "mosque",
	    "movie_rental",
	    "movie_theater",
	    "moving_company",
	    "museum",
	    "night_club",
	    "painter",
	    "park",
	    "parking",
	    "pet_store",
	    "pharmacy",
	    "physiotherapist",
	    "place_of_worship",
	    "plumber",
	    "police",
	    "post_office",
	    "real_estate_agency",
	    "restaurant",
	    "roofing_contractor",
	    "rv_park",
	    "school",
	    "shoe_store",
	    "shopping_mall",
	    "spa",
	    "stadium",
	    "storage",
	    "store",
	    "subway_station",
	    "synagogue",
	    "taxi_stand",
	    "train_station",
	    "travel_agency",
	    "university",
	    "veterinary_care",
	    "zoo",		
	};*/
	
	private static final int QUOTA_LIMIT = 200;
	private static final String types = "establishment";
	
	/*static {
		try {
			types = URLEncoder.encode(StringUtils.join(TYPES, "|"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
		}
	}*/
	
    @Override
	public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String language, String flexString2) throws MalformedURLException, IOException, JSONException {
        int r = NumberUtils.normalizeNumber(radius, 1000, 50000);
        int l = NumberUtils.normalizeNumber(limit, 1, QUOTA_LIMIT);
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, r, version, l, stringLimit, language, flexString2);

        JSONObject response = null;

        String cachedResponse = cacheProvider.getString(key);
        if (cachedResponse == null) {
            List<String> placeDetails = Collections.synchronizedList(new ArrayList<String>());
            String queryStringSuffix = "&types=" + types + "&sensor=false&key=" + Commons.getProperty(Property.GOOGLE_API_KEY);

            String url = "location=" + latitude + "," + longitude + "&radius=" + r + "&language=" + language;
            if (query != null) {
                url += "&keyword=" + URLEncoder.encode(query, "UTF-8");
            }
            url += queryStringSuffix;

            processRadarRequest(placeDetails, url, l, language);

            response = createCustomJSonGooglePlacesList(placeDetails, stringLimit, version);

            if (response.getJSONArray("ResultSet").length() > 0) {
                cacheProvider.put(key, response.toString());
                logger.log(Level.INFO, "Adding GL landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading GL landmark list from cache with key {0}", key);
            response = new JSONObject(cachedResponse);
        }

        return response;
    }
    
    private void processRadarRequest(List<String> placeDetails, String queryString, int limit, String language) throws MalformedURLException, IOException, JSONException {
        String url = "https://maps.googleapis.com/maps/api/place/radarsearch/json?" + queryString;
        URL placesUrl = new URL(url);
        String placesResponse = HttpUtils.processFileRequest(placesUrl);

        //System.out.println(placesResponse);

        if (StringUtils.startsWith(placesResponse,"{")) {
            JSONObject json = new JSONObject(placesResponse);
            String status = json.getString("status");
            if (status.equals("OK")) {
                processDetails(placeDetails, json.getJSONArray("results"), limit, language);
            } else {
            	logger.log(Level.WARNING, "Response: " + placesResponse + " from " + url);
            }
        } else {
        	logger.log(Level.WARNING, "Response: " + placesResponse + " from " + url);
        }

        //return null;
    }

    private static JSONObject createCustomJSonGooglePlacesList(List<String> placesJsonV, int stringLimit, int version) throws JSONException {
        ArrayList<Object> jsonArray = new ArrayList<Object>();

        for (int i = 0; i < placesJsonV.size(); i++) {
            String placesJson = placesJsonV.get(i);

            if (StringUtils.startsWith(placesJson, "{")) {
                JSONObject json = new JSONObject(placesJson);
                String status = json.getString("status");

                if (status.equals("OK")) {

                    JSONObject item = json.getJSONObject("result");
                    Map<String, Object> jsonObject = new HashMap<String, Object>();

                    String name = item.getString("name");

                    JSONObject geometry = item.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");
                    String place_id = item.getString("place_id");

                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");

                    Map<String, String> desc = new HashMap<String, String>();
                    JSONArray types = item.optJSONArray("types");
                    String typeslist = "";

                    if (types != null) {
                        boolean addc = false;
                        for (int j = 0; j < types.length(); j++) {
                            if (addc) {
                                typeslist += ", ";
                            }
                            typeslist += types.getString(j);
                            addc = true;
                        }
                    }

                    if (typeslist.length() > 0) {
                        desc.put("category", typeslist);
                    }

                    JSONUtils.putOptValue(desc, "phone", item, "formatted_phone_number", false, stringLimit, false);
                    JSONUtils.putOptValue(desc, "address", item, "formatted_address", false, stringLimit, false);

                    if (item.has("rating")) {
                        desc.put("rating", Double.toString(item.getDouble("rating")));
                    }
                    
                    jsonObject.put("name", name);
                    jsonObject.put("lat", lat);
                    jsonObject.put("lng", lng);
                    String url = item.getString("url");
                    jsonObject.put("url", url);
                    jsonObject.put("reference", place_id);

                    if (version >= 2) {
                        String icon = item.optString("icon");
                        if (icon != null) {
                            desc.put("icon", icon);
                        }
                    }

                    jsonObject.put("desc", desc);

                    jsonArray.add(jsonObject);
                }
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
    
    private static List<ExtendedLandmark> createLandmarkGooglePlacesList(List<String> placesJsonV, int stringLimit, Locale locale) throws JSONException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
        
        for (int i = 0; i < placesJsonV.size(); i++) {
            String placeJson = placesJsonV.get(i);
            ExtendedLandmark landmark = processLandmark(placeJson, stringLimit, locale);
            if (landmark != null) {
            	landmarks.add(landmark);
            }
            
        }

        return landmarks;
    }

    private void processDetails(List<String> placeDetails, JSONArray results, int limit, String language) throws JSONException, MalformedURLException, IOException {

        ThreadManager threadManager = new ThreadManager(threadProvider);
        
        int l = limit;
        if (results.length() < l) {
        	l = results.length();
        }
        
        logger.log(Level.INFO, "Looking for: " + l + " details...");
        
        int count = 0;
        
        while (count < l) {
        
        	int batchSize = NumberUtils.normalizeNumber(l - count, 0, 30);
        	
        	logger.log(Level.INFO, "Running batch from " + count + " to " + (count+batchSize));
        	
        	for (int i = count; i < (count+batchSize); i++) {
        		JSONObject item = results.getJSONObject(i);
        		String place_id = item.getString("place_id");
        		threadManager.put(place_id, new VenueDetailsRetriever(threadManager, placeDetails, place_id, language));
        	}

        	threadManager.waitForThreads();
        	
        	count += batchSize;
        	
        	logger.log(Level.INFO, "Processed " + count + " from " + l + " places"); 
        }
    }
    
    public static ExtendedLandmark processLandmark(String placeJson, int stringLimit, Locale locale) {
    	ExtendedLandmark landmark = null;
    	if (StringUtils.startsWith(placeJson, "{")) {
            JSONObject json = new JSONObject(placeJson);
            String status = json.getString("status");

            if (status.equals("OK")) {

                JSONObject item = json.getJSONObject("result");
                
                String name = item.getString("name");

                JSONObject geometry = item.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                String place_id = item.getString("place_id");

                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                Map<String, String> tokens = new HashMap<String, String>();
                JSONArray types = item.optJSONArray("types");
                String typeslist = "";

                if (types != null) {
                    boolean addc = false;
                    for (int j = 0; j < types.length(); j++) {
                        if (addc) {
                            typeslist += ", ";
                        }
                        typeslist += types.getString(j);
                        addc = true;
                    }
                }

                if (typeslist.length() > 0) {
                    tokens.put("category", typeslist);
                }

                JSONUtils.putOptValue(tokens, "address", item, "formatted_address", false, stringLimit, false);
          
                String url = item.getString("url");         
                
                String icon = null;
                JSONArray photos = item.optJSONArray("photos");
                if (photos != null && photos.length() > 0) {
                	//for (int j = 0; j < photos.length(); j++) {
                    //    JSONObject photo = photos.getJSONObject(j);
                	//    logger.log(Level.INFO, photo.toString());
                	//}    
                	JSONObject firstPhoto = photos.getJSONObject(0);
                	String photoRef = firstPhoto.getString("photo_reference");
                    icon = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=128&maxheight=128&photoreference=" + photoRef + "&key=" + Commons.getProperty(Property.GOOGLE_API_KEY);	             	
                }
                
                if (icon == null) {
                	icon = item.optString("icon");
                }
                AddressInfo address = new AddressInfo();
                address.setField(AddressInfo.EXTENSION, place_id);
                
                String website = item.optString("website");
                if (website != null) {
                	tokens.put("homepage", website);
                }
                                    
                String phone = item.optString("international_phone_number");
                if (phone != null) {
                	address.setField(AddressInfo.PHONE_NUMBER, phone);
                } else {
                	phone = item.optString("formatted_phone_number");
                	if (phone != null) {
                		address.setField(AddressInfo.PHONE_NUMBER, phone);
                	}
                }
                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.GOOGLE_PLACES_LAYER, address, -1, null);
                landmark.setThumbnail(icon);
                landmark.setUrl(url);
                
                if (item.has("rating")) {
                    landmark.setRating(item.getDouble("rating"));
                }
                
                String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                landmark.setDescription(description);
            }
        }
    	
    	return landmark;
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String language, String flexString2, Locale locale, boolean useCache) throws Exception {
		if (language == null) {
			language = locale.getLanguage();
		}
		int r = NumberUtils.normalizeNumber(radius, 1000, 50000);
		int l = NumberUtils.normalizeNumber(limit, 1, QUOTA_LIMIT);
        List<String> placeDetails = new ArrayList<String>();
        String queryStringSuffix = "&types=" + types + "&sensor=false&key=" + Commons.getProperty(Property.GOOGLE_API_KEY);

        String url = "location=" + lat + "," + lng + "&radius=" + r + "&language=" + language;
        if (query != null) {
           url += "&keyword=" + URLEncoder.encode(query, "UTF-8");
        }
        url += queryStringSuffix;

        processRadarRequest(placeDetails, url, l, language);

        return createLandmarkGooglePlacesList(placeDetails, stringLimit, locale);        
	}
	
	public static String getPlaceDetails(String placeid, String language) throws Exception {
		try {
			String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeid + "&key=" + Commons.getProperty(Property.GOOGLE_API_KEY) + "&language=" + language;
            URL itemDetails = new URL(url);
            String response = HttpUtils.processFileRequest(itemDetails);
            Integer responseCode = HttpUtils.getResponseCode(url);
            if (responseCode != null && responseCode == 200 && response != null) {
               return response;	
            } else {
               logger.log(Level.SEVERE, "Received following server response code " + responseCode);
               return null;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "GooglePlacesUtils.gerPlaceDetails() exception:", e);
            return null;
        } 
    }
	
	public String getLayerName() {
		return Commons.GOOGLE_PLACES_LAYER;
	}
	
	private static class VenueDetailsRetriever implements Runnable {

        private String placeid, language;
        private List<String> placeDetails;
        private ThreadManager threadManager;

        public VenueDetailsRetriever(ThreadManager threadManager, List<String> placeDetails, String placeid, String language) {
            this.threadManager = threadManager;
            this.placeDetails = placeDetails;
            this.placeid= placeid;
            this.language = language;
        }

        public void run() {
            try {
                URL itemDetails = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeid + "&key=" + Commons.getProperty(Property.GOOGLE_API_KEY) + "&language=" + language);
                String details = HttpUtils.processFileRequest(itemDetails);
                placeDetails.add(details);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "VenueDetailsRetriever.run exception:", e);
            } finally {
                threadManager.take(placeid);
            }
        }
    }
}
