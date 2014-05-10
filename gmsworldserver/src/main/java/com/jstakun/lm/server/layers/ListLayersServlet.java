/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import com.jstakun.lm.server.persistence.Layer;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;
import com.jstakun.lm.server.utils.xml.XMLUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jstakun
 */
public class ListLayersServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ListLayersServlet.class.getName());
    private static final String XML_KEY = "customXmlLayersList";
    //private static final String JSON_KEY = "customJsonLayersList";
    private static final String JSON_LAYER_LIST = "jsonLayersList";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String format = StringUtil.getStringParam(request.getParameter("format"), "xml");
        if (format.equals("json")) {
            response.setContentType("text/json;charset=UTF-8");
        } else if (format.equals("xml")) {
            response.setContentType("text/xml;charset=UTF-8");
        }

        PrintWriter out = response.getWriter();

        double latitudeMin = GeocodeUtils.getLatitude(request.getParameter("latitudeMin"));
        double longitudeMin = GeocodeUtils.getLongitude(request.getParameter("longitudeMin"));
        double latitudeMax = GeocodeUtils.getLatitude(request.getParameter("latitudeMax"));
        double longitudeMax = GeocodeUtils.getLongitude(request.getParameter("longitudeMax"));
        int version = NumberUtils.getVersion(request.getParameter("version"), 1);
        int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);

        if (format.equals("json")) {
            String key = JSON_LAYER_LIST + "_" + StringUtil.formatCoordE2(latitudeMin) + "_" + StringUtil.formatCoordE2(longitudeMin) + "_" + radius;

            if (version == 1) {
                key += "_" + StringUtil.formatCoordE2(latitudeMax) + "_" + StringUtil.formatCoordE2(longitudeMax);
            }
            String json = CacheUtil.getString(key);
            if (json == null) {
                List<Layer> layerList = LayerPersistenceUtils.listAllLayers(2);
                try {
                    if (version == 1) {
                        json = JSONUtils.createCustomJSonLayersList(layerList, latitudeMin, longitudeMin, latitudeMax, longitudeMax);
                    } else {
                        json = JSONUtils.createCustomJSonLayersList(layerList, latitudeMin, longitudeMin, radius * 1000);
                    }
                    if (json != null) {
                        CacheUtil.put(key, json);
                        logger.log(Level.INFO, "Adding layer landmark to cache with key {0}", key);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            } else {
                logger.log(Level.INFO, "Reading layer list from cache with key {0}", key);
            }
            if (json != null) {
            	//logger.log(Level.INFO, json);
                out.print(json);
            } else {
                out.print("{ResultSet:[]}");
            }
        } else if (format.equals("xml")) {
            String xml = CacheUtil.getString(XML_KEY);
            if (xml == null) {
                try {
                    List<Layer> layerList = new ArrayList<Layer>();
                    layerList = LayerPersistenceUtils.listAllLayers(1);
                    xml = XMLUtils.createCustomXmlLayersList(layerList);
                    CacheUtil.put(XML_KEY, xml);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            out.print(xml);
        }


        out.close();
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
        return "List Layers Servlet";
    }// </editor-fold>
}
