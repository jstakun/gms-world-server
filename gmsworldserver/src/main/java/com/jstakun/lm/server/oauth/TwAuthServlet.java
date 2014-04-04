/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.TokenUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

/**
 * 
 * @author jstakun
 */
public class TwAuthServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TwAuthServlet.class.getName());

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Twitter twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(Commons.TW_CONSUMER_KEY, Commons.TW_CONSUMER_SECRET);
			String verifier = request.getParameter("oauth_verifier");
			String token = request.getParameter("oauth_token");
			RequestToken requestToken = (RequestToken) CacheUtil.getObject("twRequestToken_" + token);
			
			if (requestToken != null) {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				
				User me = twitter.showUser(twitter.getId());
				
				Map<String, String> userData = new HashMap<String, String>();
				userData.put("token", accessToken.getToken());
				userData.put("secret", accessToken.getTokenSecret());
				userData.put(ConfigurationManager.TWEET_USERNAME, me.getScreenName());
				userData.put(ConfigurationManager.TWEET_NAME, me.getName());

				String key = TokenUtil.generateToken("lm", me.getScreenName() + "@" + Commons.TWITTER);
        		userData.put("gmsToken", key); 
				
        		Map<String, String> params = new ImmutableMap.Builder<String, String>().
                   	put("service", Commons.TWITTER).
					put("accessToken", accessToken.getToken()).
					put("tokenSecret", accessToken.getTokenSecret()).
					put("username", userData.get(ConfigurationManager.TWEET_USERNAME)).
					put("name", userData.get(ConfigurationManager.TWEET_NAME)).build();
        		NotificationUtils.createNotificationTask(params);
			    
        		request.setAttribute("title", new JSONObject(userData).toString());
        		request.getRequestDispatcher("/m/oauth_logon_confirmation.jsp").forward(request, response);
			} else {
				logger.log(Level.SEVERE, "RequestToken is null !");
				response.sendRedirect("/m/oauth_logon_error.jsp");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendRedirect("/m/oauth_logon_error.jsp");
		} 
	}

	// <editor-fold defaultstate="collapsed"
	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * 
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}
