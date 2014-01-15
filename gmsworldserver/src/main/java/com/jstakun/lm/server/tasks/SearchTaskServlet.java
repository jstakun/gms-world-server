/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.tasks;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.layers.LayerHelperFactory;
import com.jstakun.lm.server.layers.YelpUtils;
import com.jstakun.lm.server.utils.GeocodeUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class SearchTaskServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SearchTaskServlet.class.getName());
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius", "query", "layer", "cacheLabel")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String layer = request.getParameter("layer");
                String cacheLabel = request.getParameter("cacheLabel");
                double latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
                double longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
                String query = URLDecoder.decode(request.getParameter("query"), "UTF-8");
                String ftoken = request.getParameter("ftoken");
                int radius = NumberUtils.getRadius(request.getParameter("radius"), 10, 6371);
                String language = StringUtil.getLanguage(request.getParameter("language"), "en", 2);
                int dealLimit = NumberUtils.getInt(request.getParameter("dealLimit"), 300);
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
                //int version = NumberUtils.getVersion(request.getParameter("version"), 1);
                //String country = request.getParameter("country");

                JSONObject json = null;

                logger.log(Level.INFO, "Processing search in layer {0}", layer);

                if (layer.equals(Commons.COUPONS_LAYER)) {
                    if (GeocodeUtils.isNorthAmericaLocation(request.getParameter("lat"), request.getParameter("lng"))) {
                        json = LayerHelperFactory.getCouponsUtils().processRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, null, null);
                    } else {
                        json = new JSONObject().put("ResultSet", new HashMap<String, Object>());
                    }
                } else if (layer.equals(Commons.LOCAL_LAYER)) {
                    json = GeocodeUtils.geocodeToJSonObject(query, GeocodeUtils.processRequest(query, null, request.getLocale(), true));
                } else if (layer.equals(Commons.GROUPON_LAYER)) {
                    if (GeocodeUtils.isNorthAmericaLocation(request.getParameter("lat"), request.getParameter("lng"))) {
                        json = LayerHelperFactory.getGrouponUtils().processRequest(latitude, longitude, query, radius, 4, dealLimit, stringLimit, null, null);
                    } else {
                        json = new JSONObject().put("ResultSet", new HashMap<String, Object>());
                    }
                } else if (layer.equals(Commons.FOURSQUARE_LAYER)) {
                    json = LayerHelperFactory.getFoursquareUtils().processRequest(latitude, longitude, query, radius * 1000, 3, limit, stringLimit, "browse", language);
                } else if (layer.equals(Commons.FACEBOOK_LAYER)) {
                    json = LayerHelperFactory.getFacebookUtils().processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, ftoken, null);
                } else if (layer.equals(Commons.GOOGLE_PLACES_LAYER)) {
                    json = LayerHelperFactory.getGooglePlacesUtils().processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, language, null);
                } else if (layer.equals(Commons.LM_SERVER_LAYER)) {
                    json = LayerHelperFactory.getGmsUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
                } else if (layer.equals(Commons.YOUTUBE_LAYER)) {
                    json = LayerHelperFactory.getYoutubeUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
                //} else if (layer.equals(Commons.UPCOMING_LAYER)) {
                //    json = LayerHelperFactory.getUpcomingUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
                } else if (layer.equals(Commons.FLICKR_LAYER)) {
                    json = LayerHelperFactory.getFlickrUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
                } else if (layer.equals(Commons.EVENTFUL_LAYER)) {
                    json = LayerHelperFactory.getEventfulUtils().processRequest(latitude, longitude, query, radius, 4, limit, stringLimit, null, null);
                } else if (layer.equals(Commons.YELP_LAYER)) {
                    if (YelpUtils.hasNeighborhoods(latitude, longitude)) {
                        json = LayerHelperFactory.getYelpUtils().processRequest(latitude, longitude, query, radius * 1000, 2, limit, stringLimit, "false", null);
                    } else {
                        json = new JSONObject().put("ResultSet", new HashMap<String, Object>());
                    }
                } else if (layer.equals(Commons.QYPE_LAYER)) {
                    json = LayerHelperFactory.getQypeUtils().processRequest(latitude, longitude, query, radius, 2, limit, stringLimit, null, null);
                }

                if (json != null) {
                    CacheUtil.updateJSONObjectHashMap(cacheLabel, layer, json);
                    logger.log(Level.INFO, "Layer {0} saved", layer);
                } else {
                    logger.log(Level.WARNING, "Layer {0} is null", layer);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            out.close();
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
        return "Short description";
    }// </editor-fold>
}
