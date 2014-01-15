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
import com.jstakun.lm.server.utils.HttpUtils;

/**
 * Servlet implementation class SocialCommentServlet
 */
public class SocialCommentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
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
    	
    	if (StringUtils.equals(service, Commons.FOURSQUARE)) {
    		if (!HttpUtils.isEmptyAny(request, "accessToken", "venueId", "text")) {
    			String accessToken = request.getParameter("accessToken");
    			String venueId = request.getParameter("venueId");
    			String text = request.getParameter("text");
    			int responseCode = FoursquareUtils.sendTip(accessToken, venueId, text);
    			//Logger.getLogger(SocialCommentServlet.class.getName()).log(Level.INFO, "FS response is: " + responseCode);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				response.sendError(responseCode);
    			}
    		} else {
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		}
    	} else if (StringUtils.equals(service, Commons.FACEBOOK)) {
    		if (!HttpUtils.isEmptyAny(request, "accessToken", "venueId", "text", "name")) {
    			String accessToken = request.getParameter("accessToken");
    			String venueId = request.getParameter("venueId");
    			String text = request.getParameter("text");
    			String name = request.getParameter("name");
    			int responseCode = FacebookUtils.sendComment(accessToken, venueId, text, name);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				response.sendError(responseCode);
    			}
    		} else {
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		}
    	} else {
    		Logger.getLogger(SocialCommentServlet.class.getName()).log(Level.SEVERE, "Wrong service: " + service);
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
