/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.ThreadUtil;

import java.util.concurrent.ConcurrentHashMap;

import com.google.appengine.api.ThreadManager;

import java.util.concurrent.ThreadFactory;

import fi.foyt.foursquare.api.entities.HereNow;

import org.apache.commons.lang.StringEscapeUtils;

import fi.foyt.foursquare.api.FoursquareApiException;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.MathUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.ResultMeta;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.CompleteVenue;
import fi.foyt.foursquare.api.entities.Contact;
import fi.foyt.foursquare.api.entities.Icon;
import fi.foyt.foursquare.api.entities.Location;
import fi.foyt.foursquare.api.entities.Photo;
import fi.foyt.foursquare.api.entities.Recommendation;
import fi.foyt.foursquare.api.entities.RecommendationGroup;
import fi.foyt.foursquare.api.entities.Recommended;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class FoursquareUtils extends LayerHelper {

    private static final CheckinComparator checkinComparator = new CheckinComparator();
    private static final String FOURSQUARE_PREFIX = "http://foursquare.com/venue/";
    
    @Override
    protected JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String intent, String locale) throws JSONException, MalformedURLException, IOException, FoursquareApiException {
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, intent, locale);
        JSONObject response = null;
        String cachedResponse = CacheUtil.getString(key);
        
        if (cachedResponse == null) {
            FoursquareApi api = new FoursquareApi(Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, null, null, new DefaultIOHandler());
            api.setUseCallback(false);

            List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

            //venues search
            //Result<VenuesSearchResult> result = api.venuesSearch(lat + "," + lng, (double) radius, null, null, query, limit, intent, null, null, null, null);
            
            Map<String, String> params = new HashMap<String, String>();
            params.put("ll", lat + "," + lng);
            params.put("radius", Integer.toString(radius));
            params.put("limit", Integer.toString(limit));
            params.put("intent", intent);
            if (StringUtils.isNotEmpty(query)) {
            	params.put("query", query);
            }
            
            Result<VenuesSearchResult> result = api.venuesSearch(params); 
            
            if (result.getMeta().getCode() == 200) {
                VenuesSearchResult searchResult = result.getResult();
                CompactVenue[] venues = searchResult.getVenues();

                logger.log(Level.INFO, "No of Foursquare search venues {0}", venues.length);
                if (venues.length > 0) {

                	List<String> venueIds = new ArrayList<String>();

                	for (int j = 0; j < venues.length; j++) {
                		venueIds.add(venues[j].getId());
                	}

                	Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);

                	for (int j = 0; j < venues.length; j++) {
                		CompactVenue venue = venues[j];

                		Map<String, String> attrs = descs.remove(venue.getId());
                		if (attrs == null) {
                			attrs = new HashMap<String, String>();
                		}

                		Map<String, Object> jsonObject = parseCompactVenueToJSon(venue, attrs, lat, lng);
                		if (!jsonObject.isEmpty()) {
                			jsonArray.add(jsonObject);
                		}
                	}
                }
            } else {
            	handleError(result.getMeta(), key);
            }
            
            //venues trending

            Result<CompactVenue[]> resultT = api.venuesTrending(lat + "," + lng, limit, radius);

            if (resultT.getMeta().getCode() == 200) {
                CompactVenue[] venues = resultT.getResult();
                logger.log(Level.INFO, "No of Foursquare trending venues {0}", venues.length);

                if (venues.length > 0) {
                	List<String> venueIds = new ArrayList<String>();

                	for (int j = 0; j < venues.length; j++) {
                    	venueIds.add(venues[j].getId());
                	}

                	Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);

                	for (int j = 0; j < venues.length; j++) {
                		CompactVenue venue = venues[j];

                    	Map<String, String> attrs = descs.remove(venue.getId());
                    	if (attrs == null) {
                        	attrs = new HashMap<String, String>();
                    	}

                    	HereNow hereNow = venue.getHereNow();
                    	if (hereNow != null) {
                    		long hereNowCount = hereNow.getCount();
                    		attrs.put("isTrending", Long.toString(hereNowCount));
                    		//CheckinGroup[] groups = venue.getHereNow().getGroups();
                    		//for (int i = 0; i < groups.length; i++) {
                    		//CheckinGroup group = groups[i];
                    		//String name = group.getName();
                    		//long count = group.getCount();
                    		//}
                    	}

                    	Map<String, Object> jsonObject = parseCompactVenueToJSon(venue, attrs, lat, lng);
                    	if (!jsonObject.isEmpty()) {
                        	jsonArray.add(jsonObject);
                    	}
                	}
                }
            } else {
            	handleError(resultT.getMeta(), key);
            }

            response = new JSONObject().put("ResultSet", jsonArray);

            //write to cache
            if (!jsonArray.isEmpty()) {
                logger.log(Level.INFO, "Adding fs search list to cache with key {0}", key);
                CacheUtil.put(key, response.toString());
            }
        } else {
            logger.log(Level.INFO, "Reading FS landmark list from cache with key {0}", key);
            response = new JSONObject(cachedResponse);
        }

        return response;
    }
    
    @Override
    protected List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String intent, String locale, Locale l) throws Exception {
       	String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, intent, locale);
           List<ExtendedLandmark> response = (List<ExtendedLandmark>) CacheUtil.getObject(key);
           
           if (response == null) {
               FoursquareApi api = new FoursquareApi(Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, null, null, new DefaultIOHandler());
               api.setUseCallback(false);
               response = new ArrayList<ExtendedLandmark>();
               //venues search
               
               //Result<VenuesSearchResult> result = api.venuesSearch(lat + "," + lng, (double) radius, null, null, query, limit, intent, null, null, null, null);

               Map<String, String> params = new HashMap<String, String>();
               params.put("ll", lat + "," + lng);
               params.put("radius", Integer.toString(radius));
               params.put("limit", Integer.toString(limit));
               params.put("intent", intent);
               if (StringUtils.isNotEmpty(query)) {
               	params.put("query", query);
               }
               
               Result<VenuesSearchResult> result = api.venuesSearch(params); 
               
               
               if (result.getMeta().getCode() == 200) {
                   VenuesSearchResult searchResult = result.getResult();
                   CompactVenue[] venues = searchResult.getVenues();
                   logger.log(Level.INFO, "No of Foursquare search venues {0}", venues.length);
                   if (venues.length > 0) {
                   		List<String> venueIds = new ArrayList<String>();

                   		for (int j = 0; j < venues.length; j++) {
                   			venueIds.add(venues[j].getId());
                   		}

                   		Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);

                   		for (int j = 0; j < venues.length; j++) {
                   			CompactVenue venue = venues[j];

                   			Map<String, String> attrs = descs.remove(venue.getId());
                   			if (attrs == null) {
                   				attrs = new HashMap<String, String>();
                   			}
                       
                   			ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, lat, lng, l);
                   			if (landmark != null) {
                   				response.add(landmark);
                   			}
                   		}
                   }	
               } else {
            	   handleError(result.getMeta(), key);
               }
               
               //venues trending

               Result<CompactVenue[]> resultT = api.venuesTrending(lat + "," + lng, limit, radius);

               if (resultT.getMeta().getCode() == 200) {
                   CompactVenue[] venues = resultT.getResult();
                   logger.log(Level.INFO, "No of Foursquare trending venues {0}", venues.length);

                   if (venues.length > 0) {
                   		List<String> venueIds = new ArrayList<String>();

                   		for (int j = 0; j < venues.length; j++) {
                   			venueIds.add(venues[j].getId());
                   		}

                   		Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);

                   		for (int j = 0; j < venues.length; j++) {
                   			CompactVenue venue = venues[j];

                   			Map<String, String> attrs = descs.remove(venue.getId());
                   			if (attrs == null) {
                   				attrs = new HashMap<String, String>();
                   			}

                   			HereNow hereNow = venue.getHereNow();
                   			if (hereNow != null) {
                   				long hereNowCount = hereNow.getCount();
                   				attrs.put("isTrending", Long.toString(hereNowCount));
                   				//CheckinGroup[] groups = venue.getHereNow().getGroups();
                   				//for (int i = 0; i < groups.length; i++) {
                   				//CheckinGroup group = groups[i];
                   				//String name = group.getName();
                   				//long count = group.getCount();
                   				//}
                   			}

                   			ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, lat, lng, l);
                   			if (landmark != null) {
                   				response.add(landmark);
                   			}
                   		}
                   }
               } else {
            	   handleError(resultT.getMeta(), key);
               }

               if (!response.isEmpty()) {
                   logger.log(Level.INFO, "Adding fs search list to cache with key {0}", key);
                   CacheUtil.put(key, response);
               }
           } else {
               logger.log(Level.INFO, "Reading FS landmark list from cache with key {0}", key);
           }

           return response;
   	}

    protected static JSONObject processMerchantRequest(double lat, double lng, String categoryid, int radius, int version, int limit, int stringLimit, String token, String locale) throws MalformedURLException, IOException, JSONException {
        String key = getCacheKey(FoursquareUtils.class, "processMerchantRequest", lat, lng, categoryid, radius, version, limit, stringLimit, token, locale);
        JSONObject response = null;
        String cachedResponse = CacheUtil.getString(key);
        
        if (cachedResponse == null) {
            StringBuilder sb = new StringBuilder("https://api.foursquare.com/v2/specials/search?ll=").append(lat).
                    append(",").append(lng).append("&llAcc=").append(radius).append("&oauth_token=").
                    append(token).append("&limit=").append(limit).append("&v=").append(FoursquareApi.DEFAULT_VERSION);
            URL url = new URL(sb.toString());
            String resp = HttpUtils.processFileRequestWithLocale(url, locale);
            //System.out.println(resp);
            response = createCustomJsonFoursquareMerchantList(resp, locale, categoryid, stringLimit);
            if (response.getJSONArray("ResultSet").length() > 0) {
                CacheUtil.put(key, response.toString());
                logger.log(Level.INFO, "Adding FSM landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading FSM landmark list from cache with key {0}", key);
            response = new JSONObject(cachedResponse);
        }

        return response;
    }
    
    protected static List<ExtendedLandmark> processBinaryMerchantRequest(double lat, double lng, String categoryid, int radius, int version, int limit, int stringLimit, String token, String locale, Locale l) throws MalformedURLException, IOException, JSONException {
        String key = getCacheKey(FoursquareUtils.class, "processBinaryMerchantRequest", lat, lng, categoryid, radius, version, limit, stringLimit, token, locale);
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);
        
        if (landmarks == null) {
            StringBuilder sb = new StringBuilder("https://api.foursquare.com/v2/specials/search?ll=").append(lat).
                    append(",").append(lng).append("&llAcc=").append(radius).append("&oauth_token=").
                    append(token).append("&limit=").append(limit).append("&v=").append(FoursquareApi.DEFAULT_VERSION);
            URL url = new URL(sb.toString());
            String resp = HttpUtils.processFileRequestWithLocale(url, locale);
            landmarks = createCustomLandmarksFoursquareMerchantList(resp, locale, categoryid, stringLimit, l);
            if (!landmarks.isEmpty()) {
                CacheUtil.put(key, landmarks);
                logger.log(Level.INFO, "Adding FSM landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading FSM landmark list from cache with key {0}", key);
        }

        return landmarks;
    }

    protected static String exploreVenuesToJSon(double lat, double lng, String query, int radius, int limit, int version, String token, String locale) throws JSONException, MalformedURLException, IOException, FoursquareApiException {
        String key = getCacheKey(FoursquareUtils.class, "exploreVenuesToJSon", lat, lng, query, radius, version, limit, 0, token, locale);
        String jsonString = CacheUtil.getString(key);
        if (jsonString == null) {
            FoursquareApi api = new FoursquareApi(Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, null, token, new DefaultIOHandler());
            api.setUseCallback(false);
            List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
            Result<Recommended> recommended = api.venuesExplore(lat + "," + lng, null, null, null, radius, null, query, limit, "friends");

            if (recommended.getMeta().getCode() == 200) {
                RecommendationGroup[] recGroup = recommended.getResult().getGroups();
                int recCount = 0;

                for (int i = 0; i < recGroup.length; i++) {
                    RecommendationGroup rg = recGroup[i];
                    Recommendation[] rec = rg.getItems();
                    recCount += rec.length;

                    List<String> venueIds = new ArrayList<String>();

                    for (int j = 0; j < rec.length; j++) {
                        Recommendation r = rec[j];
                        venueIds.add(r.getVenue().getId());
                    }

                    Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);

                    for (int j = 0; j < rec.length; j++) {

                        Recommendation r = rec[j];
                        //Reason[] reasons = r.getReasons().getItems();
                        CompactVenue venue = r.getVenue();

                        Map<String, String> attrs = descs.remove(venue.getId());
                        if (attrs == null) {
                            attrs = new HashMap<String, String>();
                        }

                        attrs.put("rating", "5");

                        Map<String, Object> jsonObject = parseCompactVenueToJSon(venue, attrs, lat, lng);
                        if (!jsonObject.isEmpty()) {
                            jsonArray.add(jsonObject);
                        }
                    }
                }

                logger.log(Level.INFO, "No of Foursquare recommended venues {0}", recCount);

                JSONObject json = new JSONObject().put("ResultSet", jsonArray);
                jsonString = json.toString();

                //write to cache
                if (!jsonArray.isEmpty()) {
                    logger.log(Level.INFO, "Adding fs explore list to cache with key {0}", key);
                    CacheUtil.put(key, jsonString);
                }
            } else {
            	handleError(recommended.getMeta(), key);
            }
        }
        return jsonString;
     }
    
    protected static List<ExtendedLandmark> exploreVenuesToLandmark(double lat, double lng, String query, int radius, int limit, int version, String token, String locale, Locale l) throws JSONException, MalformedURLException, IOException, FoursquareApiException {
        String key = getCacheKey(FoursquareUtils.class, "exploreVenuesToLandmark", lat, lng, query, radius, version, limit, 0, token, locale);
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);
        if (landmarks == null) {
            FoursquareApi api = new FoursquareApi(Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, null, token, new DefaultIOHandler());
            api.setUseCallback(false);
            Result<Recommended> recommended = api.venuesExplore(lat + "," + lng, null, null, null, radius, null, query, limit, "friends");
            landmarks = new ArrayList<ExtendedLandmark>();

            if (recommended.getMeta().getCode() == 200) {
                RecommendationGroup[] recGroup = recommended.getResult().getGroups();
                int recCount = 0;
                
                for (int i = 0; i < recGroup.length; i++) {
                    RecommendationGroup rg = recGroup[i];
                    Recommendation[] rec = rg.getItems();
                    recCount += rec.length;

                    List<String> venueIds = new ArrayList<String>();

                    for (int j = 0; j < rec.length; j++) {
                        Recommendation r = rec[j];
                        venueIds.add(r.getVenue().getId());
                    }

                    Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);

                    for (int j = 0; j < rec.length; j++) {

                        Recommendation r = rec[j];
                        //Reason[] reasons = r.getReasons().getItems();
                        CompactVenue venue = r.getVenue();

                        Map<String, String> attrs = descs.remove(venue.getId());
                        if (attrs == null) {
                            attrs = new HashMap<String, String>();
                        }

                        attrs.put("rating", "5");

                        ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, lat, lng, l);
                        if (landmark != null) {
                        	landmarks.add(landmark);
                        }
                    }
                }

                logger.log(Level.INFO, "No of Foursquare recommended venues {0}", recCount);

                //write to cache
                if (landmarks != null && !landmarks.isEmpty()) {
                    logger.log(Level.INFO, "Adding fs explore list to cache with key {0}", key);
                    CacheUtil.put(key, landmarks);
                }
            } else {
            	handleError(recommended.getMeta(), key);
            }
        }
        return landmarks;
    }

    protected static List<ExtendedLandmark> getFriendsCheckinsToLandmarks(double latitude, double longitude, int limit, int version, String token, String locale, Locale l) throws FoursquareApiException, JSONException, UnsupportedEncodingException {
        String key = getCacheKey(FoursquareUtils.class, "getFriendsCheckinsToLandmark", 0, 0, null, 0, version, limit, 0, token, locale);
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>) CacheUtil.getObject(key);

        if (landmarks == null) {
            FoursquareApi api = new FoursquareApi(Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, null, token, new DefaultIOHandler());
            api.setUseCallback(false);
            //api.setVersion(VDATE);
            
            Result<Checkin[]> response = api.checkinsRecent(latitude + "," + longitude, limit, null);
            landmarks = new ArrayList<ExtendedLandmark>();
            if (response.getMeta().getCode() == 200) {
                Checkin[] checkins = response.getResult();
                logger.log(Level.INFO, "No of Foursquare checkins {0}", checkins.length);
                
                
                for (int j = 0; j < checkins.length; j++) {
                    Checkin checkin = checkins[j];

                    CompactVenue venue = checkin.getVenue();

                    if (venue != null) {

                        Photo[] photos = checkin.getPhotos().getItems();
                        String photo = null;
                        if (photos.length > 0) {
                            photo = photos[0].getUrl();
                        }

                        String username = checkin.getUser().getFirstName();
                        String lastname = checkin.getUser().getLastName();
                        if (StringUtils.isNotEmpty(lastname)) {
                            username += " " + lastname;
                        }

                        long creationDate = checkin.getCreatedAt() * 1000;

                        Map<String, String> attrs = new HashMap<String, String>();
                        attrs.put("creationDate", Long.toString(creationDate));
                        attrs.put("username", username);

                        if (photo != null) {
                            attrs.put("photo", photo);
                        }
                        ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, latitude, longitude, l);
                        if (landmark != null) {
                        	landmarks.add(landmark);
                        }                  
                    }
                }
            } else {
            	handleError(response.getMeta(), key);
            }

            //write to cache
            if (landmarks != null && !landmarks.isEmpty()) {
                logger.log(Level.INFO, "Adding fs friends list to cache with key {0}", key);
                CacheUtil.put(key, landmarks);
            }
        } else {
            logger.log(Level.INFO, "Reading fs friends list from cache with key {0}", key);
        }

        return landmarks;
    }

    protected static String getFriendsCheckinsToJSon(double latitude, double longitude, int limit, int version, String token, String locale) throws FoursquareApiException, JSONException, UnsupportedEncodingException {
        String key = getCacheKey(FoursquareUtils.class, "getFriendsCheckinsToJSon", 0, 0, null, 0, version, limit, 0, token, locale);
        String jsonString = CacheUtil.getString(key);

        if (jsonString == null) {
            FoursquareApi api = new FoursquareApi(Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, null, token, new DefaultIOHandler());
            api.setUseCallback(false);
            //api.setVersion(VDATE);
            List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

            Result<Checkin[]> response = api.checkinsRecent(latitude + "," + longitude, 100, null);
            if (response.getMeta().getCode() == 200) {
                Checkin[] checkins = response.getResult();
                logger.log(Level.INFO, "No of Foursquare checkins {0}", checkins.length);
                
                for (int j = 0; j < checkins.length; j++) {
                    Checkin checkin = checkins[j];

                    CompactVenue venue = checkin.getVenue();

                    if (venue != null) {

                        Photo[] photos = checkin.getPhotos().getItems();
                        String photo = null;
                        if (photos.length > 0) {
                            photo = photos[0].getUrl();
                        }

                        String username = checkin.getUser().getFirstName();
                        String lastname = checkin.getUser().getLastName();
                        if (StringUtils.isNotEmpty(lastname)) {
                            username += " " + lastname;
                        }

                        long creationDate = checkin.getCreatedAt() * 1000;

                        Map<String, String> attrs = new HashMap<String, String>();
                        attrs.put("creationDate", Long.toString(creationDate));
                        attrs.put("username", username);

                        if (version > 1 && photo != null) {
                            attrs.put("photo", photo);
                        }
                        Map<String, Object> jsonObject = parseCompactVenueToJSon(venue, attrs, latitude, longitude);
                        if (!jsonObject.isEmpty()) {
                            jsonArray.add(jsonObject);
                        }
                    }
                }
            } else {
            	handleError(response.getMeta(), key);
            }

            //sort jsonArray
            Collections.sort(jsonArray, checkinComparator);
            if (jsonArray.size() > limit) {
                jsonArray = jsonArray.subList(0, limit);
            }

            JSONObject json = new JSONObject().put("ResultSet", jsonArray);
            jsonString = json.toString();

            //write to cache
            if (!jsonArray.isEmpty()) {
                logger.log(Level.INFO, "Adding fs friends list to cache with key {0}", key);
                CacheUtil.put(key, jsonString);
            }
        } else {
            logger.log(Level.INFO, "Reading fs friends list from cache with key {0}", key);
        }

        return jsonString;
    }

    private static Map<String, Object> parseCompactVenueToJSon(CompactVenue venue, Map<String, String> desc, double myLat, double myLng) {
        Map<String, Object> jsonObject = new HashMap<String, Object>();

        Location location = venue.getLocation();

        if (location != null && location.getLat() != null && location.getLng() != null) {
            double lat = MathUtils.normalizeE6(location.getLat());
            double lng = MathUtils.normalizeE6(location.getLng());
            jsonObject.put("name", venue.getName());
            jsonObject.put("lat", lat);
            jsonObject.put("lng", lng);

            double distance = NumberUtils.distanceInKilometer(lat, lng, myLat, myLng);
            jsonObject.put("distance", MathUtils.normalizeE2(distance));

            String url = venue.getId();
            jsonObject.put("url", url);

            String creationDate = desc.get("creationDate");

            Category[] categories = venue.getCategories();
            String category = "", icon = "";

            for (int k = 0; k < categories.length; k++) {
                if (category.length() > 0) {
                    category += ", ";
                }
                if (StringUtils.isEmpty(icon)) {
                    Icon iconObj = categories[k].getIcon();
                	icon = iconObj.getPrefix() + "bg_32" + iconObj.getSuffix(); //32, 44, 64, and 88 are available
                }
                category += categories[k].getName();
            }

            if (category.length() > 0) {
                desc.put("category", category);
            }
            if (!StringUtils.isEmpty(icon) && !desc.containsKey("icon")) {
                desc.put("icon", icon);
            }

            desc.put("address", location.getAddress());
            desc.put("city", location.getCity());
            desc.put("country", location.getCountry());
            desc.put("name", location.getName());
            desc.put("zip", location.getPostalCode());
            desc.put("state", location.getState());

            Contact contact = venue.getContact();
            desc.put("email", contact.getEmail());
            desc.put("facebook", contact.getFacebook());
            desc.put("phone", contact.getPhone());
            desc.put("twitter", contact.getTwitter());

            String username = desc.remove("username");
            if (username != null) {
                Map<String, Long> userCheckins = new HashMap<String, Long>();
                userCheckins.put(username, Long.parseLong(creationDate));
                jsonObject.put("checkins", userCheckins);
            }

            String photo = desc.remove("photo");
            if (photo != null) {
                desc.put("photo", UrlUtils.getShortUrl(photo));
            }

            jsonObject.put("desc", desc);
        }

        return jsonObject;
    }
    
    private static ExtendedLandmark parseCompactVenueToLandmark(CompactVenue venue, Map<String, String> desc, double myLat, double myLng, Locale locale) {
    	ExtendedLandmark landmark = null;
    	Location location = venue.getLocation();

        if (location != null && location.getLat() != null && location.getLng() != null) {
            double lat = MathUtils.normalizeE6(location.getLat());
            double lng = MathUtils.normalizeE6(location.getLng());
            QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
 		   
            String name = venue.getName();
            
            String url = venue.getId();
            
            String creationDateStr = desc.remove("creationDate");
            long creationDate = -1; 
            if (creationDateStr != null) {
            	creationDate = Long.valueOf(creationDateStr);
            }

            Category[] categories = venue.getCategories();
            String category = "", thumbnail = "";

            for (int k = 0; k < categories.length; k++) {
                if (category.length() > 0) {
                    category += ", ";
                }
                if (StringUtils.isEmpty(thumbnail)) {
                    Icon iconObj = categories[k].getIcon();
                	thumbnail = iconObj.getPrefix() + "bg_32" + iconObj.getSuffix(); //32, 44, 64, and 88 are available
                }
                category += categories[k].getName();
            }
            
            if (desc.containsKey("icon")) {
                thumbnail = desc.remove("icon");
            }
            
            Map<String, String> tokens = new HashMap<String, String>();

            if (category.length() > 0) {
                tokens.put("category", category);
            }
            
            AddressInfo address = new AddressInfo();
            address.setField(AddressInfo.STREET, location.getAddress());
            address.setField(AddressInfo.CITY, location.getCity());
            address.setField(AddressInfo.COUNTRY, location.getCountry());
            //desc.put("name", location.getName());
            address.setField(AddressInfo.POSTAL_CODE, location.getPostalCode());
            address.setField(AddressInfo.STATE, location.getState());

            Contact contact = venue.getContact();
            if (contact.getEmail() != null) {
            	tokens.put("email", contact.getEmail());
            }
            if (contact.getFacebook() != null) {
            	tokens.put("facebook", contact.getFacebook());
            }
            address.setField(AddressInfo.PHONE_NUMBER, contact.getPhone());
            if (contact.getTwitter() != null) {
            	tokens.put("twitter", contact.getTwitter());
            } 
            
            landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.FOURSQUARE_LAYER, address, creationDate, null);
            landmark.setUrl(FOURSQUARE_PREFIX + url);
            landmark.setThumbnail(thumbnail);
            
            String ratingStr = desc.remove("rating");
            int rating = -1;
            if (ratingStr != null) {
            	rating = Integer.valueOf(ratingStr).intValue();
            	landmark.setRating(rating);
            }
            
            String username = desc.remove("username");
            if (username != null) {
            	PrettyTime prettyTime = new PrettyTime(locale);
            	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource", locale);
                Calendar cal = Calendar.getInstance();
            	cal.setTimeInMillis(creationDate);
            	String checkins = String.format(rb.getString("Landmark.checkinUser"), username, prettyTime.format(cal.getTime())); 
            	tokens.put("checkins", checkins);
                landmark.setHasCheckinsOrPhotos(true);
            }

            String photo = desc.remove("photo");
            if (photo != null) {
                tokens.put("photo", UrlUtils.getShortUrl(photo));
            }
            
            String numberOfReviews = desc.remove("numberOfReviews");
            if (numberOfReviews != null) {
            	int numOfRev = NumberUtils.getInt(numberOfReviews, 0);
            	landmark.setNumberOfReviews(numOfRev);
            }
            
            tokens.putAll(desc);
            
            if (tokens.containsKey("photoUser")) {
            	landmark.setHasCheckinsOrPhotos(true);
            }

            String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
            landmark.setDescription(description);
        }

        return landmark;
    }

    private static Map<String, Map<String, String>> getVenueDetails(List<String> venueIds, String locale) throws UnsupportedEncodingException, MalformedURLException, IOException, JSONException {
        StringBuilder urlPrefix = new StringBuilder("https://api.foursquare.com/v2/multi").append("?client_id=").append(Commons.FS_CLIENT_ID).
                append("&client_secret=").append(Commons.FS_CLIENT_SECRET).
                append("&v=").append(FoursquareApi.DEFAULT_VERSION);

        String multiRequest = "";

        ThreadFactory foursquareThreadFactory = ThreadManager.currentRequestThreadFactory();

        Map<String, Map<String, String>> attrs = new HashMap<String, Map<String, String>>();

        Map<String, Thread> venueDetailsThreads = new ConcurrentHashMap<String, Thread>();

        boolean bitlyFailed = false;

        for (int i = 0; i < venueIds.size(); i++) {

            String venueId = venueIds.get(i);

            if (multiRequest.length() > 0) {
                multiRequest += ",";
            }
            multiRequest += "/venues/" + venueId;

            //max 5 requests
            if (i % 5 == 4 || i == (venueIds.size() - 1)) {
                //call foursquare

                Thread venueDetailsRetriever = foursquareThreadFactory.newThread(new VenueDetailsRetriever(venueDetailsThreads, attrs,
                        locale, urlPrefix.toString(), multiRequest, venueId, bitlyFailed));

                venueDetailsThreads.put(multiRequest, venueDetailsRetriever);

                venueDetailsRetriever.start();

                multiRequest = "";
            }
        }

        ThreadUtil.waitForLayers(venueDetailsThreads);

        return attrs;
    }

    private static JSONObject createCustomJsonFoursquareMerchantList(String fourquareJson, String locale, String categoryid, int stringLimit) throws JSONException {
        List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        if (StringUtils.startsWith(fourquareJson, "{")) {
            try {
                JSONObject jsonRoot = new JSONObject(fourquareJson);
                JSONObject meta = jsonRoot.getJSONObject("meta");

                int code = meta.getInt("code");

                if (code == 200) {

                    JSONObject response = jsonRoot.getJSONObject("response");
                    JSONObject specials = response.getJSONObject("specials");
                    int count = specials.getInt("count");

                    if (count > 0) {
                        String[] enabledCategories = {""};
                        if (categoryid != null) {
                            enabledCategories = StringUtils.split(categoryid, ",");
                        }

                        JSONArray items = specials.getJSONArray("items");

                        List<String> venueIds = new ArrayList<String>();

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject venue = item.getJSONObject("venue");
                            venueIds.add(venue.getString("id"));
                        }

                        Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            Map<String, Object> jsonObject = new HashMap<String, Object>();
                            JSONObject venue = item.getJSONObject("venue");

                            Map<String, String> desc = descs.remove(venue.getString("id"));
                            if (desc == null) {
                                desc = new HashMap<String, String>();
                            }

                            JSONArray categories = venue.getJSONArray("categories");
                            String categoryID = null;
                            if (categories.length() > 0) {
                                String category = "", icon = "";

                                for (int j = 0; j < categories.length(); j++) {
                                    JSONObject c = (JSONObject) categories.get(j);

                                    if (category.length() > 0) {
                                        category += ", ";
                                    }
                                    category += c.getString("name");

                                    String id = c.getString("id");
                                    String[] mapping = FoursquareCategoryMapping.findMapping(id);

                                    if (mapping != null) {
                                        if (mapping[0].length() > 0) {
                                            categoryID = mapping[0];
                                            jsonObject.put("categoryID", categoryID);
                                        }
                                        if (mapping[1].length() > 0) {
                                            jsonObject.put("subcategoryID", mapping[1]);
                                        }
                                    }

                                    if (StringUtils.isEmpty(icon)) {
                                        JSONObject jsonIcon = c.getJSONObject("icon");
                                        if (jsonIcon.has("prefix") && jsonIcon.has("sizes") && jsonIcon.has("name")) {
                                            icon = jsonIcon.getString("prefix") + jsonIcon.getJSONArray("sizes").getInt(0) + jsonIcon.getString("name");
                                        }
                                    }
                                }

                                JSONUtils.putOptValue(desc, "category", category, stringLimit, false);

                                desc.put("icon", icon);
                            }

                            //check if categoryID in enabledCategories
                            if (StringUtils.indexOfAny(categoryID, enabledCategories) >= 0) {

                                JSONObject location = venue.getJSONObject("location");
                                JSONObject contact = venue.getJSONObject("contact");

                                jsonObject.put("name", StringEscapeUtils.unescapeHtml(item.getString("message")));
                                jsonObject.put("lat", MathUtils.normalizeE6(location.getDouble("lat")));
                                jsonObject.put("lng", MathUtils.normalizeE6(location.getDouble("lng")));

                                String url = venue.getString("id");
                                jsonObject.put("url", url);

                                //Long creationDate = creationDates.remove(url);
                                //if (creationDate != null) {
                                //    desc.put("creationDate", Long.toString(creationDate));
                                //}

                                desc.put("merchant", venue.getString("name"));

                                JSONObject stats = venue.optJSONObject("stats");
                                if (stats != null) {
                                    int numOfReviews = stats.optInt("tipCount", 0);
                                    if (numOfReviews > 0) {
                                        desc.put("numberOfReviews", Integer.toString(numOfReviews));
                                    }
                                }

                                Iterator<String> iter = location.keys();

                                while (iter.hasNext()) {
                                    String name = iter.next();

                                    if (name.equals("postalCode")) {
                                        desc.put("zip", location.getString(name));
                                    } else if (!(name.equals("lat") || name.equals("lng") || name.equals("distance") || name.equals("isFuzzed"))) {
                                        desc.put(name, location.getString(name));
                                    }
                                }

                                JSONUtils.putOptValue(desc, "phone", contact, "phone", false, stringLimit, false);
                                JSONUtils.putOptValue(desc, "twitter", contact, "twitter", false, stringLimit, false);

                                String description = item.getString("title") + ". " + item.getString("description") + ". ";
                                description += item.optString("finePrint", "");
                                desc.put("description", StringEscapeUtils.unescapeHtml(description));

                                jsonObject.put("desc", desc);

                                jsonArray.add(jsonObject);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "FoursquareUtils.createCustomJsonFoursquareMerchantList() exception:", ex);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
     
    protected static int addVenue(String accessToken, String name, String desc, String primaryCategoryId, String ll) {
    	try {
    		FoursquareApi api = new FoursquareApi(Commons.FS_CLIENT_ID, Commons.FS_CLIENT_SECRET, null, accessToken, new DefaultIOHandler());
    		Result<CompleteVenue> result = api.venuesAdd(name, null, null, null, null, null, null, ll, primaryCategoryId, desc);
    		int res = result.getMeta().getCode();
    		if (res != 200) {
    			handleError(result.getMeta(), name + "_" + desc + "_" + primaryCategoryId + "_" + ll);
    		}
    		return res;
    	} catch (Exception ex) {
            logger.log(Level.SEVERE, "FoursquareUtils.checkin exception:", ex);
            return 500;
        }
    }

    private static class CheckinComparator implements Comparator<Map<String, Object>> {

        @Override
        public int compare(Map<String, Object> jsonObject0, Map<String, Object> jsonObject1) {
            double distance0 = 1E5;
            if (jsonObject0.containsKey("distance")) {
                distance0 = (Double) jsonObject0.get("distance");
            }
            double distance1 = 1E5;
            if (jsonObject1.containsKey("distance")) {
                distance1 = (Double) jsonObject1.get("distance");
            }

            if (distance1 > distance0) {
                return -1;
            } else if (distance0 > distance1) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private static class VenueDetailsRetriever implements Runnable {

        private Map<String, Thread> venueDetailsThreads;
        private Map<String, Map<String, String>> attrs;
        private String locale, urlPrefix, multiRequest, venueId;
        private boolean bitlyFailed;

        public VenueDetailsRetriever(Map<String, Thread> venueDetailsThreads, Map<String, Map<String, String>> attrs,
                String locale, String urlPrefix, String multiRequest, String venueId, boolean bitlyFailed) {
            this.venueDetailsThreads = venueDetailsThreads;
            this.attrs = attrs;
            this.locale = locale;
            this.urlPrefix = urlPrefix;
            this.multiRequest = multiRequest;
            this.venueId = venueId;
            this.bitlyFailed = bitlyFailed;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(urlPrefix.toString() + "&requests=" + URLEncoder.encode(multiRequest, "UTF-8"));
                String fourquareJson = HttpUtils.processFileRequestWithLocale(url, locale);

                if (StringUtils.startsWith(fourquareJson, "{")) {
                    JSONObject jsonRoot = new JSONObject(fourquareJson);
                    JSONObject meta = jsonRoot.getJSONObject("meta");
                    int code = meta.getInt("code");
                    if (code == 200) {
                        Map<String, String> venueAttrs = new HashMap<String, String>();

                        JSONObject response = jsonRoot.getJSONObject("response");
                        JSONArray responses = response.getJSONArray("responses");

                        for (int j = 0; j < responses.length(); j++) {
                            JSONObject resp = responses.getJSONObject(j);
                            JSONObject metar = jsonRoot.getJSONObject("meta");
                            int coder = metar.getInt("code");
                            if (coder == 200) {
                                JSONObject responser = resp.getJSONObject("response");
                                JSONObject venue = responser.optJSONObject("venue");
                                if (venue != null) {
                                    //creationDate
                                    Long creationDate = venue.getLong("createdAt") * 1000;
                                    venueAttrs.put("creationDate", Long.toString(creationDate));
                                    //

                                    //photos
                                    JSONObject photos = venue.getJSONObject("photos");
                                    int count = photos.getInt("count");

                                    if (count > 0) {
                                    	JSONArray groups = photos.getJSONArray("groups");
                                        boolean hasPhoto = false;
                                        for (int k = 0; k < groups.length(); k++) {
                                        	JSONObject group = groups.getJSONObject(k);
                                            int groupCount = group.getInt("count");
                                            //String type = group.getString("type");
                                            //System.out.println("Photos: type " + type + ", count " + groupCount);
                                            if (groupCount > 0) {
                                            	JSONArray items = group.getJSONArray("items");
                                                if (items.length() > 0) {
                                                	JSONObject newest = items.getJSONObject(0);

                                                	//photoUser
                                                	JSONObject user = newest.getJSONObject("user");
                                                    String photoUser = "";
                                                    if (user.has("firstName")) {
                                                        photoUser = user.getString("firstName");
                                                    }
                                                    if (user.has("lastName")) {
                                                        photoUser += " " + user.getString("lastName");
                                                    }
                                                    if (StringUtils.isNotEmpty("photoUser")) {
                                                        venueAttrs.put("photoUser", photoUser);
                                                    }

                                                    //photo url
                                                    //String photo = newest.getString("url");
                                                    	
                                                    String photo = newest.getString("prefix") + "100x100" + newest.getString("suffix");
                                                    	
                                                    if (!bitlyFailed) {
                                                    	String shortUrl = UrlUtils.getShortUrl(photo);
                                                        if (StringUtils.equals(shortUrl, photo)) {
                                                        	bitlyFailed = true;
                                                        } else {
                                                        	photo = shortUrl;
                                                        }
                                                     }
                                                        
                                                     venueAttrs.put("caption", photo);
                                                     hasPhoto = true;
                                                     	
                                                     venueAttrs.put("icon", photo);

                                                     //icon
                                                     /*JSONObject sizes = newest.optJSONObject("sizes");
                                                     if (sizes != null) {
                                                         JSONArray imgItems = sizes.getJSONArray("items");
                                                         for (int i=0;i<imgItems.length();i++) {
                                                        	JSONObject item = imgItems.getJSONObject(i);
                                                            if (item.getInt("width") == 100 && item.getInt("height") == 100) {
                                                                venueAttrs.put("icon", item.getString("url"));
                                                            }
                                                         }
                                                     }*/
                                                }
                                            }
                                            if (hasPhoto) {
                                                break;
                                            }
                                        }
                                    }
                                    
                                    //

                                    //menu
                                    JSONObject menu = venue.optJSONObject("menu");
                                    if (menu != null) {
                                        venueAttrs.put("menu", menu.getString("mobileUrl"));
                                        //menu: {
                                        //url: "https://foursquare.com/v/clinton-street-baking-co/40a55d80f964a52020f31ee3/menu"
                                        //mobileUrl: "https://foursquare.com/v/40a55d80f964a52020f31ee3/device_menu"
                                        //}
                                    }
                                    //

                                    //stats
                                    JSONObject stats = venue.optJSONObject("stats");
                                    if (stats != null) {
                                        int numOfReviews = stats.optInt("tipCount", 0);
                                        if (numOfReviews > 0) {
                                            venueAttrs.put("numberOfReviews", Integer.toString(numOfReviews));
                                        }
                                    }
                                    //

                                }
                            }
                        }

                        if (!venueAttrs.isEmpty()) {
                            attrs.put(venueId, venueAttrs);
                        }
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "FoursquareUtils.VenueDetailsRetriever execption:", ex);
            } finally {
                venueDetailsThreads.remove(multiRequest);
            }
        }
    }
    
    private static List<ExtendedLandmark> createCustomLandmarksFoursquareMerchantList(String fourquareJson, String locale, String categoryid, int stringLimit, Locale l) throws JSONException {
        List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
        
    	if (StringUtils.startsWith(fourquareJson, "{")) {
            try {
                JSONObject jsonRoot = new JSONObject(fourquareJson);
                JSONObject meta = jsonRoot.getJSONObject("meta");

                int code = meta.getInt("code");

                if (code == 200) {

                    JSONObject response = jsonRoot.getJSONObject("response");
                    JSONObject specials = response.getJSONObject("specials");
                    int count = specials.getInt("count");

                    if (count > 0) {
                        String[] enabledCategories = {""};
                        if (categoryid != null) {
                            enabledCategories = StringUtils.split(categoryid, ",");
                        }

                        JSONArray items = specials.getJSONArray("items");

                        List<String> venueIds = new ArrayList<String>();

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject venue = item.getJSONObject("venue");
                            venueIds.add(venue.getString("id"));
                        }

                        Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale);
                        String category = "", icon = "";
                        int categoryID = -1, subcategoryID = -1;
                        
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            
                            JSONObject venue = item.getJSONObject("venue");

                            Map<String, String> tokens = descs.remove(venue.getString("id"));
                            if (tokens == null) {
                                tokens = new HashMap<String, String>();
                            }

                            JSONArray categories = venue.getJSONArray("categories");
                            if (categories.length() > 0) {
                                
                                for (int j = 0; j < categories.length(); j++) {
                                    JSONObject c = (JSONObject) categories.get(j);

                                    if (category.length() > 0) {
                                        category += ", ";
                                    }
                                    category += c.getString("name");

                                    String id = c.getString("id");
                                    String[] mapping = FoursquareCategoryMapping.findMapping(id);

                                    if (mapping != null) {
                                        if (mapping[0].length() > 0) {
                                            categoryID = NumberUtils.getVersion(mapping[0], -1);
                                        }
                                        if (mapping[1].length() > 0) {
                                            subcategoryID = NumberUtils.getVersion(mapping[1], -1);
                                        }
                                    }

                                    if (StringUtils.isEmpty(icon)) {
                                        JSONObject jsonIcon = c.getJSONObject("icon");
                                        if (jsonIcon.has("prefix") && jsonIcon.has("sizes") && jsonIcon.has("name")) {
                                            icon = jsonIcon.getString("prefix") + jsonIcon.getJSONArray("sizes").getInt(0) + jsonIcon.getString("name");
                                        }
                                    }
                                }    
                            }

                            //check if categoryID in enabledCategories
                            if (StringUtils.indexOfAny(Integer.toString(categoryID), enabledCategories) >= 0) {

                                JSONObject location = venue.getJSONObject("location");
                                JSONObject contact = venue.getJSONObject("contact");

                                long creationDate = -1;
                                String creationDateString = tokens.remove("creationDate");
                                if (StringUtils.isNumeric(creationDateString)) {
                                	creationDate = Long.valueOf(creationDateString).longValue();
                                }
                                
                                String name = StringEscapeUtils.unescapeHtml(item.getString("message"));
                                String url = venue.getString("id");
                                AddressInfo address = new AddressInfo();
                                QualifiedCoordinates qc = new QualifiedCoordinates(location.getDouble("lat"), location.getDouble("lng"), 0f, 0f, 0f);
                                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.FOURSQUARE_MERCHANT_LAYER, address, creationDate, null);
                                landmark.setUrl(FOURSQUARE_PREFIX + url);
                                landmark.setCategoryId(categoryID);
                                landmark.setSubCategoryId(subcategoryID);
                                		
                                tokens.put("merchant", venue.getString("name"));

                                JSONObject stats = venue.optJSONObject("stats");
                                if (stats != null) {
                                    int numOfReviews = stats.optInt("tipCount", 0);
                                    if (numOfReviews > 0) {
                                        landmark.setNumberOfReviews(numOfReviews);
                                    }
                                } else {
                                	String numOfReviews = tokens.remove("numberOfReviews"); 
                                    if (StringUtils.isNumeric(numOfReviews)) {
                                    	landmark.setNumberOfReviews(Integer.valueOf(numOfReviews).intValue());	
                                    }
                                	
                                }
                                tokens.remove("numberOfReviews");
                             
                                for (Iterator<String> iter = location.keys(); iter.hasNext();) {
                                    String key = iter.next();

                                    if (key.equals("postalCode")) {
                                    	address.setField(AddressInfo.POSTAL_CODE, location.getString(key));
                                    } else if (key.equals("address")) {
                                    	address.setField(AddressInfo.STREET, location.getString(key));
                                    } else if (key.equals("country")) {
                                    	address.setField(AddressInfo.COUNTRY, location.getString(key));
                                    } else if (key.equals("cc")) {
                                    	address.setField(AddressInfo.COUNTRY_CODE, location.getString(key));
                                    } else if (key.equals("city")) {
                                    	address.setField(AddressInfo.CITY, location.getString(key));
                                    } else if (key.equals("state")) {
                                    	address.setField(AddressInfo.STATE, location.getString(key));
                                    } else if (key.equals("crossStreet")) {
                                    	address.setField(AddressInfo.CROSSING1, location.getString(key));
                                    }
                                }
                                
                                if (contact.has("phone")) {
                                	address.setField(AddressInfo.PHONE_NUMBER, contact.getString("phone"));
                                }
                                
                                JSONUtils.putOptValue(tokens, "homepage", venue, "url", false, stringLimit, false);

                                JSONUtils.putOptValue(tokens, "twitter", contact, "twitter", false, stringLimit, false);

                                JSONUtils.putOptValue(tokens, "category", category, stringLimit, false);

                                String image = tokens.remove("icon");
                                if (StringUtils.isNotEmpty(image)) {
                                	landmark.setThumbnail(image);
                                } else if (StringUtils.isNotEmpty(icon)) {
                                    landmark.setThumbnail(icon);
                                }
                                
                                String description = item.getString("title") + ". " + item.getString("description") + ". ";
                                description += item.optString("finePrint", "");
                                tokens.put("description", StringEscapeUtils.unescapeHtml(description));
                                
                                String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, l);
                                landmark.setDescription(desc);
                                
                                landmarks.add(landmark);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "FoursquareUtils.createCustomLandmarksFoursquareMerchantList() exception:", ex);
            }
        }

        return landmarks;
    }
    
    private static void handleError(ResultMeta meta, String key) {
    	logger.log(Level.SEVERE, "Received FS response {0} {1}: {2}", new Object[]{meta.getCode(), meta.getErrorDetail(), key});
    }
}
