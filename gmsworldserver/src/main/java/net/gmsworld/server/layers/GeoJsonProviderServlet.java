package net.gmsworld.server.layers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.layers.HotelsBookingUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.GoogleThreadProvider;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;

/**
 * Servlet implementation class geoJsonProviderServlet
 */
public class GeoJsonProviderServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(GeoJsonProviderServlet.class.getName());
	private static final long serialVersionUID = 1L;
	private static final int HOTELS_LIMIT = 500;
	private static final int DEFAULT_LIMIT = 50;
	private static final int RADIUS = 50;
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeoJsonProviderServlet() {
        super();
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LayerHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
        LayerHelperFactory.getInstance().setThreadProvider(new GoogleThreadProvider());
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String json = null, layer = null;
		int layerSize = 0;
		try {
			if (HttpUtils.isEmptyAny(request, "lat", "lng", "layer")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
	        	logger.log(Level.INFO, "User agent device type: " + os.getDeviceType().getName());
	        	layer = request.getParameter("layer"); 
        		if (!os.getDeviceType().equals(DeviceType.UNKNOWN)) {
	        		response.setContentType("text/javascript;charset=UTF-8");
	        		double lat = GeocodeUtils.getLatitude(request.getParameter("lat"));
	        		double lng =  GeocodeUtils.getLongitude(request.getParameter("lng"));
	        		Locale locale = request.getLocale();
	        		String flexString = StringUtil.getLanguage(locale.getLanguage(), "en", 2);
	        		String flexString2 = request.getParameter("sortType");
	        		int hotelsInRangeCount = -1;
	        		
	        		//Searching geojson document in local in-memory cache
	        		LayerHelper layerHelper = LayerHelperFactory.getInstance().getByName(layer);
	        		if (layerHelper != null) { 
	        			logger.log(Level.INFO, "Searching geojson document in local in-memory cache...");
	        			json = layerHelper.getGeoJson(lat, lng, layer, flexString, flexString2);		
	        		}
			    
	        		//Searching geojson document in remote document cache
	        		if (!StringUtils.startsWith(json, "{")) { 
	        			String latStr = StringUtil.formatCoordE2(lat);
	        			String lngStr = StringUtil.formatCoordE2(lng);
	        			logger.log(Level.INFO, "Searching geojson document in remote document cache...");
	        			String key = "geojson/" + layer + "/" + latStr + "/" + lngStr + "/" + flexString;
	        			if (StringUtils.isNotEmpty(flexString2)) {
	        				key += "/" + flexString2;
	        			}
	        			json = GoogleCacheProvider.getInstance().getFromSecondLevelCache(key);
	        		}
	        		
	        		//Searching geojson document in layer provider
					if (!StringUtils.startsWith(json, "{")  && layerHelper != null) {
						try {
							//layers specific code
							int radius = RADIUS;
							int limit = DEFAULT_LIMIT;
							if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
								try {
									//if less that 30 hotels is range increase search radius
									hotelsInRangeCount = ((HotelsBookingUtils)LayerHelperFactory.getInstance().getByName(Commons.HOTELS_LAYER)).countNearbyHotels(lat, lng, RADIUS);
									if (hotelsInRangeCount >= 0 && hotelsInRangeCount < 30) {
										radius = 2 * RADIUS; //max 100
									}
									logger.log(Level.INFO, hotelsInRangeCount + " hotels in range.");
								} catch (Exception e) {
						    		logger.log(Level.SEVERE, e.getMessage(), e);
						    	} 
								limit = HOTELS_LIMIT;
								flexString = "true";
							} else if (StringUtils.equals(layer, Commons.FACEBOOK_LAYER) || StringUtils.equals(layer, Commons.FOURSQUARE_LAYER) || StringUtils.equals(layer, Commons.FOURSQUARE_MERCHANT_LAYER)) {
						    	flexString = null;	
							} else if (StringUtils.equals(layer, Commons.OSM_ATM_LAYER)) {
								flexString = "atm";
							} else if (StringUtils.equals(layer, Commons.OSM_PARKING_LAYER)) {
								flexString = "parking";
							} else if (StringUtils.equals(layer, Commons.OSM_TAXI_LAYER)) {
								flexString = "taxi";
							}
						
							String newkey = null;
							int version = Integer.parseInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.LM_VERSION, "1146"));
							
							if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
								if (hotelsInRangeCount > 0) {
									json = ((HotelsBookingUtils)layerHelper).extendFeatureCollection(lat, lng, radius, limit, flexString2, locale);
									JSONObject layerJson = new JSONObject(json);
									layerSize = layerJson.getJSONArray("features").length();
								}
							} else if (StringUtils.equals(layer, Commons.GROUPON_LAYER) || StringUtils.equals(layer, Commons.COUPONS_LAYER)) {
								if (GeocodeUtils.isNorthAmericaLocation(lat, lng)) {
									List<ExtendedLandmark> landmarks = layerHelper.processBinaryRequest(lat, lng, null, radius, version, limit, StringUtil.getStringLengthLimit("l"), null, null, locale, true);
					    			layerSize = landmarks.size();
									newkey = layerHelper.cacheGeoJson(landmarks, lat, lng, layer, locale, flexString2);                       
								}
							} else {
								List<ExtendedLandmark> landmarks = layerHelper.processBinaryRequest(lat, lng, null, radius, version, limit, StringUtil.getStringLengthLimit("l"), flexString, flexString2, locale, true);
								layerSize = landmarks.size();
								newkey = layerHelper.cacheGeoJson(landmarks, lat, lng, layer, locale, flexString2);                          				    		
							}
						
							if (newkey != null) {
			    				logger.log(Level.INFO, "Searching geojson document in in-memory document cache...");
					    		json = CacheUtil.getString(newkey);
			    			}
						} catch (Exception e) {
				    		logger.log(Level.SEVERE, e.getMessage(), e);
				    	}
					} else {
						JSONObject layerJson = new JSONObject(json);
						layerSize = layerJson.getJSONArray("features").length();
					}
	        	}
			}	
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
        	//{"type":"FeatureCollection","properties":{"layer":"Layer"},"features":[]});
        	if (!StringUtils.startsWith(json, "{")) {      		
        		JSONObject resp = new JSONObject().
        				put("properties", new JSONObject().put("layer", layer)).
        				put("features", new JSONArray());
				json = resp.toString();			
			} 
			logger.log(Level.INFO, "Sending " + layerSize + " landmarks from layer " + layer);
        	String callBackJavaScripMethodName = request.getParameter("callback");
        	if (StringUtils.isNotEmpty(callBackJavaScripMethodName)) {
        		json = callBackJavaScripMethodName + "("+ json + ");";
        	}
        	response.getWriter().write(json);
        	response.getWriter().close();
        }
	}
}
