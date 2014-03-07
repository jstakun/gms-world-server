/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.layers.YelpUtils;
import com.jstakun.lm.server.layers.LayerHelperFactory;
import java.util.concurrent.ThreadFactory;
import com.google.appengine.api.ThreadManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.json.JSONObject;
import java.util.logging.Logger;
import com.jstakun.lm.server.utils.GeocodeUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.ThreadUtil;
import com.jstakun.lm.server.utils.persistence.SearchPersistenceUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class Search2Servlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Search2Servlet.class.getName());
    private double latitude, longitude;
    private String query, ftoken, language;
    private int radius, dealLimit, limit, stringLimit, counter;
    private Map<String, Thread> layers;
    private Map<String, JSONObject> jsonMap;
    private List<ExtendedLandmark> foundLandmarks;
    private Locale locale;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        layers = new ConcurrentHashMap<String, Thread>();
        String format = StringUtil.getStringParam(request.getParameter("format"), "json");
        PrintWriter out = null;
        
        if (format.equals("json")) {
        	response.setContentType("text/json;charset=UTF-8");
        	out = response.getWriter();
        } else if (format.equals("bin")) {
        	response.setContentType("deflate");        
        }
        
        try {
            if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius", "query")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                counter = 0;
                latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
                longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
                query = URLDecoder.decode(request.getParameter("query"), "utf-8");
                ftoken = request.getParameter("ftoken");
                radius = NumberUtils.getRadius(request.getParameter("radius"), 10, 6371);
                language = StringUtil.getLanguage(request.getLocale().getLanguage(), "en", 2);
                dealLimit = NumberUtils.getInt(request.getParameter("dealLimit"), 300);
                limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
                int version = NumberUtils.getVersion(request.getParameter("version"), 1);
                boolean isDeal = false;
                if (StringUtils.isNotEmpty(request.getParameter("deals"))) {
                    isDeal = true;
                }
                int geocode = 0;
                if (StringUtils.isNotEmpty(request.getParameter("geocode"))) {
                    geocode = NumberUtils.getInt(request.getParameter("geocode"), 0);
                }
                locale = request.getLocale();
                
                ThreadFactory searchThreadFactory = ThreadManager.currentRequestThreadFactory();

                if (format.equals("json")) { 
                	jsonMap = new HashMap<String, JSONObject>();
                	if (!isDeal && geocode == 0) {
                		layers.put(Commons.FOURSQUARE_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.FOURSQUARE_LAYER))); //
                		layers.put(Commons.FACEBOOK_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.FACEBOOK_LAYER))); //
                		layers.put(Commons.GOOGLE_PLACES_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.GOOGLE_PLACES_LAYER))); //
                		layers.put(Commons.LM_SERVER_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.LM_SERVER_LAYER))); //
                		layers.put(Commons.FLICKR_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.FLICKR_LAYER))); //
                		layers.put(Commons.EVENTFUL_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.EVENTFUL_LAYER))); //
                		if (YelpUtils.hasNeighborhoods(latitude, longitude)) {
                			layers.put(Commons.YELP_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.YELP_LAYER))); //
                		}
                		if (version > 1082) {
                			layers.put(Commons.TWITTER_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.TWITTER_LAYER))); //
                		}
                		layers.put(Commons.MEETUP_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.MEETUP_LAYER))); //
                	}

                	if (geocode == 0 && GeocodeUtils.isNorthAmericaLocation(request.getParameter("lat"), request.getParameter("lng"))) {
                		layers.put(Commons.COUPONS_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.COUPONS_LAYER)));
                		layers.put(Commons.GROUPON_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.GROUPON_LAYER)));
                	}

                	layers.put(Commons.LOCAL_LAYER, searchThreadFactory.newThread(new JSonSearchTask(Commons.LOCAL_LAYER)));
                } else if (format.equals("bin")) {
                	foundLandmarks = new ArrayList<ExtendedLandmark>();
                	if (!isDeal && geocode == 0) {
                		layers.put(Commons.FOURSQUARE_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.FOURSQUARE_LAYER))); //
                		layers.put(Commons.FACEBOOK_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.FACEBOOK_LAYER))); //
                		layers.put(Commons.GOOGLE_PLACES_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.GOOGLE_PLACES_LAYER))); //
                		layers.put(Commons.LM_SERVER_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.LM_SERVER_LAYER))); //
                		layers.put(Commons.FLICKR_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.FLICKR_LAYER))); //
                		layers.put(Commons.EVENTFUL_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.EVENTFUL_LAYER))); //
                		if (YelpUtils.hasNeighborhoods(latitude, longitude)) {
                			layers.put(Commons.YELP_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.YELP_LAYER))); //
                		}
                		//layers.put(Commons.QYPE_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.QYPE_LAYER))); //
                		if (version > 1082) {
                			layers.put(Commons.TWITTER_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.TWITTER_LAYER))); //
                		}
                		layers.put(Commons.MEETUP_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.MEETUP_LAYER))); //
                		if (version >= 1094) {
                			layers.put(Commons.FREEBASE_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.FREEBASE_LAYER)));
                		}	
                	}

                	if (geocode == 0 && GeocodeUtils.isNorthAmericaLocation(request.getParameter("lat"), request.getParameter("lng"))) {
                		layers.put(Commons.COUPONS_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.COUPONS_LAYER)));
                		layers.put(Commons.GROUPON_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.GROUPON_LAYER)));
                	}

                	layers.put(Commons.LOCAL_LAYER, searchThreadFactory.newThread(new SerialSearchTask(Commons.LOCAL_LAYER)));    	
                }
                
                for (Iterator<String> iter = layers.keySet().iterator(); iter.hasNext();) {
                    Thread t = layers.get(iter.next());
                    t.start();
                }

                ThreadUtil.waitForLayers(layers);

                /*String username = StringUtil.getUsername(request.getAttribute("username"), request.getParameter("username"));
                boolean auth = false;
                if (StringUtils.isNotEmpty(username)) {
                    auth = true;
                }
                
                String queryString = request.getQueryString(); //GET
                if (queryString == null) {
                	//POST
                	List<String> params = new ArrayList<String>();
                	Map<String, String[]> requestParams = request.getParameterMap();
                	for (Iterator<Map.Entry<String, String[]>> iter = requestParams.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry<String, String[]> entry = iter.next();
                	    String[] value = entry.getValue();
                	    if (value.length > 0) {
                	    	params.add(entry.getKey() + "=" + value[0]);
                	    }
                	}    
                	queryString = StringUtils.join(params, "&");
                }
                
                String requestUri = request.getRequestURI() + "?" + queryString;              
                SearchPersistenceUtils.persistSearch(username, requestUri, auth, latitude, longitude, radius, query, language, counter);
                MailUtils.sendSearchQueryNotification(query, isDeal, counter, requestUri);*/
                
                logger.log(Level.INFO, "Found {0} landmarks", counter);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (format.equals("json")) {
        		try {
        			JSONObject responseJson = new JSONObject().put("ResultSet", jsonMap);
        			out.print(responseJson);
        		} catch (Exception e) {
        			logger.log(Level.SEVERE, e.getMessage(), e);
        		}
        		out.close();
        	} else if (format.equals("bin")) {
            	LayerHelperFactory.getSearchUtils().serialize(foundLandmarks, response.getOutputStream(), 12);
        	}
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Search2 servlet";
    }// </editor-fold>

    private class JSonSearchTask implements Runnable {

        private String layer = null;
     
        public JSonSearchTask(String layer) {
            this.layer = layer;
        }

        @Override
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
                    json = LayerHelperFactory.getFlickrUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
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
                counter += JSONUtils.addJSONObjectToResultMap(jsonMap, layer, json, true);
                layers.remove(layer);
            }
        }
    }
    
    private class SerialSearchTask implements Runnable {

        private String layer = null;
     
        public SerialSearchTask(String layer) {
            this.layer = layer;
        }

		@Override
		public void run() {
			logger.log(Level.INFO, "Processing search in layer {0}", layer);
            List<ExtendedLandmark> landmarks = null;
			
            try {
            	if (layer.equals(Commons.COUPONS_LAYER)) {
                    landmarks = LayerHelperFactory.getCouponsUtils().processBinaryRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, "", language, locale);
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
                	landmarks = LayerHelperFactory.getGrouponUtils().processBinaryRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, null, null, locale);
                } else if (layer.equals(Commons.FOURSQUARE_LAYER)) {
                    landmarks = LayerHelperFactory.getFoursquareUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, 3, limit, stringLimit, "browse", language, locale);
                } else if (layer.equals(Commons.FACEBOOK_LAYER)) {
                	landmarks = LayerHelperFactory.getFacebookUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, ftoken, null, locale);
                } else if (layer.equals(Commons.GOOGLE_PLACES_LAYER)) {
                    landmarks = LayerHelperFactory.getGooglePlacesUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, language, null, locale);
                } else if (layer.equals(Commons.LM_SERVER_LAYER)) {
                	landmarks = LayerHelperFactory.getGmsUtils().processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale);
                } else if (layer.equals(Commons.FLICKR_LAYER)) {
                	landmarks = LayerHelperFactory.getFlickrUtils().processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale);
                } else if (layer.equals(Commons.EVENTFUL_LAYER)) {
                	landmarks = LayerHelperFactory.getEventfulUtils().processBinaryRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null, locale);
                } else if (layer.equals(Commons.YELP_LAYER)) {
                	landmarks = LayerHelperFactory.getYelpUtils().processBinaryRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, "false", language, locale);
                } else if (layer.equals(Commons.QYPE_LAYER)) {
                	landmarks = LayerHelperFactory.getQypeUtils().processBinaryRequest(latitude, longitude, query, radius, 2, limit, stringLimit, null, null, locale);
                } else if (layer.equals(Commons.TWITTER_LAYER)) {
                	landmarks = LayerHelperFactory.getTwitterUtils().processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale);
                } else if (layer.equals(Commons.MEETUP_LAYER)) {
                	landmarks = LayerHelperFactory.getMeetupUtils().processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale);
                } else if (layer.equals(Commons.FREEBASE_LAYER)) {
                	landmarks = LayerHelperFactory.getFreebaseUtils().processBinaryRequest(latitude, longitude, query, radius, 1, limit, stringLimit, language, null, locale);
                } 
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } finally {
            	if (landmarks != null && !landmarks.isEmpty()) {
            		counter += landmarks.size();
            		foundLandmarks.addAll(landmarks);
            	}
                layers.remove(layer);
            }		
		}
    }    
}
