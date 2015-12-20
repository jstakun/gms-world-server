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
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.HotelBean;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

public class HotelsBookingUtils extends LayerHelper {

	private static final String HOTELS_PROVIDER_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/cache/hotels/nearby/"; 
	
	private static final String HOTELS_ASYNC_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/cache/hotels/async/nearby/";
	
	private static final String HOTELS_CACHE_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/cache/cache/_id/"; 
	
	private static final String HOTELS_COUNTER_URL = ConfigurationManager.HOTELS_PROVIDER_URL + "camel/v1/count/hotels/nearby/";
	
	@Override
	protected List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int r, int version, int limit, int stringLimit, String callCacheFirst, String flexString2, Locale locale, boolean useCache) throws Exception {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
		JSONArray hotels = null;
		
		//first call hotels cache
		if (StringUtils.equals(callCacheFirst, "true")) {
			String hotelsUrl = HOTELS_CACHE_URL + StringUtil.formatCoordE2(lng) + "_" + StringUtil.formatCoordE2(lat) + "_" + normalizedRadius + "_" + limit;
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(StringUtils.trim(json), "[")) {
	    		try {
	    			JSONArray rootArray = new JSONArray(json);
	    			if (rootArray.length() > 0) {
	    				hotels = rootArray.getJSONObject(0).getJSONArray("results");
 	    			}
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following server response " + json);
			}	
		}
		
		if (hotels == null) {
			String hotelsUrl = HOTELS_PROVIDER_URL + StringUtil.formatCoordE2(lat) + "/" + StringUtil.formatCoordE2(lng) + "/" + normalizedRadius + "/" + limit;			
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(StringUtils.trim(json), "[")) {
	    		try {
	    			hotels = new JSONArray(json);
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following server response " + json);
			}
		}
		logger.log(Level.INFO, "Processing hotels list...");
		HotelToExtendedLandmarkFunction ht = new HotelToExtendedLandmarkFunction(locale);
		if (hotels != null) {
			for (int i=0; i<hotels.length(); i++) {
				landmarks.add(ht.apply(hotels.getJSONObject(i)));
			}
		}
        logger.log(Level.INFO, "Found " + landmarks.size() + " hotels.");
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
	
	/*private static List<HotelBean> jsonToHotelList(JSONArray rootArray) {
		List<HotelBean> hotels = new ArrayList<HotelBean>();
    	try {
    		if (rootArray != null && rootArray.length() > 0) {
    			ObjectMapper mapper = new ObjectMapper();
    			
    			for (int i=0;i<rootArray.length();i++) {
    				Feature feature = mapper.readValue(rootArray.getJSONObject(i).toString().replace("_id",  "id"), Feature.class);
    				HotelBean h = new HotelBean();
    				BeanUtils.populate(h, feature.getProperties());
    				Point geometry = (Point)feature.getGeometry(); 
    				h.setLatitude(geometry.getCoordinates().getLatitude());
    				h.setLongitude(geometry.getCoordinates().getLongitude());
    				if (h.getReview_nr() == 1 && h.getReview_score() == 1) {
    					h.setReview_nr(null);
    					h.setReview_score(null);
    				}
    				hotels.add(h);
    			}
    		}
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    	 	
    	return hotels;
	}*/
	
	/*private static ExtendedLandmark hotelToLandmark(HotelBean hotel, Locale locale) {
    	QualifiedCoordinates qc = new QualifiedCoordinates(hotel.getLatitude(), hotel.getLongitude(), 0f, 0f, 0f); 
    	AddressInfo address = new AddressInfo();
    	
    	address.setField(AddressInfo.STREET, hotel.getAddress());
    	address.setField(AddressInfo.CITY, hotel.getCity_hotel());
    	
    	Locale l = new Locale("", hotel.getCc1().toUpperCase(Locale.US));
    	String country = l.getDisplayCountry();
    	if (country == null) {
    		country = hotel.getCc1();
    	}
    	address.setField(AddressInfo.COUNTRY, country);
    	address.setField(AddressInfo.POSTAL_CODE, hotel.getZip());

        long creationDate = hotel.getCreationDate();
        
    	ExtendedLandmark landmark = LandmarkFactory.getLandmark(hotel.getName(), null, qc, Commons.HOTELS_LAYER, address,  creationDate, null);
    	landmark.setUrl(hotel.getHotel_url());
        
    	if (hotel.getReview_score() != null) {
        	landmark.setRating(hotel.getReview_score());
        }
        if (hotel.getReview_nr() != null) {
        	landmark.setNumberOfReviews(hotel.getReview_nr());
        }
        
        landmark.setCategoryId(7);
        landmark.setSubCategoryId(129);
        
        Map<String, String> tokens = new HashMap<String, String>();
        
        if (hotel.getMinrate() > 0.0) {
            Deal deal = new Deal(hotel.getMinrate(), -1, -1, null, hotel.getCurrencycode());
            landmark.setDeal(deal);
        } else if (hotel.getMaxrate() > 0.0) {
        	Deal deal = new Deal(hotel.getMaxrate(), -1, -1, null, hotel.getCurrencycode());
            landmark.setDeal(deal);	
        }
        
        tokens.put("maxRating", "10");
        tokens.put("star_rating", Double.toString(hotel.getStars()));
        
        if (hotel.getNr_rooms() > 0) {
        	tokens.put("no_rooms", Integer.toString(hotel.getNr_rooms()));
        }
        address.setField(AddressInfo.EXTENSION, Integer.toString(hotel.getNr_rooms()));

        if (hotel.getPhoto_url() != null) {
            landmark.setThumbnail(hotel.getPhoto_url());
        }
        
        String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
        landmark.setDescription(desc);

        return landmark;
    }*/
	
	private static ExtendedLandmark hotelToLandmark(JSONObject hotel, Locale locale) {
		JSONArray coords = hotel.getJSONObject("geometry").getJSONArray("coordinates");
    	QualifiedCoordinates qc = new QualifiedCoordinates(coords.getDouble(1), coords.getDouble(0), 0f, 0f, 0f); 
    	
    	JSONObject props = hotel.getJSONObject("properties");
    	AddressInfo address = new AddressInfo();
    	address.setField(AddressInfo.STREET, props.getString("address"));
    	address.setField(AddressInfo.CITY, props.getString("city_hotel"));
    	
    	String cc = props.getString("cc1");
    	Locale l = new Locale("", cc.toUpperCase(Locale.US));
    	String country = l.getDisplayCountry();
    	if (country == null) {
    		country = cc;
    	}
    	address.setField(AddressInfo.COUNTRY, country);
    	address.setField(AddressInfo.POSTAL_CODE, props.getString("zip"));

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
	
	public static int countNearbyHotels(double lat, double lng, int r) throws MalformedURLException, IOException {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		String hotelsUrl = HOTELS_COUNTER_URL + StringUtil.formatCoordE2(lat) + "/" + StringUtil.formatCoordE2(lng) + "/" + normalizedRadius;			
        String hotelsCount = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), false);
		return NumberUtils.getInt(hotelsCount, -1);
	}

	private class HotelToExtendedLandmarkFunction implements Function<JSONObject, ExtendedLandmark> {

		private Locale locale;
    	
    	public HotelToExtendedLandmarkFunction(Locale locale) {
    		this.locale = locale;
    	}
    	
		public ExtendedLandmark apply(JSONObject hotel) {
			return hotelToLandmark(hotel, locale);
		}
		
	}
}
