/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class CouponsUtils extends LayerHelper {

    private static final Map<Integer, String> dealType = new HashMap<Integer, String>();
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        dealType.put(1, "Printable Coupon");
        dealType.put(2, "Tip");
        dealType.put(3, "Sale");
        dealType.put(4, "Special");
        dealType.put(5, "Always cheap");
        dealType.put(6, "Happy Hour");
        dealType.put(7, "Coupon Code");
        dealType.put(8, "Free Stuff");
        dealType.put(18381, "Deals-of-the-Day");
    }

    @Override
    public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String categoryid, String language) throws MalformedURLException, IOException, JSONException, ParseException {
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, categoryid, language);

        JSONObject json = null;

        String cachedResponse = cacheProvider.getString(key);
        if (cachedResponse == null) {

            String url = "http://api.8coupons.com/v1/getdeals?key=" + Commons.getProperty(Property.COUPONS_KEY) + "&lat=" + lat + "&lon=" + lng + "&mileradius=" + radius + "&limit=" + limit + "&orderby=date"; //popular, radius, date
            if (StringUtils.isNotEmpty(query)) {
                url += "&search=" + URLEncoder.encode(query, "UTF-8");
            }
            if (StringUtils.isNotEmpty(categoryid)) {
                url += "&categoryid=" + categoryid;
            }
            URL couponsUrl = new URL(url);
            String couponsResponse = HttpUtils.processFileRequest(couponsUrl);
            //System.out.println(couponsResponse);
            if (StringUtils.isNotEmpty(couponsResponse)) {
                Map<String, Map<String, String>> reviewsArray = new HashMap<String, Map<String, String>>();
                try {
                    if (LayerHelperFactory.getYelpUtils().hasNeighborhoods(lat, lng)) {
                        reviewsArray = LayerHelperFactory.getYelpUtils().processReviewsRequest(lat, lng, query, radius * 1000, limit, true, language);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
                List<Object> jsonArray = createCustomJsonCouponsList(StringUtils.trim(couponsResponse), version, reviewsArray, stringLimit);
                json = new JSONObject().put("ResultSet", jsonArray);
                if (jsonArray.size() > 0) {
                    cacheProvider.put(key, json.toString());
                    logger.log(Level.INFO, "Adding COU landmark list to cache with key {0}", key);
                }
            } else {
                //System.out.println("Coupons response is empty: " + couponsResponse);
                json = new JSONObject().put("ResultSet", new ArrayList<Object>());
            }
        } else {
            logger.log(Level.INFO, "Reading COU landmark list from cache with key {0}", key);
            json = new JSONObject(cachedResponse);
        }

        return json;
    }

    private static List<Object> createCustomJsonCouponsList(String couponsJson, int version, Map<String, Map<String, String>> reviewsArray, int stringLimit) throws JSONException, ParseException {
        ArrayList<Object> jsonArray = new ArrayList<Object>();

        if (StringUtils.startsWith(couponsJson, "[")) {

            JSONArray jsonRoot = new JSONArray(couponsJson);
            
            for (int i = 0; i < jsonRoot.length(); i++) {
                try {
                    JSONObject deal = jsonRoot.getJSONObject(i);
                    Map<String, Object> jsonObject = new HashMap<String, Object>();

                    jsonObject.put("name", StringEscapeUtils.unescapeHtml(deal.getString("dealTitle")));
                    jsonObject.put("lat", deal.getString("lat"));
                    jsonObject.put("lng", deal.getString("lon"));
                    jsonObject.put("url", deal.getString("URL"));

                    JSONUtils.putOptValue(jsonObject, "categoryID", deal, "categoryID", false, stringLimit, false);
                    JSONUtils.putOptValue(jsonObject, "subcategoryID", deal, "subcategoryID", false, stringLimit, false);

                    Map<String, String> desc = new HashMap<String, String>();
                    desc.put("merchant", StringEscapeUtils.unescapeHtml(deal.getString("name")));
                    String phone = deal.getString("phone");
                    desc.put("phone", phone);

                    if (!reviewsArray.isEmpty()) {
                        phone = phone.replaceAll("[^\\d]", "");
                        //System.out.println("Checking if phone exists " + phone);
                        if (reviewsArray.containsKey(phone)) {
                            //System.out.println("Found review " + phone);
                            desc.putAll(reviewsArray.get(phone));
                        }
                    }

                    if (deal.has("dealPrice") && !deal.isNull("dealPrice")) {
                        String price = "$" + deal.getString("dealPrice");
                        desc.put("price", price);
                    }

                    if (deal.has("dealSavings") && !deal.isNull("dealSavings")
                            && deal.has("dealDiscountPercent") && !deal.isNull("dealDiscountPercent")) {
                        String save = "$" + deal.getString("dealSavings");
                        String discount = deal.getString("dealDiscountPercent") + "%";
                        desc.put("discount", discount);
                        desc.put("save", save);
                    }

                    if (version >= 3) {
                        JSONUtils.putOptDate(desc, "expiration_date", deal, "expirationDate", dateFormatter);
                        JSONUtils.putOptDate(desc, "creationDate", deal, "postDate", datetimeFormatter);
                    } else {
                        JSONUtils.putOptValue(desc, "expiration_date", deal, "expirationDate", false, stringLimit, false);
                    }
                    //state,city,ZIP,address
                    desc.put("state", deal.getString("state"));
                    desc.put("city", deal.getString("city"));
                    desc.put("zip", deal.getString("ZIP"));
                    desc.put("address", deal.getString("address"));

                    JSONUtils.putOptValue(desc, "description", deal, "disclaimer", true, stringLimit, true);
                    JSONUtils.putOptValue(desc, "homepage", deal, "homepage", false, stringLimit, false);
                    JSONUtils.putOptValue(desc, "source", deal, "dealSource", false, stringLimit, false);

                    if (deal.has("DealTypeID") && !deal.isNull("DealTypeID")) {
                        int dealtyp = deal.getInt("DealTypeID");
                        if (dealType.containsKey(dealtyp)) {
                            desc.put("dealType", dealType.get(dealtyp));
                        }
                    }

                    if (deal.has("userID") && deal.getString("userID").equals("18381")) {
                        desc.put("dealType", dealType.get(18381));
                    }

                    if (version >= 4) {
                        String icon = deal.optString("showLogo"); //showImage
                        if (StringUtils.isNotEmpty(icon)) {
                            desc.put("icon", icon);
                        }
                    }

                    jsonObject.put("desc", desc);
                    jsonArray.add(jsonObject);
                } catch (JSONException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        } else {
            logger.log(Level.WARNING, "Received following response: {0}", couponsJson);
        }

        return jsonArray;
    }
    
    private static List<ExtendedLandmark> createCustomLandmarksCouponsList(String couponsJson, Map<String, Map<String, String>> reviewsArray, int stringLimit, Locale locale) throws JSONException, ParseException {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        if (StringUtils.startsWith(couponsJson, "[")) {

            JSONArray jsonRoot = new JSONArray(couponsJson);
            
            for (int i = 0; i < jsonRoot.length(); i++) {
                try {
                    JSONObject deal = jsonRoot.getJSONObject(i);
                    
                    double lat = Double.valueOf(deal.getString("lat")).doubleValue();
                    double lng = Double.valueOf(deal.getString("lon")).doubleValue();
                    QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
         		    String name = StringEscapeUtils.unescapeHtml(deal.getString("dealTitle"));
                    String url = deal.getString("URL");

                    Map<String, String> tokens = new HashMap<String, String>();
                    tokens.put("merchant", StringEscapeUtils.unescapeHtml(deal.getString("name")));
                    
                    double price = 0;
                    if (deal.has("dealPrice") && !deal.isNull("dealPrice")) {
                        price = deal.getDouble("dealPrice");
                    }

                    double save = 0, discount = 0;
                    if (deal.has("dealSavings") && !deal.isNull("dealSavings")
                            && deal.has("dealDiscountPercent") && !deal.isNull("dealDiscountPercent")) {
                        save = deal.getDouble("dealSavings");
                        discount = deal.getDouble("dealDiscountPercent") / 100d;
                    }
                                    
                    String dealTypeStr = null;
                    if (deal.has("DealTypeID") && !deal.isNull("DealTypeID")) {
                        int dealtyp = deal.getInt("DealTypeID");
                        if (dealType.containsKey(dealtyp)) {
                        	dealTypeStr = dealType.get(dealtyp);
                        }
                    }

                    if (deal.has("userID") && deal.getString("userID").equals("18381")) {
                    	dealTypeStr = dealType.get(18381);
                    }
                  
                    Deal dealObj =  new Deal(price, discount, save, dealTypeStr, "$");
                    long creationDate = -1;
                    
                    JSONUtils.putOptDate(tokens, "creationDate", deal, "postDate", datetimeFormatter);
                    if (tokens.containsKey("creationDate")) {
                         creationDate = Long.valueOf(tokens.remove("creationDate")).longValue();
                    }
                    
                    JSONUtils.putOptDate(tokens, "expiration_date", deal, "expirationDate", dateFormatter);
                    if (tokens.containsKey("expiration_date")) {
                    	long expDate = Long.valueOf(tokens.get("expiration_date")).longValue();
                    	dealObj.setEndDate(expDate);
                    	//if (expDate == 0) {
                    	//    tokens.remove("expiration_date");	
                    	//} //else if (creationDate == -1) {
                    		//creationDate = expDate;
                    	//}
                    }
                    
                    AddressInfo address = new AddressInfo();
                    String phone = deal.getString("phone");
                    address.setField(AddressInfo.PHONE_NUMBER, phone);
                    address.setField(AddressInfo.STATE, deal.getString("state"));
                    address.setField(AddressInfo.CITY, deal.getString("city"));
                    address.setField(AddressInfo.POSTAL_CODE, deal.getString("ZIP"));
                    address.setField(AddressInfo.STREET, deal.getString("address"));

                    JSONUtils.putOptValue(tokens, "description", deal, "disclaimer", true, stringLimit, true);
                    JSONUtils.putOptValue(tokens, "homepage", deal, "homepage", false, stringLimit, false);
                    JSONUtils.putOptValue(tokens, "source", deal, "dealSource", false, stringLimit, false);

                    ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.COUPONS_LAYER, address, creationDate, null);
         		    
                    if (!reviewsArray.isEmpty()) {
                        phone = phone.replaceAll("[^\\d]", "");
                        //System.out.println("Checking if phone exists " + phone);
                        if (reviewsArray.containsKey(phone)) {
                            //System.out.println("Found review " + phone);
                        	Map<String, String> business = reviewsArray.get(phone);
                        	if (business.containsKey("rating")) {
                                landmark.setRating(Double.valueOf(business.get("rating")).doubleValue());
                            }
                            if (business.containsKey("numberOfReviews")) {
                            	landmark.setNumberOfReviews(Integer.valueOf(business.get("numberOfReviews")).intValue());
                            }
                        }
                    }
                    
                    landmark.setDeal(dealObj);
                    
                    if (deal.has("categoryID") && !deal.isNull("categoryID")) {
                    	landmark.setCategoryId(deal.getInt("categoryID"));
                    }
                    
                    if (deal.has("subcategoryID") && !deal.isNull("subcategoryID")) {
                    	landmark.setSubCategoryId(deal.getInt("subcategoryID"));
                    }
                     
                    String icon = deal.optString("showLogo"); //showImage
                    if (StringUtils.isNotEmpty(icon)) {
                       landmark.setThumbnail(icon);
                    }
                    
                    landmark.setUrl(url);
         		    String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
         		    landmark.setDescription(desc);		   
                 		   
         		    landmarks.add(landmark);

                } catch (JSONException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        } else {
            logger.log(Level.WARNING, "Received following response: {0}", couponsJson);
        }

        return landmarks;
    }

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String categoryid, String language, Locale locale, boolean useCache) throws Exception {
		String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, categoryid, language);
		List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)cacheProvider.getObject(key);
        if (landmarks == null) {
            String url = "http://api.8coupons.com/v1/getdeals?key=" + Commons.getProperty(Property.COUPONS_KEY) + "&lat=" + lat + "&lon=" + lng + "&mileradius=" + radius + "&limit=" + limit + "&orderby=date"; //popular, radius, date
            if (StringUtils.isNotEmpty(query)) {
                url += "&search=" + URLEncoder.encode(query, "UTF-8");
            }
            if (StringUtils.isNotEmpty(categoryid)) {
                url += "&categoryid=" + categoryid;
            }
            URL couponsUrl = new URL(url);
            String couponsResponse = HttpUtils.processFileRequest(couponsUrl);
            //System.out.println(couponsResponse);
            if (StringUtils.isNotEmpty(couponsResponse)) {
                Map<String, Map<String, String>> reviewsArray = new HashMap<String, Map<String, String>>();
                try {
                    if (LayerHelperFactory.getYelpUtils().hasNeighborhoods(lat, lng)) {
                        reviewsArray = LayerHelperFactory.getYelpUtils().processReviewsRequest(lat, lng, query, radius * 1000, limit, true, language);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }
                
                landmarks = createCustomLandmarksCouponsList(couponsResponse.trim(), reviewsArray, stringLimit, locale);
                
                if (!landmarks.isEmpty()) {
                    cacheProvider.put(key, landmarks);
                    logger.log(Level.INFO, "Adding COU landmark list to cache with key {0}", key);
                }
            } else {
                //System.out.println("Coupons response is empty: " + couponsResponse);
                landmarks = new ArrayList<ExtendedLandmark>();
            }
        } else {
            logger.log(Level.INFO, "Reading COU landmark list from cache with key {0}", key);
        }
        logger.log(Level.INFO, "Found {0} landmarks", landmarks.size()); 
		
		return landmarks;
	}
}
