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
public class Panoramio2ProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Panoramio2ProviderServlet.class.getName());

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
            if (HttpUtils.isEmptyAny(request, "minx", "miny", "maxx", "maxy")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                double miny = GeocodeUtils.getLatitude(request.getParameter("miny"));
                double minx = GeocodeUtils.getLongitude(request.getParameter("minx"));
                double maxy = GeocodeUtils.getLatitude(request.getParameter("maxy"));
                double maxx = GeocodeUtils.getLongitude(request.getParameter("maxx"));

                String bbox = "minx=" + minx + "&miny=" + miny + "&maxx=" + maxx + "&maxy=" + maxy;

                double latitude = (miny + maxy) / 2;
                double longitude = (minx + maxy) / 2;

                int version = NumberUtils.getVersion(request.getParameter("version"), 2);
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));

                
                String output = LayerHelperFactory.getPanoramioUtils().processRequest(latitude, longitude, null, 0, version, limit, stringLimit, bbox, null).toString();
                        
                out.print(output);
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
        return "Short description";
    }// </editor-fold>
}
