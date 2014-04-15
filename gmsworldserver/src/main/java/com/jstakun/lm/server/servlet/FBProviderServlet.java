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
import com.restfb.exception.FacebookOAuthException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
@Deprecated
public class FBProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FBProviderServlet.class.getName());
    
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

            if (HttpUtils.isEmptyAny(request,"lat","lng","distance")) { //,"token")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                double latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
                double longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
                String token = null;
                if (StringUtils.isNotEmpty(request.getParameter("token"))) {
                   token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                }
                String query = request.getParameter("q");
                int radius = NumberUtils.getRadius(request.getParameter("distance"), 1000, 6371*1000);
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
                int version = NumberUtils.getVersion(request.getParameter("version"), 1);

                String json = LayerHelperFactory.getFacebookUtils().processRequest(latitude, longitude, query, radius, version, limit, stringLimit, token, null).toString();

                out.print(json);
            }

        } catch (FacebookOAuthException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            out.print("{\"error\":{\"message\":\"Facebook authentication error\"}}");
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
