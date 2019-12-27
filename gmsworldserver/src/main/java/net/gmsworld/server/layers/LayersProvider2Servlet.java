package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
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
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import twitter4j.TwitterException;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.utils.GoogleThreadProvider;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.restfb.exception.FacebookOAuthException;

import fi.foyt.foursquare.api.FoursquareApiException;

/**
 * Servlet implementation class LayersProvider2Servlet
 */
public class LayersProvider2Servlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LayersProvider2Servlet.class.getName());
	private static enum Format {BIN, XML, KML, JSON};
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LayersProvider2Servlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LayerHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
        LayerHelperFactory.getInstance().setThreadProvider(new GoogleThreadProvider());
    }
       
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	 String formatParam = StringUtil.getStringParam(request.getParameter("format"), "json");
         Format outFormat = null;
         PrintWriter outPrinter = null;
         String outString = null;
         int version = NumberUtils.getVersion(request.getParameter("version"), 1);

         if (formatParam.equals("kml")) {
             response.setContentType("text/kml;charset=UTF-8");
             outPrinter = response.getWriter();
             outString = "<kml/>";
             outFormat = Format.KML; 
         } else if (formatParam.equals("xml")) {
             response.setContentType("text/xml;charset=UTF-8");
             outPrinter = response.getWriter();
             outString = "<results/>";
             outFormat = Format.XML;
         } else if (formatParam.equals("bin")) {
         	if (version >= 12) {
         		response.setContentType("deflate");
         	} else {
         		//version = 11; //this will use only serialization
         		response.setContentType("application/x-java-serialized-object"); 
         	}
             outFormat = Format.BIN;
         } else {
             response.setContentType("text/json;charset=UTF-8");
             outPrinter = response.getWriter();
             outString = "{ResultSet:[]}";
             outFormat = Format.JSON;
         }
         
         try {             
             if (HttpUtils.isEmptyAny(request, "lat", "lng") && HttpUtils.isEmptyAny(request, "latitude", "longitude")) {
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             } else {
            	 Double latitude;
                 if (request.getParameter("lat") != null) {
                     latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
                 } else {
                     latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
                 }

                 Double longitude;
                 if (request.getParameter("lng") != null) {
                     longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
                 } else {
                     longitude = GeocodeUtils.getLongitude(request.getParameter("longitude"));
                 }

                 Locale l = request.getLocale();
                 String language;
                 if (request.getParameter("lang") != null) {
                     language = StringUtil.getLanguage(request.getParameter("lang"), "en", 2);
                 } else if (request.getParameter("language") != null) {
                     language = StringUtil.getLanguage(request.getParameter("language"), "en", 2);
                 } else {
                     language = StringUtil.getLanguage(l.getLanguage(), "en", 2);
                 }
                 String locale = StringUtil.getLanguage(l.getLanguage() + "_" + l.getCountry(), "en_US", 5);

                 if (latitude == null) {
                 	latitude = GeocodeUtils.getLatitude(request.getParameter("latitudeMin"));
                 }
                 if (longitude == null) {
                 	longitude = GeocodeUtils.getLongitude(request.getParameter("longitudeMin"));
                 }
                 
                 //String layer = StringUtil.getStringParam(request.getParameter("layer"), "Public");
                 int radiusInKm = NumberUtils.getRadius(request.getParameter("radius"), 3, 100);
                 int radiusInMeters = radiusInKm * 1000;
                 int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                 //int dealLimit = NumberUtils.getInt(request.getParameter("dealLimit"), 300);
                 int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
                 
                 String uri = request.getRequestURI();
                 
                 String flexString = null;
                 String flexString2 = null;
                 String query = null;
                 boolean useCache = true;
                 int radius = radiusInMeters;

            	 LayerHelper layerHelper = LayerHelperFactory.getInstance().getByURI(uri);
            	 if (layerHelper != null && latitude != null && longitude != null) {
            		 //set layer specific params here
            		 //flexString, flexString2, query, useCache, radius, ...
            		 if (StringUtils.equals(layerHelper.getLayerName(), Commons.FACEBOOK_LAYER)) {
            			 if (request.getParameter("distance") != null) {
                     		radius = NumberUtils.getRadius(request.getParameter("distance"), 1, 6371) * 1000; //meters
                     	}
                     	 query = request.getParameter("q");
                         if (StringUtils.isNotEmpty(request.getParameter("token"))) {
                             flexString = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                         }
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.FOURSQUARE_LAYER)) {
            			 flexString = "checkin"; 
            			 flexString2 = language;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.YELP_LAYER)) {
            			 radius = NumberUtils.getRadius(request.getParameter("radius"), 1000, 40000);
                         int deals = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), 0);
                         flexString = "false";
                         if (deals == 1) {
                        	 flexString = "true";
                         }
                         flexString2 = locale;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.GOOGLE_PLACES_LAYER)) {
            			 flexString = language;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.COUPONS_LAYER)) {
            			 radius = radiusInKm;
            			 flexString = "";
                         if (StringUtils.isNotEmpty(request.getParameter("categoryid"))) {
                        	 flexString = request.getParameter("categoryid");
                         }
            			 flexString2 = language;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.GROUPON_LAYER)) {
            			 radius = radiusInKm;
            			 flexString = "";
                         if (StringUtils.isNotEmpty(request.getParameter("categoryid"))) {
                        	 flexString = request.getParameter("categoryid");
                         }
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.MC_ATM_LAYER)) {
            			 radius = radiusInKm;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.MEETUP_LAYER)) {
            			 radius = radiusInKm;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.YOUTUBE_LAYER)) {
            			 radius = radiusInKm;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.WIKIPEDIA_LAYER)) {
            			 radius = radiusInKm;
            			 flexString = language;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.WEBCAM_LAYER)) {
            			 radius = radiusInKm;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.FOURSQUARE_MERCHANT_LAYER)) {
            			 if (StringUtils.isNotEmpty(request.getParameter("token"))) {
                             flexString = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                         }
            			 flexString2 = request.getParameter("categoryid");
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.EXPEDIA_LAYER)) {
            			 radius = radiusInKm;
            			 flexString = locale;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.TWITTER_LAYER)) {
            			 radius = radiusInKm;
            			 flexString = language;
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.PANORAMIO_LAYER)) {
            			 if (HttpUtils.isEmptyAny(request, "minx", "miny", "maxx", "maxy")) {
                             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                         } else {
                             Double miny = GeocodeUtils.getLatitude(request.getParameter("miny"));
                             Double minx = GeocodeUtils.getLongitude(request.getParameter("minx"));
                             Double maxy = GeocodeUtils.getLatitude(request.getParameter("maxy"));
                             Double maxx = GeocodeUtils.getLongitude(request.getParameter("maxx"));
                             
                             //minx, miny, maxx, maxy -> minimum longitude, latitude, maximum longitude and latitude,
                             
                             if ((minx == 0d && maxx == 0d && miny == 0d && maxy == 0d) ||
                             	(miny == 85.05d && maxy == 85.05d && minx == -180d && maxx == -180d)) {
                     			logger.log(Level.WARNING, "Bounding box is zero. Changing to latitude and longitude");
                     			minx = maxx = longitude;
                     			miny = maxy = latitude;
                     		 }
                             
                             if (Math.abs(maxx - minx) < 0.1) {
                     			if (minx > -180d) {
                     				minx -= 0.1;
                     			}
                     			if (maxx < 180d) {
                     				maxx += 0.1;
                     			}
                     		}
                     		if (Math.abs(maxy - miny) < 0.1) {
                     			if (miny > -90d) {
                     				miny -= 0.1;
                     			}
                     			if (maxy < 90d) {
                     				maxy += 0.1;
                     			}
                     		}

                            flexString = "minx=" + minx + "&miny=" + miny + "&maxx=" + maxx + "&maxy=" + maxy;
                         }     
            		 } else if (StringUtils.equals(layerHelper.getLayerName(), Commons.HOTELS_LAYER)) {
            			 flexString = language;
            			 useCache = false;
            		 } 
            		 //not for layer Flickr, Public, Eventful, OSM, Hotels, 
            		 
            		 if (outFormat.equals(Format.BIN)) {
                 		List<ExtendedLandmark> landmarks = layerHelper.processBinaryRequest(latitude, longitude, query, radius, version, limit, stringLimit, flexString, flexString2, l, useCache);               	
                 		layerHelper.serialize(landmarks, response.getOutputStream(), version);
                 		layerHelper.cacheGeoJson(landmarks, latitude, longitude, layerHelper.getLayerName(), l, null);
                 	} else {
                 		outString = layerHelper.processRequest(latitude, longitude, query, radius, version, limit, stringLimit, flexString, flexString2).toString();
                 	}	
            	 }
             }
             
         } catch (FacebookOAuthException e) {
             logger.log(Level.SEVERE, e.getMessage(), e);
             if (outPrinter != null) {
             	outString = "{\"error\":{\"message\":\"Facebook authentication error\"}}";
             } else {
             	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
             }
         } catch (TwitterException e) {
         	logger.log(Level.SEVERE, e.getMessage(), e);
         	if (e.getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
         		if (outPrinter != null) {
                 	outString = "{\"error\":{\"message\":\"Twitter authentication error\"}}";
                 } else {
                 	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                 }
         	}
         } catch (FoursquareApiException e) {
         	if (StringUtils.equals(e.getMessage(), "Unauthorized")) {
         		if (outPrinter != null) {
                 	outString = "{\"error\": {\"message\": \"Foursquare authentication error\"}}";
                 } else {
                 	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                 }
         	}
         	logger.log(Level.SEVERE, e.getMessage(), e);
         } catch (Exception e) {
             logger.log(Level.SEVERE, e.getMessage(), e);
         } finally {
         	if (outFormat.equals(Format.BIN)) {
         		//do nothing
         	} else {
         		if (outString == null && outFormat.equals(Format.JSON)) {
         			 outString = "{ResultSet:[]}";
         		}
         		if (outPrinter != null) {
         			if ( outString != null) {
         				outPrinter.print(outString);
         			}
         			outPrinter.close();
         		}
         	} 
         }    
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
	
	/** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Layers provider 2 servlet";
    }

}
