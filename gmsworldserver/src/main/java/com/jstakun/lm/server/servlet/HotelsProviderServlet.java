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
public class HotelsProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(HotelsProviderServlet.class.getName());

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin")) { //, "latitudeMax", "longitudeMax")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                double latitudeMin = GeocodeUtils.getLatitude(request.getParameter("latitudeMin"));
                double longitudeMin = GeocodeUtils.getLongitude(request.getParameter("longitudeMin"));
                double latitudeMax = GeocodeUtils.getLatitude(request.getParameter("latitudeMax"));
                double longitudeMax = GeocodeUtils.getLongitude(request.getParameter("longitudeMax"));
                String language = StringUtil.getLanguage(request.getParameter("lang"), "en", 2);
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30); //max 500
                int version = NumberUtils.getVersion(request.getParameter("version"), 1);
                int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);

                String flex2 = null;

                if (version <= 2) {
                    flex2 = Double.toString(latitudeMax) + "_" + Double.toString(longitudeMax);
                }

                String output = LayerHelperFactory.getHotelsCombinedUtils().processRequest(latitudeMin, longitudeMin, null, radius, version, limit, limit, language, flex2).toString();

                out.print(output);
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
