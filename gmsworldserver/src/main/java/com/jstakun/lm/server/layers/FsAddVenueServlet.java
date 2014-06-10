package com.jstakun.lm.server.layers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.utils.HttpUtils;

/**
 * Servlet implementation class FsAddVenueServlet
 */
public class FsAddVenueServlet extends HttpServlet {
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
    	
    	if (StringUtils.equals(service, "fs")) {
    		if (!HttpUtils.isEmptyAny(request, "accessToken", "name", "catId", "ll")) {
    			String accessToken = request.getParameter("accessToken");
    			String name = request.getParameter("name");
    			String desc = request.getParameter("desc");
    			String catId = request.getParameter("catId");
    			String ll = request.getParameter("ll");
    			int responseCode = FoursquareUtils.addVenue(accessToken, name, desc, catId, ll);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				response.sendError(responseCode);
    			} else {
    				//TODO remove foursquare layer from cache
    				//com.jstakun.lm.server.layers.FoursquareUtils_processBinaryRequest_52.24_20.96_11000_12_93_256_checkin_pl
    			}
    		} else {
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		}
    	} else {
    		Logger.getLogger(FsAddVenueServlet.class.getName()).log(Level.SEVERE, "Wrong service: " + service);
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
