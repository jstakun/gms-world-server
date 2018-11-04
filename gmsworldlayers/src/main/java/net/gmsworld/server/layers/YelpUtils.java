package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.authn.oauth.OAuthException;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadManager;

/**
 *
 * @author jstakun
 */
public class YelpUtils extends LayerHelper {

	private static final String USAGE_LIMIT_MARKER = "YelpUsageLimitsMarker";
	private static final String LOCATION_UNAVAILABILITY_MARKER = "YelpLocationUnavailabilityMarker";	
	
	private static final String[] LOCALES = {"cs_CZ","da_DK","de_AT","de_CH","de_DE","en_AU","en_BE","en_CA","en_CH","en_GB","en_HK","en_IE","en_MY","en_NZ","en_PH","en_SG","en_US","es_AR","es_CL","es_ES","es_MX","fi_FI","fil_PH","fr_BE","fr_CA","fr_CH","fr_FR","it_CH","it_IT","ja_JP","ms_MY","nb_NO","nl_BE","nl_NL","pl_PL","pt_BR","pt_PT","sv_FI","sv_SE","tr_TR","zh_HK","zh_TW"};
	private static final String[] LONG_LOCALES = {"en_US","ja_JP","ms_MY","nb_NO","sv_SE","zh_TW","cs_CZ","da_DK","fil_PH"};	
	
    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String hasDeals, String locale) throws Exception {
        int normalizedRadius = NumberUtils.normalizeNumber(radius, 1000, 40000);
        int normalizedLimit = NumberUtils.normalizeNumber(limit, 20, 100);
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, normalizedRadius, version, normalizedLimit, stringLimit, hasDeals, locale);

        String cachedResponse = cacheProvider.getString(key);
        if (cachedResponse == null) {
        	List<Object> venueArray = new ArrayList<Object>();
            
        	if (!cacheProvider.containsKey(USAGE_LIMIT_MARKER)) {
        		ThreadManager threadManager = new ThreadManager(threadProvider);
                boolean isDeal = Boolean.parseBoolean(hasDeals);
        		int offset = 0;

        		Locale l = null;
    			if (StringUtils.isNotEmpty(locale)) {
    				l = new Locale(locale);
    			}
        		while (offset < normalizedLimit) {	
        			threadManager.put(new VenueDetailsRetriever(venueArray, lat, lng, query, normalizedRadius, offset, isDeal, stringLimit, "json", l));
        			offset += 50;
        		}

        		threadManager.waitForThreads();
            
        		if (venueArray.size() > normalizedLimit) {
        			venueArray = venueArray.subList(0, normalizedLimit);
        		}
            
        	} else {
            	logger.log(Level.WARNING, "Yelp Rate Limit Exceeded");
            }

            JSONObject json = new JSONObject().put("ResultSet", venueArray);

            if (!venueArray.isEmpty()) {
                cacheProvider.put(key, json.toString());
                logger.log(Level.INFO, "Adding YP landmark list to cache with key {0}", key);
            }

            return json;
        } else {
            logger.log(Level.INFO, "Reading YP landmark list from cache with key {0}", key);
            return new JSONObject(cachedResponse);
        }
    }

    private String processRequest(double latitude, double longitude, String query, int radius, boolean hasDeals, int offset, String locale) throws OAuthException, IOException {
    	String responseBody = null;
    	
    	if (!cacheProvider.containsKey(USAGE_LIMIT_MARKER)) {
    	
    		String urlString = "https://api.yelp.com/v3/businesses/search?" + "latitude=" + StringUtil.formatCoordE6(latitude) + "&longitude=" + StringUtil.formatCoordE6(longitude)  + "&radius=" + radius + "&limit=50";
    		
    		if (StringUtils.isNotEmpty(query)) {
    			urlString += "&term=" + URLEncoder.encode(query, "UTF-8");
    		}
        
    		if (offset >= 0) {
    			urlString += "&offset=" + offset;
    		}
        
    		if (hasDeals) {
    			urlString += "&attributes=deals";
    		}
        
    		
    		if (locale != null && locale.length() == 2) {
    			for (String longLocale : LONG_LOCALES) {
    			     if (longLocale.startsWith(locale)) {
    			    	  locale = longLocale;
    			    	  break;
    			    }
    			}	
    			if (locale != null && locale.length() == 2) {
       			 	locale = locale + "_" + locale.toUpperCase();
    			} 
    		}
    		
    		if (StringUtils.endsWithAny(locale, LOCALES) && locale.length() == 5) {
    			urlString += "&locale=" + locale;
    		}  else {
    			logger.log(Level.WARNING, "Unsupported locale " + locale);
    		}

    		logger.log(Level.INFO, "Calling: " + urlString);

      		responseBody = HttpUtils.processFileRequestWithAuthn(new URL(urlString), "Bearer " + Commons.getProperty(Property.YELP_API_KEY));
    		
    	}

        return responseBody;
    }

    private int createCustomJsonYelpList(String yelpJson, List<Object> jsonArray, double latitude, double longitude, int stringLimit, boolean hasDeals) throws JSONException {
        int total = 0;
        if (StringUtils.startsWith(yelpJson, "{")) {
            JSONObject jsonRoot = new JSONObject(yelpJson);
            if (jsonRoot.has("businesses")) {
            	JSONArray businesses = jsonRoot.getJSONArray("businesses");
                total = businesses.length(); 
                if (total > 0) {
                   for (int i = 0; i < businesses.length(); i++) {
                        JSONObject business = businesses.getJSONObject(i);

                        Map<String, Object> jsonObject = new HashMap<String, Object>();

                        JSONObject location = business.getJSONObject("location");
                        JSONObject coordinates = business.getJSONObject("coordinates");
                        
                        Double lat = null, lng = null;
                    	try {
                    		lat = coordinates.optDouble("latitude", Double.NaN);
                    		lng = coordinates.optDouble("longitude", Double.NaN);
                    		if (lat.isNaN() || lng.isNaN()) {
                                throw new Exception("Invalid latitude or longitude!");
                            }
                    	} catch (Exception e) {
                    		logger.log(Level.WARNING, "Invalid latitude or logitude!");
                    		continue;
                    	}
                    	
                        jsonObject.put("name", business.getString("name"));
                        jsonObject.put("lat", Double.toString(lat));
                        jsonObject.put("lng", Double.toString(lng));
                        jsonObject.put("url", business.getString("url"));

                        Map<String, String> desc = new HashMap<String, String>();

                        //add desc
                        JSONUtils.putOptValue(desc, "phone", business, "display_phone", false, stringLimit, false);
                        if (business.has("rating")) {
                            desc.put("rating", Double.toString(business.getDouble("rating")));
                        }
                        if (business.has("review_count")) {
                            desc.put("numberOfReviews", Integer.toString(business.getInt("review_count")));
                        }

                        if (location.has("display_address")) {
                            JSONArray displayAddressArray = location.getJSONArray("display_address");
                            String display_address = "";
                            for (int j = 0; j < displayAddressArray.length(); j++) {
                                if (display_address.length() > 0) {
                                    display_address += ", ";
                                }
                                display_address += displayAddressArray.getString(j);
                            }
                            desc.put("address", display_address);
                        }

                        String icon = business.optString("image_url");
                        if (StringUtils.isNotEmpty(icon)) {
                            desc.put("icon", icon);
                        }
                        
                        JSONUtils.putOptValue(desc, "description", business, "snippet_text", false, stringLimit, false);

                        //categories
                        String category = "";
                        JSONArray categories = business.optJSONArray("categories");
                        if (categories != null && categories.length() > 0) {
                        	for (int j = 0; j < categories.length() ; j++) {
                        		JSONObject cat = categories.getJSONObject(j);
                        		category += cat.getString("title");
                        		if (j <  categories.length()-1) {
                        			category += ", ";
                        		}
                        	}
                        	if (StringUtils.isNotEmpty(category)) {
                        		desc.put("category", category);
                        	}
                        }
                                              
                        jsonObject.put("desc", desc);
                        jsonArray.add(jsonObject);
                    }
                }
            } else {
            	handleError(jsonRoot, latitude, longitude);
            }
        }
        return total;
    }

    private int createCustomJsonReviewsList(String yelpJson, double latitude, double longitude, Map<String, Map<String, String>> jsonObjects) throws JSONException {
        int total = 0;
        if (StringUtils.startsWith(yelpJson, "{")) {
            JSONObject jsonRoot = new JSONObject(yelpJson);
            if (jsonRoot.has("businesses")) {
            	JSONArray businesses = jsonRoot.getJSONArray("businesses");
                total = businesses.length();
                if (total > 0) {
                    for (int i = 0; i < businesses.length(); i++) {
                        JSONObject business = businesses.getJSONObject(i);

                        String phone = business.optString("phone");
                        if (phone != null) {
                            Map<String, String> desc = new HashMap<String, String>();

                            //add desc

                            phone = phone.replaceAll("[^\\d]", "");

                            if (business.has("rating")) {
                                desc.put("rating", Double.toString(business.getDouble("rating")));
                            }
                            if (business.has("review_count")) {
                                desc.put("numberOfReviews", Integer.toString(business.getInt("review_count")));
                            }

                            jsonObjects.put(phone, desc);
                        }
                    }
                }
            } else {
            	handleError(jsonRoot, latitude, longitude);
            }
        }

        return total;
    }

    public Map<String, Map<String, String>> processReviewsRequest(double latitude, double longitude, String query, int radius, int limit, boolean hasDeals, String locale) throws JSONException, IOException, OAuthException {
        
    	Map<String, Map<String, String>> reviewsArray = new HashMap<String, Map<String, String>>();
    	
    	if (!cacheProvider.containsKey(USAGE_LIMIT_MARKER)) {
        	
    		ThreadManager threadManager = new ThreadManager(threadProvider);
            int normalizedRadius = NumberUtils.normalizeNumber(radius, 1000, 40000);
    		int offset = 0;

    		while (offset < limit) {
    			threadManager.put(new ReviewDetailsRetriever(reviewsArray, latitude, longitude, query, normalizedRadius, offset, hasDeals, locale));
    			offset += 50;
    		}

    		threadManager.waitForThreads();
    	} else {
        	logger.log(Level.WARNING, "Yelp Rate Limit Exceeded");
        }

        return reviewsArray;
    }

    @Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String hasDeals, String language, Locale locale, boolean useCache) throws Exception {
		if (language == null) {
			language =  StringUtil.getLanguage(locale.getLanguage() + "_" + locale.getCountry(), "en_US", 5);
		}
    	int normalizedRadius = NumberUtils.normalizeNumber(radius, 1000, 40000);
        int normalizedLimit = NumberUtils.normalizeNumber(limit, 20, 100);
        
        List<ExtendedLandmark> landmarks = Collections.synchronizedList(new ArrayList<ExtendedLandmark>());
            
        if (!cacheProvider.containsKey(USAGE_LIMIT_MARKER)) {
        		ThreadManager threadManager = new ThreadManager(threadProvider);
                boolean isDeal = false;
        		if (hasDeals != null) {
        			isDeal = Boolean.parseBoolean(hasDeals);
        		}
        		int offset = 0;

        		while (offset < normalizedLimit) {
        			threadManager.put(new VenueDetailsRetriever(landmarks, lat, lng, query, normalizedRadius, offset, isDeal, stringLimit, "bin", locale));
        			offset += 50;
        		}

        		threadManager.waitForThreads();
            
        		if (landmarks.size() > normalizedLimit) {
        			landmarks = new ArrayList<ExtendedLandmark>(landmarks.subList(0, normalizedLimit));
        		}
        } else {
        		logger.log(Level.WARNING, "Yelp Rate Limit Exceeded");
        }

        return landmarks;
	}
	
	private int createCustomLandmarkYelpList(String yelpJson, List<ExtendedLandmark> landmarks, double latitude, double longitude, int stringLimit, boolean hasDeals, Locale locale) throws JSONException {
        int total = 0;
        if (StringUtils.startsWith(yelpJson, "{")) {
            JSONObject jsonRoot = new JSONObject(yelpJson);
            if (jsonRoot.has("businesses")) {
                //total = jsonRoot.optInt("total");
                JSONArray businesses = jsonRoot.getJSONArray("businesses");
                total = businesses.length();
                if (total > 0) {
                    for (int i = 0; i < businesses.length(); i++) {
                        JSONObject business = businesses.getJSONObject(i);
                        JSONObject coordinates= business.optJSONObject("coordinates");

                        if (coordinates != null) {
                        	String name = business.getString("name");
                        	String url = business.getString("url");

                        	Double lat = null, lng = null;
                        	try {
                        		lat = coordinates.optDouble("latitude", Double.NaN);
                        		lng = coordinates.optDouble("longitude", Double.NaN);
                        		if (lat.isNaN() || lng.isNaN()) {
                                    throw new Exception("Invalid latitude or longitude!");
                                }
                        	} catch (Exception e) {
                        		logger.log(Level.WARNING, "Invalid latitude or logitude!");
                        		continue;
                        	}
                        	QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
             		   
                        	AddressInfo address = new AddressInfo();
                        	if (business.has("display_phone") && !business.isNull("display_phone")) {
                        		address.setField(AddressInfo.PHONE_NUMBER, business.getString("display_phone"));
                        	}
                        	JSONObject location = business.getJSONObject("location");
                        	if (location.has("display_address")) {
                        		JSONArray displayAddressArray = location.getJSONArray("display_address");
                        		String display_address = "";
                        		for (int j = 0; j < displayAddressArray.length(); j++) {
                        			if (display_address.length() > 0) {
                        				display_address += ", ";
                        			}
                        			display_address += displayAddressArray.getString(j);
                        		}
                        		address.setField(AddressInfo.STREET, display_address);
                        	}
                      
                        	ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.YELP_LAYER, address, -1, null);
                        	landmark.setUrl(url);
             		   
                        	if (business.has("rating")) {
                        		landmark.setRating(business.getDouble("rating"));
                        	}
                        	if (business.has("review_count")) {
                        		landmark.setNumberOfReviews(business.getInt("review_count"));
                        	}

                        	String icon = business.optString("image_url");
                        	if (StringUtils.isNotEmpty(icon)) {
                        		landmark.setThumbnail(icon);
                        	}
                        
                        	Map<String, String> tokens = new HashMap<String, String>();
                        
                        	JSONUtils.putOptValue(tokens, "description", business, "snippet_text", false, stringLimit, false);

                        	//categories
                        	String category = "";
                        	JSONArray categories = business.optJSONArray("categories");
                        	if (categories != null && categories.length() > 0) {
                        		for (int j = 0; j < categories.length() ; j++) {
                        			JSONObject cat = categories.getJSONObject(j);
                        			category += cat.getString("title");
                        			if (j <  categories.length()-1) {
                        				category += ", ";
                        			}
                        		}
                        		if (StringUtils.isNotEmpty(category)) {
                        			tokens.put("category", category);
                        		}
                        		
                        	}
                                                
                        	landmark.setDescription(JSONUtils.buildLandmarkDesc(landmark, tokens, locale));
                                              
                        	landmarks.add(landmark);
                        }
                    }
                }
            } else {
            	handleError(jsonRoot, latitude, longitude);
            }
        }
        return total;
    }
	
	private void handleError(JSONObject root, double latitude, double longitude) {
		JSONObject error = root.optJSONObject("error");
		if (error != null && StringUtils.equals(error.optString("id"), "EXCEEDED_REQS")) {
			cacheProvider.put(USAGE_LIMIT_MARKER, "1");
			logger.log(Level.WARNING, "Yelp error: {0}", root);
		} else if (error != null && StringUtils.equals(error.optString("id"), "UNAVAILABLE_FOR_LOCATION")) {
			String key = LOCATION_UNAVAILABILITY_MARKER + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude);
        	cacheProvider.put(key, "1");
        	logger.log(Level.WARNING, "Yelp error: {0}", root);
		} else {
			logger.log(Level.SEVERE, "Received Yelp error response {0}", root);
		}
	}
	
	public String getLayerName() {
    	return Commons.YELP_LAYER;
    }
	
	private class ReviewDetailsRetriever implements Runnable {

		private Map<String, Map<String, String>> reviewsArray;
        private double latitude, longitude;
        private String query, locale;
        private int radius, offset;
        private boolean hasDeals;

        public ReviewDetailsRetriever(Map<String, Map<String, String>> reviewsArray,  double latitude, double longitude, String query, int radius, int offset, boolean hasDeals, String locale) {
            this.reviewsArray = reviewsArray;
            this.latitude = latitude;
            this.longitude = longitude;
            this.query = query;
            this.radius = radius;
            this.offset = offset;
            this.hasDeals = hasDeals;
            this.locale = locale;
        }

        public void run() {
            try {
            	String key = LOCATION_UNAVAILABILITY_MARKER + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude);
            	if (!cacheProvider.containsKey(key)) { 
            		String responseBody = processRequest(latitude, longitude, query, radius, hasDeals, offset, locale);
            		createCustomJsonReviewsList(responseBody, latitude, longitude, reviewsArray);
            	} else {
            		logger.log(Level.INFO, "Yelp api is unavailable from this location.");
            	}
            } catch (Exception e) {
                logger.log(Level.SEVERE, "ReviewDetailsRetriever.run exception:", e);
            } 
        }
    }

    private class VenueDetailsRetriever implements Runnable {

    	private List<? extends Object> venueArray;
        private double latitude, longitude;
        private String query, format;
        private int radius, offset, stringLimit;
        private boolean hasDeals;
        private Locale locale;

        public VenueDetailsRetriever(List<? extends Object> venueArray, double latitude, double longitude, String query, int radius, int offset, boolean hasDeals, int stringLimit, String format, Locale locale) {
            this.venueArray = venueArray;
            this.latitude = latitude;
            this.longitude = longitude;
            this.query = query;
            this.radius = radius;
            this.offset = offset;
            this.hasDeals = hasDeals;
            this.stringLimit = stringLimit;
            this.format = format;
            this.locale = locale;
        }

        public void run() {
            try {
            	String key = LOCATION_UNAVAILABILITY_MARKER + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude);
            	if (!cacheProvider.containsKey(key)) { 
            		String l = null;
            		if (locale != null ) {
            			l = locale.getLanguage() ;
            			if (StringUtils.isNotEmpty(locale.getCountry())) {
                			l += "_" + locale.getCountry();
                		}
            		}
            		
            		String responseBody = processRequest(latitude, longitude, query, radius, hasDeals, offset, l);
            		if (format.equals("bin")) {
            			createCustomLandmarkYelpList(responseBody, (List<ExtendedLandmark>)venueArray, latitude, longitude, stringLimit, hasDeals, locale);
            		} else {
            			createCustomJsonYelpList(responseBody, (List<Object>)venueArray, latitude, longitude, stringLimit, hasDeals);
            		}
            	} else {
            		logger.log(Level.INFO, "Yelp api is unavailable from this location.");
            	}
            } catch (Exception e) {
                logger.log(Level.SEVERE, "VenueDetailsRetriever.run exception:", e);
            } 
        }
    }
}
