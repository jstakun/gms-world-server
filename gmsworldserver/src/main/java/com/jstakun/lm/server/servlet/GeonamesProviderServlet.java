/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import com.jstakun.lm.server.layers.LayerHelperFactory;
import com.jstakun.lm.server.utils.GeocodeUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;

import java.io.IOException;
import java.io.PrintWriter;
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
@Deprecated
public class GeonamesProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private static final Logger logger = Logger.getLogger(GeonamesProviderServlet.class.getName());
    
    //http://api.geonames.org/findNearbyWikipediaJSON?lat=52.25&lng=20.95&maxRows=50&radius=10&username=gms&lang=pl
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                double lat = GeocodeUtils.getLatitude(request.getParameter("latitude"));
                double lng = GeocodeUtils.getLongitude(request.getParameter("longitude"));
                int radius = NumberUtils.getRadius(request.getParameter("radius"), 1, 20);
                String lang = StringUtil.getLanguage(request.getLocale().getLanguage(), "en", 2);
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                int version = NumberUtils.getVersion(request.getParameter("version"), 1);
                int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));

                String output = LayerHelperFactory.getGeonamesUtils().processRequest(lat, lng, null, radius, version, limit, stringLimit, lang, null).toString();

                out.println(output);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        return "Geonames Provider Servlet";
    }// </editor-fold>
}
