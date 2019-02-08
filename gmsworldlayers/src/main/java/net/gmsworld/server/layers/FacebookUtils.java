package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.json.Json;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.json.JsonValue;
import com.restfb.types.User;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadManager;

/**
 *
 * @author jstakun
 */
public class FacebookUtils extends LayerHelper {

	private static final String FBPLACES_PREFIX = "http://touch.facebook.com/profile.php?id=";
	
	@Override
	public JSONObject processRequest(double latitude, double longitude, String query, int distance, int version, int limit, int stringLength, String fbtoken, String flexString2) throws Exception {
		JSONObject response = null;
		if (isEnabled()) {  
			int dist = NumberUtils.normalizeNumber(distance, 1000, 50000);
        	String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, dist, version, limit, stringLength, fbtoken, flexString2);
        	String cachedResponse = cacheProvider.getString(key);
        	
        	if (cachedResponse == null) {
        		String token = fbtoken;
        		if (!StringUtils.isNotEmpty(token)) {
        			token = Commons.getProperty(Property.fb_app_token);
        		}
        		FacebookClient facebookClient = getFacebookClient(token);

            	JsonObject placesSearch = null;

            	if (query != null && query.length() > 0) {
                	placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("q", query), Parameter.with("limit", limit), Parameter.with("fields", "name,location,website,picture.type(large),phone,description"));
            	} else {
                	placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("limit", limit), Parameter.with("fields", "name,location,website,picture.type(large),phone,description"));
            	}

            	JsonArray data = placesSearch.get("data").asArray();

            	int dataSize = data.size();

            	List<String> pages = new ArrayList<String>();
            	for (int i = 0; i < dataSize; i++) {
                	JsonObject place = (JsonObject) data.get(i);
                	pages.add(place.get("id").asString());
            	}

            	Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();

            	readFacebookPlacesDetails(facebookClient, pages, pageDescs, stringLength);

            	response = createCustomJsonFacebookList(data, pageDescs);

            	logger.log(Level.INFO, "No of FB places {0}", dataSize);

            	if (dataSize > 0) {
                	cacheProvider.put(key, response.toString());
                	logger.log(Level.INFO, "Adding FB landmark list to cache with key {0}", key);
            	}
        	} else {
            	logger.log(Level.INFO, "Reading FB landmark list from cache with key {0}", key);
            	response = new JSONObject(cachedResponse);
        	}
		} else {
    		response = new JSONObject().put("ResultSet", new JSONArray());
    	}
        return response;
    }

    private static JSONObject createCustomJsonFacebookList(JsonArray data, Map<String, Map<String, String>> pageDescs) throws Exception {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            JsonObject place = (JsonObject) data.get(i);
            JsonObject location = place.get("location").asObject();
            if (location.names().contains("latitude") && location.names().contains("longitude")) {

                double lat = location.get("latitude").asDouble();
                double lng = location.get("longitude").asDouble();
                
                String placeid = place.get("id").asString();

                jsonObject.put("name", place.get("name").asString());
                jsonObject.put("url", placeid);
                Map<String, String> desc = new HashMap<String, String>();

                Map<String, String> pageDesc = pageDescs.remove(placeid);

                if (pageDesc != null) {
                    desc.putAll(pageDesc);
                }

                processCategories(place, desc);
                
                jsonObject.put("lat", lat);
                jsonObject.put("lng", lng);

                for (String name : location.names()) {
                	if (!(name.equals("latitude") || name.equals("longitude"))) {
                		String value = location.get(name).asString();
                		if (StringUtils.isNotEmpty(value)) {
                			desc.put(name, value);
                		}
                	}
                }
                jsonObject.put("desc", desc);

                jsonArray.add(jsonObject);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);

        return json;
    }
    
    private static List<ExtendedLandmark> createCustomLandmarkFacebookList(List<JsonObject> places, Map<String, Map<String, String>> pageDescs, Locale locale) {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        for (JsonObject place : places) {
        	String name = place.get("name").asString();
        	JsonObject location = place.get("location").asObject();
            if (location.names().contains("latitude") && location.names().contains("longitude")) {
            	
                double lat = location.get("latitude").asDouble();
                double lng = location.get("longitude").asDouble();
                
                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
     		    
                String placeid = place.get("id").asString();

                Map<String, String> pageDesc = pageDescs.remove(placeid);
                if (pageDesc == null) {
                	pageDesc = new HashMap<String, String>();
                }
                
                String url = pageDesc.remove("url");
                if (url == null) {
                	url =  FBPLACES_PREFIX + placeid;
                }
                
                AddressInfo address = new AddressInfo();
                if (location.names().contains("street")) {
                	String val = location.get("street").asString();
                	if (StringUtils.isNotEmpty(val)) {
                		address.setField(AddressInfo.STREET, val);
                	}
                }
                if (location.names().contains("city")) {
                	String val = location.get("city").asString();;
                	if (StringUtils.isNotEmpty(val)) {
                		address.setField(AddressInfo.CITY, val);
                	}
                }
                if (location.names().contains("country")) {
                	String val = location.get("country").asString();
                	if (StringUtils.isNotEmpty(val)) {
                		address.setField(AddressInfo.COUNTRY, val);
                	}
                }
                if (location.names().contains("zip")) {
                	String val = location.get("zip").asString();
                	if (StringUtils.isNotEmpty(val)) {
                		address.setField(AddressInfo.POSTAL_CODE, val);
                	}
                }
                if (location.names().contains("state")) {
                    String val = location.get("state").asString();
                    if (StringUtils.isNotEmpty(val)) {
                    	address.setField(AddressInfo.STATE, val);
                    }
                }
                if (location.names().contains("phone")) {
                    String val = pageDesc.remove("phone");
                    if (StringUtils.isNotEmpty(val) && val.matches(".*\\d+.*")) {
                    	address.setField(AddressInfo.PHONE_NUMBER, val);
                    }
                }
                
                long creationDate = NumberUtils.getLong(pageDesc.remove("creation_date"), -1);
                
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.FACEBOOK_LAYER, address, creationDate, null);
     		    landmark.setUrl(url);
     		    String thumbnail = pageDesc.remove("thumbnail");
     		    if (thumbnail == null) {
     		    	thumbnail = pageDesc.remove("icon");
     		    } else {
     		    	pageDesc.remove("icon"); //remove icon from desc
     		    }
     		    if (thumbnail != null) {
                	 landmark.setThumbnail(thumbnail);
                }
     		    
     		    if (pageDesc.containsKey("rating")) {
     			    landmark.setRating(Double.valueOf(pageDesc.remove("rating")));
     		    }
   		    
     		    if (pageDesc.containsKey("numberOfReviews")) {
     			    landmark.setNumberOfReviews(Integer.valueOf(pageDesc.remove("numberOfReviews")));
     		    }
   		        
            	String desc = JSONUtils.buildLandmarkDesc(landmark, pageDesc, locale);
     		    landmark.setDescription(desc);		   
             
     		    landmarks.add(landmark);
            } else {
            	logger.log(Level.WARNING, "Object {0} has no coordinates.", name);
            }
        }

        return landmarks;
    }

	private static void processCategories(JsonObject place, Map<String, String> pageDesc) {
		if (place.names().contains("category_list")) {
			JsonArray category_list = place.get("category_list").asArray();
			Set<String> categories = new HashSet<String>();
			for (int j=0;j<category_list.size();j++) {
				JsonObject cat = category_list.get(j).asObject();
				categories.add(cat.get("name").asString());
			}
			if (!categories.isEmpty()) {
				pageDesc.put("category", StringUtils.join(categories, ", "));
			}
		}  else if (place.names().contains("category")) {
		    pageDesc.put("category", place.get("category").asString());
		}
	}
    
    private static List<ExtendedLandmark> createCustomLandmarkFacebookList(JsonArray data, Locale locale, int stringLength)  {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        for (JsonValue value : data) {
        	JsonObject place = value.asObject();
        	String name = place.get("name").asString();
        	JsonObject location = place.get("location").asObject();
            if (location.names().contains("latitude") && location.names().contains("longitude")) {
                
            	double lat = location.get("latitude").asDouble();
                double lng = location.get("longitude").asDouble();
               
                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
     		    
                String placeid = place.get("id").asString();

                Map<String, String> pageDesc = new HashMap<String, String>();
                
                String url  = null;
                if (place.names().contains("website")) {
                	url = place.get("website").asString();
                }
                if (StringUtils.isEmpty(url)) {
                	url =  FBPLACES_PREFIX + placeid;
                }
                
                processCategories(place, pageDesc);

                AddressInfo address = new AddressInfo();
                if (location.names().contains("street")) {
                	String val = location.get("street").asString();
                	if (StringUtils.isNotEmpty(val)) {
                		address.setField(AddressInfo.STREET, val);
                	}
                }
                if (location.names().contains("city")) {
                    String val = location.get("city").asString();
                    if (StringUtils.isNotEmpty(val)) {
                    	address.setField(AddressInfo.CITY, val);
                    }
                }
                if (location.names().contains("country")) {
                    String val = location.get("country").asString();
                    if (StringUtils.isNotEmpty(val)) {
                    	address.setField(AddressInfo.COUNTRY, val);
                    }
                }
                if (location.names().contains("zip")) {
                    String val = location.get("zip").asString();
                    if (StringUtils.isNotEmpty(val)) {
                    	address.setField(AddressInfo.POSTAL_CODE, val);
                    }
                }
                if (location.names().contains("state")) { 
                	String val = location.get("state").asString();
                	if (StringUtils.isNotEmpty(val)) {
                		address.setField(AddressInfo.STATE, val);
                	}
                }
                if (location.names().contains("phone")) {
                	String val = place.get("phone").asString();
                	if (StringUtils.isNotEmpty(val) && val.matches(".*\\d+.*")) {
                		address.setField(AddressInfo.PHONE_NUMBER, val);
                	}
                }  
                long creationDate = NumberUtils.getLong(pageDesc.remove("creation_date"), -1);
                
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.FACEBOOK_LAYER, address, creationDate, null);
     		    landmark.setUrl(url);
     		    
     		    JsonObject picture = place.get("picture").asObject();
    		    if (picture != null) {
    		    	landmark.setThumbnail(picture.get("data").asObject().get("url").asString());
    		    }
     		    
    		    if (place.names().contains("description")) {
    		    	pageDesc.put("description", StringUtils.abbreviate(place.get("description").asString(), stringLength));
    		    }
    		    
     		    if (place.names().contains("overall_star_rating")) {
    		    	landmark.setRating(place.get("overall_star_rating").asDouble());
    		    }
    		    if (place.names().contains("rating_count")) {
    		    	landmark.setNumberOfReviews(place.get("rating_count").asInt());
    		    }
    		    if (place.names().contains("fan_count")) {
    		        pageDesc.put("Likes", place.get("fan_count").asString());
    		    }
    		    
    		    if (place.names().contains("price_range")) {
    		    	pageDesc.put("Pricing", place.get("price_range").asString());
    		    }
     		    
            	String desc = JSONUtils.buildLandmarkDesc(landmark, pageDesc, locale);
     		    landmark.setDescription(desc);		   
             
     		    landmarks.add(landmark);
            } else {
            	logger.log(Level.WARNING, "Object {0} has no coordinates.", name);
            }
        }

        return landmarks;
    }

    private void readFacebookPlacesDetails(FacebookClient facebookClient, List<String> pages, Map<String, Map<String, String>> pageDescs, int stringLength) {
        if (!pages.isEmpty()) {
            //limited due to url fetch limit = 2048 characters
            int first = 0, last = 50;

            ThreadManager threadManager = new ThreadManager(threadProvider);
            
            while (first < pages.size()) {
                //System.out.println("sublist: " + first + " " + last);
                if (last > pages.size()) {
                    last = pages.size();
                }
                threadManager.put(new VenueDetailsRetriever(pageDescs, facebookClient, pages.subList(first, last), stringLength));
                first = last;
                last += 50;
            }

            threadManager.waitForThreads();
        }
    }

    protected List<String> getMyFriends(String token) {

    	List<String> friendIds = new ArrayList<String>();
    	FacebookClient facebookClient = getFacebookClient(token);
    	List<User> myFriends = facebookClient.fetchConnection("me/friends", User.class).getData();
    	for (User friend : myFriends) {
    		friendIds.add(friend.getId());
    		System.out.println(friend.getId() + ": " + friend.getName());
    	}

    	return friendIds;
    }
    
    @Override
	public List<ExtendedLandmark> loadLandmarks(double latitude, double longitude, String query, int distance, int version, int limit, int stringLength, String fbtoken, String flexString2, Locale locale, boolean useCache) throws Exception {
		if (distance < 1000) {
			distance = distance * 1000;
		}
    	int dist = NumberUtils.normalizeNumber(distance, 1000, 50000);

        String token = fbtoken;
        if (StringUtils.isEmpty(token)) {
        	token = Commons.getProperty(Property.fb_app_token);
        }
        FacebookClient facebookClient = getFacebookClient(token);
        JsonObject placesSearch = null;
        
        String picture = "picture.type(normal)";
        if (stringLength == StringUtil.XLARGE) {
        	picture = "picture.type(large)";
        }

        if (query != null && query.length() > 0) {
        	placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("q", query), Parameter.with("limit", limit), Parameter.with("fields", "name,location,website,phone,description,category_list,overall_star_rating,rating_count,fan_count,price_range," + picture));
        } else {
        	placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("limit", limit), Parameter.with("fields", "name,location,website,phone,description,category_list,overall_star_rating,rating_count,fan_count,price_range," + picture));
        }
  
        return createCustomLandmarkFacebookList(placesSearch.get("data").asArray(), locale, stringLength);
	}
    
    public static FacebookClient getFacebookClient(String token) {
    	return new DefaultFacebookClient(token, Version.VERSION_3_2);
    }
    
    public String getLayerName() {
    	return Commons.FACEBOOK_LAYER;
    }
    
    public List<ExtendedLandmark> getMyTaggedPlaces(int version, int limit, int stringLength, String token, Locale locale, boolean useCache) throws UnsupportedEncodingException, ParseException {
    	String key = null;
    	List<ExtendedLandmark> landmarks = null;
    	if (useCache) {
        	key = getCacheKey(getClass(), "getMyTaggedPlaces", 0, 0, null, 0, version, limit, stringLength, token, null);
        	landmarks = cacheProvider.getList(ExtendedLandmark.class, key);
    	}
        if (landmarks == null) {
        	FacebookClient facebookClient = getFacebookClient(token);
        	List<JsonObject> placesSearch = facebookClient.fetchConnection("me/tagged_places", JsonObject.class, Parameter.with("limit", limit * 3), Parameter.with("fields", "place,created_time")).getData();
        	int dataSize = placesSearch.size();
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        	Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();     
        	List<String> pages = new ArrayList<String>(dataSize);
        	List<JsonObject> places = new ArrayList<JsonObject>(dataSize);
        	logger.log(Level.INFO, "Found " + dataSize + " tagged places.");
        	for (JsonObject tagged : placesSearch) {
        		JsonObject place = tagged.get("place").asObject();
        		String placeid = place.get("id").asString();
        		if (!pages.contains(placeid)) {
        			pages.add(placeid);
        			places.add(place);
        			Map<String, String> pageDesc = new HashMap<String, String>();
        			Date d = sdf.parse(tagged.get("created_time").asString());//2015-05-05T06:20:42+0000
        			pageDesc.put("tagged", "1");  
        			pageDesc.put("creation_date", Long.toString(d.getTime()));
        			pageDescs.put(placeid, pageDesc);
        		}
        	}

        	readFacebookPlacesDetails(facebookClient, pages, pageDescs, stringLength);
        	landmarks = createCustomLandmarkFacebookList(places, pageDescs, locale);
        	
        	for (ExtendedLandmark landmark : landmarks) {
        		landmark.setHasCheckinsOrPhotos(true);
        	}

        	if (landmarks.size() > limit) {
        		landmarks = landmarks.subList(0, limit);
        	}
        	
        	logger.log(Level.INFO, "No of unique FB tagged places {0}", landmarks.size());

        	if (useCache && !landmarks.isEmpty()) {
        		cacheProvider.put(key, landmarks);
        		logger.log(Level.INFO, "Adding FB landmark list to cache with key {0}", key);
        	}
        } else {
        	logger.log(Level.INFO, "Reading FB landmark list from cache with key {0}", key);
        }
        logger.log(Level.INFO, "Found {0} landmarks", landmarks.size()); 
        return landmarks;
    }
    
    public List<ExtendedLandmark> getMyPlaces(int version, int limit, int stringLength, String token, Locale locale, boolean useCache) throws UnsupportedEncodingException, ParseException {
    	String key = null;
    	List<ExtendedLandmark> landmarks = null;
    	if (useCache) {
    		key = getCacheKey(getClass(), "getMyPlaces", 0, 0, null, 0, version, limit, stringLength, token, null);
    		landmarks = cacheProvider.getList(ExtendedLandmark.class, key);
    	}
        if (landmarks == null) {
        	FacebookClient facebookClient = getFacebookClient(token);
        	//me/feed?with=location, me/posts
        	List<JsonObject> placesSearch = facebookClient.fetchConnection("me/feed", JsonObject.class, Parameter.with("with", "location"), Parameter.with("limit", limit), Parameter.with("fields", "place,from,created_time")).getData();
        	int dataSize = placesSearch.size();
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        	Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();     
        	List<String> pages = new ArrayList<String>(dataSize);
        	List<JsonObject> places = new ArrayList<JsonObject>(dataSize);
        	logger.log(Level.INFO, "Found " + dataSize + " places.");
        	for (JsonObject post : placesSearch) {
        		if (post.names().contains("place")) {
        			JsonObject place = post.get("place").asObject();
        			String placeid = place.get("id").asString();
        			if (!pages.contains(placeid)) {
        				pages.add(placeid);
        				places.add(place);
        				JsonObject from = post.get("from").asObject();
        				Map<String, String> pageDesc = new HashMap<String, String>();
        				Date d = sdf.parse(post.get("created_time").asString());//2015-05-05T06:20:42+0000
        				pageDesc.put("checkin_user", from.get("name").asString());
        				pageDesc.put("creation_date", Long.toString(d.getTime()));
        				pageDescs.put(placeid, pageDesc);
        			}
        		} 
        	}

        	readFacebookPlacesDetails(facebookClient, pages, pageDescs, stringLength);
        	landmarks = createCustomLandmarkFacebookList(places, pageDescs, locale);
        	
        	for (ExtendedLandmark landmark : landmarks) {
        		landmark.setHasCheckinsOrPhotos(true);
        	}

        	if (landmarks.size() > limit) {
        		landmarks = landmarks.subList(0, limit);
        	}
        	
        	logger.log(Level.INFO, "No of unique FB places {0}", landmarks.size());

        	if (useCache && !landmarks.isEmpty()) {
        		cacheProvider.put(key, landmarks);
        		logger.log(Level.INFO, "Adding FB landmark list to cache with key {0}", key);
        	}
        } else {
        	logger.log(Level.INFO, "Reading FB landmark list from cache with key {0}", key);
        }
        logger.log(Level.INFO, "Found {0} landmarks", landmarks.size()); 
        
        return landmarks;
    } 
    
    public List<ExtendedLandmark> getMyPhotos(int version, int limit, int stringLength, String token, Locale locale, boolean useCache) throws UnsupportedEncodingException, ParseException {
    	String key = null;
    	List<ExtendedLandmark> landmarks = null;
    	if (useCache) {
    		key = getCacheKey(getClass(), "getMyPhotos", 0, 0, null, 0, version, limit, stringLength, token, null);
    		landmarks = cacheProvider.getList(ExtendedLandmark.class, key);
    	}
    	if (landmarks == null) {
        	FacebookClient facebookClient = getFacebookClient(token);
        	List<JsonObject> photos = facebookClient.fetchConnection("me/photos", JsonObject.class, Parameter.with("type","uploaded"), Parameter.with("limit", limit), Parameter.with("fields", "picture,place,from,created_time,link")).getData();
        	int dataSize = photos.size();
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        	Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();     
        	List<String> placeIds = new ArrayList<String>(dataSize);
        	List<JsonObject> places = new ArrayList<JsonObject>(dataSize);
        	logger.log(Level.INFO, "Found " + dataSize + " photos.");
        	for (JsonObject photo : photos) {
        		if (photo.names().contains("place")) {
        			//TODO add support for multiple photos per page
        			JsonObject place = photo.get("place").asObject();
        			String placeid = place.get("id").asString();
        			if (!placeIds.contains(placeid)) {
        				placeIds.add(placeid);
        				places.add(place);
        				
        				JsonObject from = photo.get("from").asObject();
        				Map<String, String> pageDesc = new HashMap<String, String>();
        				Date d = sdf.parse(photo.get("created_time").asString());//2015-05-05T06:20:42+0000
        				pageDesc.put("photoUser", from.get("name").asString());
        				pageDesc.put("thumbnail", photo.get("picture").asString());  
        				pageDesc.put("caption", photo.get("link").asString());
        				pageDesc.put("url", photo.get("link").asString());
        				pageDesc.put("creation_date", Long.toString(d.getTime()));
        				
        				pageDescs.put(placeid, pageDesc);
        			}
        		} 
        	}
        	
        	readFacebookPlacesDetails(facebookClient, placeIds, pageDescs, stringLength);
        	landmarks = createCustomLandmarkFacebookList(places, pageDescs, locale);
        	
        	for (ExtendedLandmark landmark : landmarks) {
        		landmark.setHasCheckinsOrPhotos(true);
        	}

        	if (landmarks.size() > limit) {
        		landmarks = landmarks.subList(0, limit);
        	}
        	
        	logger.log(Level.INFO, "No of unique FB places {0}", landmarks.size());

        	if (useCache && !landmarks.isEmpty()) {
        		cacheProvider.put(key, landmarks);
        		logger.log(Level.INFO, "Adding FB photo list to cache with key {0}", key);
        	}
        } else {
        	logger.log(Level.INFO, "Reading FB photo list from cache with key {0}", key);
        }
        
        logger.log(Level.INFO, "Found {0} landmarks", landmarks.size()); 
    	
    	return landmarks;
    } 
    
    
    private static class VenueDetailsRetriever implements Runnable {

        private FacebookClient facebookClient;
        private Map<String, Map<String, String>> pageDescs;
        private int stringLimit;
        private List<String> pageIds;

        public VenueDetailsRetriever(Map<String, Map<String, String>> pageDescs, FacebookClient facebookClient, List<String> pageIds, int stringLimit) {
            this.pageDescs = pageDescs;
            this.facebookClient = facebookClient;
            this.pageIds = pageIds;
            this.stringLimit = stringLimit;
        }

        public void run() {
            try {
            	List<BatchRequest> requests = new ArrayList<BatchRequest>(pageIds.size());
            	for (String pageId : pageIds) {
            		if (stringLimit == StringUtil.XLARGE) {
            			requests.add(new BatchRequest.BatchRequestBuilder(pageId + "?fields=website,picture.type(large),phone,description,category_list,overall_star_rating,rating_count,fan_count,price_range").build()); //small, normal, large, square
            		} else {
            			requests.add(new BatchRequest.BatchRequestBuilder(pageId + "?fields=website,picture.type(normal),phone,description,category_list,overall_star_rating,rating_count,fan_count,hours,price_range").build()); //small, normal, large, square
            		}
            	}
                List<BatchResponse> batchResponses = facebookClient.executeBatch(requests);
                for (BatchResponse batchResponse : batchResponses) {        
            		JsonObject reply = Json.parse(batchResponse.getBody()).asObject(); //The HTTP response body JSON.
            		HashMap<String, String> details = new HashMap<String, String>();

            		if (reply.names().contains("description")) {
            			JSONUtils.putOptValue(details, "description", reply.get("description").asString(), stringLimit, true);
            			if (details.containsKey("description")) {
            				String desc = ((String)details.get("description")); 
            				details.put("description", desc.replaceAll("/pages/w/", "http://facebook.com/pages/w/"));
            			}
            		}

                    if (reply.names().contains("phone")) {
                        details.put("phone", reply.get("phone").asString());
                    }

                    if (reply.names().contains("website")) {
                        details.put("homepage", reply.get("website").asString());
                    }

                    if (reply.names().contains("picture")) {
                    	details.put("icon", reply.get("picture").asObject().get("data").asObject().get("url").asString());
                    }
                    
                    if (reply.names().contains("overall_star_rating")) {
                    	details.put("rating", Double.toString(reply.get("overall_star_rating").asDouble()));
                    }
                    
                    if (reply.names().contains("rating_count")) {
                    	details.put("numberOfReviews", Integer.toString(reply.get("rating_count").asInt()));
                    }
                    
                    if (reply.names().contains("fan_count")) {
                    	details.put("Likes", Integer.toString(reply.get("fan_count").asInt()));
                    }
                    
                    if (reply.names().contains("price_range")) {
                    	details.put("Pricing", reply.get("price_range").asString());
                    }
                    
                    processCategories(reply, details);                    

                    if (!details.isEmpty()) {
                    	String objectId = reply.get("id").asString();
                    	if (pageDescs.containsKey(objectId)) {
                    		pageDescs.get(objectId).putAll(details);
                    	} else {
                    		//System.out.println("Creating new desc");
                    		pageDescs.put(objectId, details);
                    	}
                    }
            	}
            } catch (Exception e) {
                logger.log(Level.SEVERE, "FacebookUtils.readFacebookPlacesDetails() exception", e);
            } 
        }
    }
}
