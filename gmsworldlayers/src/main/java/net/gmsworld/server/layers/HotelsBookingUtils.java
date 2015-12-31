package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

public class HotelsBookingUtils extends LayerHelper {

	private static final String HOTELS_PROVIDER_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/cache/hotels/nearby/"; 
	
	private static final String HOTELS_ASYNC_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/cache/hotels/async/nearby/";
	
	private static final String HOTELS_CACHE_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/one/cache/_id/"; 
	
	private static final String HOTELS_COUNTER_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/count/hotels/nearby/";
	
	@Override
	protected List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int r, int version, int limit, int stringLimit, String callCacheFirst, String flexString2, Locale locale, boolean useCache) throws Exception {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		return loadLandmarksJackson(lat, lng, query, normalizedRadius, version, limit, stringLimit, callCacheFirst, flexString2, locale, useCache);
		//return loadLandmarksJSON(lat, lng, query, normalizedRadius, version, limit, stringLimit, callCacheFirst, flexString2, locale, useCache);
	}
	
	private List<ExtendedLandmark> loadLandmarksJSON(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String callCacheFirst, String flexString2, Locale locale, boolean useCache) throws Exception {
		JSONArray hotels = null;
		
		//first call hotels cache
		if (StringUtils.equals(callCacheFirst, "true")) {
			String hotelsUrl = HOTELS_CACHE_URL + StringUtil.formatCoordE2(lng) + "_" + StringUtil.formatCoordE2(lat) + "_" + radius + "_" + limit;
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(StringUtils.trim(json), "[")) {
	    		try {
	    			JSONArray root = new JSONArray(json);
	    			if (root.length() > 0) {
	    				hotels = root.getJSONObject(0).getJSONArray("features");
	    			}
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else if (StringUtils.startsWith(StringUtils.trim(json), "{")) {
	    		try {
	    			JSONObject root = new JSONObject(json);
	    			hotels = root.optJSONArray("features");
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following server response " + json);
			}	
		}
		
		if (hotels == null) {
			String hotelsUrl = HOTELS_PROVIDER_URL + StringUtil.formatCoordE2(lat) + "/" + StringUtil.formatCoordE2(lng) + "/" + radius + "/" + limit;			
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(json, "[")) {
	    		try {
	    			hotels = new JSONArray(json);
	       		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following server response " + json);
			}
		}
		long start = System.currentTimeMillis();
		logger.log(Level.INFO, "Processing hotels list with JSON...");
		
		int size = 0;
		if (hotels != null) {
			size = hotels.length();
		}
		
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>(size);
		if (size > 0) {
			for (int i=0; i<size; i++) {
				try {
					landmarks.add(hotelToLandmark(hotels.getJSONObject(i), locale));
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}	
			}
		}
		logger.log(Level.INFO, "Processed " + landmarks.size() + " hotels in " + (System.currentTimeMillis()-start) + " millis.");
		return landmarks;
	}
	
	private List<ExtendedLandmark> loadLandmarksJackson(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String callCacheFirst, String flexString2, Locale locale, boolean useCache) throws Exception {
		FeatureCollection hotels = null;
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		//first call hotels cache
		if (StringUtils.equals(callCacheFirst, "true")) {
			String hotelsUrl = HOTELS_CACHE_URL + StringUtil.formatCoordE2(lng) + "_" + StringUtil.formatCoordE2(lat) + "_" + radius + "_" + limit;
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(json, "[") && json.length() > 2) {
	    		try {
	    			json = json.substring(1, json.length()-1);
	    			hotels = objectMapper.readValue(json, FeatureCollection.class);
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else if (StringUtils.startsWith(json, "{") && json.length() > 2) {
				try {
	    			hotels = objectMapper.readValue(json, FeatureCollection.class);
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following server response " + json);
			}	
		}
		
		if (hotels == null) {
			String hotelsUrl = HOTELS_PROVIDER_URL + StringUtil.formatCoordE2(lat) + "/" + StringUtil.formatCoordE2(lng) + "/" + radius + "/" + limit;			
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(json, "[")) {
	    		try {
	    			json = "{\"type\": \"FeatureCollection\", \"features\":" + json + "}";
	    			hotels = objectMapper.readValue(json, FeatureCollection.class);
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following server response " + json);
			}
		}
		long start = System.currentTimeMillis();
		logger.log(Level.INFO, "Processing hotels list with Jackson...");
		
		int size = 0;
		if (hotels != null) {
			size = hotels.getFeatures().size();
		}
		
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>(size);
		if (size > 0) {
			for (int i=0; i<size; i++) {
				try {
					landmarks.add(hotelToLandmark(hotels.getFeatures().get(i), locale));
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}	
			}
		}
		logger.log(Level.INFO, "Processed " + landmarks.size() + " hotels in " + (System.currentTimeMillis()-start) + " millis.");
		return landmarks;
	}
	
	public String loadHotelsAsync(double lat, double lng, int r, int limit) {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		String lngStr = StringUtil.formatCoordE2(lng);
		String latStr = StringUtil.formatCoordE2(lat);	
		String id = lngStr + "_" + latStr + "_" + normalizedRadius + "_" + limit;	
		String hotelsUrl = HOTELS_ASYNC_URL + latStr + "/" + lngStr + "/" + normalizedRadius + "/" + limit;
		
		try {
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), false);
		    int responseCode = HttpUtils.getResponseCode(hotelsUrl);
			if (responseCode >= 400) {
		    	id = null;
		    	logger.log(Level.SEVERE, "Received following server response code {0}", responseCode);
		    } else {
		    	logger.log(Level.INFO, "Received following server response code {0}", responseCode);
		    }
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return id;
	}

	@Override
	protected String getLayerName() {
		return Commons.HOTELS_LAYER;
	}
	
	private static ExtendedLandmark hotelToLandmark(JSONObject hotel, Locale locale) {
		JSONArray coords = hotel.getJSONObject("geometry").getJSONArray("coordinates");
    	QualifiedCoordinates qc = new QualifiedCoordinates(coords.getDouble(1), coords.getDouble(0), 0f, 0f, 0f); 
    	
    	JSONObject props = hotel.getJSONObject("properties");
    	AddressInfo address = new AddressInfo();
    	if (!props.isNull("address")) {
    		address.setField(AddressInfo.STREET, props.getString("address"));
    	}
    	address.setField(AddressInfo.CITY, props.getString("city_hotel"));
    	
    	String cc = props.getString("cc1");
    	Locale l = new Locale("", cc.toUpperCase(Locale.US));
    	String country = l.getDisplayCountry();
    	if (country == null) {
    		country = cc;
    	}
    	address.setField(AddressInfo.COUNTRY, country);
    	if (!props.isNull("zip")) {
    		address.setField(AddressInfo.POSTAL_CODE, props.getString("zip"));
    	}
    	
        long creationDate = props.getLong("creationDate");
        
    	ExtendedLandmark landmark = LandmarkFactory.getLandmark(props.getString("name"), null, qc, Commons.HOTELS_LAYER, address,  creationDate, null);
    	landmark.setUrl(props.getString("hotel_url"));
        
    	int rs = props.getInt("review_score");
    	int rn = props.getInt("review_nr");
    	
    	if (rs != 1 || rn != 1) {
    		landmark.setRating(rs);
    		landmark.setNumberOfReviews(rn);
        }
        
        landmark.setCategoryId(7);
        landmark.setSubCategoryId(129);
        
        Map<String, String> tokens = new HashMap<String, String>();
        
        String currencycode = props.getString("currencycode");
        if (!props.isNull("minrate")) {
            Deal deal = new Deal(props.getDouble("minrate"), -1, -1, null, currencycode);
            landmark.setDeal(deal);
        } else if (!props.isNull("maxrate")) {
        	Deal deal = new Deal(props.getDouble("maxrate"), -1, -1, null, currencycode);
        	landmark.setDeal(deal);       	
        }
        
        tokens.put("maxRating", "10");
        tokens.put("star_rating", Double.toString(props.getDouble("stars")));
        
        int nr = props.getInt("nr_rooms");
        if (nr > 0) {
        	tokens.put("no_rooms", Integer.toString(nr));
        }
        address.setField(AddressInfo.EXTENSION, Integer.toString(nr));

        landmark.setThumbnail(props.getString("photo_url"));
        
        String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
        landmark.setDescription(desc);

        return landmark;
    }
	
	private static ExtendedLandmark hotelToLandmark(Feature hotel, Locale locale) {
		Point g = (Point)hotel.getGeometry();
		QualifiedCoordinates qc = new QualifiedCoordinates(g.getCoordinates().getLatitude(), g.getCoordinates().getLongitude(), 0f, 0f, 0f); 
    	
    	AddressInfo address = new AddressInfo();
    	String value = hotel.getProperty("address");
    	if (StringUtils.isNotEmpty(value)) {
    		address.setField(AddressInfo.STREET, value);
    	}
    	value = hotel.getProperty("city_hotel");
    	if (StringUtils.isNotEmpty(value)) {
    		address.setField(AddressInfo.CITY, value);
    	}
    	
    	String cc = hotel.getProperty("cc1");
    	Locale l = new Locale("", cc.toUpperCase(Locale.US));
    	String country = l.getDisplayCountry();
    	if (country == null) {
    		country = cc;
    	}
    	address.setField(AddressInfo.COUNTRY, country);
    	
    	value = hotel.getProperty("zip");
    	if (StringUtils.isNotEmpty(value)) {
    		address.setField(AddressInfo.POSTAL_CODE, value);
    	}
    	
        long creationDate = hotel.getProperty("creationDate");
        
        value = hotel.getProperty("name");
    	ExtendedLandmark landmark = LandmarkFactory.getLandmark(value, null, qc, Commons.HOTELS_LAYER, address,  creationDate, null);
    	
    	value = hotel.getProperty("hotel_url");
        landmark.setUrl(value);
        
    	int rs = hotel.getProperty("review_score");
    	int rn = hotel.getProperty("review_nr");
    	
    	if (rs != 1 || rn != 1) {
    		landmark.setRating(rs);
    		landmark.setNumberOfReviews(rn);
        }
        
        landmark.setCategoryId(7);
        landmark.setSubCategoryId(129);
        
        Map<String, String> tokens = new HashMap<String, String>();
        
        String currencycode = hotel.getProperty("currencycode");
        Double rate = NumberUtils.getDouble(hotel.getProperty("minrate"));
        if (rate == null) {
        	rate = NumberUtils.getDouble(hotel.getProperty("maxrate"));
        }
        
        if (rate != null) {
            Deal deal = new Deal(rate, -1, -1, null, currencycode);
            landmark.setDeal(deal);
        } 
        
        tokens.put("maxRating", "10");
        Double stars = NumberUtils.getDouble(hotel.getProperty("stars"));
        tokens.put("star_rating", Double.toString(stars));
        
        int nr = hotel.getProperty("nr_rooms");
        if (nr > 0) {
        	tokens.put("no_rooms", Integer.toString(nr));
        }
        address.setField(AddressInfo.EXTENSION, Integer.toString(nr));

        value = hotel.getProperty("photo_url");
        landmark.setThumbnail(value);
        
        String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
        landmark.setDescription(desc);

        return landmark;
    }
	
	public static int countNearbyHotels(double lat, double lng, int r) throws MalformedURLException, IOException {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		String hotelsUrl = HOTELS_COUNTER_URL + StringUtil.formatCoordE2(lat) + "/" + StringUtil.formatCoordE2(lng) + "/" + normalizedRadius;			
        String hotelsCount = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), false);
		return NumberUtils.getInt(hotelsCount, -1);
	}
	
	/*private class ConcurrentHotelsProcessor implements Runnable {

		private List<ExtendedLandmark> landmarks;
		private JSONObject hotel;
		private ThreadManager threadManager;
		private String key;
		private Locale locale;
		
		public ConcurrentHotelsProcessor(Locale locale, List<ExtendedLandmark> landmarks, ThreadManager threadManager, String key, JSONObject hotel) {
			this.landmarks = landmarks;
			this.threadManager = threadManager;
			this.locale = locale;
			this.hotel = hotel;
			this.key = key;
		}
		
		@Override
		public void run() {
			try {
				landmarks.add(hotelToLandmark(hotel, locale));
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				threadManager.take(key);
			}
		}
	}*/
}
