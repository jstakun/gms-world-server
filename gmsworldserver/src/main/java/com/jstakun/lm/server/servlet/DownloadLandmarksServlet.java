/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import com.jstakun.lm.server.layers.GMSUtils;
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
public class DownloadLandmarksServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DownloadLandmarksServlet.class.getName());

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
        if (format.equals("kml")) {
            response.setContentType("text/kml;charset=UTF-8");
        } else if (format.equals("json")) {
            response.setContentType("text/json;charset=UTF-8");
        } else {
            response.setContentType("text/xml;charset=UTF-8");
        }
        PrintWriter out = response.getWriter();
        String results = null;

        try {
            //http://localhost:8080/services/downloadLandmark?latitudeMin=0.0&latitudeMax=100.0&longitudeMin=0.0&longitudeMax=100.0&layer=Public&format=kml
            if (HttpUtils.isEmptyAny(request, "latitudeMin", "longitudeMin")) { //, "latitudeMax", "longitudeMax")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                double latitudeMin = GeocodeUtils.getLatitude(request.getParameter("latitudeMin"));
                double longitudeMin = GeocodeUtils.getLongitude(request.getParameter("longitudeMin"));
                double latitudeMax = GeocodeUtils.getLatitude(request.getParameter("latitudeMax"));
                double longitudeMax = GeocodeUtils.getLongitude(request.getParameter("longitudeMax"));
                String layer = StringUtil.getStringParam(request.getParameter("layer"), "Public");
                int version = NumberUtils.getVersion(request.getParameter("version"), 1);
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
                int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);

                if (version > 4) {
                    results = LayerHelperFactory.getGmsUtils().processRequest(latitudeMin, longitudeMin, null, radius * 1000, version, limit, stringLimit, layer, null).toString();
                } else {
                    results = GMSUtils.processRequest(latitudeMin, longitudeMin, latitudeMax, longitudeMax, version, limit, stringLimit, layer, format);
                }

                if (results != null) {
                    out.print(results);
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
        return "Download Landmark Servlet";
    }// </editor-fold>
}
