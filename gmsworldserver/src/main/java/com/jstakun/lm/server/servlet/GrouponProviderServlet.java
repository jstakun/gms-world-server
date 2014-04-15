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

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
@Deprecated
public class GrouponProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GrouponProviderServlet.class.getName());
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    //http://api.groupon.com/v2/deals.json?lat=52.25&lng=20.95&radius=10&client_id=<id>&force_http_success=true
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (HttpUtils.isEmptyAny(request, "lat", "lng", "radius")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String resp = "{ResultSet:[]}";
                if (GeocodeUtils.isNorthAmericaLocation(request.getParameter("lat"), request.getParameter("lng"))) {
                    double lat = GeocodeUtils.getLatitude(request.getParameter("lat"));
                    double lng = GeocodeUtils.getLongitude(request.getParameter("lng"));
                    int dealLimit = NumberUtils.getInt(request.getParameter("dealLimit"), 300);
                    int radius = NumberUtils.getRadius(request.getParameter("radius"), 1, 1000);
                    int version = NumberUtils.getVersion(request.getParameter("version"), 1);
                    int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));

                    String categoryid = null;
                    if (StringUtils.isNotEmpty(request.getParameter("categoryid"))) {
                       categoryid = request.getParameter("categoryid");
                    }

                    //email_address this can be used to load targeted deal content for a user
                    resp = LayerHelperFactory.getGrouponUtils().processRequest(lat, lng, null, radius, version, dealLimit, stringLimit, categoryid, null).toString();
                }

                out.print(resp);
                //out.print(grouponResponse);
            }
        } catch (IllegalArgumentException iae) {
            logger.log(Level.WARNING, iae.getMessage(), iae);
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
        return "Groupon Provider Servlet";
    }// </editor-fold>
}
