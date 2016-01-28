package com.jstakun.lm.server.tasks;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.utils.HttpUtils;

import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

/**
 * Servlet implementation class NotificationTaskServlet
 */
public class NotificationTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(NotificationTaskServlet.class.getName());
       
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        GeocodeHelperFactory.setCacheProvider(new GoogleCacheProvider()); //this is for landmark creation notification
    }    
	/** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	try {
    		String status = null;
    		Map<String, String[]> params = request.getParameterMap();
    		if (!HttpUtils.isEmptyAny(request, "key", "landmarkUrl", "title", "body", "username", "userUrl", "service")) {
    			status = NotificationUtils.sendLandmarkCreationNotification(params, getServletContext());
    		} else if (!HttpUtils.isEmptyAny(request, "service", "accessToken", "name", "username")) {
    			status = NotificationUtils.sendUserLoginNotification(params, getServletContext());
            } else if (!HttpUtils.isEmptyAny(request, "imageUrl", "showImageUrl", "lat", "lng", "service")) {
            	status = NotificationUtils.sendImageCreationNotification(params);
            } else if (!HttpUtils.isEmptyAny(request, "url", "type", "title", "service")) {
            	status = NotificationUtils.sendUserProfileNotification(params);
            } else if (!HttpUtils.isEmptyAny(request, "url", "name", "service")) { 
            	status = NotificationUtils.sendCheckinNotification(params);         	
            } else if (!HttpUtils.isEmptyAny(request, "routeType", "username", "imageUrl")) { 
            	status = NotificationUtils.sendRouteCreationNotification(params);         	
            } else {
            	String paramsStr = "";
            	for (String param : params.keySet()) {
            		paramsStr += param + " ";
            	}
            	logger.log(Level.SEVERE, "Wrong parameters: " + paramsStr);
            	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
    		if (status == null) {
				//in case of failure retry request 
				//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				logger.log(Level.SEVERE, "This request should be retried!");
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
