package com.jstakun.lm.server.social;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.utils.persistence.CheckinPersistenceUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.layers.GooglePlacesUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.UrlUtils;

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
    			String lat = request.getParameter("lat");
    			String lng = request.getParameter("lng");
    			//TODO check if user has checked in to the place from different device within 8 hours (checkinTimeInterval param in the client)
    	    	CheckinPersistenceUtils.persistCheckin(accessToken, venueId, -1, 2);
    			int responseCode = FoursquareUtils.checkin(accessToken, venueId, name);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				//response.sendError(responseCode);
    				logger.log(Level.SEVERE, "Received following http response code: {0}", responseCode);
    			} else {
    				//TODO put imageUrl 
    				Map<String, String> params = new ImmutableMap.Builder<String, String>().
                        put("user", "Foursquare User").
                        put("name", name).
                        put("url", UrlUtils.getShortUrl("http://foursquare.com/venue/" + venueId)).
                        put("lat", lat != null ? lat : "").
    					put("lng", lng != null ? lng : "").
    					put("imageUrl", ConfigurationManager.SERVER_URL + "images/fs_checkin.png").build();
    				NotificationUtils.createSocialCheckinNotificationTask(params);
    			}
    		} else {
    			//response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		}
    	} else if (StringUtils.equals(service, Commons.FACEBOOK)) { 
    		if (!HttpUtils.isEmptyAny(request, "accessToken", "venueId", "name")) {
    			String accessToken = request.getParameter("accessToken");
    			String venueId = request.getParameter("venueId");
    			String name = request.getParameter("name");
    			String lat = request.getParameter("lat");
    			String lng = request.getParameter("lng");
    			//TODO check if user has checked in to the place from different device within 8 hours (checkinTimeInterval param in the client)
    	    	CheckinPersistenceUtils.persistCheckin(accessToken, venueId, -1, 2);
    			int responseCode = FacebookSocialUtils.checkin(accessToken, venueId, name);
    			if (responseCode != HttpServletResponse.SC_OK) {
    				response.sendError(responseCode);
    			} else {
    				Map<String, String> params = new ImmutableMap.Builder<String, String>().
    					put("user", "Facebook User").
    					put("name", name).
                    	put("url", UrlUtils.getShortUrl("http://facebook.com/profile.php?id=" + venueId)).
                    	put("lat", lat != null ? lat : "").
    					put("lng", lng != null ? lng : "").
    					put("imageUrl", ConfigurationManager.SERVER_URL + "images/fb_checkin.jpg").build();
    				NotificationUtils.createSocialCheckinNotificationTask(params);
    			}
    		} else {
    			//response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		}   
    	} else if (StringUtils.equals(service, Commons.GOOGLE) || StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
    		String reference = request.getParameter("reference");
    		String lat = request.getParameter("lat");
			String lng = request.getParameter("lng");
			
			if (StringUtils.isNotEmpty(reference)) {
    			try {
    				String placeJson = GooglePlacesUtils.getPlaceDetails(reference, "en");
    				ExtendedLandmark landmark = GooglePlacesUtils.processLandmark(placeJson, 128, Locale.US);
    				if (landmark != null) {
    					   String url = landmark.getUrl();
    					   if (StringUtils.isNotEmpty(url)) {
    						    //TODO put imageUrl 
    		    				Map<String, String> params = new ImmutableMap.Builder<String, String>().
    								put("user", "Google User").
    								put("name", landmark.getName()).
    								put("url", UrlUtils.getGoogleShortUrl(url)). 
    								put("lat", lat != null ? lat : "").
    		    					put("lng", lng != null ? lng : "").
    		    					put("imageUrl", ConfigurationManager.SERVER_URL + "images/checkin.png").build();
    		    				NotificationUtils.createSocialCheckinNotificationTask(params);
    					   }
    				}   
    			} catch (Exception e) {
    					logger.log(Level.SEVERE, "SocialCheckinServlet.processRequest() exception", e);
    			}
    		} else {
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    		} 
    	} else {
    		logger.log(Level.SEVERE, "Wrong service called: " + service);
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
