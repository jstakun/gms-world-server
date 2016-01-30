package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class GrouponUtils extends LayerHelper {

    //2011-08-20 03:59:59
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
    
    private static final String API_URL = "https://partner-api.groupon.com/v2/deals.json?tsToken=US_AFF_0_" + Commons.getProperty(Property.GROUPON_AFFILIATE_ID) + "_212556_0";

    
    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int dealLimit, int stringLimit, String categoryid, String flexString2) throws MalformedURLException, IOException, JSONException {
    	if (dealLimit > 250) {
			dealLimit = 250;
		}
    	if (radius > 1000) {
			radius = radius / 1000;
		}
		if (radius > 100) {
			radius = 100;
		}
		String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, dealLimit, stringLimit, categoryid, flexString2);

        JSONObject json = null;

        String cachedResponse = cacheProvider.getString(key);
        if (cachedResponse == null) {
            URL grouponUrl = new URL(API_URL + "&lat=" + lat + "&lng=" + lng + "&radius=" + radius + "&limit=" + dealLimit);
            String grouponResponse = HttpUtils.processFileRequest(grouponUrl);
            if (version == 1) {
                json = createCustomJsonGrouponListV1(grouponResponse, dealLimit);
            } else {
                json = createCustomJsonGrouponList(grouponResponse, version, categoryid, dealLimit, query, stringLimit);
            }
            if (json.getJSONArray("ResultSet").length() > 0) {
                cacheProvider.put(key, json.toString());
                logger.log(Level.INFO, "Adding GR landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading GR landmark list from cache with key {0}", key);
            json = new JSONObject(cachedResponse);
        }

        return json;
    }

    private static JSONObject createCustomJsonGrouponList(String grouponJson, int version, String categoryid, int limit, String query, int stringLimit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        String[] enabledCategories = {""};
        if (categoryid != null) {
            enabledCategories = StringUtils.split(categoryid, ",");
        }

        JSONArray deals = JSONUtils.getJSonArray(grouponJson, "deals");
        if (deals != null) {
            
            for (int i = 0; i < deals.length(); i++) {
                try {
                    JSONObject deal = deals.getJSONObject(i);

                    String categoryID = null;
                    String subcategoryID = null;

                    String category = "";
                    JSONArray tags = deal.getJSONArray("tags");
                    if (tags.length() > 0) {
                        for (int j = 0; j < tags.length(); j++) {
                            JSONObject c = (JSONObject) tags.get(j);
                            if (category.length() > 0) {
                                category += ", ";
                            }
                            category += c.getString("name");
                        }
                    }

                    if (category.length() > 0) {
                        String[] mapping = GrouponCategoryMapping.findMapping(category);
                        if (mapping != null) {
                            if (mapping[0].length() > 0) {
                                categoryID = mapping[0];
                            }
                            if (mapping[1].length() > 0) {
                                subcategoryID = mapping[1];
                            }
                        }
                    }

                    //check if deal title || announcementTitle matching query

                    boolean hasQuery = (query == null);

                    if (!hasQuery && deal.has("title")) {
                        String title = deal.getString("title");
                        hasQuery = StringUtils.containsIgnoreCase(title, query);
                    }

                    if (!hasQuery && deal.has("announcementTitle")) {
                        String title = deal.getString("announcementTitle");
                        hasQuery = StringUtils.containsIgnoreCase(title, query);
                    }

                    //check if categoryID in enabledCategories
                    if (StringUtils.indexOfAny(categoryID, enabledCategories) >= 0 && hasQuery) {

                        String start_date = null;
                        String end_date = null;

                        Object start_d = deal.get("startAt");
                        if (StringUtils.isNotEmpty(start_d.toString())) {
                            start_date = start_d.toString();
                            start_date = start_date.replace("T", " ").replace("Z", "");
                        }

                        Object end_d = deal.get("endAt");
                        if (StringUtils.isNotEmpty(end_d.toString())) {
                            end_date = end_d.toString();
                            end_date = end_date.replace("T", " ").replace("Z", "");
                        }

                        if (version >= 3) {
                        	if (start_date != null) {
                                try {
                                	start_date = Long.toString(DateUtils.parseDate(dateFormat, start_date).getTime());
                                } catch (Exception e) {
                                    start_date = null;
                                }
                            }
                            if (end_date != null) {
                                try {
                                    end_date = Long.toString(DateUtils.parseDate(dateFormat, end_date).getTime());
                                } catch (Exception e) {
                                    end_date = null;
                                }
                            }
                        }

                        JSONObject mer = deal.getJSONObject("merchant");
                        String merchant = mer.getString("name");

                        JSONObject division = deal.getJSONObject("division");

                        double divlat = MathUtils.normalizeE6(division.getDouble("lat"));
                        double divlng = MathUtils.normalizeE6(division.getDouble("lng"));

                        JSONArray options = deal.getJSONArray("options");
                        if (options.length() > 0) {
                            for (int j = 0; j < options.length(); j++) {

                                JSONObject option = options.getJSONObject(j);
                                String title = option.getString("title");

                                String description = "";
                                JSONArray details = option.getJSONArray("details");
                                if (details.length() > 0) {
                                    JSONObject detail = details.getJSONObject(0);
                                    description = detail.getString("description");
                                }

                                JSONObject priceobj = option.getJSONObject("price");
                                String price = priceobj.getString("formattedAmount");

                                JSONObject discountobj = option.getJSONObject("discount");
                                String save = discountobj.getString("formattedAmount");
                                String discount = option.getDouble("discountPercent") + "%";
                                //option.getString("discountPercent");

                                String url = "http://www.anrdoezrs.net/click-5379376-10804307?url="
                                        + URLEncoder.encode(option.getString("buyUrl"), "UTF-8");

                                JSONArray locations = option.getJSONArray("redemptionLocations");
                                if (locations.length() > 0) {
                                    for (int k = 0; k < locations.length(); k++) {
                                        JSONObject location = locations.getJSONObject(k);

                                        Map<String, Object> jsonObject = new HashMap<String, Object>();
                                        Map<String, String> desc = new HashMap<String, String>();

                                        JSONUtils.putOptValue(desc, "category", category, stringLimit, false);
                                        JSONUtils.putOptValue(desc, "description", description, stringLimit, true);

                                        desc.put("merchant", merchant);
                                        if (start_date != null) {
                                            desc.put("start_date", start_date);
                                        }
                                        if (end_date != null) {
                                            desc.put("end_date", end_date);
                                        }
                                        desc.put("price", price);
                                        desc.put("discount", discount);

                                        jsonObject.put("url", url);
                                        jsonObject.put("name", title);

                                        if (StringUtils.isNotEmpty(categoryID)) {
                                            jsonObject.put("categoryID", categoryID);
                                        }

                                        if (StringUtils.isNotEmpty(subcategoryID)) {
                                            jsonObject.put("subcategoryID", subcategoryID);
                                        }

                                        jsonObject.put("lat", MathUtils.normalizeE6(location.getDouble("lat")));
                                        jsonObject.put("lng", MathUtils.normalizeE6(location.getDouble("lng")));

                                        //city, streetAddress1, streetAddress2,phoneNumber,postalCode,state
                                        if (location.has("streetAddress1")) {
                                            String address = location.getString("streetAddress1");
                                            if (address.length() > 0) {
                                                if (location.has("streetAddress2")) {
                                                	Object s = location.get("streetAddress2");
                                                    if (s != null && s.toString().length() > 0) {
                                                        address += ", " + s.toString();
                                                    }
                                                }
                                                desc.put("address", address);
                                            }
                                        }

                                        JSONUtils.putOptValue(desc, "city", location, "city", false, stringLimit, false);
                                        JSONUtils.putOptValue(desc, "phone", location, "phoneNumber", false, stringLimit, false);
                                        JSONUtils.putOptValue(desc, "zip", location, "postalCode", false, stringLimit, false);
                                        JSONUtils.putOptValue(desc, "state", location, "state", false, stringLimit, false);
                                        if (version >= 4) {
                                            JSONUtils.putOptValue(desc, "icon", deal, "mediumImageUrl", false, stringLimit, false);
                                        }

                                        jsonObject.put("desc", desc);
                                        jsonArray.add(jsonObject);
                                    }
                                } else {
                                    //no redemptionLocations
                                    Map<String, Object> jsonObject = new HashMap<String, Object>();
                                    Map<String, String> desc = new HashMap<String, String>();

                                    JSONUtils.putOptValue(desc, "category", category, stringLimit, false);
                                    JSONUtils.putOptValue(desc, "description", description, stringLimit, true);
                                    if (version >= 4) {
                                        JSONUtils.putOptValue(desc, "icon", deal, "mediumImageUrl", false, stringLimit, false);
                                    }
                                    desc.put("merchant", merchant);
                                    desc.put("start_date", start_date);
                                    desc.put("end_date", end_date);

                                    desc.put("price", price);
                                    desc.put("discount", discount);
                                    desc.put("save", save);

                                    if (StringUtils.isNotEmpty(categoryID)) {
                                        jsonObject.put("categoryID", categoryID);
                                    }

                                    if (StringUtils.isNotEmpty(subcategoryID)) {
                                        jsonObject.put("subcategoryID", subcategoryID);
                                    }

                                    jsonObject.put("url", url);
                                    jsonObject.put("name", title);

                                    jsonObject.put("desc", desc);

                                    jsonObject.put("lat", divlat);
                                    jsonObject.put("lng", divlng);

                                    jsonArray.add(jsonObject);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

                if (jsonArray.size() >= limit) {
                    break;
                }
            }
        } else {
        	logger.log(Level.SEVERE, "Received following server response: " + grouponJson);
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

    private static JSONObject createCustomJsonGrouponListV1(String grouponJson, int limit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        try {
            JSONArray deals = JSONUtils.getJSonArray(grouponJson, "deals");
            if (deals != null) {
                for (int i = 0; i < deals.length(); i++) {
                    JSONObject deal = deals.getJSONObject(i);
                    if (deal.has("division")) {
                        JSONObject division = deal.getJSONObject("division");
                        Map<String, Object> jsonObject = new HashMap<String, Object>();

                        jsonObject.put("lat", MathUtils.normalizeE6(division.getDouble("lat")));
                        jsonObject.put("lng", MathUtils.normalizeE6(division.getDouble("lng")));
                        jsonObject.put("name", deal.getString("title"));
                        jsonObject.put("desc", deal.getString("dealUrl"));
                        jsonArray.add(jsonObject);
                    }
                    if (jsonArray.size() >= limit) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int dealLimit, int stringLimit, String categoryid, String flexString2, Locale locale, boolean useCache) throws Exception {
		if (dealLimit > 250) {
			dealLimit = 250;
		}
		if (radius > 1000) {
			radius = radius / 1000;
		}
		if (radius > 100) {
			radius = 100;
		}
		URL grouponUrl = new URL(API_URL + "&lat=" + lat + "&lng=" + lng + "&radius=" + radius + "&limit=" + dealLimit); 
        logger.log(Level.INFO, "Calling: " + grouponUrl.toExternalForm());
		String grouponResponse = HttpUtils.processFileRequest(grouponUrl);
        return createCustomLandmarkGrouponList(grouponResponse, categoryid, dealLimit, query, stringLimit, locale);
   }
	
	private static List<ExtendedLandmark> createCustomLandmarkGrouponList(String grouponJson, String categoryid, int limit, String query, int stringLimit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
		String[] enabledCategories = {""};
        if (categoryid != null) {
            enabledCategories = StringUtils.split(categoryid, ",");
        }

        JSONArray deals = JSONUtils.getJSonArray(grouponJson, "deals");
        if (deals != null && deals.length() > 0) {
            logger.log(Level.INFO, "Processing {0} deals...", deals.length());
            for (int i = 0; i < deals.length(); i++) {
                try {
                    JSONObject deal = deals.getJSONObject(i);

                    int categoryID = -1;
                    int subcategoryID = -1;
                    String catID = null;

                    String category = "";
                    JSONArray tags = deal.getJSONArray("tags");
                    if (tags.length() > 0) {
                        for (int j = 0; j < tags.length(); j++) {
                            JSONObject c = (JSONObject) tags.get(j);
                            if (category.length() > 0) {
                                category += ", ";
                            }
                            category += c.getString("name");
                        }
                    }

                    if (category.length() > 0) {
                        String[] mapping = GrouponCategoryMapping.findMapping(category);
                        if (mapping != null) {
                            if (mapping[0].length() > 0) {
                                categoryID = Integer.valueOf(mapping[0]).intValue();
                                catID = mapping[0];
                            }
                            if (mapping[1].length() > 0) {
                                subcategoryID = Integer.valueOf(mapping[1]).intValue();
                            }
                        }
                    }

                    //check if deal title || announcementTitle matching query

                    boolean hasQuery = (query == null);

                    if (!hasQuery && deal.has("title")) {
                        String title = deal.getString("title");
                        hasQuery = StringUtils.containsIgnoreCase(title, query);
                    }

                    if (!hasQuery && deal.has("announcementTitle")) {
                        String title = deal.getString("announcementTitle");
                        hasQuery = StringUtils.containsIgnoreCase(title, query);
                    }

                    //check if categoryID in enabledCategories
                    if (StringUtils.indexOfAny(catID, enabledCategories) >= 0 && hasQuery) {

                        String start_date_str = null;
                        String end_date_str = null;
                        long start_date = -1, end_date = -1;

                        Object start_d = deal.get("startAt");
                        if (StringUtils.isNotEmpty(start_d.toString())) {
                            start_date_str = start_d.toString();
                            start_date_str = start_date_str.replace("T", " ").replace("Z", "");
                        }

                        Object end_d = deal.get("endAt");
                        if (StringUtils.isNotEmpty(end_d.toString())) {
                            end_date_str = end_d.toString();
                            end_date_str = end_date_str.replace("T", " ").replace("Z", "");
                        }

                        if (start_date_str != null) {
                           try {
                                    start_date = DateUtils.parseDate(dateFormat, start_date_str).getTime();
                            } catch (Exception e) {
                            }
                        }
                        if (end_date_str != null) {
                            try {
                                    end_date = DateUtils.parseDate(dateFormat, end_date_str).getTime();
                            } catch (Exception e) {
                            }
                        }
                        
                        JSONObject mer = deal.getJSONObject("merchant");
                        String merchant = mer.getString("name");

                        JSONObject division = deal.getJSONObject("division");

                        double divlat = MathUtils.normalizeE6(division.getDouble("lat"));
                        double divlng = MathUtils.normalizeE6(division.getDouble("lng"));

                        JSONArray options = deal.getJSONArray("options");
                        if (options.length() > 0) {
                            for (int j = 0; j < options.length(); j++) {

                                JSONObject option = options.getJSONObject(j);
                                String title = option.getString("title");

                                String description = "";
                                JSONArray details = option.getJSONArray("details");
                                if (details.length() > 0) {
                                    JSONObject detail = details.getJSONObject(0);
                                    description = detail.getString("description");
                                }

                                JSONObject priceobj = option.getJSONObject("price");
                                double price = priceobj.getDouble("amount");
                                String currencyCode = priceobj.getString("currencyCode");

                                JSONObject discountobj = option.getJSONObject("discount");
                                double save = discountobj.getDouble("amount");
                                double discount = option.getDouble("discountPercent");
                                
                                String url = "http://www.anrdoezrs.net/click-5379376-10804307?url="
                                        + URLEncoder.encode(option.getString("buyUrl"), "UTF-8");

                                JSONArray locations = option.getJSONArray("redemptionLocations");
                                if (locations.length() > 0) {
                                    for (int k = 0; k < locations.length(); k++) {
                                        JSONObject location = locations.getJSONObject(k);

                                        Map<String, String> tokens = new HashMap<String, String>();

                                        JSONUtils.putOptValue(tokens, "category", category, stringLimit, false);
                                        JSONUtils.putOptValue(tokens, "description", description, stringLimit, true);

                                        tokens.put("merchant", merchant);
                                        if (start_date != -1) {
                                            tokens.put("start_date", Long.toString(start_date));
                                        }
                                        if (end_date != -1) {
                                            tokens.put("end_date", Long.toString(end_date));
                                        }
                                        Deal dealObj = new Deal(price, discount, save, null, currencyCode);
                                        dealObj.setEndDate(end_date);
                                        QualifiedCoordinates qc = new QualifiedCoordinates(divlat, divlng, 0f, 0f, 0f);

                                        AddressInfo addressInfo = new AddressInfo();
                                        //city, streetAddress1, streetAddress2,phoneNumber,postalCode,state
                                        if (location.has("streetAddress1")) {
                                            String address = location.getString("streetAddress1");
                                            if (address.length() > 0) {
                                                if (location.has("streetAddress2")) {
                                                    Object s = location.get("streetAddress2");
                                                    if (s != null && s.toString().length() > 0) {
                                                        address += ", " + s.toString();
                                                    }
                                                }
                                                addressInfo.setField(AddressInfo.STREET, address);
                                            }
                                        }

                                        addressInfo.setField(AddressInfo.CITY, location.getString("city"));
                                        String phone = location.optString("phoneNumber");
                                        if (phone != null) {
                                        	addressInfo.setField(AddressInfo.PHONE_NUMBER, phone);
                                        }
                                        addressInfo.setField(AddressInfo.POSTAL_CODE, location.getString("postalCode"));
                                        addressInfo.setField(AddressInfo.STATE, location.getString("state"));
                                        
                                        ExtendedLandmark landmark = LandmarkFactory.getLandmark(title, null, qc, Commons.GROUPON_LAYER, addressInfo, start_date, null);
                             		    landmark.setUrl(url);
                             		    String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                             		    landmark.setDescription(desc);		   
                                     	landmark.setDeal(dealObj);	   
                             		    
                             		    if (categoryID != -1) {
                                           landmark.setCategoryId(categoryID);
                                        }

                                        if (subcategoryID != -1) {
                                           landmark.setSubCategoryId(subcategoryID);
                                        }
                             		    
                                        if (deal.has("mediumImageUrl")) {
                                            landmark.setThumbnail(deal.getString("mediumImageUrl"));	
                                        }
                                        
                                        landmarks.add(landmark);
                                    }
                                } else {
                                    //no redemptionLocations
                                	Map<String, String> tokens = new HashMap<String, String>();
                                    
                                    JSONUtils.putOptValue(tokens, "category", category, stringLimit, false);
                                    JSONUtils.putOptValue(tokens, "description", description, stringLimit, true);
                                    
                                    tokens.put("merchant", merchant);
                                    tokens.put("start_date", Long.toString(start_date));
                                    tokens.put("end_date", Long.toString(end_date));

                                    Deal dealObj = new Deal(price, discount, save, null, currencyCode);
                                    dealObj.setEndDate(end_date);
                                    
                                    QualifiedCoordinates qc = new QualifiedCoordinates(divlat, divlng, 0f, 0f, 0f);
                         		   
                                    ExtendedLandmark landmark = LandmarkFactory.getLandmark(title, null, qc, Commons.GROUPON_LAYER, new AddressInfo(), start_date, null);
                         		    landmark.setUrl(url);
                         		    String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                         		    landmark.setDescription(desc);		   
                                 	landmark.setDeal(dealObj);	   
                         		    
                         		    if (categoryID != -1) {
                                       landmark.setCategoryId(categoryID);
                                    }

                                    if (subcategoryID != -1) {
                                       landmark.setSubCategoryId(subcategoryID);
                                    }
                         		    
                                    if (deal.has("mediumImageUrl")) {
                                        landmark.setThumbnail(deal.getString("mediumImageUrl"));	
                                    }
                                    
                                    landmarks.add(landmark);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

                if (landmarks.size() >= limit) {
                    break;
                }
            }
        } else {
        	logger.log(Level.SEVERE, "Received following server response: " + grouponJson);
        }

        return landmarks;
    }
	
	public String getLayerName() {
    	return Commons.GROUPON_LAYER;
    }

}
