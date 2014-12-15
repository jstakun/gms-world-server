package com.jstakun.lm.server.oauth;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Servlet implementation class OAuthLoginServlet
 */
public class OAuthLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(OAuthLoginServlet.class.getName());
       
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
		String uri = request.getRequestURI();
		
		if (StringUtils.contains(uri, "fblogin")) {
			response.sendRedirect(FBCommons.getLoginRedirectURL());
		} else if (StringUtils.contains(uri, "fslogin")) {
			response.sendRedirect(FSCommons.getAuthorizationUrl());
		} else if (StringUtils.contains(uri, "gllogin")) {
			response.sendRedirect(GlCommons.getAuthorizationUrl());
		} else if (StringUtils.contains(uri, "lnlogin")) {
			response.sendRedirect(LnCommons.getAuthorizationUrl());
		} else if (StringUtils.contains(uri, "twlogin")) {
			try {
				response.sendRedirect(TwCommons.getAuthorizationUrl());
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else if (StringUtils.contains(uri, "cblogin")) {
			response.sendRedirect(CbCommons.getAuthorizationUrl());
		} else {
			logger.log(Level.SEVERE, "Unexpected uri: {0}", uri);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

}
