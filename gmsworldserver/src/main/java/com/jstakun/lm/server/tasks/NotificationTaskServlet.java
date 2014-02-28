package com.jstakun.lm.server.tasks;

import java.io.IOException;
import java.io.PrintWriter;
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
    	try {
    		if (!HttpUtils.isEmptyAny(request, "key", "landmarkUrl", "title", "body", "username", "userUrl")) {
    			String key = request.getParameter("key");
            	String landmarkUrl = request.getParameter("landmarkUrl");
            	String email = request.getParameter("email");
            	String title = request.getParameter("title");
            	String body = request.getParameter("body");   
            	String userUrl = request.getParameter("userUrl");
            	String username = request.getParameter("username");
            	
    			FacebookUtils.sendMessageToPageFeed(key, landmarkUrl);
                TwitterUtils.sendMessage(key, landmarkUrl, ConfigurationManager.getParam(ConfigurationManager.TW_TOKEN, null), ConfigurationManager.getParam(ConfigurationManager.TW_SECRET, null), Commons.SERVER);
                GoogleBloggerUtils.sendMessage(key, landmarkUrl, Commons.gl_plus_token, Commons.gl_plus_refresh, true);
                GooglePlusUtils.sendMessage(Commons.gl_plus_token, Commons.gl_plus_refresh, key, landmarkUrl, Commons.SERVER);
                MailUtils.sendLandmarkCreationNotification(title, body);
                //send landmark creation notification email to user
                if (StringUtils.isNotEmpty(email)) {
                	String userMask = UrlUtils.createUsernameMask(username);
                	MailUtils.sendLandmarkNotification(email, userUrl, userMask, landmarkUrl, key, getServletContext());
                }			
    		} else if (!HttpUtils.isEmptyAny(request, "service", "accessToken", "name", "username")) {
    			String service = request.getParameter("service");
            	String accessToken = request.getParameter("accessToken");
            	String username = request.getParameter("username");
            	String name = request.getParameter("name");
            	String email = request.getParameter("email");
            	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
                
            	if (StringUtils.equals(service, Commons.FACEBOOK)) {
            		FacebookUtils.sendMessageToUserFeed(accessToken, null, Commons.LOGIN);
                    MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, Commons.FACEBOOK));
                    if (StringUtils.isNotEmpty(email)) {
                    	MailUtils.sendLoginNotification(email, name, "Facebook", getServletContext());
                    }
            	} else if (StringUtils.equals(service, Commons.FOURSQUARE)) {
            		MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, Commons.FOURSQUARE));
                    if (StringUtils.isNotEmpty(email)) {
                    	MailUtils.sendLoginNotification(email, name, "Foursquare", getServletContext());
                    }
            	} else if (StringUtils.equals(service, Commons.GOOGLE_PLUS)) {
            		String refreshToken = request.getParameter("refreshToken");
                	GooglePlusUtils.sendMessage(accessToken, refreshToken, null, ConfigurationManager.SERVER_URL, Commons.LOGIN);
                    MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, Commons.GOOGLE_PLUS));
                    if (StringUtils.isNotEmpty(email)) {
                    	MailUtils.sendLoginNotification(email, name, "Google", getServletContext());
                    }
            	} else if (StringUtils.equals(service, Commons.LINKEDIN)) {
            		LinkedInUtils.sendPost(ConfigurationManager.SERVER_URL, "GMS World", Commons.LOGIN, accessToken, null);
                    MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, Commons.LINKEDIN));
                    if (StringUtils.isNotEmpty(email)) {
                    	MailUtils.sendLoginNotification(email, name, "LinkedIn", getServletContext());
                    }
            	} else if (StringUtils.equals(service, Commons.TWITTER)) {
            		String tokenSecret = request.getParameter("tokenSecret");
                	TwitterUtils.sendMessage(null, ConfigurationManager.SERVER_URL, accessToken, tokenSecret, Commons.LOGIN);
                    MailUtils.sendUserCreationNotification(String.format(rb.getString("Social.user.login"), ConfigurationManager.SERVER_URL, username, Commons.TWITTER));
                    if (StringUtils.isNotEmpty(email)) {
                    	MailUtils.sendLoginNotification(email, name, "Twitter", getServletContext());
                    }
            	}
            } else if (!HttpUtils.isEmptyAny(request, "key", "imageUrl", "lat", "lng")) {
    			String key = request.getParameter("key");
            	String imageUrl = request.getParameter("imageUrl");
            	String username = request.getParameter("username");
            	double lat = NumberUtils.getDouble(request.getParameter("lat"), 0d);
            	double lng = NumberUtils.getDouble(request.getParameter("lng"), 0d);
            	
            	String showImageUrl = UrlUtils.getShortUrl(ConfigurationManager.SERVER_URL + "showImage/" + key);
                
                FacebookUtils.sendImageMessage(imageUrl, showImageUrl, username);
                TwitterUtils.sendImageMessage(showImageUrl, username, lat, lng);
                GoogleBloggerUtils.sendImageMessage(showImageUrl, username, imageUrl);
                GooglePlusUtils.sendImageMessage(showImageUrl, username, imageUrl);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
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
