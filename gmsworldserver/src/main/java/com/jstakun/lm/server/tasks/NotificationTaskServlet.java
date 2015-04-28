package com.jstakun.lm.server.tasks;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.utils.HttpUtils;

import com.jstakun.lm.server.social.NotificationUtils;

/**
 * Servlet implementation class NotificationTaskServlet
 */
public class NotificationTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(NotificationTaskServlet.class.getName());
       
	/** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		if (!HttpUtils.isEmptyAny(request, "key", "landmarkUrl", "title", "body", "username", "userUrl", "service")) {
    			NotificationUtils.sendLandmarkCreationNotification(request.getParameterMap(), getServletContext());
    		} else if (!HttpUtils.isEmptyAny(request, "service", "accessToken", "name", "username")) {
    			NotificationUtils.sendUserLoginNotification(request.getParameterMap(), getServletContext());
            } else if (!HttpUtils.isEmptyAny(request, "imageUrl", "showImageUrl", "lat", "lng", "service")) {
            	NotificationUtils.sendImageCreationNotification(request.getParameterMap());
            } else if (!HttpUtils.isEmptyAny(request, "url", "type", "title", "service")) {
            	NotificationUtils.sendUserProfileNotification(request.getParameterMap());
            } else if (!HttpUtils.isEmptyAny(request, "url", "name", "service")) { 
            	NotificationUtils.sendCheckinNotification(request.getParameterMap());         	
            } else if (!HttpUtils.isEmptyAny(request, "routeType", "username", "imageUrl")) { 
            	NotificationUtils.sendRouteCreationNotification(request.getParameterMap());         	
            } else {
            	String params = "";
            	for (Enumeration<String> iter=request.getParameterNames();iter.hasMoreElements(); ) {
            		params += iter.nextElement() + " ";
            	}
            	logger.log(Level.SEVERE, "Wrong parameters: " + params);
            	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
