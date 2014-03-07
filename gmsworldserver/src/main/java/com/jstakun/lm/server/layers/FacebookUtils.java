/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.appengine.api.ThreadManager;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.MathUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.ThreadUtil;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonException;
import com.restfb.json.JsonObject;
import com.restfb.types.User;

/**
 *
 * @author jstakun
 */
public class FacebookUtils extends LayerHelper {

	private static final SimpleDateFormat outf = new SimpleDateFormat("yyyyMMdd", java.util.Locale.US);
	private static final String FBPLACES_PREFIX = "http://touch.facebook.com/profile.php?id=";
	
    public static String getFriendsPhotosToJSon(double lat, double lng, int version, int limit, int stringLength, String token) throws JSONException, UnsupportedEncodingException {

        String key = getCacheKey(FacebookUtils.class, "getFriendsPhotosToJSon", 0, 0, null, 0, version, limit, stringLength, token, null);
        String jsonString = CacheUtil.getString(key);

        if (jsonString == null) {

            Map<String, String> queries = new HashMap<String, String>();

            queries.put("photos", "SELECT object_id, caption, aid, owner, link, created, place_id, src_small FROM photo WHERE aid IN "
                    + "(SELECT aid FROM album WHERE owner IN"
                    + "(SELECT uid2 FROM friend WHERE uid1=me())"
                    + ") ORDER BY created DESC");
            queries.put("places", "SELECT page_id, name, description, latitude, longitude, display_subtext, checkin_count FROM place WHERE page_id IN (select place_id from #photos)");
            queries.put("users", "SELECT uid, name FROM user WHERE uid IN (select owner from #photos)");

            FacebookClient facebookClient = new DefaultFacebookClient(token);

            FBMultiQueryResults multiqueryResults = facebookClient.executeFqlMultiquery(queries, FBMultiQueryResults.class);

            Map<Long, String> users = new HashMap<Long, String>();
            for (Iterator<FBUser> iter = multiqueryResults.users.iterator(); iter.hasNext();) {
                FBUser user = iter.next();
                users.put(user.uid, user.name);
            }

            Map<String, FBPlace> places = new HashMap<String, FBPlace>();
            for (Iterator<FBPlace> iter = multiqueryResults.places.iterator(); iter.hasNext();) {
                FBPlace place = iter.next();
                places.put(place.pageId, place);
            }

            List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
            List<String> placeids = new ArrayList<String>();

            int counter = 0;

            boolean bitlyFailed = false;

            for (Iterator<FBPhoto> iter = multiqueryResults.photos.iterator(); iter.hasNext();) {
                FBPhoto photo = iter.next();

                //System.out.println("place_id: " + photo.place_id + ", link: " + photo.link);
                if (photo.place_id != null && photo.link != null) {
                    FBPlace place = places.get(photo.place_id);
                    if (place != null) {
                        counter++;
                        Long creationDate = photo.created * 1000;
                        String photoUser = users.get(Long.parseLong(photo.owner));
                        placeids.add(photo.place_id);

                        Map<String, Object> jsonObject = new HashMap<String, Object>();

                        //System.out.println(checkin.pageId + " " + checkin.timestamp + " " + checkin.userId);

                        jsonObject.put("name", place.name);
                        Map<String, String> desc = new HashMap<String, String>();

                        String url = photo.link;
                        if (!bitlyFailed) {
                            url = UrlUtils.getShortUrl(photo.link);
                            if (StringUtils.equals(photo.link, url)) {
                                bitlyFailed = true;
                            }
                        }

                        jsonObject.put("url", url);

                        JSONUtils.putOptValue(desc, "caption", photo.caption, stringLength, false);
                        if (StringUtils.isEmpty(photo.caption)) {
                            desc.remove("link");
                            desc.put("caption", url);
                        }

                        jsonObject.put("lat", MathUtils.normalizeE6(place.latitude));
                        jsonObject.put("lng", MathUtils.normalizeE6(place.longitude));

                        //desc.put("category", getCategoryFromDisplayString(place.displaySubtext));
                        desc.put("creationDate", Long.toString(creationDate));
                        JSONUtils.putOptValue(desc, "address", place.displaySubtext, stringLength, false);

                        desc.put("photoUser", photoUser);

                        if (version >= 2) {
                            if (StringUtils.isNotEmpty(photo.src_small)) {
                                desc.put("icon", photo.src_small);
                            }
                        }

                        JSONUtils.putOptValue(desc, "description", place.description, stringLength, false);

                        if (!desc.isEmpty()) {
                            jsonObject.put("desc", desc);
                        }

                        jsonArray.add(jsonObject);
                    }
                }

                if (counter >= limit) {
                    break;
                }
            }

            Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();

            readFacebookPlacesDetails(facebookClient, placeids, pageDescs, stringLength);

            for (Iterator<Map<String, Object>> iter = jsonArray.iterator(); iter.hasNext();) {
                Map<String, Object> placeDetails = iter.next();
                Object placeid = placeDetails.get("url");
                String placeidstr = null;
                if (placeid instanceof Long) {
                    placeidstr = Long.toString((Long) placeDetails.get("url"));
                } else if (placeid instanceof String) {
                    placeidstr = (String) placeDetails.get("url");
                }
                //

                if (placeidstr != null) {
                    Map<String, String> desc = (Map<String, String>) placeDetails.get("desc");

                    Map<String, String> pageDesc = pageDescs.remove(placeidstr);

                    if (pageDesc != null) {

                        desc.putAll(pageDesc);
                    }
                }
            }

            logger.log(Level.INFO, "No of FB friends photos venues {0}", jsonArray.size());

            JSONObject json = new JSONObject().put("ResultSet", jsonArray);
            jsonString = json.toString();

            if (!jsonArray.isEmpty()) {
                logger.log(Level.INFO, "Adding FB friends photos list to cache with key {0}", key);
                CacheUtil.put(key, jsonString);
            }
        } else {
            logger.log(Level.INFO, "Reading FB friends photos list from cache with key {0}", key);
        }

        return jsonString;
    }
    
    public static List<ExtendedLandmark> getFriendsPhotosToLandmark(double lat, double lng, int version, int limit, int stringLength, String token, Locale locale) throws JSONException, UnsupportedEncodingException {

        String key = getCacheKey(FacebookUtils.class, "getFriendsPhotosToLandmarks", 0, 0, null, 0, version, limit, stringLength, token, null);
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);

        if (landmarks == null) {
        	
        	landmarks = new ArrayList<ExtendedLandmark>();
            
            Map<String, String> queries = new HashMap<String, String>();

            queries.put("photos", "SELECT object_id, caption, aid, owner, link, created, place_id, src_small FROM photo WHERE aid IN "
                    + "(SELECT aid FROM album WHERE owner IN"
                    + "(SELECT uid2 FROM friend WHERE uid1=me())"
                    + ") ORDER BY created DESC");
            queries.put("places", "SELECT page_id, name, description, latitude, longitude, display_subtext, checkin_count FROM place WHERE page_id IN (select place_id from #photos)");
            queries.put("users", "SELECT uid, name FROM user WHERE uid IN (select owner from #photos)");

            FacebookClient facebookClient = new DefaultFacebookClient(token);

            FBMultiQueryResults multiqueryResults = facebookClient.executeFqlMultiquery(queries, FBMultiQueryResults.class);

            Map<Long, String> users = new HashMap<Long, String>();
            for (Iterator<FBUser> iter = multiqueryResults.users.iterator(); iter.hasNext();) {
                FBUser user = iter.next();
                users.put(user.uid, user.name);
            }

            Map<String, FBPlace> places = new HashMap<String, FBPlace>();
            for (Iterator<FBPlace> iter = multiqueryResults.places.iterator(); iter.hasNext();) {
                FBPlace place = iter.next();
                places.put(place.pageId, place);
            }

            List<String> placeids = new ArrayList<String>();
            int counter = 0;
            boolean bitlyFailed = false;
            Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();
            
            for (Iterator<FBPhoto> iter = multiqueryResults.photos.iterator(); iter.hasNext();) {
                FBPhoto photo = iter.next();

                //System.out.println("place_id: " + photo.place_id + ", link: " + photo.link);
                if (photo.place_id != null && photo.link != null) {
                    FBPlace place = places.get(photo.place_id);
                    if (place != null) {
                        counter++;
                        Long creationDate = photo.created * 1000;
                        String photoUser = users.get(Long.parseLong(photo.owner));
                        placeids.add(photo.place_id);

                        //System.out.println(checkin.pageId + " " + checkin.timestamp + " " + checkin.userId);

                        String name = place.name;
                        Map<String, String> tokens = new HashMap<String, String>();

                        String url = photo.link;
                        if (!bitlyFailed) {
                            url = UrlUtils.getShortUrl(photo.link);
                            if (StringUtils.equals(photo.link, url)) {
                                bitlyFailed = true;
                            }
                        }

                        JSONUtils.putOptValue(tokens, "caption", photo.caption, stringLength, false);
                        if (StringUtils.isEmpty(photo.caption)) {
                            tokens.put("caption", url);
                        } else {
                        	tokens.put("link", url);
                        }

                        QualifiedCoordinates qc = new QualifiedCoordinates(place.latitude, place.longitude, 0f, 0f, 0f);
             		   
                        JSONUtils.putOptValue(tokens, "address", place.displaySubtext, stringLength, false);

                        tokens.put("photoUser", photoUser);

                        ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, photo.place_id, qc, Commons.FACEBOOK_LAYER, new AddressInfo(), creationDate, null);
             		    landmark.setUrl(url);
             		    landmark.setHasCheckinsOrPhotos(true);
             		    
             		    if (StringUtils.isNotEmpty(photo.src_small)) {
                        	landmark.setThumbnail(photo.src_small);
                        }
                        
                        JSONUtils.putOptValue(tokens, "description", place.description, stringLength, false);
                       
                        if (!tokens.isEmpty()){
                        	pageDescs.put(photo.place_id, tokens);
                        }
             		    	   
             		    landmarks.add(landmark);
                    }
                }

                if (counter >= limit) {
                    break;
                }
            }

            readFacebookPlacesDetails(facebookClient, placeids, pageDescs, stringLength);

            for (ExtendedLandmark landmark : landmarks) {
            	String placeidstr = landmark.getDescription();             
                //
            	String desc = null;
                if (placeidstr != null) {
                    Map<String, String> pageDesc = pageDescs.remove(placeidstr);
                    if (pageDesc != null) {
                    	pageDesc.remove("icon");
                    	AddressInfo addressInfo = landmark.getAddressInfo();
                    	String address = pageDesc.remove("address"); 
                    	if (address != null ){
                    		addressInfo.setField(AddressInfo.STREET, address);
                    	}
                    	String phone = pageDesc.remove("phone");
                    	if (phone != null ) {
                    		addressInfo.setField(AddressInfo.PHONE_NUMBER, phone);
                    	}
                    	desc = JSONUtils.buildLandmarkDesc(landmark, pageDesc, locale);	
                    } 
                }
                if (desc != null) {
                	landmark.setDescription(desc);
                }
            }

            logger.log(Level.INFO, "No of FB friends photos venues {0}", landmarks.size());

            if (!landmarks.isEmpty()) {
                logger.log(Level.INFO, "Adding FB friends photos list to cache with key {0}", key);
                CacheUtil.put(key, landmarks);
            }
        } else {
            logger.log(Level.INFO, "Reading FB friends photos list from cache with key {0}", key);
        }

        return landmarks;
    }

    public static List<ExtendedLandmark> getFriendsCheckinsToLandmarks(double lat, double lng, int version, int limit, int stringLength, String token, Locale locale) throws JSONException, UnsupportedEncodingException {

        String key = getCacheKey(FacebookUtils.class, "getFriendsCheckinsToLandmarks", 0, 0, null, 0, version, limit, stringLength, token, null);
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);

        if (landmarks == null) {

            Map<String, String> queries = new HashMap<String, String>();

            queries.put("checkins", "SELECT author_uid, target_id, timestamp FROM checkin WHERE author_uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) ORDER BY timestamp DESC LIMIT " + limit);
            queries.put("places", "SELECT page_id, name, description, latitude, longitude, display_subtext, checkin_count FROM place WHERE page_id IN (select target_id from #checkins)");
            queries.put("users", "SELECT uid, name FROM user WHERE uid IN (select author_uid from #checkins)");

            FacebookClient facebookClient = new DefaultFacebookClient(token);
            FBMultiQueryResults multiqueryResults = facebookClient.executeFqlMultiquery(queries, FBMultiQueryResults.class);

            Map<Long, String> users = new HashMap<Long, String>();
            for (Iterator<FBUser> iter = multiqueryResults.users.iterator(); iter.hasNext();) {
                FBUser user = iter.next();
                users.put(user.uid, user.name);
            }

            Map<String, FBPlace> places = new HashMap<String, FBPlace>();
            for (Iterator<FBPlace> iter = multiqueryResults.places.iterator(); iter.hasNext();) {
                FBPlace place = iter.next();
                places.put(place.pageId, place);
            }

            Map<String, Map<String, Long>> userCheckins = new HashMap<String, Map<String, Long>>();
            List<String> placeids = new ArrayList<String>();
            landmarks = new ArrayList<ExtendedLandmark>();
            Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();
            
            for (FBCheckin checkin : multiqueryResults.checkins) {

                String placeid = checkin.targetId;
                FBPlace place = places.get(placeid);
                if (place != null) {
                    Long checkinDate = checkin.timestamp * 1000;
                    String checkinUser = users.get(checkin.userId);
                    placeids.add(placeid);

                    if (userCheckins.containsKey(placeid)) {
                        Map<String, Long> placeCheckins = userCheckins.get(placeid);
                        Long currentValue = placeCheckins.remove(checkinUser);
                        if (currentValue == null || currentValue < checkinDate) {
                            currentValue = checkinDate;
                        }
                        placeCheckins.put(checkinUser, currentValue);
                    } else {
                        String name = place.name;
                        String url = FBPLACES_PREFIX + placeid.toString();

                        QualifiedCoordinates qc = new QualifiedCoordinates(place.latitude, place.longitude, 0f, 0f, 0f);
             		   
                        Map<String, String> tokens = new HashMap<String, String>();

                        Map<String, Long> placeCheckins = new HashMap<String, Long>();
                        placeCheckins.put(checkinUser, checkinDate);
                        userCheckins.put(placeid, placeCheckins);

                        JSONUtils.putOptValue(tokens, "description", place.description, stringLength, false);
                        JSONUtils.putOptValue(tokens, "address", place.displaySubtext, stringLength, false);

                        ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, placeid, qc, Commons.FACEBOOK_LAYER, new AddressInfo(), checkinDate, null);
             		    landmark.setUrl(url);
             		    landmark.setHasCheckinsOrPhotos(true);
             		   
             		    if (! tokens.isEmpty()) {
             		    	pageDescs.put(placeid, tokens);
             		    }
                     		   
             		    landmarks.add(landmark);
                    }
                }
            }

            readFacebookPlacesDetails(facebookClient, placeids, pageDescs, stringLength);

            for (ExtendedLandmark landmark : landmarks) {
            	String placeidstr = landmark.getDescription();             
                //
            	String desc = null;
                if (placeidstr != null) {
                    Map<String, String> pageDesc = pageDescs.remove(placeidstr);
                    if (pageDesc != null) {
                    	String icon = pageDesc.remove("icon");
                    	landmark.setThumbnail(icon);
                    	AddressInfo addressInfo = landmark.getAddressInfo();
                    	String address = pageDesc.remove("address"); 
                    	if (address != null ){
                    		addressInfo.setField(AddressInfo.STREET, address);
                    	}
                    	String phone = pageDesc.remove("phone");
                    	if (phone != null ) {
                    		addressInfo.setField(AddressInfo.PHONE_NUMBER, phone);
                    	}
                    	
                    	Map<String, Long> checkinsMap = userCheckins.remove(placeidstr);
                    	String checkins = "";
                    	PrettyTime prettyTime = new PrettyTime(locale);
                    	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource", locale);
                        Calendar cal = Calendar.getInstance();
                    	for (Map.Entry<String, Long> checkin : checkinsMap.entrySet() ) {
                        	cal.setTimeInMillis(checkin.getValue());
                        	if (checkins.length() > 0) {
                        		checkins += ", ";
                        	}
                        	checkins += String.format(rb.getString("Landmark.checkinUser"), checkin.getKey(), prettyTime.format(cal.getTime()));                        	                       
                        }
                    	pageDesc.put("checkins", checkins);  
                    	
                    	desc = JSONUtils.buildLandmarkDesc(landmark, pageDesc, locale);	
                    }                  
                }
                landmark.setDescription(desc);
            }

            if (landmarks.size() > limit) {
                landmarks = new ArrayList<ExtendedLandmark>(landmarks.subList(0, limit));
            }

            //write to cache
            if (!landmarks.isEmpty()) {
                logger.log(Level.INFO, "Adding FB friends list to cache with key {0}", key);
                CacheUtil.put(key, landmarks);
            }

            logger.log(Level.INFO, "No of FB friends checkins {0}", landmarks.size());
        } else {
            logger.log(Level.INFO, "Reading fb friends list from cache with key {0}", key);
        }

        return landmarks;
    }
    
    public static String getFriendsCheckinsToJSon(double lat, double lng, int version, int limit, int stringLength, String token) throws JSONException, UnsupportedEncodingException {

        String key = getCacheKey(FacebookUtils.class, "getFriendsCheckinsToJSon", 0, 0, null, 0, version, limit, stringLength, token, null);
        String jsonString = CacheUtil.getString(key);

        if (jsonString == null) {

            Map<String, String> queries = new HashMap<String, String>();

            queries.put("checkins", "SELECT author_uid, target_id, timestamp FROM checkin WHERE author_uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) ORDER BY timestamp DESC LIMIT " + limit);
            queries.put("places", "SELECT page_id, name, description, latitude, longitude, display_subtext, checkin_count FROM place WHERE page_id IN (select target_id from #checkins)");
            queries.put("users", "SELECT uid, name FROM user WHERE uid IN (select author_uid from #checkins)");

            FacebookClient facebookClient = new DefaultFacebookClient(token);
            FBMultiQueryResults multiqueryResults = facebookClient.executeFqlMultiquery(queries, FBMultiQueryResults.class);

            Map<Long, String> users = new HashMap<Long, String>();
            for (Iterator<FBUser> iter = multiqueryResults.users.iterator(); iter.hasNext();) {
                FBUser user = iter.next();
                users.put(user.uid, user.name);
            }

            Map<String, FBPlace> places = new HashMap<String, FBPlace>();
            for (Iterator<FBPlace> iter = multiqueryResults.places.iterator(); iter.hasNext();) {
                FBPlace place = iter.next();
                places.put(place.pageId, place);
            }

            Map<String, Map<String, Long>> userCheckins = new HashMap<String, Map<String, Long>>();
            List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
            List<String> placeids = new ArrayList<String>();

            for (FBCheckin checkin : multiqueryResults.checkins) {

                String placeid = checkin.targetId;
                FBPlace place = places.get(placeid);
                if (place != null) {
                    Long checkinDate = checkin.timestamp * 1000;
                    String checkinUser = users.get(checkin.userId);
                    placeids.add(placeid);

                    if (userCheckins.containsKey(placeid)) {
                        Map<String, Long> placeCheckins = userCheckins.get(placeid);
                        Long currentValue = placeCheckins.remove(checkinUser);
                        if (currentValue == null || currentValue < checkinDate) {
                            currentValue = checkinDate;
                        }
                        placeCheckins.put(checkinUser, currentValue);
                    } else {
                        Map<String, Object> jsonObject = new HashMap<String, Object>();

                        //System.out.println(checkin.pageId + " " + checkin.timestamp + " " + checkin.userId);

                        jsonObject.put("name", place.name);
                        jsonObject.put("url", placeid.toString());

                        jsonObject.put("lat", MathUtils.normalizeE6(place.latitude));
                        jsonObject.put("lng", MathUtils.normalizeE6(place.longitude));

                        Map<String, String> desc = new HashMap<String, String>();

                        Map<String, Long> placeCheckins = new HashMap<String, Long>();
                        placeCheckins.put(checkinUser, checkinDate);
                        userCheckins.put(placeid, placeCheckins);

                        //desc.put("category", getCategoryFromDisplayString(place.displaySubtext));
                        desc.put("creationDate", Long.toString(checkinDate));

                        JSONUtils.putOptValue(desc, "description", place.description, stringLength, false);
                        JSONUtils.putOptValue(desc, "address", place.displaySubtext, stringLength, false);

                        if (!desc.isEmpty()) {
                            jsonObject.put("desc", desc);
                        }

                        jsonArray.add(jsonObject);
                    }
                }
            }

            Map<String, Map<String, String>> pageDescs = new HashMap<String, Map<String, String>>();

            readFacebookPlacesDetails(facebookClient, placeids, pageDescs, stringLength);

            for (Map<String, Object> placeDetails : jsonArray) {
                String placeidstr = (String) placeDetails.get("url");
                placeDetails.put("checkins", userCheckins.remove(placeidstr));

                Map<String, String> desc = (Map<String, String>) placeDetails.get("desc");

                Map<String, String> pageDesc = pageDescs.remove(placeidstr);

                if (pageDesc != null) {
                    desc.putAll(pageDesc);
                }
            }

            if (jsonArray.size() > limit) {
                jsonArray = jsonArray.subList(0, limit);
            }

            JSONObject json = new JSONObject().put("ResultSet", jsonArray);
            jsonString = json.toString();

            //write to cache
            if (!jsonArray.isEmpty()) {
                logger.log(Level.INFO, "Adding FB friends list to cache with key {0}", key);
                CacheUtil.put(key, jsonString);
            }

            logger.log(Level.INFO, "No of FB friends checkins {0}", jsonArray.size());
        } else {
            logger.log(Level.INFO, "Reading fb friends list from cache with key {0}", key);
        }

        return jsonString;
    }

    @Override
    public JSONObject processRequest(double latitude, double longitude, String query, int distance, int version, int limit, int stringLength, String fbtoken, String flexString2) throws JsonException, JSONException, UnsupportedEncodingException {

        int dist = NumberUtils.normalizeNumber(distance, 1000, 50000);

        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, dist, version, limit, stringLength, fbtoken, flexString2);

        JSONObject response = null;

        String cachedResponse = CacheUtil.getString(key);
        if (cachedResponse == null) {
            FacebookClient facebookClient = null;
            if (StringUtils.isNotEmpty(fbtoken)) {
                facebookClient = new DefaultFacebookClient(fbtoken);
            } else {
                facebookClient = new DefaultFacebookClient(Commons.fb_app_token);
            }

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
                CacheUtil.put(key, response.toString());
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
    
    private static List<ExtendedLandmark> createCustomLandmarkFacebookList(JsonArray data, Map<String, Map<String, String>> pageDescs, Locale locale) throws JsonException, JSONException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        for (int i = 0; i < data.length(); i++) {
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
                
                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
     		    String name = place.getString("name");
                String url =  FBPLACES_PREFIX + place.getString("id");
                
                String placeid = place.getString("id");

                Map<String, String> pageDesc = pageDescs.remove(placeid);
                if (pageDesc == null) {
                	pageDesc = new HashMap<String, String>();
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
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.FACEBOOK_LAYER, address, -1, null);
     		    landmark.setUrl(url);
     		    String thumbnail = pageDesc.remove("icon");
                if (thumbnail != null) {
                	 landmark.setThumbnail(thumbnail);
                }
     		    
            	String desc = JSONUtils.buildLandmarkDesc(landmark, pageDesc, locale);
     		    landmark.setDescription(desc);		   
             
     		    landmarks.add(landmark);
            }
        }

        return landmarks;
    }

    /*private static String getCategoryFromDisplayString(String displayStr) {
        String response = "";
        String[] tokens = StringUtils.split(displayStr, "・");
        for (int i = 0; i < tokens.length - 1; i++) {
            response += tokens[i];
            if (i < tokens.length - 2) {
                response += "/";
            }
        }
        return response;
    }*/

    private static void readFacebookPlacesDetails(FacebookClient facebookClient, List<String> pages, Map<String, Map<String, String>> pageDescs, int stringLength) {
        if (!pages.isEmpty()) {
            //limited due to url fetch limit = 2048 characters
            int first = 0, last = 50;

            ThreadFactory fbThreadFactory = ThreadManager.currentRequestThreadFactory();

            Map<String, Thread> venueDetailsThreads = new ConcurrentHashMap<String, Thread>();

            while (first < pages.size()) {
                //System.out.println("sublist: " + first + " " + last);
                if (last > pages.size()) {
                    last = pages.size();
                }

                String pageIds = StringUtils.join(pages.subList(first, last), ",");

                Thread venueDetailsRetriever = fbThreadFactory.newThread(new VenueDetailsRetriever(venueDetailsThreads, pageDescs,
                        facebookClient, pageIds, stringLength));

                venueDetailsThreads.put(pageIds, venueDetailsRetriever);

                venueDetailsRetriever.start();

                first = last;
                last += 50;
            }

            ThreadUtil.waitForLayers(venueDetailsThreads);
        }
    }

    public static List<String> getMyFriends(String token) {

    	List<String> friendIds = new ArrayList<String>();
    	FacebookClient facebookClient = new DefaultFacebookClient(token);
    	List<User> myFriends = facebookClient.fetchConnection("me/friends", User.class).getData();
    	for (Iterator<User> friends = myFriends.iterator(); friends.hasNext();) {
    			User friend = friends.next();
    			friendIds.add(friend.getId());
    	}

    	return friendIds;
    }
    
    public static Map<String, String> getMyData(String token) {
    	Map<String, String> userData = new HashMap<String, String>();
    	
    	FacebookClient facebookClient = new DefaultFacebookClient(token);
        User me = facebookClient.fetchObject("me", User.class);
        
        userData.put(ConfigurationManager.FB_USERNAME, me.getId());
        String name = me.getName();
        if (name != null) {
            userData.put(ConfigurationManager.FB_NAME, name);
        } else {
        	userData.put(ConfigurationManager.FB_NAME, me.getId());
        }
        String gender = me.getGender();
        if (gender != null) {
           userData.put(ConfigurationManager.FB_GENDER, gender);
        }
        Date birthday = me.getBirthdayAsDate();
        if (birthday != null) {
        	String outd = outf.format(birthday);
			userData.put(ConfigurationManager.FB_BIRTHDAY, outd);
        } 
    	
        String email = me.getEmail();
        if (StringUtils.isNotEmpty(email)) {
        	userData.put(ConfigurationManager.USER_EMAIL, email);
        }
        
    	return userData;
    }
    
    private static class VenueDetailsRetriever implements Runnable {

        private Map<String, Thread> venueDetailsThreads;
        private FacebookClient facebookClient;
        private Map<String, Map<String, String>> pageDescs;
        private int stringLength;
        private String pageIds;

        public VenueDetailsRetriever(Map<String, Thread> venueDetailsThreads, Map<String, Map<String, String>> pageDescs,
                FacebookClient facebookClient, String pageIds, int stringLength) {
            this.venueDetailsThreads = venueDetailsThreads;
            this.pageDescs = pageDescs;
            this.facebookClient = facebookClient;
            this.pageIds = pageIds;
            this.stringLength = stringLength;
        }

        @Override
        public void run() {
            try {

                List<FBPlaceDetails> queryResults = new ArrayList<FBPlaceDetails>();

                String query = "SELECT page_id, pic_small, website, phone, description FROM page WHERE page_id IN (" + pageIds + ")";

                queryResults.addAll(facebookClient.executeFqlQuery(query, FBPlaceDetails.class));

                for (Iterator<FBPlaceDetails> iter = queryResults.iterator(); iter.hasNext();) {

                    FBPlaceDetails pageDetails = iter.next();

                    HashMap<String, String> details = new HashMap<String, String>();

                    JSONUtils.putOptValue(details, "description", pageDetails.desc, stringLength, false);

                    if (StringUtils.isNotEmpty(pageDetails.phone)) {
                        details.put("phone", pageDetails.phone);
                    }

                    if (StringUtils.isNotEmpty(pageDetails.website)) {
                        details.put("homepage", pageDetails.website);
                    }

                    if (StringUtils.isNotEmpty(pageDetails.picSmall)) {
                            details.put("icon", pageDetails.picSmall);
                    }

                    if (!details.isEmpty()) {
                    	if (pageDescs.containsKey(pageDetails.objectId)) {
                    		pageDescs.get(pageDetails.objectId).putAll(details);
                    	} else {
                    		//System.out.println("Creating new desc");
                    	}
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "FacebookUtils.readFacebookPlacesDetails() exception", e);
            } finally {
                venueDetailsThreads.remove(pageIds);
            }
        }
    }

    /*private static String sendMessage(FacebookClient facebookClient, String connection, Parameter[] params, boolean verifyPermission) {
        try {          
        	boolean hasPermission = false;
        	if (verifyPermission) {
        		//check if user has given messaging permission            
        		try {
        			JsonObject permissions = facebookClient.fetchObject("me/permissions", JsonObject.class);
        			JsonArray data = permissions.getJsonArray("data");
        			JsonObject d = data.getJsonObject(0);
        			if (d.optInt("publish_stream", 0) == 1) {
        				logger.log(Level.INFO, "User has granted publish permission");
        				hasPermission = true;
        			} else {
        				logger.log(Level.INFO, permissions.toString());
        			}	
        		} catch (Exception e) {
        			logger.log(Level.SEVERE, "FacebookUtils.sendMessage() exception", e);
        		}
        	}
            
        	if (!verifyPermission || hasPermission) {
        		FacebookType publishMessageResponse = (FacebookType) facebookClient.publish(connection, FacebookType.class, params);
        		String id = publishMessageResponse.getId();
        		logger.log(Level.INFO, "Published Facebook message ID: {0}", id);
        		return id;
        	} else {
        		return null;
        	}
        } catch (FacebookException ex) {
        	logger.log(Level.SEVERE, "FacebookUtils.sendMessage() exception", ex);
            return null;
        }
    }

    public static void sendMessageToUserFeed(String token, String key, int type) {
        if (token != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(token);
            Parameter params[] = null;
            String name = null;
            String link = null;
            if (key != null) {
            	Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
            	link = UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark));    
            	name = landmark.getName();
            }
            //message, picture, link, name, caption, description, source, place, tags
            
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
                      
            if (type == Commons.BLOGEO) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.blogeo")),
                            Parameter.with("name", name),
                            Parameter.with("description", rb.getString("Social.fb.desc.blogeo")),
                            Parameter.with("link", link),
                            Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/blogeo_j.png")
                        };
            } else if (type == Commons.LANDMARK) {
                params = new Parameter[]{
                            Parameter.with("message", rb.getString("Social.fb.message.landmark")),
                            Parameter.with("name", name),
                            Parameter.with("description", rb.getString("Social.fb.desc.landmark")),
                            Parameter.with("link", link),
                            Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/poi_j.png")
                        };
            } else if (type == Commons.LOGIN) {
            	 params = new Parameter[]{
            			 Parameter.with("message", rb.getString("Social.login")),
            			 Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/3globe_80.png"),
            			 Parameter.with("description", rb.getString("Social.login.desc")),
            			 Parameter.with("link", ConfigurationManager.SERVER_URL),
             			Parameter.with("name", "Message from GMS World"),
                     }; 
            } else if (type == Commons.MY_POS) {
            	params = new Parameter[]{
            			Parameter.with("message", rb.getString("Social.fb.message.mypos")),
            			Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/location.png"),
            			Parameter.with("description", rb.getString("Social.fb.desc.mypos")),
            			Parameter.with("link", link),
            			Parameter.with("name", name),
            	};		
            }
            sendMessage(facebookClient, "me/feed", params, true);
        } else {
            logger.log(Level.SEVERE, "Landmark or token is null! Key: {0}, token: {1}", new Object[]{key, token});
        }
    }

    //login with manage_pages permission
    public static void sendMessageToPageFeed(String key, String landmarkUrl) {
        final String[] images = {"blogeo_j.png", "blogeo_a.png", "poi_j.png", "poi_a.png"};
        int imageId = 2;
        try {
            imageId = random.nextInt(4);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        //logger.log(Level.INFO, "Image id: {0}", imageId);
        if (imageId > 3 || imageId < 0) {
            imageId = 2;
        }
        Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
        if (landmark != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(Commons.fb_page_token);
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            Parameter params[] = null;
            //message, picture, link, name, caption, description, source, place, tags
            String userMask = UrlUtils.createUsernameMask(landmark.getUsername());
            //logger.log(Level.INFO, "FB message link is: {0}", link);
            params = new Parameter[]{
                        Parameter.with("message", String.format(rb.getString("Social.fb.message.server"), userMask)),
                        Parameter.with("name", landmark.getName()),
                        Parameter.with("description", rb.getString("Social.fb.desc.server")),
                        Parameter.with("link", landmarkUrl),
                        Parameter.with("picture", ConfigurationManager.SERVER_URL + "images/" + images[imageId])
                    };
            sendMessage(facebookClient, Commons.FB_GMS_WORLD_FEED, params, false);
        } else {
            logger.log(Level.SEVERE, "Landmark key is wrong! Key: {0}", key);
        }
    }

    public static void sendImageMessage(String imageUrl, String showImageUrl, String username) {
        if (imageUrl != null) {
            FacebookClient facebookClient = new DefaultFacebookClient(Commons.fb_page_token);
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            String userMask = UrlUtils.createUsernameMask(username);
            //logger.log(Level.INFO, "FB message link is: {0}", link);
            Parameter[] params = new Parameter[]{
                Parameter.with("message", String.format(rb.getString("Social.fb.message.screenshot"),userMask)),
                Parameter.with("name", "GMS World"),
                Parameter.with("description", rb.getString("Social.fb.desc.screenshot")),
                Parameter.with("link", showImageUrl),
                Parameter.with("picture", imageUrl + "=s128")
            };

            sendMessage(facebookClient, Commons.FB_GMS_WORLD_FEED, params, false);
        } else {
            logger.log(Level.SEVERE, "Image url is null!");
        }
    }
    
    public static int checkin(String token, String place, String name) {
    	FacebookClient facebookClient = new DefaultFacebookClient(token);
    	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
    	Parameter[] params = new Parameter[]{
    			Parameter.with("message", String.format(rb.getString("Social.checkin"), name)),
                Parameter.with("place", place),
    	};
    	String id = sendMessage(facebookClient, "me/feed", params, true);
    	if (id != null) {
    		return 200;
    	} else {
    		return 200; //TODO change to 500;
    	}
    }
    
    public static int sendComment(String token, String place, String message, String name) {
    	FacebookClient facebookClient = new DefaultFacebookClient(token);
    	Parameter[] params = new Parameter[]{
    			Parameter.with("message", message),
                Parameter.with("link", place),
                Parameter.with("name", name),
    	};
    	String id = sendMessage(facebookClient, "me/feed", params, true);
    	if (id != null) {
    		return 200;
    	} else {
    		return 500;
    	}
    }*/

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double latitude, double longitude, String query, int distance, int version, int limit, int stringLength, String fbtoken, String flexString2, Locale locale) throws Exception {
		int dist = NumberUtils.normalizeNumber(distance, 1000, 50000);

        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, dist, version, limit, stringLength, fbtoken, flexString2);

        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);
        if (landmarks == null) {
            FacebookClient facebookClient = null;
            if (StringUtils.isNotEmpty(fbtoken)) {
                facebookClient = new DefaultFacebookClient(fbtoken);
            } else {
                facebookClient = new DefaultFacebookClient(Commons.fb_app_token);
            }

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

            landmarks = createCustomLandmarkFacebookList(data, pageDescs, locale);

            logger.log(Level.INFO, "No of FB places {0}", dataSize);

            if (!landmarks.isEmpty()) {
                CacheUtil.put(key, landmarks);
                logger.log(Level.INFO, "Adding FB landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading FB landmark list from cache with key {0}", key);
        }

        return landmarks;
	}
}
