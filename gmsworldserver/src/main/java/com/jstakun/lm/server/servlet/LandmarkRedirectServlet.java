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
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.HttpUtils;

/**
 * Servlet implementation class LandmarkRedirectServlet
 */
public class LandmarkRedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(LandmarkRedirectServlet.class.getName());
    
	private static final String[] remote = new String[]{Commons.LOCAL_LAYER, Commons.MY_POSITION_LAYER,
            Commons.MC_ATM_LAYER, Commons.OSM_ATM_LAYER, Commons.OSM_PARKING_LAYER};
    
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

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		    throws ServletException, IOException {
		try {
            if (HttpUtils.isEmptyAny(request, "layer", "lat", "lng")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
            	String layer = request.getParameter("layer");
            	String lat = request.getParameter("lat");
            	String lng = request.getParameter("lng");
            	String url = request.getParameter("url");
	        
            	if (url == null || StringUtils.indexOfAny(layer, remote) >= 0) {
            		url = String.format("%sshowLocation.do?lat=%s&lon=%s", ConfigurationManager.SERVER_URL, lat, lng);
            	}
            	
            	if (!url.startsWith("http")) {
            		url = "http://" + url;
            	}
            	
            	logger.log(Level.INFO, "Layer: " + layer + ", redirecting to: " + url);
            	
            	response.sendRedirect(url);
            }
		} catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }   
	}
}
