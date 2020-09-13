package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;

/**
 * Servlet implementation class LandmarkRedirectServlet
 */
public class LandmarkRedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(LandmarkRedirectServlet.class.getName());
    
	private static final String[] remote = new String[]{Commons.LOCAL_LAYER, Commons.MY_POSITION_LAYER,
            Commons.MC_ATM_LAYER, Commons.OSM_ATM_LAYER, Commons.OSM_PARKING_LAYER};
	
	private static final Map<String, String> LAYER_HOSTNAME = new HashMap<String, String>();
	
	static {
		LAYER_HOSTNAME.put(Commons.FOURSQUARE_MERCHANT_LAYER, "foursquare.com");
		LAYER_HOSTNAME.put(Commons.HOTELS_LAYER, "www.booking.com");
		LAYER_HOSTNAME.put("4d4b7105d754a06375d81259", "bit.ly");
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

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		    throws ServletException, IOException {
		try {
            if (HttpUtils.isEmptyAny(request, "layer", "lat", "lng")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
            	final String layer = request.getParameter("layer");
            	final String lat = request.getParameter("lat");
            	final String lng = request.getParameter("lng");
            	String url = request.getParameter("url");
	        
            	if (url == null || StringUtils.indexOfAny(layer, remote) >= 0) {
            		url = String.format("%sshowLocation/%s/%s", ConfigurationManager.SERVER_URL, lat, lng);
            	} else {
            		try {
            			final URL redirectURL = new URL(url);
            			final String hostname = redirectURL.getHost();
            	
            			if (StringUtils.containsIgnoreCase(hostname, layer) || (LAYER_HOSTNAME.containsKey(layer) && StringUtils.equals(LAYER_HOSTNAME.get(layer), hostname))) {
            				logger.log(Level.INFO, "Hostname " + hostname + " matched for layer " + layer);
            			} else {
            				logger.log(Level.SEVERE, "Suspicious url " + hostname + " for layer " + layer);
            			}
            		} catch (Exception e) {
            			logger.log(Level.SEVERE, "Invalid url " + url);
            		}
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
