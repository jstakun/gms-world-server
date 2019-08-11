package com.jstakun.lm.server.oauth;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

/**
 * Servlet implementation class OAuthServlet
 */
public class OAuthServlet extends HttpServlet {
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
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String uri = request.getRequestURI();
			Map<String, String> userData = null;
			
			if (StringUtils.contains(uri, "fbauth")) {
				String code = request.getParameter("code");
				if (StringUtils.isNotEmpty(code)) {
					userData = FBCommons.authorize(code);
				} else {
					response.sendRedirect("/fblogin");
				}
			} else if (StringUtils.contains(uri, "fsauth")) {
				String code = request.getParameter("code");
				if (StringUtils.isNotEmpty(code)) {
					userData = FSCommons.authorize(code);
				} else {
					response.sendRedirect("/fslogin");
				}
			} else if (StringUtils.contains(uri, "glauth")) {
				String code = request.getParameter("code");
				if (StringUtils.isNotEmpty(code)) {
					userData = GlCommons.authorize(code);
				} else {
					response.sendRedirect("/gllogin");
				}
			} else if (StringUtils.contains(uri, "lnauth")) {
				String code = request.getParameter("code");
                String state = request.getParameter("state");
                if (StringUtils.isNotEmpty(code)  && StringUtils.isNotEmpty(state)) {
    				userData = LnCommons.authorize(code, state);
                } else {
                	response.sendRedirect("/lnlogin");
                }
			} else if (StringUtils.contains(uri, "twauth")) {
				String verifier = request.getParameter("oauth_verifier");
				String token = request.getParameter("oauth_token");
				if (StringUtils.isNotEmpty(token)  && StringUtils.isNotEmpty(verifier)) {
					userData = TwCommons.authorize(token, verifier);
				} else {
					response.sendRedirect("/twlogin");
				}
			} else {
				logger.log(Level.SEVERE, "Unexpected uri: {0}", uri);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			
			if (userData != null && !userData.isEmpty()) {
				logger.log(Level.INFO, "Setting attributes: " + StringUtils.join(userData.keySet(), ","));
				request.setAttribute("title", new JSONObject(userData).toString());
        		request.getRequestDispatcher("/m/oauth_logon_confirmation.jsp").forward(request, response);
			} else {
				logger.log(Level.SEVERE, "userData is empty");
				response.sendRedirect("/m/oauth_logon_error.jsp");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendRedirect("/m/oauth_logon_error.jsp");
		} 
	}

}
