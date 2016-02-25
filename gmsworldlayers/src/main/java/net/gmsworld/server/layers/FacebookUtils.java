package net.gmsworld.server.layers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ThreadManager;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
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
import com.restfb.json.JsonArray;
import com.restfb.json.JsonException;
import com.restfb.json.JsonObject;
import com.restfb.types.User;

/**
 *
 * @author jstakun
 */
public class FacebookUtils extends LayerHelper {

	private static final String FBPLACES_PREFIX = "http://touch.facebook.com/profile.php?id=";
	
	@Override
	public JSONObject processRequest(double latitude, double longitude, String query, int distance, int version, int limit, int stringLength, String fbtoken, String flexString2) throws JsonException, JSONException, UnsupportedEncodingException {

        int dist = NumberUtils.normalizeNumber(distance, 1000, 50000);

        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, dist, version, limit, stringLength, fbtoken, flexString2);

        JSONObject response = null;

        String cachedResponse = cacheProvider.getString(key);
        if (cachedResponse == null) {
        	String token = fbtoken;
        	if (!StringUtils.isNotEmpty(token)) {
               token = Commons.getProperty(Property.fb_app_token);
            }
        	FacebookClient facebookClient = getFacebookClient(token);

            JsonObject placesSearch = null;

            if (query != null && query.length() > 0) {
                placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("q", query), Parameter.with("limit", limit));
            } else {
                placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("limit", limit));
            }

            JsonArray data = placesSearch.getJsonArray("data");

            int dataSize = data.length();

            List<String> pages = new ArrayList<String>();
            for (int i = 0; i < dataSize; i++) {
                JsonObject place = (JsonObject) data.get(i);
                pages.add(place.getString("id"));
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

        return response;
    }

    private static JSONObject createCustomJsonFacebookList(JsonArray data, Map<String, Map<String, String>> pageDescs) throws JsonException, JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < data.length(); i++) {
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            JsonObject place = (JsonObject) data.get(i);
            JsonObject location = place.getJsonObject("location");
            if (location.has("latitude") && location.has("longitude")) {

                double lat;
                Object c = location.remove("latitude");
                if (c instanceof Double) {
                    lat = MathUtils.normalizeE6((Double) c);
                } else if (c instanceof Integer) {
                    lat = MathUtils.normalizeE6((double) ((Integer) c).intValue());
                } else {
                    continue;
                }

                double lng;
                c = location.remove("longitude");
                if (c instanceof Double) {
                    lng = MathUtils.normalizeE6((Double) c);
                } else if (c instanceof Integer) {
                    lng = MathUtils.normalizeE6((double) ((Integer) c).intValue());
                } else {
                    continue;
                }

                jsonObject.put("name", place.getString("name"));
                jsonObject.put("url", place.getString("id"));
                Map<String, String> desc = new HashMap<String, String>();

                String placeid = place.getString("id");

                Map<String, String> pageDesc = pageDescs.remove(placeid);

                if (pageDesc != null) {
                    desc.putAll(pageDesc);
                }

                if (place.has("category_list")) {
                	String category = "";
                	JsonArray category_list = place.getJsonArray("category_list");
                	for (int j=0;j<category_list.length();j++) {
                		JsonObject cat = category_list.getJsonObject(j);
                		if (StringUtils.isNotEmpty(category)) {
                			category += ", ";
                		}
                	    category += cat.getString("name");
                	}
                	if (StringUtils.isNotEmpty(category)) {
                		desc.put("category", category);
                	}
                } else if (place.has("category")) {
                    desc.put("category", place.getString("category"));
                }

                jsonObject.put("lat", lat);
                jsonObject.put("lng", lng);

                Iterator<?> iter = location.sortedKeys();
                while (iter.hasNext()) {
                    String next = (String) iter.next();
                    String value = location.getString(next);
                    if (StringUtils.isNotEmpty(value)) {
                        desc.put(next, value);
                    }
                }
                jsonObject.put("desc", desc);

                jsonArray.add(jsonObject);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);

        return json;
    }
    
    private static List<ExtendedLandmark> createCustomLandmarkFacebookList(List<JsonObject> places, Map<String, Map<String, String>> pageDescs, Locale locale) throws JsonException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        for (JsonObject place : places) {
        	String name = place.getString("name");
        	JsonObject location = place.optJsonObject("location");
        	if (location != null && location.has("latitude") && location.has("longitude")) {

                double lat;
                Object c = location.remove("latitude");
                if (c instanceof Double) {
                    lat = MathUtils.normalizeE6((Double) c);
                } else if (c instanceof Integer) {
                    lat = MathUtils.normalizeE6((double) ((Integer) c).intValue());
                } else {
                    continue;
                }

                double lng;
                c = location.remove("longitude");
                if (c instanceof Double) {
                    lng = MathUtils.normalizeE6((Double) c);
                } else if (c instanceof Integer) {
                    lng = MathUtils.normalizeE6((double) ((Integer) c).intValue());
                } else {
                    continue;
                }
                
                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
     		    
                String placeid = place.getString("id");

                Map<String, String> pageDesc = pageDescs.remove(placeid);
                if (pageDesc == null) {
                	pageDesc = new HashMap<String, String>();
                }
                
                String url = pageDesc.remove("url");
                if (url == null) {
                	url =  FBPLACES_PREFIX + placeid;
                }
                
                if (place.has("category_list")) {
                	String category = "";
                	JsonArray category_list = place.getJsonArray("category_list");
                	for (int j=0;j<category_list.length();j++) {
                		JsonObject cat = category_list.getJsonObject(j);
                		if (StringUtils.isNotEmpty(category)) {
                			category += ", ";
                		}
                	    category += cat.getString("name");
                	}
                	if (StringUtils.isNotEmpty(category)) {
                		pageDesc.put("category", category);
                	}
                } else if (place.has("category")) {
                    pageDesc.put("category", place.getString("category"));
                }

                AddressInfo address = new AddressInfo();
                String val = location.optString("street");
                if (StringUtils.isNotEmpty(val)) {
                	address.setField(AddressInfo.STREET, val);
                }
                val = location.optString("city");
                if (StringUtils.isNotEmpty(val)) {
                	address.setField(AddressInfo.CITY, val);
                }
                val = location.optString("country");
                if (StringUtils.isNotEmpty(val)) {
                    address.setField(AddressInfo.COUNTRY, val);
                }
                val = location.optString("zip");
                if (StringUtils.isNotEmpty(val)) {
                	address.setField(AddressInfo.POSTAL_CODE, val);
                }
                val = location.optString("state");
                if (StringUtils.isNotEmpty(val)) {
                	address.setField(AddressInfo.STATE, val);
                }
                val = pageDesc.remove("phone");
                if (StringUtils.isNotEmpty(val)) {
                    address.setField(AddressInfo.PHONE_NUMBER, val);
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

                String pageIds = StringUtils.join(pages.subList(first, last), ",");

                threadManager.put(pageIds, new VenueDetailsRetriever(threadManager, pageDescs,
                        facebookClient, pages.subList(first, last), stringLength));

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

        if (query != null && query.length() > 0) {
        	placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("q", query), Parameter.with("limit", limit));
        } else {
        	placesSearch = facebookClient.fetchObject("search", JsonObject.class, Parameter.with("type", "place"), Parameter.with("center", latitude + "," + longitude), Parameter.with("distance", dist), Parameter.with("limit", limit));
        }
  
        JsonArray data = placesSearch.getJsonArray("data");           
        int dataSize = data.length();
        List<String> pages = new ArrayList<String>(dataSize);
        List<JsonObject> places = new ArrayList<JsonObject>(dataSize);
            
        for (int i = 0; i < dataSize; i++) {
        	JsonObject place = data.getJsonObject(i);
        	pages.add(place.getString("id"));
        	places.add(place);
        }

        Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();
        readFacebookPlacesDetails(facebookClient, pages, pageDescs, stringLength);
        return createCustomLandmarkFacebookList(places, pageDescs, locale);
	}
    
    public static FacebookClient getFacebookClient(String token) {
    	return new DefaultFacebookClient(token, Version.VERSION_2_5);
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
        		JsonObject place = tagged.getJsonObject("place");
        		String placeid = place.getString("id");
        		if (!pages.contains(placeid)) {
        			pages.add(placeid);
        			places.add(place);
        			Map<String, String> pageDesc = new HashMap<String, String>();
        			Date d = sdf.parse(tagged.getString("created_time"));//2015-05-05T06:20:42+0000
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
        		if (post.has("place")) {
        			JsonObject place = post.getJsonObject("place");
        			String placeid = place.getString("id");
        			if (!pages.contains(placeid)) {
        				pages.add(placeid);
        				places.add(place);
        				JsonObject from = post.getJsonObject("from");
        				Map<String, String> pageDesc = new HashMap<String, String>();
        				Date d = sdf.parse(post.getString("created_time"));//2015-05-05T06:20:42+0000
        				pageDesc.put("checkin_user", from.getString("name"));
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
        	//{user-id}/photos,/{user-id}/photos?type=uploaded order created_time desc
        	List<JsonObject> photos = facebookClient.fetchConnection("me/photos", JsonObject.class, Parameter.with("type","uploaded"), Parameter.with("limit", limit), Parameter.with("fields", "picture,place,from,created_time,link")).getData();
        	int dataSize = photos.size();
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        	Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();     
        	List<String> pages = new ArrayList<String>(dataSize);
        	List<JsonObject> places = new ArrayList<JsonObject>(dataSize);
        	logger.log(Level.INFO, "Found " + dataSize + " photos.");
        	for (JsonObject photo : photos) {
        		if (photo.has("place")) {
        			//TODO add support for multiple photos per page
        			JsonObject place = photo.getJsonObject("place");
        			String placeid = place.getString("id");
        			if (!pages.contains(placeid)) {
        				pages.add(placeid);
        				places.add(place);
        				JsonObject from = photo.getJsonObject("from");
        				Map<String, String> pageDesc = new HashMap<String, String>();
        				Date d = sdf.parse(photo.getString("created_time"));//2015-05-05T06:20:42+0000
        				pageDesc.put("photoUser", from.getString("name"));
        				pageDesc.put("thumbnail", photo.getString("picture"));  
        				pageDesc.put("caption", photo.getString("link"));
        				pageDesc.put("url", photo.getString("link"));
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
        		logger.log(Level.INFO, "Adding FB photo list to cache with key {0}", key);
        	}
        } else {
        	logger.log(Level.INFO, "Reading FB photo list from cache with key {0}", key);
        }
        
        logger.log(Level.INFO, "Found {0} landmarks", landmarks.size()); 
    	
    	return landmarks;
    } 
    
    
    private static class VenueDetailsRetriever implements Runnable {

        private ThreadManager threadManager;
        private FacebookClient facebookClient;
        private Map<String, Map<String, String>> pageDescs;
        private int stringLength;
        private List<String> pageIds;

        public VenueDetailsRetriever(ThreadManager threadManager, Map<String, Map<String, String>> pageDescs,
                FacebookClient facebookClient, List<String> pageIds, int stringLength) {
            this.threadManager = threadManager;
            this.pageDescs = pageDescs;
            this.facebookClient = facebookClient;
            this.pageIds = pageIds;
            this.stringLength = stringLength;
        }

        public void run() {
            try {
            	List<BatchRequest> requests = new ArrayList<BatchRequest>(pageIds.size());
            	for (String pageId : pageIds) {
            		requests.add(new BatchRequest.BatchRequestBuilder(pageId + "?fields=website,picture.type(normal),phone,description").build());
            	}
                List<BatchResponse> batchResponses = facebookClient.executeBatch(requests);
            	for (BatchResponse batchResponse : batchResponses) {           		
            		JSONObject reply = new JSONObject(batchResponse.getBody()); //The HTTP response body JSON.
            		
            		HashMap<String, String> details = new HashMap<String, String>();

            		if (reply.has("description")) {
            			JSONUtils.putOptValue(details, "description", reply.getString("description"), stringLength, true);
            			if (details.containsKey("description")) {
            				String desc = ((String)details.get("description")); 
            				details.put("description", desc.replaceAll("/pages/w/", "http://facebook.com/pages/w/"));
            			}
            		}

                    if (reply.has("phone")) {
                        details.put("phone", reply.getString("phone"));
                    }

                    if (reply.has("website")) {
                        details.put("homepage", reply.getString("website"));
                    }

                    if (reply.has("picture")) {
                    	details.put("icon", reply.getJSONObject("picture").getJSONObject("data").getString("url"));
                    }

                    if (!details.isEmpty()) {
                    	String objectId = reply.getString("id");
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
            } finally {
            	threadManager.take(StringUtils.join(pageIds, ","));
            }
        }
    }
}
