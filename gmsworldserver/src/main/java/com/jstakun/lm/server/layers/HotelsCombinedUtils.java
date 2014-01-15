/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.persistence.Hotel;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.MathUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.HotelPersistenceUtils;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class HotelsCombinedUtils extends LayerHelper {

    @Override
    public JSONObject processRequest(double latitudeMin, double longitudeMin, String query, int radius, int version, int limit, int stringLimit, String language, String flexString2) throws Exception {
        double lat, lng;
        double latitudeMax = 0.0, longitudeMax = 0.0;
        if (version > 2) {
            lat = latitudeMin;
            lng = longitudeMin;
        } else {
            String[] coords = StringUtils.split(flexString2, "_");
            latitudeMax = Double.parseDouble(coords[0]);
            longitudeMax = Double.parseDouble(coords[1]);
            lat = (latitudeMin + latitudeMax) / 2;
            lng = (longitudeMin + longitudeMax) / 2;
        }

        int l = NumberUtils.normalizeNumber(limit, 1, 100);

        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, l, stringLimit, language, null);

        String output = CacheUtil.getString(key);

        JSONObject json = null;

        if (output == null) {
            List<Hotel> hotels = new ArrayList<Hotel>();
            if (version > 2) {
                hotels = HotelPersistenceUtils.selectHotelsByPointAndRadius(latitudeMin, longitudeMin, radius * 1000, l);
            } else {
                hotels = HotelPersistenceUtils.selectHotelsByCoordsAndLayer(latitudeMin, longitudeMin, latitudeMax, longitudeMax, l);
            }

            json = createCustomJSonHotelsCombinedList(hotels, language, version);

            if (!hotels.isEmpty()) {
                CacheUtil.put(key, json.toString());
                logger.log(Level.INFO, "Adding H landmark list to cache with key {0}", key);
            }

        } else {
            logger.log(Level.INFO, "Reading H landmark list from cache with key {0}", key);
            json = new JSONObject(output);
        }

        return json;
    }

    private static JSONObject createCustomJSonHotelsCombinedList(List<Hotel> hotelList, String language, int version) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
        Iterator<Hotel> iter = hotelList.iterator();
        
        while (iter.hasNext()) {
            Hotel hotel = iter.next();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("name", hotel.getHotelName());
            jsonObject.put("lat", MathUtils.normalizeE6(hotel.getLatitude()));
            jsonObject.put("lng", MathUtils.normalizeE6(hotel.getLongitude()));

            String url = hotel.getHotelFileName() + ".htm?a_aid=31803&languageCode=" + language + "&Mobile=1";
            if (version >= 1) {
                url = "http://www.hotelscombined.com/Hotel/" + url;
            }
            jsonObject.put("url", url);

            Map<String, String> desc = new HashMap<String, String>();
            desc.put("address", hotel.getAddress());
            desc.put("city", hotel.getCityName());
            desc.put("country", hotel.getCountryName());

            if (hotel.getStateName() != null) {
                desc.put("state", hotel.getStateName());
            }
            desc.put("star_rating", Double.toString(hotel.getRating()));
            desc.put("maxRating", "10");
            if (hotel.getMinRate() > 0.0) {
                String price = StringUtil.formatCoordE2(hotel.getMinRate()) + " " + hotel.getCurrencyCode();
                desc.put("average_price", price);
            }

            if (hotel.getConsumerRating() > 0.0) {
                desc.put("rating", Double.toString(hotel.getConsumerRating()));
            }
            if (hotel.getNumberOfReviews() > 0) {
                desc.put("numberOfReviews", Integer.toString(hotel.getNumberOfReviews()));
            }

            if (version >= 2 && hotel.getImageId() > 0) {
                desc.put("icon", "http://media.hotelscombined.com/HT" + hotel.getImageId() + ".jpg");
            }

            Date updateDate = hotel.getLastUpdateDate();
            if (updateDate != null) {
                desc.put("creationDate", Long.toString(updateDate.getTime()));
            }

            jsonObject.put("desc", desc);

            jsonArray.add(jsonObject);

        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
    
    private static ExtendedLandmark hotelToLandmark(Hotel hotel, String language, Locale locale) {
    	QualifiedCoordinates qc = new QualifiedCoordinates(hotel.getLatitude(), hotel.getLongitude(), 0f, 0f, 0f); 
    	AddressInfo address = new AddressInfo();
    	
    	address.setField(AddressInfo.STREET, hotel.getAddress());
    	address.setField(AddressInfo.CITY, hotel.getCityName());
    	address.setField(AddressInfo.COUNTRY, hotel.getCountryName());

        if (hotel.getStateName() != null) {
        	address.setField(AddressInfo.STATE, hotel.getStateName());
        }
        
        Date updateDate = hotel.getLastUpdateDate();
        long creationDate = -1;
        if (updateDate != null) {
            creationDate = updateDate.getTime();
        }
        
    	ExtendedLandmark landmark = LandmarkFactory.getLandmark(hotel.getHotelName(), null, qc, Commons.HOTELS_LAYER, address,  creationDate, null);
    	String url = "http://www.hotelscombined.com/Hotel/" + 
    	    hotel.getHotelFileName() + ".htm?a_aid=31803&languageCode=" + language + "&Mobile=1";
        landmark.setUrl(url);
        if (hotel.getConsumerRating() > 0.0) {
        	landmark.setRating(hotel.getConsumerRating());
        }
        landmark.setNumberOfReviews(hotel.getNumberOfReviews());
        
        landmark.setCategoryId(7);
        landmark.setSubCategoryId(129);
        
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("maxRating", "10");
        if (hotel.getMinRate() > 0.0) {
            //String price = StringUtil.formatCoordE2(hotel.getMinRate()) + " " + hotel.getCurrencyCode();
            //tokens.put("average_price", price);
            Deal deal = new Deal(hotel.getMinRate(), -1, -1, null, hotel.getCurrencyCode());
            landmark.setDeal(deal);
        }
        tokens.put("star_rating", Double.toString(hotel.getRating()));

        if (hotel.getConsumerRating() > 0.0) {
            landmark.setRating(hotel.getConsumerRating());
        }
        if (hotel.getNumberOfReviews() > 0) {
            landmark.setNumberOfReviews(hotel.getNumberOfReviews());
        }

        if (hotel.getImageId() > 0) {
            landmark.setThumbnail("http://media.hotelscombined.com/HT" + hotel.getImageId() + ".jpg");
        }
        
        String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
        landmark.setDescription(desc);

        return landmark;
    }
    
	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String language, String flexString2, Locale locale) throws Exception {
		int l = NumberUtils.normalizeNumber(limit, 1, 100);
        String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, l, stringLimit, language, null);
        List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);

        if (landmarks == null) {
            List<Hotel> hotels = HotelPersistenceUtils.selectHotelsByPointAndRadius(lat, lng, radius * 1000, l);       
            landmarks = new ArrayList<ExtendedLandmark>();
            landmarks.addAll(Lists.transform(hotels, new HotelToExtendedLandmarkFunction(language, locale)));

            if (!landmarks.isEmpty()) {
                CacheUtil.put(key, landmarks);
                logger.log(Level.INFO, "Adding H landmark list to cache with key {0}", key);
            }

        } else {
            logger.log(Level.INFO, "Reading H landmark list from cache with key {0}", key);
        }

        return landmarks;
	}
	
	private class HotelToExtendedLandmarkFunction implements Function<Hotel, ExtendedLandmark> {

    	private String language;
    	private Locale locale;
    	
    	public HotelToExtendedLandmarkFunction(String language, Locale locale) {
    		this.language = language;
    		this.locale = locale;
    	}
    	
		@Override
		public ExtendedLandmark apply(Hotel hotel) {
			return hotelToLandmark(hotel, language, locale);
		}
    	
    }
}
