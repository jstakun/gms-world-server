package net.gmsworld.server.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadManager;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class SearchUtils extends LayerHelper {

	protected JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flex, String ftoken, Locale locale) throws Exception {
		Map<String, JSONObject> jsonMap = new HashMap<String, JSONObject>();
    	
		if (StringUtils.isNotEmpty(query)) {
		
			int counter = 0;
		
			boolean isDeal = false;
			String[] config = StringUtils.split(flex, "_");
			if (config.length > 0 && config[0].equals("1")) {
				isDeal = true;
			}
			boolean geocode = false;
			if (config.length > 1 && config[1].equals("1")) {
				geocode = true;
			}
        
			int dealLimit = 300;
			if (config.length > 2) {
				dealLimit = NumberUtils.getInt(config[2], 300);
			}
        
			String language = StringUtil.getLanguage(locale.getLanguage(), "en", 2);
			ThreadManager threadManager = new ThreadManager(threadProvider);
        
			if (!isDeal && !geocode) {
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.FOURSQUARE_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, ftoken, language, Commons.FACEBOOK_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.GOOGLE_PLACES_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.LM_SERVER_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.FLICKR_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.EVENTFUL_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.YELP_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				if (version > 1082) {
					threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.TWITTER_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
				}
				threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.MEETUP_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap))); //
			}

			//if (!geocode && GeocodeUtils.isNorthAmericaLocation(lat, lng)) {
				//threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.COUPONS_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap)));
				//threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.GROUPON_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap)));
			//}

			threadManager.put(threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.LOCAL_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, jsonMap)));
   
			logger.log(Level.INFO, "Found {0} landmarks", counter);        
        
			threadManager.waitForThreads();
    	} else {
    		logger.log(Level.WARNING, "Search query was empty!");
    	}
        
        return new JSONObject().put("ResultSet", jsonMap);
    }
    
	@Override
	protected List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flex, String ftoken, Locale locale, boolean useCache) throws Exception {
		List<ExtendedLandmark> foundLandmarks = new ArrayList<ExtendedLandmark>();
        
		if (StringUtils.isNotEmpty(query)) {
			boolean isDeal = false;
			boolean geocode = false;
			int dealLimit = 300;
			String[] config = StringUtils.split(flex, "_");
        
			if (config != null) {
				if (config.length > 0 && config[0].equals("1")) {
					isDeal = true;
				}
				if (config.length > 1 && config[1].equals("1")) {
					geocode = true;
				}     
				if (config.length > 2) {
					dealLimit = NumberUtils.getInt(config[2], 300);
				}     
			}
        
			String language = StringUtil.getLanguage(locale.getLanguage(), "en", 2);
			ThreadManager threadManager = new ThreadManager(threadProvider);
        
			if (!isDeal && !geocode) {
				threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.FOURSQUARE_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
				threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, ftoken, language, Commons.FACEBOOK_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
				threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.GOOGLE_PLACES_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
				threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.LM_SERVER_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
				threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.FLICKR_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
				threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.EVENTFUL_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
				threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.YELP_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
        		if (version > 1082) {
        			threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.TWITTER_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
        		}
        		threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.MEETUP_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks))); //
			}

			//if (!geocode && GeocodeUtils.isNorthAmericaLocation(lat, lng)) {
				//threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.COUPONS_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks)));
				//threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.GROUPON_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks)));
			//}

			threadManager.put(threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.LOCAL_LAYER, radius, dealLimit, limit, stringLimit, locale, isDeal, foundLandmarks)));      
        
			threadManager.waitForThreads();
        
			logger.log(Level.INFO, "Found {0} landmarks", foundLandmarks.size());        
		} else {
			logger.log(Level.WARNING, "Search query was empty!");
		}
		
        return foundLandmarks;
	}

	private class JSonSearchTask implements Runnable {

		private double latitude, longitude;
	    private String query, ftoken, language, layer;
	    private int radius, dealLimit, limit, stringLimit;
	    private Locale locale;
	    private Map<String, JSONObject> jsonMap;
	    private boolean isDeal;
	    
		public JSonSearchTask(double latitude, double longitude, String query, String ftoken, String language, String layer, int radius, int dealLimit, int limit, int stringLimit, Locale locale, boolean isDeal, Map<String, JSONObject> jsonMap) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.query = query;
			this.ftoken = ftoken;
			this.language = language;
			this.layer = layer;
			this.radius = radius;
			this.dealLimit = dealLimit;
			this.limit = limit;
			this.stringLimit = stringLimit;
			this.locale = locale;
			this.jsonMap = jsonMap;
			this.isDeal = isDeal;
		}

		public void run() {
			JSONObject json = null;

			logger.log(Level.INFO, "Processing search in layer {0}", layer);

			try {
				if (layer.equals(Commons.COUPONS_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.COUPONS_LAYER).processRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, "", language);
				} else if (layer.equals(Commons.LOCAL_LAYER)) {
					int appId;
					if (isDeal) {
						appId = Commons.DA_ID;
					} else {
						appId = Commons.LM_ID;
					}
					String placeGeocode = GeocodeUtils.processRequest(query, null, locale, appId, true);
					if (!GeocodeUtils.geocodeEquals(placeGeocode, GeocodeUtils.processRequest(null, null, locale, appId, true))) {
						json = GeocodeUtils.geocodeToJSonObject(query, placeGeocode);
						logger.log(Level.INFO, "Geocode service found this place.");
					} else {
						logger.log(Level.INFO, "Geocode service couldn't find this place.");
					}
				} else if (layer.equals(Commons.GROUPON_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.GROUPON_LAYER).processRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, null, null);
				} else if (layer.equals(Commons.FOURSQUARE_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).processRequest(latitude, longitude, query, radius * 1000, 3, limit, stringLimit, "browse", language);
				} else if (layer.equals(Commons.FACEBOOK_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, ftoken, null);
				} else if (layer.equals(Commons.GOOGLE_PLACES_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.GOOGLE_PLACES_LAYER).processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, language, null);
				} else if (layer.equals(Commons.LM_SERVER_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER).processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
				} else if (layer.equals(Commons.FLICKR_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.FLICKR_LAYER).processRequest(latitude, longitude, query, radius, 4, limit,stringLimit, null, null);
				} else if (layer.equals(Commons.EVENTFUL_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.EVENTFUL_LAYER).processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
				} else if (layer.equals(Commons.YELP_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.YELP_LAYER).processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, "false", language);
				} else if (layer.equals(Commons.TWITTER_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER).processRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null);
				} else if (layer.equals(Commons.MEETUP_LAYER)) {
					json = LayerHelperFactory.getInstance().getByName(Commons.MEETUP_LAYER).processRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				int counter = JSONUtils.addJSONObjectToResultMap(jsonMap, layer, json, true);
				logger.log(Level.INFO, "Found {0} landmarks in layer {1}", new Object[]{counter, layer});
			}
		}
	}

	private class SerialSearchTask implements Runnable {

		private double latitude, longitude;
	    private String query, ftoken, language, layer;
	    private int radius, dealLimit, limit, stringLimit;
	    private Locale locale;
	    private List<ExtendedLandmark> foundLandmarks;
	    private boolean isDeal;
	    
		public SerialSearchTask(double latitude, double longitude, String query, String ftoken, String language, String layer, int radius, int dealLimit, int limit, int stringLimit, Locale locale, boolean isDeal, List<ExtendedLandmark> foundLandmarks) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.query = query;
			this.ftoken = ftoken;
			this.language = language;
			this.layer = layer;
			this.radius = radius;
			this.dealLimit = dealLimit;
			this.limit = limit;
			this.stringLimit = stringLimit;
			this.locale = locale;
			this.foundLandmarks = foundLandmarks;
			this.isDeal = isDeal;
		}

		public void run() {
			logger.log(Level.INFO, "Processing search in layer {0}", layer);
			List<ExtendedLandmark> landmarks = null;

			try {
				if (layer.equals(Commons.COUPONS_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.COUPONS_LAYER).processBinaryRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, "", language, locale, true);
				} else if (layer.equals(Commons.LOCAL_LAYER)) {
					int appId;
					if (isDeal) {
						appId = Commons.DA_ID;
					} else {
						appId = Commons.LM_ID;
					}
					String placeGeocode = GeocodeUtils.processRequest(query, null, locale, appId, true);
					if (!GeocodeUtils.geocodeEquals(placeGeocode, GeocodeUtils.processRequest(null, null, locale, appId, true))) {
						ExtendedLandmark landmark = GeocodeUtils.geocodeToLandmark(query, placeGeocode, locale);
						if (landmark != null) {
							landmarks = new ArrayList<ExtendedLandmark>();
							landmarks.add(landmark);
						}
						logger.log(Level.INFO, "Geocode service found this place.");
					} else {
						logger.log(Level.INFO, "Geocode service couldn't find this place.");
					}
				} else if (layer.equals(Commons.GROUPON_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.GROUPON_LAYER).processBinaryRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.FOURSQUARE_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.FOURSQUARE_LAYER).processBinaryRequest(latitude, longitude, query, radius * 1000, 3, limit, stringLimit, "browse", language, locale, true);
				} else if (layer.equals(Commons.FACEBOOK_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.FACEBOOK_LAYER).processBinaryRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, ftoken, null, locale, true);
				} else if (layer.equals(Commons.GOOGLE_PLACES_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.GOOGLE_PLACES_LAYER).processBinaryRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, language, null, locale, true);
				} else if (layer.equals(Commons.LM_SERVER_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.LM_SERVER_LAYER).processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.FLICKR_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.FLICKR_LAYER).processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.EVENTFUL_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.EVENTFUL_LAYER).processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.YELP_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.YELP_LAYER).processBinaryRequest(latitude, longitude, query,radius * 1000, 2, limit, stringLimit, "false", language, locale, true);
				} else if (layer.equals(Commons.TWITTER_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.TWITTER_LAYER).processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale, true);
				} else if (layer.equals(Commons.MEETUP_LAYER)) {
					landmarks = LayerHelperFactory.getInstance().getByName(Commons.MEETUP_LAYER).processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale, true);
				} 
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				if (landmarks != null && !landmarks.isEmpty()) {
					foundLandmarks.addAll(landmarks);
				}
			}
		}
	}
	
    public String getLayerName() {
    	return Commons.SEARCH_LAYER;
    }

    public boolean isEnabled() {
    	return false; //this layer should be only called by search servlet
    }
}
