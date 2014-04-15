/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

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
public class UpcomingProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(UpcomingProviderServlet.class.getName());

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    //http://upcoming.yahooapis.com/services/rest/?method=event.search&api_key=<key>&location=52.25,20.95&radius=10&format=json
    ///upcomingProvider?latitude=40.71455&longitude=-74.007118&radius=10&version=3
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /*if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "radius")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                //Will work only until 30-Apr-2013
                if (System.currentTimeMillis() < 1335844800000L) {
                    double latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
                    double longitude = GeocodeUtils.getLongitude(request.getParameter("longitude"));
                    int radius = NumberUtils.getRadius(request.getParameter("radius"), 1, 6371);
                    int version = NumberUtils.getVersion(request.getParameter("version"), 2);
                    int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                    int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));

                    String resp = LayerHelperFactory.getUpcomingUtils().processRequest(latitude, longitude, null, radius, version, limit, stringLimit, null, null).toString();

                    out.println(resp);
                } else {
                    out.print("{\"ResultSet\":[]}");
                }
            }*/
            out.print("{\"ResultSet\":[]}");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
