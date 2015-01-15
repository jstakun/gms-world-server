package net.gmsworld.server.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.ThreadUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

import net.gmsworld.server.utils.StringUtil;

public class SearchUtils extends LayerHelper {

	protected JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flex, String ftoken, Locale locale) throws Exception {
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
        Map<String, Thread> layers = new ConcurrentHashMap<String, Thread>();      
        Map<String, JSONObject> jsonMap = new HashMap<String, JSONObject>();
    	
    	if (!isDeal && !geocode) {
    		layers.put(Commons.FOURSQUARE_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.FOURSQUARE_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		layers.put(Commons.FACEBOOK_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, ftoken, language, Commons.FACEBOOK_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		layers.put(Commons.GOOGLE_PLACES_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.GOOGLE_PLACES_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		layers.put(Commons.LM_SERVER_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.LM_SERVER_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		layers.put(Commons.FLICKR_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.FLICKR_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		layers.put(Commons.EVENTFUL_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.EVENTFUL_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		if (LayerHelperFactory.getYelpUtils().hasNeighborhoods(lat, lng)) {
    			layers.put(Commons.YELP_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.YELP_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		}
    		if (version > 1082) {
    			layers.put(Commons.TWITTER_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.TWITTER_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		}
    		layers.put(Commons.MEETUP_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.MEETUP_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap))); //
    		if (version >= 1094) {
        		layers.put(Commons.FREEBASE_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.FREEBASE_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap)));
        	}
    	}

    	if (!geocode && GeocodeUtils.isNorthAmericaLocation(Double.toString(lat), Double.toString(lng))) {
    		layers.put(Commons.COUPONS_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.COUPONS_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap)));
    		layers.put(Commons.GROUPON_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.GROUPON_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap)));
    	}

    	layers.put(Commons.LOCAL_LAYER, threadProvider.newThread(new JSonSearchTask(lat, lng, query, null, language, Commons.LOCAL_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, jsonMap)));
   
    	logger.log(Level.INFO, "Found {0} landmarks", counter);        
        
        for (Iterator<String> iter = layers.keySet().iterator(); iter.hasNext();) {
            Thread t = layers.get(iter.next());
            t.start();
        }

        ThreadUtil.waitForLayers(layers);
        
        return new JSONObject().put("ResultSet", jsonMap);
    }
    
	@Override
	protected List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flex, String ftoken, Locale locale, boolean useCache) throws Exception {
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
        Map<String, Thread> layers = new ConcurrentHashMap<String, Thread>();
        
        List<ExtendedLandmark> foundLandmarks = new ArrayList<ExtendedLandmark>();
        
        if (!isDeal && !geocode) {
        	layers.put(Commons.FOURSQUARE_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.FOURSQUARE_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	layers.put(Commons.FACEBOOK_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, ftoken, language, Commons.FACEBOOK_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	layers.put(Commons.GOOGLE_PLACES_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.GOOGLE_PLACES_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	layers.put(Commons.LM_SERVER_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.LM_SERVER_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	layers.put(Commons.FLICKR_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.FLICKR_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	layers.put(Commons.EVENTFUL_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.EVENTFUL_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	if (LayerHelperFactory.getYelpUtils().hasNeighborhoods(lat, lng)) {
        		layers.put(Commons.YELP_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.YELP_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	}
        	if (version > 1082) {
        		layers.put(Commons.TWITTER_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.TWITTER_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	}
        	layers.put(Commons.MEETUP_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.MEETUP_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks))); //
        	if (version >= 1094) {
        		layers.put(Commons.FREEBASE_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.FREEBASE_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks)));
        	}	
        }

        if (!geocode && GeocodeUtils.isNorthAmericaLocation(Double.toString(lat), Double.toString(lng))) {
        	layers.put(Commons.COUPONS_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.COUPONS_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks)));
        	layers.put(Commons.GROUPON_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.GROUPON_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks)));
        }

        layers.put(Commons.LOCAL_LAYER, threadProvider.newThread(new SerialSearchTask(lat, lng, query, null, language, Commons.LOCAL_LAYER, radius, dealLimit, limit, stringLimit, locale, layers, foundLandmarks)));
        
        for (Iterator<String> iter = layers.keySet().iterator(); iter.hasNext();) {
            Thread t = layers.get(iter.next());
            t.start();
        }

        ThreadUtil.waitForLayers(layers);
        
        logger.log(Level.INFO, "Found {0} landmarks", foundLandmarks.size());        
        
        return foundLandmarks;
	}

	private class JSonSearchTask implements Runnable {

		private double latitude, longitude;
	    private String query, ftoken, language, layer;
	    private int radius, dealLimit, limit, stringLimit;
	    private Locale locale;
	    private Map<String, Thread> layers;      
	    private Map<String, JSONObject> jsonMap;
	    
		public JSonSearchTask(double latitude, double longitude, String query, String ftoken, String language, String layer, int radius, int dealLimit, int limit, int stringLimit, Locale locale, Map<String, Thread> layers, Map<String, JSONObject> jsonMap) {
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
			this.layers = layers;
		}

		public void run() {
			JSONObject json = null;

			logger.log(Level.INFO, "Processing search in layer {0}", layer);

			try {
				if (layer.equals(Commons.COUPONS_LAYER)) {
					json = LayerHelperFactory.getCouponsUtils().processRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, "", language);
				} else if (layer.equals(Commons.LOCAL_LAYER)) {
					String placeGeocode = GeocodeUtils.processRequest(query, null, locale, true);
					if (!GeocodeUtils.geocodeEquals(placeGeocode, GeocodeUtils.processRequest(null, null, locale, true))) {
						json = GeocodeUtils.geocodeToJSonObject(query, placeGeocode);
						logger.log(Level.INFO, "Geocode service found this place.");
					} else {
						logger.log(Level.INFO, "Geocode service couldn't find this place.");
					}
				} else if (layer.equals(Commons.GROUPON_LAYER)) {
					json = LayerHelperFactory.getGrouponUtils().processRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, null, null);
				} else if (layer.equals(Commons.FOURSQUARE_LAYER)) {
					json = LayerHelperFactory.getFoursquareUtils().processRequest(latitude, longitude, query, radius * 1000, 3, limit, stringLimit, "browse", language);
				} else if (layer.equals(Commons.FACEBOOK_LAYER)) {
					json = LayerHelperFactory.getFacebookUtils().processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, ftoken, null);
				} else if (layer.equals(Commons.GOOGLE_PLACES_LAYER)) {
					json = LayerHelperFactory.getGooglePlacesUtils().processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, language, null);
				} else if (layer.equals(Commons.LM_SERVER_LAYER)) {
					json = LayerHelperFactory.getGmsUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
				} else if (layer.equals(Commons.FLICKR_LAYER)) {
					json = LayerHelperFactory.getFlickrUtils().processRequest(latitude, longitude, query, radius, 4, limit,stringLimit, null, null);
				} else if (layer.equals(Commons.EVENTFUL_LAYER)) {
					json = LayerHelperFactory.getEventfulUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
				} else if (layer.equals(Commons.YELP_LAYER)) {
					json = LayerHelperFactory.getYelpUtils().processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, "false", language);
				} else if (layer.equals(Commons.TWITTER_LAYER)) {
					json = LayerHelperFactory.getTwitterUtils().processRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null);
				} else if (layer.equals(Commons.MEETUP_LAYER)) {
					json = LayerHelperFactory.getMeetupUtils().processRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				int counter = JSONUtils.addJSONObjectToResultMap(jsonMap, layer, json, true);
				logger.log(Level.INFO, "Found {0} landmarks in layer {1}", new Object[]{counter, layer});
				layers.remove(layer);
			}
		}
	}

	private class SerialSearchTask implements Runnable {

		private double latitude, longitude;
	    private String query, ftoken, language, layer;
	    private int radius, dealLimit, limit, stringLimit;
	    private Locale locale;
	    private Map<String, Thread> layers;     
	    private List<ExtendedLandmark> foundLandmarks;
	    
		public SerialSearchTask(double latitude, double longitude, String query, String ftoken, String language, String layer, int radius, int dealLimit, int limit, int stringLimit, Locale locale, Map<String, Thread> layers, List<ExtendedLandmark> foundLandmarks) {
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
			this.layers = layers;
			this.foundLandmarks = foundLandmarks;
		}

		public void run() {
			logger.log(Level.INFO, "Processing search in layer {0}", layer);
			List<ExtendedLandmark> landmarks = null;

			try {
				if (layer.equals(Commons.COUPONS_LAYER)) {
					landmarks = LayerHelperFactory.getCouponsUtils().processBinaryRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, "", language, locale, true);
				} else if (layer.equals(Commons.LOCAL_LAYER)) {
					String placeGeocode = GeocodeUtils.processRequest(query, null, locale, true);
					if (!GeocodeUtils.geocodeEquals(placeGeocode, GeocodeUtils.processRequest(null, null, locale, true))) {
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
					landmarks = LayerHelperFactory.getGrouponUtils().processBinaryRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.FOURSQUARE_LAYER)) {
					landmarks = LayerHelperFactory.getFoursquareUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, 3, limit, stringLimit, "browse", language, locale, true);
				} else if (layer.equals(Commons.FACEBOOK_LAYER)) {
					landmarks = LayerHelperFactory.getFacebookUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, ftoken, null, locale, true);
				} else if (layer.equals(Commons.GOOGLE_PLACES_LAYER)) {
					landmarks = LayerHelperFactory.getGooglePlacesUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, language, null, locale, true);
				} else if (layer.equals(Commons.LM_SERVER_LAYER)) {
					landmarks = LayerHelperFactory.getGmsUtils().processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.FLICKR_LAYER)) {
					landmarks = LayerHelperFactory.getFlickrUtils().processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.EVENTFUL_LAYER)) {
					landmarks = LayerHelperFactory.getEventfulUtils().processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale, true);
				} else if (layer.equals(Commons.YELP_LAYER)) {
					landmarks = LayerHelperFactory.getYelpUtils().processBinaryRequest(latitude, longitude, query,radius * 1000, 2, limit, stringLimit, "false", language, locale, true);
				} else if (layer.equals(Commons.TWITTER_LAYER)) {
					landmarks = LayerHelperFactory.getTwitterUtils().processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale, true);
				} else if (layer.equals(Commons.MEETUP_LAYER)) {
					landmarks = LayerHelperFactory.getMeetupUtils().processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale, true);
				} else if (layer.equals(Commons.FREEBASE_LAYER)) {
					landmarks = LayerHelperFactory.getFreebaseUtils().processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale, true);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				if (landmarks != null && !landmarks.isEmpty()) {
					foundLandmarks.addAll(landmarks);
				}
				layers.remove(layer);
			}
		}
	}

}
