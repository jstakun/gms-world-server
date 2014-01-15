package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.layers.FacebookUtils;
import com.jstakun.lm.server.layers.FoursquareUtils;
import com.jstakun.lm.server.layers.GooglePlacesUtils;
import com.jstakun.lm.server.utils.HttpUtils;

/**
 * Servlet implementation class SocialCheckinServlet
 */

public final class SocialCheckinServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 private static final Logger logger = Logger.getLogger(SocialCheckinServlet.class.getName());

	 /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	String service = request.getParameter("service");
    	
    	logger.log(Level.INFO, "Checkin to social network: " + service);
    	
    	if (StringUtils.equals(service, Commons.FOURSQUARE)) {
    		if (!HttpUtils.isEmptyAny(request, "accessToken", "venueId", "name")) {
    			String accessToken = request.getParameter("accessToken");
    			String venueId = request.getParameter("venueId");
    			String name = request.getParameter("name");
    			int responseCode = FoursquareUtils.checkin(accessToken, venueId, name);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				response.sendError(responseCode);
    			}
    		} else {
    			//response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		}
    	} else if (StringUtils.equals(service, Commons.FACEBOOK)) { 
    		if (!HttpUtils.isEmptyAny(request, "accessToken", "venueId", "name")) {
    			String accessToken = request.getParameter("accessToken");
    			String venueId = request.getParameter("venueId");
    			String name = request.getParameter("name");
    			
    			int responseCode = FacebookUtils.checkin(accessToken, venueId, name);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				response.sendError(responseCode);
    			}
    		} else {
    			//response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		}   
    	} else if (StringUtils.equals(service, Commons.GOOGLE_BLOGGER) || StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		String reference = request.getParameter("reference");
    		if (StringUtils.isNotEmpty(reference)) {
    			int responseCode = GooglePlacesUtils.checkin(reference);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				response.sendError(responseCode);
    			}
    		} else {
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		} 
    	} else {
    		Logger.getLogger(SocialCheckinServlet.class.getName()).log(Level.SEVERE, "Wrong service: " + service);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

}
