package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.social.FacebookUtils;
import com.jstakun.lm.server.social.GooglePlusUtils;
import com.jstakun.lm.server.social.LinkedInUtils;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.social.TwitterUtils;

/**
 * Servlet implementation class SocialMessageServlet
 */
public class SocialMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static final Logger logger = Logger.getLogger(SocialMessageServlet.class.getName());
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
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String uri = request.getRequestURI();
		
		String token = request.getParameter("token");
		String key = request.getParameter("key");
		
		Map<String, String> params = NotificationUtils.getNotificationParams(key);
		
		if (StringUtils.contains(uri, "fbSendMessage")) {
			if (StringUtils.isNotEmpty(token)) {
				params.put("token", token);
				params.put("service", Commons.FACEBOOK);
				NotificationUtils.createNotificationTask(params);
			} else {
				logger.log(Level.SEVERE, "FB access token is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else if (StringUtils.contains(uri, "twSendUpdate")) {
			String secret = request.getParameter("secret");
			if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(secret)) {
				params.put("token", token);
				params.put("secret", secret);
				params.put("service", Commons.TWITTER);
				NotificationUtils.createNotificationTask(params);
			} else {
				logger.log(Level.SEVERE, "TW access token and secret is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}	 
		} else if (StringUtils.contains(uri, "lnSendUpdate")) {
			String secret = request.getParameter("secret");
			if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(secret)) {
				params.put("token", token);
				params.put("secret", secret);
				params.put("service", Commons.LINKEDIN);
				NotificationUtils.createNotificationTask(params);
			} else {
				logger.log(Level.SEVERE, "LN access token and secret is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else if (StringUtils.contains(uri, "glSendPost")) {
			String refresh = request.getParameter("refresh_token");
			if (StringUtils.isNotEmpty(token) || StringUtils.isNotEmpty(refresh)) {
				params.put("token", token);
				if (refresh != null) {
					params.put("refresh_token", refresh);
				}
				params.put("service", Commons.GOOGLE_PLUS);
				NotificationUtils.createNotificationTask(params);
			} else {
				logger.log(Level.SEVERE, "GL access token and refresh token is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			logger.log(Level.SEVERE, "Unexpected uri: {0}", uri);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		/*if (StringUtils.contains(uri, "fbSendMessage")) {
			if (StringUtils.isNotEmpty(token)) {
				FacebookUtils.sendMessage(token, key);
			} else {
				logger.log(Level.SEVERE, "FB access token is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else if (StringUtils.contains(uri, "twSendUpdate")) {
			String secret = request.getParameter("secret");
			if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(secret)) {
				TwitterUtils.sendMessage(token, secret, key);
			} else {
				logger.log(Level.SEVERE, "TW access token and secret is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} 
		} else if (StringUtils.contains(uri, "lnSendUpdate")) {
			String secret = request.getParameter("secret");
			if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(secret)) {
				LinkedInUtils.sendMessage(token, secret, key);
			} else {
				logger.log(Level.SEVERE, "LN access token and secret is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else if (StringUtils.contains(uri, "glSendPost")) {
			String refresh = request.getParameter("refresh_token");
			if (StringUtils.isNotEmpty(token) || StringUtils.isNotEmpty(refresh)) {
				GooglePlusUtils.sendMessage(token, refresh, key);
			} else {
				logger.log(Level.SEVERE, "GL access token and refresh token is empty!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			logger.log(Level.SEVERE, "Unexpected uri: {0}", uri);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}*/
	}
}
