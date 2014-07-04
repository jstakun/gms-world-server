package com.jstakun.lm.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.MathUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import fi.foyt.foursquare.api.FoursquareApi;

public class FoursquareMerchantUtils extends FoursquareUtils {
	
	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String token, String categoryid, Locale l) throws MalformedURLException, IOException, JSONException {
        String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, categoryid, radius, version, limit, stringLimit, token, l.getLanguage());
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);
        
        if (landmarks == null) {
            StringBuilder sb = new StringBuilder("https://api.foursquare.com/v2/specials/search?ll=").append(lat).
                    append(",").append(lng).append("&llAcc=").append(radius).append("&oauth_token=").
                    append(token).append("&limit=").append(limit).append("&v=").append(FoursquareApi.DEFAULT_VERSION);
            URL url = new URL(sb.toString());
            String resp = HttpUtils.processFileRequestWithLocale(url, l.getLanguage());
            landmarks = createCustomLandmarksFoursquareMerchantList(resp, l.getLanguage(), categoryid, stringLimit, l);
            if (!landmarks.isEmpty()) {
                CacheUtil.put(key, landmarks);
                logger.log(Level.INFO, "Adding FSM landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading FSM landmark list from cache with key {0}", key);
        }

        return landmarks;
    }
	
	@Override
	public JSONObject processRequest(double lat, double lng, String categoryid, int radius, int version, int limit, int stringLimit, String token, String locale) throws MalformedURLException, IOException, JSONException {
        String key = getCacheKey(getClass(), "processRequest", lat, lng, categoryid, radius, version, limit, stringLimit, token, locale);
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
	
	private JSONObject createCustomJsonFoursquareMerchantList(String fourquareJson, String locale, String categoryid, int stringLimit) throws JSONException {
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
                logger.log(Level.SEVERE, "FoursquareMerchantUtils.createCustomJsonFoursquareMerchantList() exception:", ex);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
	
	private List<ExtendedLandmark> createCustomLandmarksFoursquareMerchantList(String fourquareJson, String locale, String categoryid, int stringLimit, Locale l) throws JSONException {
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
                logger.log(Level.SEVERE, "FoursquareMerchantUtils.createCustomLandmarksFoursquareMerchantList() exception:", ex);
            }
        }

        return landmarks;
    }
}
