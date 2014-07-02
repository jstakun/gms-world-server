/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.Commons.Property;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;
import com.twitter.Autolink;

/**
 *
 * @author jstakun
 */
public class TwitterUtils extends LayerHelper {

    @Override
    protected JSONObject processRequest(double latitude, double longitude, String query, int distance, int version, int limit, int stringLimit, String lang, String flexString2) throws TwitterException, JSONException, UnsupportedEncodingException {
        int radius = NumberUtils.normalizeNumber(distance, 1, 3);

        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, lang, flexString2);

        JSONObject response = null;

        String cachedResponse = CacheUtil.getString(key);
        if (cachedResponse == null) {

            Query twquery;
            if (query != null) {
                twquery = new Query(query);
            } else {
                twquery = new Query();
            }
            twquery.setGeoCode(new GeoLocation(latitude, longitude), radius, Query.KILOMETERS);
            twquery.setCount(limit);
            twquery.setResultType(Query.RECENT); //POPULAR, MIXED, RECENT
            //twquery.setLang(lang);
            if (query != null) {
            }

            QueryResult results = getTwitter(null, null).search(twquery);
            List<Status> tweets = results.getTweets();

            response = createCustomTweetsList(tweets);

            if (response.getJSONArray("ResultSet").length() > 0) {
                CacheUtil.put(key, response.toString());
                logger.log(Level.INFO, "Adding TW landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading TW landmark list from cache with key {0}", key);
            response = new JSONObject(cachedResponse);
        }
        return response;
    }

    private static JSONObject createCustomTweetsList(List<Status> tweets) throws JSONException {
        ArrayList<Object> jsonArray = new ArrayList<Object>();
        Autolink autolink = new Autolink();

        for (Status tweet : tweets) {

            GeoLocation location = tweet.getGeoLocation();
            if (location != null) {
                Map<String, Object> jsonObject = new HashMap<String, Object>();

                User user = tweet.getUser();
                //Place place = tweet.getPlace();

                jsonObject.put("name", user.getScreenName());
                jsonObject.put("lat", location.getLatitude());
                jsonObject.put("lng", location.getLongitude());
                String url; // = user.getURL();
                //if (url == null) {
                url = "http://twitter.com/" + user.getScreenName();
                //}
                jsonObject.put("url", url);

                Map<String, String> desc = new HashMap<String, String>();
                jsonObject.put("desc", desc);

                String text = autolink.autoLink(tweet.getText());

                //#tag  http://twitter.com/search?q=%23tag&src=hash
                //@user  http://twitter.com/user
                //http://

                desc.put("description", text);
                desc.put("icon", user.getBiggerProfileImageURL()); //getProfileImageURL()
                desc.put("creationDate", Long.toString(tweet.getCreatedAt().getTime()));

                jsonArray.add(jsonObject);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
    
    private static List<ExtendedLandmark> createCustomLandmarksList(List<Status> tweets, List<User> users, Locale locale, boolean isFriends) {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
        Autolink autolink = new Autolink();

        for (int i=0;i<tweets.size();i++) {
        	Status tweet = tweets.get(i);
        	if (tweet != null && tweet.getGeoLocation() != null && (tweet.getUser() != null || (users != null && users.get(i) != null))) {
        		GeoLocation location = tweet.getGeoLocation();
                User user = tweet.getUser();
                
                if (user == null) {
                	user = users.get(i);
                }
                
                String name = user.getScreenName();
                String thumbnail = user.getBiggerProfileImageURL();
                
                String url = "http://twitter.com/" + name;
                
                Map<String, String> tokens = new HashMap<String, String>();
                String text = autolink.autoLink(tweet.getText());
                tokens.put("description", text);

                //#tag  http://twitter.com/search?q=%23tag&src=hash
                //@user  http://twitter.com/user
                //http://

                long creationDate = tweet.getCreatedAt().getTime();
                
                AddressInfo address = new AddressInfo();
                QualifiedCoordinates qc = new QualifiedCoordinates(location.getLatitude(), location.getLongitude(), 0f, 0f, 0f);
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.TWITTER_LAYER, address, creationDate, null);
                landmark.setThumbnail(thumbnail);
                landmark.setUrl(url);
                
                if (isFriends) {
                	landmark.setHasCheckinsOrPhotos(true);
                }
                
                String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                landmark.setDescription(description);
                
                landmarks.add(landmark);
            }
        }

        return landmarks;
    }

    private static Twitter getTwitter(String token, String secret) {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Commons.getProperty(Property.TW_CONSUMER_KEY), Commons.getProperty(Property.TW_CONSUMER_SECRET));
        AccessToken accessToken;
        if (token != null && secret != null) {
            accessToken = new AccessToken(token, secret);
        } else {
            accessToken = new AccessToken(Commons.getProperty(Property.TW_TOKEN), Commons.getProperty(Property.TW_SECRET));
        }
        twitter.setOAuthAccessToken(accessToken);
        return twitter;
    }

	@Override
	protected List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int distance, int version, int limit, int stringLimit, String lang, String flexString2, Locale locale) throws Exception {
		
		int radius = NumberUtils.normalizeNumber(distance, 1, 3);

        String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, lang, flexString2);

        List<ExtendedLandmark> reply = (List<ExtendedLandmark>) CacheUtil.getObject(key);
        if (reply == null) {

            Query twquery;
            if (query != null) {
                twquery = new Query(query);
            } else {
                twquery = new Query();
            }
            twquery.setGeoCode(new GeoLocation(lat, lng), radius, Query.KILOMETERS);
            twquery.setCount(limit);
            twquery.setResultType(Query.RECENT); //POPULAR, MIXED, RECENT
            //twquery.setLang(lang);
            if (query != null) {
            }

            QueryResult results = getTwitter(null, null).search(twquery);
            List<Status> tweets = results.getTweets();

            reply = createCustomLandmarksList(tweets, null, locale, false);

            if (!reply.isEmpty()) {
                CacheUtil.put(key, reply);
                logger.log(Level.INFO, "Adding TW landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading TW landmark list from cache with key {0}", key);
        }
        
        return reply;
	}
	
	protected static List<ExtendedLandmark> getFriendsStatuses(String token, String secret, Locale locale) throws TwitterException, UnsupportedEncodingException {
		String key = getCacheKey(TwitterUtils.class, "getFriendsStatuses", 0, 0, null, 0, 1, 1, 0, token, locale.getCountry());
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>) CacheUtil.getObject(key);

        if (landmarks == null) {
        	Twitter twitter = getTwitter(token, secret);
        	String username = twitter.getScreenName();
		
        	List<User> friends = twitter.getFriendsList(username, -1);
        	if (!friends.isEmpty()) {
        		List<Status> friendsStatuses = new ArrayList<Status>(friends.size());
        		for (User friend : friends) {
        			Status status = friend.getStatus();
        			friendsStatuses.add(status);
        		}
        		landmarks = createCustomLandmarksList(friendsStatuses, friends, locale, true);
        		logger.log(Level.INFO, "Found " + landmarks.size() + " friends statuses");
        	} else {
        		logger.log(Level.INFO, "No friends found");
        		landmarks = new ArrayList<ExtendedLandmark>();
        	}
		
        	List<User> followers = twitter.getFollowersList(username, -1);
        	if (!followers.isEmpty()) {
        		List<Status> followersStatuses = new ArrayList<Status>(followers.size());
        		for (User follower : followers) {
        			Status status = follower.getStatus();
        			followersStatuses.add(status);
        		}
        		int friendsSize = landmarks.size();
        		landmarks.addAll(createCustomLandmarksList(followersStatuses, friends, locale, true));
        		logger.log(Level.INFO, "Found " + (landmarks.size()-friendsSize) + " followers statuses");
        	} else {
        		logger.log(Level.INFO, "No followers found");
        	}		
        	
        	if (!landmarks.isEmpty()) {
                CacheUtil.put(key, landmarks);
                logger.log(Level.INFO, "Adding TW friends to cache with key {0}", key);
            }
        } else {
        	logger.log(Level.INFO, "Reading TW friends list from cache with key {0}", key);
        }
		return landmarks;
	}
}
