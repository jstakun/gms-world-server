package net.gmsworld.server.layers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadManager;

import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

public class HotelsBookingUtils extends LayerHelper {

	public static final String HOTELS_API_URL =  "https://hotels-api.b9ad.pro-us-east-1.openshiftapps.com"; 
   
	private static final String HOTELS_PROVIDER_URL = HOTELS_API_URL + "/camel/v1/cache/hotels/nearby/"; 
	
	private static final String HOTELS_PROVIDER_ASYNC_URL = HOTELS_API_URL + "/camel/v1/cache/hotels/async/nearby/";
	
	private static final String HOTELS_CHEAPEST_URL = HOTELS_API_URL + "/camel/v1/cache/hotels/cheapest/nearby/"; 
	
	private static final String HOTELS_CHEAPEST_ASYNC_URL = HOTELS_API_URL + "/camel/v1/cache/hotels/async/cheapest/nearby/"; 
	
	private static final String HOTELS_STARS_URL = HOTELS_API_URL + "/camel/v1/cache/hotels/stars/nearby/"; 
	
	private static final String HOTELS_STARS_ASYNC_URL = HOTELS_API_URL + "/camel/v1/cache/hotels/async/stars/nearby/"; 
	
	private static final String HOTELS_CACHE_URL = HOTELS_API_URL + "/camel/v1/getById/cache/"; 
	
	private static final String HOTELS_COUNTER_URL = HOTELS_API_URL + "/camel/v1/count/hotels/nearby/";
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	static {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private static final ObjectReader featureCollectionReader = objectMapper.reader(FeatureCollection.class);
	
	@Override
	protected List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int r, int version, int limit, int stringLimit, String callCacheFirst, String sortType, Locale locale, boolean useCache) throws Exception {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		return loadLandmarksJackson(lat, lng, query, normalizedRadius, version, limit, stringLimit, callCacheFirst, sortType, locale, useCache);
	}

	private List<ExtendedLandmark> loadLandmarksJackson(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String callCacheFirst, String sortType, Locale locale, boolean useCache) throws Exception {
		FeatureCollection hotels = null;
		String lngStr = StringUtil.formatCoordE2(lng);
		String latStr = StringUtil.formatCoordE2(lat);	

		//first call hotels cache
		if (useCache) {
			//save to cache with sort type
			String hotelsUrl = HOTELS_CACHE_URL + lngStr + "_" + latStr + "_" + radius + "_" + limit + "?user_key=" + Commons.getProperty(Property.RH_HOTELS_API_KEY);
			if (StringUtils.equalsIgnoreCase(sortType, "stars")) {
				hotelsUrl += "_stars";
			} else if (StringUtils.equalsIgnoreCase(sortType, "cheapest")) {
				hotelsUrl += "_cheapest";
			}
			logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(json, "[") && json.length() > 2) {
	    		try {
	    			json = json.substring(1, json.length()-1);
	    			hotels = featureCollectionReader.readValue(json);
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else if (StringUtils.startsWith(json, "{") && json.length() > 2) {
				try {
	    			hotels = featureCollectionReader.readValue(json);
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following hotels cache server response " + json);
			}	
		}
		
		if (hotels == null) {
			String hotelsUrlPrefix = HOTELS_PROVIDER_URL;
			if (StringUtils.equalsIgnoreCase(sortType, "stars")) {
				hotelsUrlPrefix = HOTELS_STARS_URL;
			} else if (StringUtils.equalsIgnoreCase(sortType, "cheapest")) {
				hotelsUrlPrefix = HOTELS_CHEAPEST_URL;
			}
			
			String hotelsUrl = hotelsUrlPrefix + latStr + "/" + lngStr + "/" + radius + "/" + limit + "?user_key=" + Commons.getProperty(Property.RH_HOTELS_API_KEY);			
			//logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(json, "[")) {
	    		try {
	    			json = "{\"type\": \"FeatureCollection\", \"features\":" + json + "}";
	    			hotels = featureCollectionReader.readValue(json);
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
	
	public String extendFeatureCollection(double lat, double lng, int r, int limit, String sortType, Locale locale) throws Exception {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}
		FeatureCollection hotels = null;
		String lngStr = StringUtil.formatCoordE2(lng);
		String latStr = StringUtil.formatCoordE2(lat);	

		String hotelsUrlPrefix = HOTELS_PROVIDER_URL;
		if (StringUtils.equalsIgnoreCase(sortType, "stars")) {
			hotelsUrlPrefix = HOTELS_STARS_URL;
		} else if (StringUtils.equalsIgnoreCase(sortType, "cheapest")) {
			hotelsUrlPrefix = HOTELS_CHEAPEST_URL;
		}
		
		String hotelsUrl = hotelsUrlPrefix + latStr + "/" + lngStr + "/" + normalizedRadius + "/" + limit + "?user_key=" + Commons.getProperty(Property.RH_HOTELS_API_KEY);			
		logger.log(Level.INFO, "Calling: " + hotelsUrl);
		String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
		if (StringUtils.startsWith(json, "[")) {
    		try {
    			json = "{\"type\": \"FeatureCollection\", \"features\":" + json + "}";
    			hotels = featureCollectionReader.readValue(json);
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
    		}
		} else {
			logger.log(Level.WARNING, "Received following server response " + json);
		}
		
		long start = System.currentTimeMillis();
		logger.log(Level.INFO, "Processing hotels list with Jackson...");
		
		int size = 0;
		if (hotels != null) {
			size = hotels.getFeatures().size();
		}
		
		if (size > 0) {	
		    Map<String, Double> exchangeRates = new HashMap<String, Double>();
		    exchangeRates.put("EUR", 1d);
			Map<Integer, Integer> starsMap = new HashMap<Integer, Integer>();
			Map<Integer, Integer> pricesMap = new HashMap<Integer, Integer>();
			
			hotels.setProperty("layer", Commons.HOTELS_LAYER);
			hotels.setProperty("creationDate", new Date());
			hotels.setProperty("language", locale.getLanguage());
			
			String language = locale.getLanguage();
        	String country = locale.getCountry();
        	String tocc = null;
        	
        	try {
        		if (StringUtils.isEmpty(country) && StringUtils.isNotEmpty(language)) {
            		if (StringUtils.equals(language, "en")) {
            			country = "US";
            		}  else {
            			country = StringUtils.upperCase(language, locale);
            		}
    			}
        		tocc = Currency.getInstance(new Locale(language, country)).getCurrencyCode();
        		if (tocc != null) {
            		hotels.setProperty("currencycode", tocc);
            	}
        	} catch (Exception e) {
        		logger.log(Level.WARNING, "Error getting currency for: " + country + "," + language + "\n" + e.getMessage());
        	}
        	
        	Calendar cal = Calendar.getInstance();
            PrettyTime prettyTime = new PrettyTime(locale); 
            
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource", locale);
			
            if (size <= 100) {
            	for (int i=0; i<size; i++) {
            		extendFeature(hotels.getFeatures().get(i), starsMap, pricesMap, exchangeRates, tocc, cal, rb, prettyTime, locale);			
            	}
            } else {
            	final int chunkSize = 50;
            	int first = 0, last = chunkSize;
            	ThreadManager threadManager = new ThreadManager(threadProvider);          
            	while (first < size) {
            		if (last > size) {
            			last = size;
            		}

            		threadManager.put(new HotelsProcessor(hotels.getFeatures().subList(first, last), starsMap, pricesMap, exchangeRates, tocc, cal, rb, prettyTime, locale));

            		first = last;
            		last += chunkSize;
            	}
            	threadManager.waitForThreads();
            }
			
			//stats and exchange rate for hotels			    
			hotels.setProperty("stats_price", pricesMap);
			hotels.setProperty("stats_stars", starsMap);
			hotels.setProperty("eurexchangerates", exchangeRates);
			if (StringUtils.isNotEmpty(sortType)) {
				hotels.setProperty("sortType", sortType);
			}
		}
		logger.log(Level.INFO, "Processed " + size + " hotels in " + (System.currentTimeMillis()-start) + " millis.");
				
		if (hotels != null) {
			String hotelsJson = null;
			try {
    			hotelsJson = objectMapper.writeValueAsString(hotels);
    			
    			if (size > 0 && StringUtils.isNotEmpty(hotelsJson)) {
    				logger.log(Level.INFO, "Saving geojson list to second level cache");
    				String key = "geojson/" + latStr + "/" + lngStr + "/" + Commons.HOTELS_LAYER;
    				cacheProvider.putToSecondLevelCache(key, hotelsJson);					
    			}
    			
    			if (cacheProvider != null) {
    				String key = "geojson_" + latStr + "_" + lngStr + "_" + Commons.HOTELS_LAYER + "_" + locale.getLanguage();
    				if (StringUtils.isNotEmpty(sortType)) {
    					key += "_" + sortType;
    				}
    				logger.log(Level.INFO, "Saved geojson list to local in-memory cache with key: " + key);
    				cacheProvider.put(key, hotelsJson, 1);
    			}
			} catch (JsonProcessingException e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
			} 
			return hotelsJson;
		} else {
			return null;
		}
	}
	
	public String loadHotelsAsync(double lat, double lng, int r, int limit, String sortType, boolean useCache) {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		String lngStr = StringUtil.formatCoordE2(lng);
		String latStr = StringUtil.formatCoordE2(lat);	
		String id = lngStr + "_" + latStr + "_" + normalizedRadius + "_" + limit;	
		String hotelsUrlPrefix = HOTELS_PROVIDER_ASYNC_URL;
		if (StringUtils.equalsIgnoreCase(sortType, "stars")) {
			hotelsUrlPrefix = HOTELS_STARS_ASYNC_URL;
			id += "_stars";
		} else if (StringUtils.equalsIgnoreCase(sortType, "cheapest")) {
			hotelsUrlPrefix = HOTELS_CHEAPEST_ASYNC_URL;
			id += "_cheapest";
		}
		String hotelsUrl = hotelsUrlPrefix + latStr + "/" + lngStr + "/" + normalizedRadius + "/" + limit + "?user_key=" + Commons.getProperty(Property.RH_HOTELS_API_KEY);
		
		boolean isCached = false;
		if (useCache && cacheProvider != null) {
			isCached = cacheProvider.containsKey(id);
		}
		
		if (!isCached) {
			try {
				logger.log(Level.INFO, "Calling: " + hotelsUrl);
				HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), false);
				Integer responseCode = HttpUtils.getResponseCode(hotelsUrl);
				if (responseCode != null && responseCode >= 400) {
					id = null;
					logger.log(Level.SEVERE, "Received following server response code {0}", responseCode);
				} else if (responseCode != null && responseCode == 200) {
					 if ( cacheProvider != null) {
						 cacheProvider.put(id, "1");
					 }
				} else if (responseCode == null ){
					logger.log(Level.WARNING, "No response code found"); 
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			logger.log(Level.INFO, hotelsUrl + " exists in cache");
		}
		
		return id;
	}
	
	public String findCheapestHotel(double lat, double lng, int r, int limit) {
        String response = "";
		JSONObject cheapest = findCheapestHotelJSon(lat, lng, r, limit);
		
		if (cheapest != null) {
			JSONObject props = cheapest.getJSONObject("properties");
			String currencycode = props.getString("currencycode");
			Double minrate = null;
			if (!props.isNull("minrate")) {
				minrate = props.getDouble("minrate");
			}			
			if (StringUtils.isNotEmpty(currencycode) && minrate != null) {
				if (!StringUtils.endsWithAny(currencycode, new String[]{"USD", "GBP", "EUR"})) {
					Map<String, Double> ratesMap = cacheProvider.getObject(HashMap.class, "http://api.fixer.io/latest?base=EUR");
	        		if (ratesMap != null) {
	        			Double exchangeRate = ratesMap.get(currencycode);
	        			if (exchangeRate != null) {
	        				minrate = minrate / exchangeRate;
	        				currencycode = "EUR";
	        			}
	        		}
				}
				
				response = Math.round(minrate) + " " + currencycode;
			}
		}
		
		return response;
	}
	
	
	private JSONObject findCheapestHotelJSon(double lat, double lng, int r, int limit) {
		int normalizedRadius = r;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}	
		String lngStr = StringUtil.formatCoordE2(lng);
		String latStr = StringUtil.formatCoordE2(lat);	
		String hotelsUrl = HOTELS_CHEAPEST_URL + latStr + "/" + lngStr + "/" + normalizedRadius + "/" + limit + "?user_key=" + Commons.getProperty(Property.RH_HOTELS_API_KEY);
		JSONObject cheapest = null;
		
		try {
			//logger.log(Level.INFO, "Calling: " + hotelsUrl);
			String json = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), true);
			if (StringUtils.startsWith(StringUtils.trim(json), "[")) {
	    		try {
	    			JSONArray root = new JSONArray(json);
	    			if (root.length() > 0) {
	    				cheapest = root.getJSONObject(0);
	    			}
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else if (StringUtils.startsWith(StringUtils.trim(json), "{")) {
	    		try {
	    			cheapest = new JSONObject(json);
	    		} catch (Exception e) {
	    			logger.log(Level.SEVERE, e.getMessage(), e);
	    		}
			} else {
				logger.log(Level.WARNING, "Received following server response " + json);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return cheapest;
	}

	public String getLayerName() {
		return Commons.HOTELS_LAYER;
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
        landmark.setUrl(value.replace("a_id", "aid")); //bug fixed
        
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
        
        Integer nr = hotel.getProperty("nr_rooms");
        if (nr != null && nr > 0) {
        	tokens.put("no_rooms", Integer.toString(nr));
        	address.setField(AddressInfo.EXTENSION, Integer.toString(nr));
        } else {
        	address.setField(AddressInfo.EXTENSION, "0");
        }      

        value = hotel.getProperty("photo_url");
        landmark.setThumbnail(value);
        
        String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
        landmark.setDescription(desc);

        return landmark;
    }
	
	private void extendFeature(final Feature hotel, final Map<Integer, Integer> starsMap, final Map<Integer, Integer> pricesMap, final Map<String, Double> exchangeRates, 
			String tocc, final Calendar cal, final ResourceBundle rb, final PrettyTime prettyTime, final Locale locale) {
		long start = System.currentTimeMillis();
		final long timeLimit = 500;
		final Map<String, Object> props = hotel.getProperties(); //new HashMap<String, Object>();
		props.put("name", hotel.getProperty("name"));
		String url = hotel.getProperty("hotel_url");
		props.put("url", url.replace("a_id", "aid"));
		
		Integer stars = null;
		Object starsObj = hotel.getProperty("stars");
		if (starsObj instanceof Integer) {
			stars = (Integer)starsObj;
		} else if (starsObj instanceof Double) {
			stars = ((Double)starsObj).intValue();
		} else {
			stars = 0;
		}
		
		int s = stars.intValue();
		if (starsMap.containsKey(s)) {
			starsMap.put(s, starsMap.get(s)+1);
		} else {
			starsMap.put(s, 1);
		}
		
		Integer nr = hotel.getProperty("nr_rooms");
        if (nr != null && nr > 1) {
			props.put("icon", "star_" + s + ".png");
		} else {
			props.put("icon", s + "stars_blue.png");
		}
		
        props.put("thumbnail", hotel.getProperty("photo_url"));

        String fromcc = hotel.getProperty("currencycode");
        Double rate = NumberUtils.getDouble(hotel.getProperty("minrate"));
        if (rate == null) {
        	rate = NumberUtils.getDouble(hotel.getProperty("maxrate"));
        }
        if (rate != null) {
        	boolean rateChanged = false;
        	if (tocc != null && fromcc != null && !StringUtils.equals(tocc, fromcc) && fromcc.length() == 3) {  	
        		Map<String, Double> ratesMap = cacheProvider.getObject(HashMap.class, "http://api.fixer.io/latest?base=" + fromcc);
        		if (ratesMap != null) {
        			Double toccrate = ratesMap.get(tocc);
        			if (toccrate != null) {
            			rate = rate * toccrate;
            			rateChanged = true;
            		}
        		}
        	} 

        	props.put("price", StringUtil.formatCoordE0(rate));
        	if (tocc == null || !rateChanged) {
        		tocc = fromcc;
        	} 
        	
        	props.put("cc", tocc);
        	
        	s = 0;
        	Map<String, Double> ratesMap = cacheProvider.getObject(HashMap.class, "http://api.fixer.io/latest?base=EUR");
    		if (ratesMap != null) {
    			Double exchangeRate = ratesMap.get(tocc);
    			if (exchangeRate != null) {
    				exchangeRates.put(tocc, exchangeRate);
    				double eurvalue = rate / exchangeRate;
    				if (eurvalue < 50d) {
    					s = 1;
    				} else if (eurvalue >= 50d && eurvalue < 100d) {
    					s = 2;
    				} else if (eurvalue >= 100d && eurvalue < 150d) {
    					s = 3;
    				} else if (eurvalue >= 150d && eurvalue < 200d) {
    					s = 4;
    				} else if (eurvalue >= 200d) {
    					s = 5;
    				}
    			}
    		}
        	if (pricesMap.containsKey(s)) {
        		pricesMap.put(s, pricesMap.get(s)+1);
        	} else {
        		pricesMap.put(s, 1);
        	}
        }
		
        long end = System.currentTimeMillis();
        if (end - start > timeLimit) {
    		logger.log(Level.WARNING, "End of section 1 in " + (end - start) + " millis");
        }
        start = end;
        //-------------------------------------2------------------------------------------
        
		String desc = "";
		//stars
		for (int j=0;j<stars;j++) {
			desc += "<img src=\"/images/star_blue.png\" alt=\"*\"/>";
		}
		if (desc.length() > 0) {
			desc += "<br/>";
		}
		//price
		if (rate != null) {
			Deal deal = new Deal(rate, -1, -1, null, tocc);
			desc += JSONUtils.formatDeal(deal, locale, rb) + "<br/>";
		}
		
		end = System.currentTimeMillis();
        if (end - start > timeLimit) {
    		logger.log(Level.WARNING, "End of section 2 in " + (end - start) + " millis");
        }
        start = end;
        //-------------------------------------3------------------------------------------
        
        //address
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
		desc += JSONUtils.formatAddress(address) + "<br/>";
		
		end = System.currentTimeMillis();
        if (end - start > timeLimit) {
    		logger.log(Level.WARNING, "End of section 3 in " + (end - start) + " millis");
        }
        start = end;
        //----------------------------------------4------------------------------------------
        
        //creation date
		cal.setTimeInMillis((Long)hotel.getProperty("creationDate"));
    	desc += String.format(rb.getString("Landmark.creation_date"), prettyTime.format(cal)) + "<br/>";
		//no of rooms
		desc += String.format(rb.getString("Landmark.no_rooms"), nr);
		props.put("desc", desc); 
		
		hotel.setProperties(props);
		//-----------------------------
		end = System.currentTimeMillis();
		if (end - start > timeLimit) {
			logger.log(Level.WARNING, "End of section 4 in " + (end - start) + " millis");
		}        
	}
	
	public int countNearbyHotels(double lat, double lng, int r) {
		int normalizedRadius = r;
		String hotelsCount = null;
		if (r < 1000) {
			normalizedRadius = r * 1000;
		}
		
		try {
			String hotelsUrl = HOTELS_COUNTER_URL + StringUtil.formatCoordE2(lat) + "/" + StringUtil.formatCoordE2(lng) + "/" + normalizedRadius + "?user_key=" + Commons.getProperty(Property.RH_HOTELS_API_KEY);			
			hotelsCount = HttpUtils.processFileRequestWithBasicAuthn(new URL(hotelsUrl), Commons.getProperty(Property.RH_GMS_USER), false);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		
        return NumberUtils.getInt(hotelsCount, -1);
	}
	
	public String getIcon() {
		return "hotel.png";
	}
	
	public String getURI() {
		return "hotelsProvider";
	}
	
	private final class HotelsProcessor implements Runnable {

		private Map<Integer, Integer> starsMap;
		private Map<Integer, Integer> pricesMap;
		private Map<String, Double> exchangeRates; 
		private String tocc;
		private Calendar cal;
		private ResourceBundle rb;
		private PrettyTime prettyTime;
		private Locale locale;
		private List<Feature> features;
		
		public HotelsProcessor(final List<Feature> features, final Map<Integer, Integer> starsMap, final Map<Integer, Integer> pricesMap, final Map<String, Double> exchangeRates, 
				String tocc, final Calendar cal, final ResourceBundle rb, final PrettyTime prettyTime, final Locale locale) {
			this.cal = cal;
			this.exchangeRates = exchangeRates;
			this.locale = locale;
			this.prettyTime = prettyTime;
			this.pricesMap = pricesMap;
			this.rb = rb;
			this.starsMap = starsMap;
			this.tocc = tocc;
			this.features = features;
		}
		
		@Override
		public void run() {
			//long start = System.currentTimeMillis();
			try {
				for (Feature feature : features) {
					extendFeature(feature, starsMap, pricesMap, exchangeRates, tocc, cal, rb, prettyTime, locale);	
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} 
			//logger.log(Level.INFO, "Processed " + features.size() + " hotels in " + (System.currentTimeMillis()-start) + " millis");
		}	
	}
}
