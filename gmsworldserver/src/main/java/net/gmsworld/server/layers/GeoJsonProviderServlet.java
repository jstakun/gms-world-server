package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

/**
 * Servlet implementation class geoJsonProviderServlet
 */
public class GeoJsonProviderServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(GeoJsonProviderServlet.class.getName());
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeoJsonProviderServlet() {
        super();
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
		String json = null;
		try {
			if (HttpUtils.isEmptyAny(request, "lat", "lng", "layer")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				response.setContentType("text/javascript;charset=UTF-8");
				double lat = GeocodeUtils.getLatitude(request.getParameter("lat"));
			    double lng =  GeocodeUtils.getLongitude(request.getParameter("lng"));
			    String layer = request.getParameter("layer"); 
			    LayerHelper layerHelper = LayerHelperFactory.getByName(layer);
			    if (layerHelper != null) {
			    	json = layerHelper.getGeoJson(lat, lng, layer);		
			    }
			    
				if (!StringUtils.startsWith(json, "{")) {
				    URL cacheUrl = new URL("http://cache-gmsworld.rhcloud.com/rest/cache/geojson/" + layer + "/" + lat + "/" + lng);
					json = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, Commons.getProperty(Property.RH_GMS_USER));				
				}
				
				if (!StringUtils.startsWith(json, "{")  && layerHelper != null) {
					try {
						int limit = 50;
						if (layer.equals(Commons.HOTELS_LAYER)) {
							limit = 350;
						}
			    		List<ExtendedLandmark> landmarks = layerHelper.processBinaryRequest(lat, lng, null, 20, 1032, limit, StringUtil.getStringLengthLimit("l"), "en", null, Locale.US, true);
			    		String newkey = layerHelper.cacheGeoJson(landmarks, lat, lng, layer);                          
			    		if (newkey != null) {
			    			json = CacheUtil.getString(newkey);
			    		}
					} catch (Exception e) {
				    	logger.log(Level.SEVERE, e.getMessage(), e);
				    }
				}
			}	
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
        	if (!StringUtils.startsWith(json, "{")) {
				json = "{}";
			} else {
				try {
					//{"type":"FeatureCollection","properties":{"layer":"Layer"},"features":[]});
					JSONObject layerJson = new JSONObject(json);
					int layerSize = layerJson.getJSONArray("features").length();
					String layerName = layerJson.getJSONObject("properties").getString("layer");
					logger.log(Level.INFO, "Sending " + layerSize + " landmarks from layer " + layerName);
				} catch (Exception e) {
		            logger.log(Level.SEVERE, e.getMessage(), e);
		        }
			}
        	String callBackJavaScripMethodName = request.getParameter("callback");
        	if (StringUtils.isNotEmpty(callBackJavaScripMethodName)) {
        		json = callBackJavaScripMethodName + "("+ json + ");";
        	}
        	response.getWriter().write(json);
        	response.getWriter().close();
        }
	}
}
