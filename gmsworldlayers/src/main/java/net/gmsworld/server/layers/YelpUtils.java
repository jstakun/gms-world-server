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

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.AuthUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadManager;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class YelpUtils extends LayerHelper {

	private static final String USAGE_LIMIT_MARKER = "YelpUsageLimitsMarker";
	private static final String LOCATION_UNAVAILABILITY_MARKER = "YelpLocationUnavailabilityMarker";	
	
    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String hasDeals, String language) throws Exception {
        int normalizedRadius = NumberUtils.normalizeNumber(radius, 1000, 40000);
        int normalizedLimit = NumberUtils.normalizeNumber(limit, 20, 100);
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, normalizedRadius, version, normalizedLimit, stringLimit, hasDeals, language);

        String cachedResponse = cacheProvider.getString(key);
        if (cachedResponse == null) {
        	List<Object> venueArray = new ArrayList<Object>();
            
        	if (!cacheProvider.containsKey(USAGE_LIMIT_MARKER)) {
        		ThreadManager threadManager = new ThreadManager(threadProvider);
                boolean isDeal = Boolean.parseBoolean(hasDeals);
        		int offset = 0;

        		while (offset < normalizedLimit) {
        			threadManager.put(new VenueDetailsRetriever(venueArray, lat, lng, query, normalizedRadius, offset, isDeal, stringLimit, language, "json", null));
        			offset += 20;
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

    private String processRequest(double latitude, double longitude, String query, int radius, boolean hasDeals, int offset, String language) throws OAuthException, IOException {
    	String responseBody = null;
    	
    	if (!cacheProvider.containsKey(USAGE_LIMIT_MARKER)) {
    	
    		OAuthHmacSha1Signer hmacSigner = new OAuthHmacSha1Signer();
    		OAuthParameters parameters = new OAuthParameters();
    		parameters.setOAuthConsumerKey(Commons.getProperty(Property.YELP_Consumer_Key));
    		parameters.setOAuthConsumerSecret(Commons.getProperty(Property.YELP_Consumer_Secret));
    		parameters.setOAuthToken(Commons.getProperty(Property.YELP_Token));
    		parameters.setOAuthTokenSecret(Commons.getProperty(Property.YELP_Token_Secret));
    		parameters.setOAuthTimestamp(Long.toString(System.currentTimeMillis()));
    		int nonce = (int) (Math.random() * 1e8);
    		parameters.setOAuthNonce(Integer.toString(nonce));
    		parameters.setOAuthSignatureMethod("HMAC-SHA1");

    		//sort: Sort mode: 0=Best matched (default), 1=Distance, 2=Highest Rated
    		String urlString = "http://api.yelp.com/v2/search?ll=" + StringUtil.formatCoordE6(latitude) + "," + StringUtil.formatCoordE6(longitude) + "&radius_filter=" + radius; // + "&sort=1";

    		if (StringUtils.isNotEmpty(query)) {
    			urlString += "&term=" + URLEncoder.encode(query, "UTF-8");
    		}
        
    		if (offset >= 0) {
    			urlString += "&offset=" + offset;
    		}
        
    		if (hasDeals) {
    			urlString += "&deals_filter=true";
    		}
        
    		if (StringUtils.isNotEmpty(language)) {
    			urlString += "&lang=" + language + "&cc=" + language;
    		}

    		//System.out.println("Calling: " + urlString);

    		String baseString = OAuthUtil.getSignatureBaseString(urlString, "GET", parameters.getBaseParameters());
    		String signature = hmacSigner.getSignature(baseString, parameters);
    		parameters.addCustomBaseParameter("oauth_signature", signature);

    		responseBody = HttpUtils.processFileRequestWithAuthn(new URL(urlString), AuthUtils.buildAuthHeaderString(parameters));

    		//System.out.println(responseBody);
    	}

        return responseBody;
    }

    private int createCustomJsonYelpList(String yelpJson, List<Object> jsonArray, double latitude, double longitude, int stringLimit, boolean hasDeals) throws JSONException {
        int total = 0;
        if (StringUtils.startsWith(yelpJson, "{")) {
            JSONObject jsonRoot = new JSONObject(yelpJson);
            if (jsonRoot.has("total")) {
                total = jsonRoot.getInt("total");
                if (total > 0) {
                    //System.out.println("total: " + total);
                    JSONArray businesses = jsonRoot.getJSONArray("businesses");
                    for (int i = 0; i < businesses.length(); i++) {
                        JSONObject business = businesses.getJSONObject(i);

                        Map<String, Object> jsonObject = new HashMap<String, Object>();

                        JSONObject location = business.getJSONObject("location");
                        JSONObject coordinate = location.getJSONObject("coordinate");

                        jsonObject.put("name", business.getString("name"));
                        jsonObject.put("lat", Double.toString(coordinate.getDouble("latitude")));
                        jsonObject.put("lng", Double.toString(coordinate.getDouble("longitude")));
                        jsonObject.put("url", business.getString("mobile_url"));

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
                        	String[] categoryCodes = new String[categories.length()];
                        	for (int j = 0; j < categories.length() ; j++) {
                        		JSONArray cat = categories.getJSONArray(j);
                        		category += cat.getString(0);
                        		categoryCodes[j] = cat.getString(1);
                        		if (j <  categories.length()-1) {
                        			category += ", ";
                        		}
                        	}
                        	if (StringUtils.isNotEmpty(category)) {
                        		desc.put("category", category);
                        	}
                        	if (hasDeals) {
                        		String[] categoryCode = YelpCategoryMapping.findMapping(categoryCodes);
                        		jsonObject.put("categoryID", categoryCode[0]);
                        		String subcat = categoryCode[1];
                        		if (StringUtils.isNotEmpty(subcat)) {
                        			jsonObject.put("subcategoryID", subcat);
                        		}
                        	}
                        }
                        
                        //deals
                        JSONArray deals = business.optJSONArray("deals");
                        if (deals != null && deals.length() > 0) {
                        	for (int d = 0; d < deals.length(); d++) {
                                JSONObject deal = deals.getJSONObject(d);
                                
                                desc.put("start_date", Long.toString(deal.getLong("time_start")*1000));
                                if (deal.has("time_end")) {
                                	desc.put("end_date", Long.toString(deal.getLong("time_end")*1000));
                                }
                                jsonObject.put("url", deal.getString("url"));
                                jsonObject.put("name", deal.getString("title") + " Deal At " + business.getString("name"));
                                //desc.put("icon", deal.getString("image_url"));
                                String description = ""; 
                                if (deal.has("what_you_get")) {
                                	description = "<b>What You Get</b><br/>" + deal.getString("what_you_get") + "<br/>";
                                } if (deal.has("important_restrictions")) {
                                	description += "<b>Important Restrictions</b><br/>" + deal.getString("important_restrictions") + "<br/>";
                                } if (deal.has("additional_restrictions")) {
                                	description += "<b>Additional Restrictions</b><br/>" + deal.getString("additional_restrictions") + "<br/>";
                                }
                                desc.put("description", description);
                                JSONArray options = deal.getJSONArray("options");
                                
                                JSONObject option = options.getJSONObject(0);
                                
                                desc.put("price", option.getString("formatted_price"));
                                
                                double original_price = option.getDouble("original_price");
                                double price = option.getDouble("price");
                                
                                double discount = 100 - (price / original_price * 100);
                                desc.put("discount", Double.toString(discount) + "%");
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
            if (jsonRoot.has("total")) {
                total = jsonRoot.getInt("total");
                if (total > 0) {
                    //System.out.println("total: " + total);
                    JSONArray businesses = jsonRoot.getJSONArray("businesses");
                    for (int i = 0; i < businesses.length(); i++) {
                        JSONObject business = businesses.getJSONObject(i);

                        String phone = business.optString("phone");
                        if (phone != null) {
                            Map<String, String> desc = new HashMap<String, String>();

                            //add desc

                            phone = phone.replaceAll("[^\\d]", "");

                            //System.out.println("Adding review " + phone);

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

    public Map<String, Map<String, String>> processReviewsRequest(double latitude, double longitude, String query, int radius, int limit, boolean hasDeals, String language) throws JSONException, IOException, OAuthException {
        
    	Map<String, Map<String, String>> reviewsArray = new HashMap<String, Map<String, String>>();
    	
    	if (!cacheProvider.containsKey(USAGE_LIMIT_MARKER)) {
        	
    		ThreadManager threadManager = new ThreadManager(threadProvider);
            int normalizedRadius = NumberUtils.normalizeNumber(radius, 1000, 40000);
    		int offset = 0;

    		while (offset < limit) {
    			threadManager.put(new ReviewDetailsRetriever(reviewsArray, latitude, longitude, query, normalizedRadius, offset, hasDeals, language));
    			offset += 20;
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
			language = locale.getLanguage();
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
        			threadManager.put(new VenueDetailsRetriever(landmarks,
                        lat, lng, query, normalizedRadius, offset, isDeal, stringLimit, language, "bin", locale));
        			offset += 20;
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
            if (jsonRoot.has("total")) {
                total = jsonRoot.getInt("total");
                if (total > 0) {
                    //System.out.println("total: " + total);
                    JSONArray businesses = jsonRoot.getJSONArray("businesses");
                    for (int i = 0; i < businesses.length(); i++) {
                        JSONObject business = businesses.getJSONObject(i);

                        JSONObject location = business.getJSONObject("location");
                        
                        JSONObject coordinate = location.optJSONObject("coordinate");

                        if (coordinate != null) {
                        	String name = business.getString("name");
                        	String url = business.getString("mobile_url");

                        	QualifiedCoordinates qc = new QualifiedCoordinates(coordinate.getDouble("latitude"), coordinate.getDouble("longitude"), 0f, 0f, 0f);
             		   
                        	AddressInfo address = new AddressInfo();
                        	if (business.has("display_phone") && !business.isNull("display_phone")) {
                        		address.setField(AddressInfo.PHONE_NUMBER, business.getString("display_phone"));
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
                        		String[] categoryCodes = new String[categories.length()];
                        		for (int j = 0; j < categories.length() ; j++) {
                        			JSONArray cat = categories.getJSONArray(j);
                        			category += cat.getString(0);
                        			categoryCodes[j] = cat.getString(1);
                        			if (j <  categories.length()-1) {
                        				category += ", ";
                        			}
                        		}
                        		if (StringUtils.isNotEmpty(category)) {
                        			tokens.put("category", category);
                        		}
                        		if (hasDeals) {
                        			String[] categoryCode = YelpCategoryMapping.findMapping(categoryCodes);
                        			landmark.setCategoryId(Integer.valueOf(categoryCode[0]).intValue());
                        			String subcat = categoryCode[1];
                        			if (StringUtils.isNotEmpty(subcat)) {
                        				landmark.setSubCategoryId(Integer.valueOf(subcat).intValue());
                        			}
                        		}
                        	}
                        
                        	//deals
                        	JSONArray deals = business.optJSONArray("deals");
                        	if (deals != null && deals.length() > 0) {
                        		for (int d = 0; d < deals.length(); d++) {
                        			JSONObject deal = deals.getJSONObject(d);
                                
                        			long creationDate = deal.getLong("time_start")*1000;
                        			landmark.setCreationDate(creationDate);
                        			tokens.put("start_date", Long.toString(creationDate));
                        			long endDate = 0;
                        			if (deal.has("time_end")) {
                        				endDate = deal.getLong("time_end")*1000;
                        				tokens.put("end_date", Long.toString(endDate));
                        			}
                        			landmark.setUrl(deal.getString("url"));
                        			landmark.setName(deal.getString("title") + " Deal At " + business.getString("name"));
                        			//desc.put("icon", deal.getString("image_url"));
                        			String description = ""; 
                        			if (deal.has("what_you_get")) {
                        				description = "<b>What You Get</b><br/>" + deal.getString("what_you_get") + "<br/>";
                        			} if (deal.has("important_restrictions")) {
                        				description += "<b>Important Restrictions</b><br/>" + deal.getString("important_restrictions") + "<br/>";
                        			} if (deal.has("additional_restrictions")) {
                        				description += "<b>Additional Restrictions</b><br/>" + deal.getString("additional_restrictions") + "<br/>";
                        			}
                        			tokens.put("description", description);
                        			String currencyCode = deal.getString("currency_code");
                        			JSONArray options = deal.getJSONArray("options");
                                
                        			JSONObject option = options.getJSONObject(0);
                                
                        			double original_price = option.getDouble("original_price") / 100d;
                        			double price = option.getDouble("price") / 100d;
                        			double save = (original_price - price);
                                
                        			double discount = (100d - (price / original_price * 100d)) / 100d;
                                
                                	Deal dealObj = new Deal(price, discount, save, null, currencyCode);
                                	dealObj.setEndDate(endDate);
                                	landmark.setDeal(dealObj);
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
        private String query, language;
        private int radius, offset;
        private boolean hasDeals;

        public ReviewDetailsRetriever(Map<String, Map<String, String>> reviewsArray,
                double latitude, double longitude, String query, int radius, int offset, boolean hasDeals, String language) {
            this.reviewsArray = reviewsArray;
            this.latitude = latitude;
            this.longitude = longitude;
            this.query = query;
            this.radius = radius;
            this.offset = offset;
            this.hasDeals = hasDeals;
            this.language = language;
        }

        public void run() {
            try {
            	String key = LOCATION_UNAVAILABILITY_MARKER + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude);
            	if (!cacheProvider.containsKey(key)) { 
            		String responseBody = processRequest(latitude, longitude, query, radius, hasDeals, offset, language);
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
        private String query, language, format;
        private int radius, offset, stringLimit;
        private boolean hasDeals;
        private Locale locale;

        public VenueDetailsRetriever(List<? extends Object> venueArray, double latitude, double longitude, String query, int radius,
                int offset, boolean hasDeals, int stringLimit, String language, String format, Locale locale) {
            this.venueArray = venueArray;
            this.latitude = latitude;
            this.longitude = longitude;
            this.query = query;
            this.radius = radius;
            this.offset = offset;
            this.hasDeals = hasDeals;
            this.stringLimit = stringLimit;
            this.language = language;
            this.format = format;
            this.locale = locale;
        }

        public void run() {
            try {
            	String key = LOCATION_UNAVAILABILITY_MARKER + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude);
            	if (!cacheProvider.containsKey(key)) { 
            		String responseBody = processRequest(latitude, longitude, query, radius, hasDeals, offset, language);
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
