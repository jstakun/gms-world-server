package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadManager;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.ResultMeta;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.CompleteVenue;
import fi.foyt.foursquare.api.entities.Contact;
import fi.foyt.foursquare.api.entities.HereNow;
import fi.foyt.foursquare.api.entities.Icon;
import fi.foyt.foursquare.api.entities.Location;
import fi.foyt.foursquare.api.entities.Photo;
import fi.foyt.foursquare.api.entities.Recommendation;
import fi.foyt.foursquare.api.entities.RecommendationGroup;
import fi.foyt.foursquare.api.entities.Recommended;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;
import fi.foyt.foursquare.api.io.DefaultIOHandler;

/**
 *
 * @author jstakun
 */
public class FoursquareUtils extends LayerHelper {

    private static final CheckinComparator checkinComparator = new CheckinComparator();
    protected static final String FOURSQUARE_PREFIX = "http://foursquare.com/venue/";
    private static final String TRENDING_ENDPOINT_ERROR_MARKER = "FoursquareTrendingApiEndpointErrorMarker";
    
    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String intent, String locale) throws JSONException, MalformedURLException, IOException, FoursquareApiException {
    	JSONObject response = null;
        if (isEnabled()) {
        	String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, intent, locale);
        	String cachedResponse = cacheProvider.getString(key);
        
        	if (cachedResponse == null) {
        		FoursquareApi api = getFoursquareApi(null);
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

        				Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale, stringLimit);

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

        				Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale, stringLimit);

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
        			cacheProvider.put(key, response.toString());
        		}
        	} else {
        		logger.log(Level.INFO, "Reading FS landmark list from cache with key {0}", key);
        		response = new JSONObject(cachedResponse);
        	}
        } else {
    		response = new JSONObject().put("ResultSet", new JSONArray());
    	}

        return response;
    }
    
    @Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String intent, String language, Locale locale, boolean useCache) throws Exception {
       	   if (lat == 0d && lng == 0d) {
       		  throw new IllegalArgumentException("Latitude or longitude mustn't be zero!");
       	   }
       	   if (language == null) {
       		   language = locale.getLanguage();
       	   }
       	   if (intent == null) {
       		   intent = "checkin";
       	   }
    	   List<ExtendedLandmark> response = new ArrayList<ExtendedLandmark>();
           
           FoursquareApi api = getFoursquareApi(null);
           api.setUseCallback(false);
           
           logger.log(Level.INFO, "Searching for venues around...");
           
           //venues search
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

                   		Map<String, Map<String, String>> descs = getVenueDetails(venueIds, language, stringLimit);

                   		for (int j = 0; j < venues.length; j++) {
                   			CompactVenue venue = venues[j];

                   			Map<String, String> attrs = descs.remove(venue.getId());
                   			if (attrs == null) {
                   				attrs = new HashMap<String, String>();
                   			}
                       
                   			ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, locale, stringLimit);
                   			if (landmark != null) {
                   				response.add(landmark);
                   			}
                   		}
              }	
          } else {
        	  handleError(result.getMeta(), "loadLandmarks");
          }
               
          //venues trending
          
          if (!cacheProvider.containsKey(TRENDING_ENDPOINT_ERROR_MARKER)) {
        	  logger.log(Level.INFO, "Loading trending venues...");

        	  Result<CompactVenue[]> resultT = api.venuesTrending(lat + "," + lng, limit, radius);

        	  if (resultT.getMeta().getCode() == 200) {
        		  CompactVenue[] venues = resultT.getResult();
        		  logger.log(Level.INFO, "No of Foursquare trending venues {0}", venues.length);

        		  if (venues.length > 0) {
        			  List<String> venueIds = new ArrayList<String>();
                   		 
                   	  for (int j = 0; j < venues.length; j++) {
                   		  venueIds.add(venues[j].getId());
                   	  }

                   	  Map<String, Map<String, String>> descs = getVenueDetails(venueIds, language, stringLimit);

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

                   		  ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, locale, stringLimit);
                   		  if (landmark != null) {
                   			  response.add(landmark);
                   		  }
                   	  }
        		  }
        	  } else {
        		  handleError(resultT.getMeta(), "loadLandmarksTrending");
        	   	  Integer responseCode = resultT.getMeta().getCode();
        	   	  if (responseCode != null && responseCode == 400) {
        	   		  cacheProvider.put(TRENDING_ENDPOINT_ERROR_MARKER, "1");
        	   	  }
        	  }
          }

          logger.log(Level.INFO, "Done.");    
               
          return response;
   	}

    public String exploreVenuesToJSon(double lat, double lng, String query, int radius, int limit, int stringLimit, int version, String token, String locale) throws JSONException, MalformedURLException, IOException, FoursquareApiException {
        String key = getCacheKey(FoursquareUtils.class, "exploreVenuesToJSon", lat, lng, query, radius, version, limit, 0, token, locale);
        String jsonString = cacheProvider.getString(key);
        if (jsonString == null) {
            FoursquareApi api = getFoursquareApi(token);
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

                    Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale, stringLimit);

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
                    cacheProvider.put(key, jsonString);
                }
            } else {
            	handleError(recommended.getMeta(), key);
            }
        }
        return jsonString;
     }
    
    public List<ExtendedLandmark> exploreVenuesToLandmark(double lat, double lng, String query, int radius, int limit, int stringLimit, int version, String token, Locale locale, boolean useCache) throws JSONException, MalformedURLException, IOException, FoursquareApiException {
    	String key = null;
    	List<ExtendedLandmark> landmarks = null;
    	if (useCache) {
    		key = getCacheKey(FoursquareUtils.class, "exploreVenuesToLandmark", lat, lng, query, radius, version, limit, 0, token, locale.getLanguage());
    		landmarks = cacheProvider.getList(ExtendedLandmark.class,key);
    	}
    	if (landmarks == null) {
            FoursquareApi api = getFoursquareApi(token);
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

                    Map<String, Map<String, String>> descs = getVenueDetails(venueIds, locale.getLanguage(), stringLimit);

                    for (int j = 0; j < rec.length; j++) {

                        Recommendation r = rec[j];
                        //Reason[] reasons = r.getReasons().getItems();
                        CompactVenue venue = r.getVenue();

                        Map<String, String> attrs = descs.remove(venue.getId());
                        if (attrs == null) {
                            attrs = new HashMap<String, String>();
                        }

                        attrs.put("rating", "5");

                        ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, locale, stringLimit);
                        if (landmark != null) {
                        	landmarks.add(landmark);
                        }
                    }
                }

                logger.log(Level.INFO, "No of Foursquare recommended venues {0}", recCount);

                //write to cache
                if (useCache && landmarks != null && !landmarks.isEmpty()) {
                    logger.log(Level.INFO, "Adding fs explore list to cache with key {0}", key);
                    cacheProvider.put(key, landmarks);
                }
            } else {
            	handleError(recommended.getMeta(), key);
            }
        }
        return landmarks;
    }

    public List<ExtendedLandmark> getFriendsCheckinsToLandmarks(double latitude, double longitude, int limit, int stringLimit, int version, String token, Locale locale, boolean useCache) throws FoursquareApiException, JSONException, UnsupportedEncodingException {
    	String key = null;
    	List<ExtendedLandmark> landmarks = null;
    	if (useCache) {
    		key = getCacheKey(FoursquareUtils.class, "getFriendsCheckinsToLandmark", 0, 0, null, 0, version, limit, 0, token, locale.getLanguage());
        	landmarks = cacheProvider.getList(ExtendedLandmark.class,key);
    	}
        if (landmarks == null) {
            FoursquareApi api = getFoursquareApi(token);
            api.setUseCallback(false);
            //api.setVersion(VDATE);
            
            Result<Checkin[]> response = api.checkinsRecent(latitude + "," + longitude, limit, null);
            landmarks = new ArrayList<ExtendedLandmark>();
            if (response.getMeta().getCode() == 200) {
                Checkin[] checkins = response.getResult();
                logger.log(Level.INFO, "No of Foursquare checkins {0}", checkins.length);
                ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource", locale);
                PrettyTime prettyTime = new PrettyTime(locale);
            	Calendar cal = Calendar.getInstance();
            	List<CompactVenue> venues = new ArrayList<CompactVenue>();
            	Map<String, Map<String, String>> venuesAttrs = new HashMap<String, Map<String, String>>(); 
                       
                for (int j = 0; j < checkins.length; j++) {
                    Checkin checkin = checkins[j];
                    CompactVenue venue = checkin.getVenue();
                    if (venue != null) {
                    	String venueid = venue.getId();                	
                    	Map<String, String> attrs = venuesAttrs.get(venueid);
                    	
                    	if (attrs == null) {
                    		venues.add(venue);                   	
                        	attrs = new HashMap<String, String>();                    
                    		//photo
                    		Photo[] photos = checkin.getPhotos().getItems();
                    		String photo = null;
                    		if (photos.length > 0) {
                    			photo = photos[0].getUrl();
                    		}
                    		if (photo != null) {
                    			attrs.put("photo", photo);
                    		}
                        
                    		//checkin
                    		long creationDate = checkin.getCreatedAt() * 1000;
                    		attrs.put("creationDate", Long.toString(creationDate));
                    		String username = checkin.getUser().getFirstName();
                    		String lastname = checkin.getUser().getLastName();
                    		if (StringUtils.isNotEmpty(lastname)) {
                    			username += " " + lastname;
                    		}
                    		cal.setTimeInMillis(creationDate);
                    		String checkinStr = String.format(rb.getString("Landmark.checkinUser"), username, prettyTime.format(cal.getTime())); 
                    		attrs.put("checkin", checkinStr);
                    		
                    		venuesAttrs.put(venueid, attrs);
                    	} else {
                    		//checkin
                    		long creationDate = checkin.getCreatedAt() * 1000;
                    		String username = checkin.getUser().getFirstName();
                    		String lastname = checkin.getUser().getLastName();
                    		if (StringUtils.isNotEmpty(lastname)) {
                    			username += " " + lastname;
                    		}
                    		cal.setTimeInMillis(creationDate);
                    		String checkinStr = String.format(rb.getString("Landmark.checkinUser"), username, prettyTime.format(cal.getTime())); 
                    		attrs.put("checkin", attrs.get("checkin") + ", " + checkinStr);
                    	}                 
                    }
                }
                
                for (CompactVenue venue : venues) {
                	//create landmark
                	Map<String, String> attrs = venuesAttrs.get(venue.getId());
                	ExtendedLandmark landmark = parseCompactVenueToLandmark(venue, attrs, locale, stringLimit);
                    if (landmark != null) {
                    	landmarks.add(landmark);
                    } 
                }
            } else {
            	handleError(response.getMeta(), key);
            }

            //write to cache
            if (useCache && landmarks != null && !landmarks.isEmpty()) {
                logger.log(Level.INFO, "Adding fs friends list to cache with key {0}", key);
                cacheProvider.put(key, landmarks);
            }
        } else {
            logger.log(Level.INFO, "Reading fs friends list from cache with key {0}", key);
        }

        return landmarks;
    }

    public String getFriendsCheckinsToJSon(double latitude, double longitude, int limit, int version, String token, String locale) throws FoursquareApiException, JSONException, UnsupportedEncodingException {
        String key = getCacheKey(FoursquareUtils.class, "getFriendsCheckinsToJSon", 0, 0, null, 0, version, limit, 0, token, locale);
        String jsonString = cacheProvider.getString(key);

        if (jsonString == null) {
            FoursquareApi api = getFoursquareApi(token);
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

                        Map<String, String> attrs = new HashMap<String, String>();
                        
                        String username = checkin.getUser().getFirstName();
                        String lastname = checkin.getUser().getLastName();
                        if (StringUtils.isNotEmpty(lastname)) {
                            username += " " + lastname;
                        }
                        attrs.put("username", username);

                        long creationDate = checkin.getCreatedAt() * 1000;
                        attrs.put("creationDate", Long.toString(creationDate));
                        
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
                cacheProvider.put(key, jsonString);
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
                	icon = iconObj.getPrefix() + "bg_64" + iconObj.getSuffix(); //32, 44, 64, and 88 are available
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
                desc.put("photo", photo);//UrlUtils.getShortUrl(photo));
            }

            jsonObject.put("desc", desc);
        }

        return jsonObject;
    }
    
    private static ExtendedLandmark parseCompactVenueToLandmark(CompactVenue venue, Map<String, String> desc, Locale locale, int stringLimit) {
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
                    if (stringLimit == StringUtil.XLARGE) {
                    	thumbnail = iconObj.getPrefix() + "bg_88" + iconObj.getSuffix();
                    } else {
                    	thumbnail = iconObj.getPrefix() + "bg_64" + iconObj.getSuffix(); //32, 44, 64, and 88 are available
                    }
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
            String phone = contact.getPhone();
            if (phone != null && phone.matches(".*\\d+.*")) {
            	address.setField(AddressInfo.PHONE_NUMBER, phone);
            }
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
            
            String checkin = desc.remove("checkin");
            if (StringUtils.isNotEmpty(checkin)) {
            	tokens.put("checkins", checkin);
                landmark.setHasCheckinsOrPhotos(true);
            }

            String photo = desc.remove("photo");
            if (photo != null) {
                tokens.put("photo", photo); //UrlUtils.getShortUrl(photo));
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

    protected Map<String, Map<String, String>> getVenueDetails(List<String> venueIds, String locale, int stringLimit) throws UnsupportedEncodingException, MalformedURLException, IOException, JSONException {
        StringBuilder urlPrefix = new StringBuilder("https://api.foursquare.com/v2/multi").
        		append("?client_id=").append(Commons.getProperty(Property.FS_CLIENT_ID)).
                append("&client_secret=").append(Commons.getProperty(Property.FS_CLIENT_SECRET)).
                append("&v=").append(FoursquareApi.DEFAULT_VERSION);

        String multiRequest = "";

        Map<String, Map<String, String>> attrs = new HashMap<String, Map<String, String>>();

        ThreadManager threadManager = new ThreadManager(threadProvider);
        
        for (int i = 0; i < venueIds.size(); i++) {

            String venueId = venueIds.get(i);

            if (multiRequest.length() > 0) {
                multiRequest += ",";
            }
            multiRequest += "/venues/" + venueId;

            //max 5 requests
            if (i % 5 == 4 || i == (venueIds.size() - 1)) {
                //call foursquare
            	
                threadManager.put(new VenueDetailsRetriever(attrs, locale, urlPrefix.toString(), multiRequest, venueId, stringLimit));

                multiRequest = "";
            }
        }

        threadManager.waitForThreads();

        return attrs;
    }
     
    public int addVenue(String accessToken, String name, String desc, String primaryCategoryId, String ll) {
    	try {
    		FoursquareApi api = getFoursquareApi(accessToken);
    		Result<CompleteVenue> result = api.venuesAdd(name, null, null, null, null, null, null, ll, primaryCategoryId, desc);
    		int res = result.getMeta().getCode();
    		if (res != 200) {
    			handleError(result.getMeta(), "name:" + name + ",desc:" + desc + ",cat:" + primaryCategoryId + ",ll:" + ll);
    		}
    		return res;
    	} catch (Exception ex) {
            logger.log(Level.SEVERE, "FoursquareUtils.checkin exception:", ex);
            return 500;
        }
    }

    private static void handleError(ResultMeta meta, String key) throws FoursquareApiException {
    	logger.log(Level.SEVERE, "Received FS response {0} {1} {2}: {3}", new Object[]{meta.getCode(), meta.getErrorType(), meta.getErrorDetail(), key});
    	if (meta.getCode() == HttpServletResponse.SC_UNAUTHORIZED) {
    		throw new FoursquareApiException("Unauthorized");
    	}
    }
    
    private static FoursquareApi getFoursquareApi(String token) {
    	return new FoursquareApi(Commons.getProperty(Property.FS_CLIENT_ID), Commons.getProperty(Property.FS_CLIENT_SECRET), null, token, new DefaultIOHandler());
        
    }
    
    private static class CheckinComparator implements Comparator<Map<String, Object>> {

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
    
    public String getLayerName() {
    	return Commons.FOURSQUARE_LAYER;
    }

    private static class VenueDetailsRetriever implements Runnable {

        private Map<String, Map<String, String>> attrs;
        private String locale, urlPrefix, multiRequest, venueId;
        private int stringLimit;

        public VenueDetailsRetriever(Map<String, Map<String, String>> attrs, String locale, String urlPrefix, String multiRequest, String venueId, int stringLimit) {
            this.attrs = attrs;
            this.locale = locale;
            this.urlPrefix = urlPrefix;
            this.multiRequest = multiRequest;
            this.venueId = venueId;
            this.stringLimit = stringLimit;
        }

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
                                                    //36, 100, 300, or 500 
                                                    String photo = newest.getString("prefix");
                                                        
                                                    if (stringLimit == StringUtil.XLARGE) {
                                                    	photo += "200x200";
                                                    } else {
                                                    	photo += "100x100";
                                                    }
                                                    
                                                    photo += newest.getString("suffix");
                                                    
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

                                } else {
                                	logger.log(Level.WARNING, "Venue is null!");
                                }
                            }
                        }

                        if (!venueAttrs.isEmpty()) {
                            attrs.put(venueId, venueAttrs);
                        }
                    }
                } else {
                	logger.log(Level.WARNING, "Received following server response: " + fourquareJson);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "FoursquareUtils.VenueDetailsRetriever execption:", ex);
            } 
        }
    }
}
