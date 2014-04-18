package com.jstakun.lm.server.tasks;

import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.social.FacebookUtils;
import com.jstakun.lm.server.social.GoogleBloggerUtils;
import com.jstakun.lm.server.social.GooglePlusUtils;
import com.jstakun.lm.server.social.LinkedInUtils;
import com.jstakun.lm.server.social.TwitterUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.UrlUtils;

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
    			
    			String service = request.getParameter("service");
            	String key = request.getParameter("key");
            	String landmarkUrl = request.getParameter("landmarkUrl");
            	String email = request.getParameter("email");
            	String title = request.getParameter("title");
            	String body = request.getParameter("body");   
            	String userUrl = request.getParameter("userUrl");
            	String username = request.getParameter("username");
            	
            	logger.log(Level.INFO, "Sending landmark creation notification to service {0}...", service);
            	
            	if (StringUtils.equals(service, Commons.FACEBOOK)) {
            		FacebookUtils.sendMessageToPageFeed(key, landmarkUrl);
            	} else if (StringUtils.equals(service, Commons.TWITTER)) {
            		TwitterUtils.sendMessage(key, landmarkUrl, ConfigurationManager.getParam(ConfigurationManager.TW_TOKEN, null), ConfigurationManager.getParam(ConfigurationManager.TW_SECRET, null), Commons.SERVER);
            	} else if (StringUtils.equals(service, Commons.GOOGLE_BLOGGER)) {
            		GoogleBloggerUtils.sendMessage(key, landmarkUrl, Commons.gl_plus_token, Commons.gl_plus_refresh, true);
            	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
            		GooglePlusUtils.sendMessage(Commons.gl_plus_token, Commons.gl_plus_refresh, key, landmarkUrl, Commons.SERVER);
            	} else if (StringUtils.equals(service, Commons.MAIL)) {
            		MailUtils.sendLandmarkCreationNotification(title, body);
            		//send landmark creation notification email to user
            		if (StringUtils.isNotEmpty(email)) {
            			String userMask = UrlUtils.createUsernameMask(username);
            			MailUtils.sendLandmarkNotification(email, userUrl, userMask, landmarkUrl, key, getServletContext());
            		}			
            	}
            	
    		} else if (!HttpUtils.isEmptyAny(request, "service", "accessToken", "name", "username")) {
    			
    			String service = request.getParameter("service");
            	String accessToken = request.getParameter("accessToken");
            	String username = request.getParameter("username");
            	String name = request.getParameter("name");
            	String email = request.getParameter("email");
            	String layer = null;
            	
            	logger.log(Level.INFO, "Sending user login notification to {0}...", service);
            	
            	if (StringUtils.equals(service, Commons.FACEBOOK)) {
            		FacebookUtils.sendMessageToUserFeed(accessToken, ConfigurationManager.SERVER_URL, "Message from GMS World", Commons.LOGIN);
                    layer = Commons.FACEBOOK_LAYER;
            	} else if (StringUtils.equals(service, Commons.FOURSQUARE)) {
            		layer = Commons.FOURSQUARE_LAYER;
            	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
            		String refreshToken = request.getParameter("refreshToken");
                	GooglePlusUtils.sendMessage(accessToken, refreshToken, null, ConfigurationManager.SERVER_URL, Commons.LOGIN);
                	layer = "Google";
            	} else if (StringUtils.equals(service, Commons.LINKEDIN)) {
            		LinkedInUtils.sendPost(ConfigurationManager.SERVER_URL, "GMS World", Commons.LOGIN, accessToken, null);
            		layer = "LinkedIn";
            	} else if (StringUtils.equals(service, Commons.TWITTER)) {
            		String tokenSecret = request.getParameter("tokenSecret");
                	TwitterUtils.sendMessage(null, ConfigurationManager.SERVER_URL, accessToken, tokenSecret, Commons.LOGIN);
                	layer = Commons.TWITTER_LAYER;
            	}
            	
            	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
                MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, service));
                if (StringUtils.isNotEmpty(email) && layer != null) {
                	MailUtils.sendLoginNotification(email, name, layer, getServletContext());
                }
            	
            } else if (!HttpUtils.isEmptyAny(request, "imageUrl", "showImageUrl", "lat", "lng", "service")) {
            	
            	String imageUrl = request.getParameter("imageUrl");
            	String username = request.getParameter("username");
            	double lat = NumberUtils.getDouble(request.getParameter("lat"), 0d);
            	double lng = NumberUtils.getDouble(request.getParameter("lng"), 0d);
            	String service = request.getParameter("service");
            	String showImageUrl = request.getParameter("showImageUrl");
                
            	logger.log(Level.INFO, "Sending image creation notification to {0}...", service);
            	
            	if (StringUtils.equals(service, Commons.FACEBOOK)) {
                	FacebookUtils.sendImageMessage(imageUrl, showImageUrl, username);
            	} else if (StringUtils.equals(service, Commons.TWITTER)) {
                	TwitterUtils.sendImageMessage(showImageUrl, username, lat, lng);
            	} else if (StringUtils.equals(service, Commons.GOOGLE_BLOGGER)) {
            		GoogleBloggerUtils.sendImageMessage(showImageUrl, username, imageUrl);
            	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
                	GooglePlusUtils.sendImageMessage(showImageUrl, username, imageUrl);
                }
                
            } else if (!HttpUtils.isEmptyAny(request, "url", "type", "title", "service")) {
            	
            	String service = request.getParameter("service");
            	String url = request.getParameter("url");
            	int type = NumberUtils.getInt(request.getParameter("type"),-1);
            	String title = request.getParameter("title");
            	String key = request.getParameter("key");
            	String token = request.getParameter("token");
            	
            	logger.log(Level.INFO, "Sending notification to {0} user social profile...", service);
            	
            	if (StringUtils.equals(service, Commons.FACEBOOK)) {
                	FacebookUtils.sendMessageToUserFeed(token, url, title, type);
            	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
            		String refreshToken = request.getParameter("refresh_token");
            	    GooglePlusUtils.sendMessage(token, refreshToken, key, url, type);
            	} else if (StringUtils.equals(service, Commons.LINKEDIN)) {
            		String secret = request.getParameter("secret");
            		LinkedInUtils.sendPost(url, title, type, token, secret);
            	} else if (StringUtils.equals(service, Commons.TWITTER)) {
            		String secret = request.getParameter("secret");
            		TwitterUtils.sendMessage(key, url, token, secret, type);
            	}
            	
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
