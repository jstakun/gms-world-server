package net.gmsworld.server.layers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.persistence.HotelBean;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.Point;
import org.json.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

public class HotelsBookingUtils extends LayerHelper {

	private static final String HOTELS_PROVIDER_URL = "http://hotels-gmsworldatoso.rhcloud.com/camel/v1/cache/hotels/nearby/"; 
			
	@Override
	protected List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int r, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
		String hotelsUrl = HOTELS_PROVIDER_URL + lat + "/" + lng + "/" + normalizedRadius + "/" + limit;			
        logger.log(Level.INFO, "Calling: " + hotelsUrl);
        String hotelsJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER));
		List<HotelBean> hotels = jsonToHotelList(hotelsJson);
		logger.log(Level.INFO, "Found " + hotels.size() + " hotels...");
		landmarks.addAll(Lists.transform(hotels, new HotelToExtendedLandmarkFunction(locale)));
		return landmarks;
	}

	@Override
	protected String getLayerName() {
		return Commons.HOTELS_LAYER;
	}
	
	private static List<HotelBean> jsonToHotelList(String json) {
    	List<HotelBean> hotels = new ArrayList<HotelBean>();
    	if (StringUtils.startsWith(StringUtils.trim(json), "[{")) {
    		try {
    			JSONArray rootArray = new JSONArray(json);
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
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
    		}
    	} else {
    		logger.log(Level.SEVERE, "Received following response from server: " + json);
    	}
    	 	
    	return hotels;
	}
	
	private static ExtendedLandmark hotelToLandmark(HotelBean hotel, Locale locale) {
    	QualifiedCoordinates qc = new QualifiedCoordinates(hotel.getLatitude(), hotel.getLongitude(), 0f, 0f, 0f); 
    	AddressInfo address = new AddressInfo();
    	
    	address.setField(AddressInfo.STREET, hotel.getAddress());
    	address.setField(AddressInfo.CITY, hotel.getCity_hotel());
    	/*for (Locale l : Locale.getAvailableLocales()) {
    		if (StringUtils.equalsIgnoreCase(l.getCountry(), hotel.getCc1())) {
    			address.setField(AddressInfo.COUNTRY, l.getDisplayCountry());
    			break;
    		}
    	}*/
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
        
        //TODO calculate price in USD (call exchange rate service)
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
    }


	private class HotelToExtendedLandmarkFunction implements Function<HotelBean, ExtendedLandmark> {

		private Locale locale;
    	
    	public HotelToExtendedLandmarkFunction(Locale locale) {
    		this.locale = locale;
    	}
    	
		public ExtendedLandmark apply(HotelBean hotel) {
			return hotelToLandmark(hotel, locale);
		}
		
	}
}
