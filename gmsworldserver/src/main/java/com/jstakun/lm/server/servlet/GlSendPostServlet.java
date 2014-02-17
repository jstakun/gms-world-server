/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.layers.GooglePlusUtils;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.persistence.OAuthToken;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.OAuthTokenPersistenceUtils;

/**
 * 
 * @author jstakun
 */
public class GlSendPostServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GlSendPostServlet.class.getName());
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
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
	    	
		logger.log(Level.SEVERE, "Oops !!! Somebody called " + GlSendPostServlet.class.getName());

	}
	
	
	/*protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			String token = request.getParameter("token");
			String refresh = request.getParameter("refresh_token");
			if (token == null && refresh == null) {
				String username = (String) request.getSession().getAttribute("token");
				String password = (String) request.getSession().getAttribute("password");
				OAuthToken oauth_token = OAuthTokenPersistenceUtils.selectOAuthTokenByService(username, password, Commons.GOOGLE_BLOGGER);
				if (oauth_token != null) {
					String[] st = oauth_token.getToken().split("\\s+");
					if (st.length == 2) {
						token = st[0];
						refresh = st[1];
					}
				}
			}
			
			if (StringUtils.isNotEmpty(token) || StringUtils.isNotEmpty(refresh)) {
				int type = -1;
				String url = null;
				String key = request.getParameter("key");
				if (StringUtils.isNotEmpty(key)) {
					Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
					if (landmark != null) {
						url = UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark));
						if (landmark.getLayer().equals("Social")) {
							type = Commons.BLOGEO;
						} else if (landmark.getLayer().equals(Commons.MY_POS_CODE)) {
							type = Commons.MY_POS;
						} else {
							type = Commons.LANDMARK;
						}
					}
				} else {
					type = Commons.LOGIN;
					url = ConfigurationManager.SERVER_URL;
				}
				if (type != -1) {
					GooglePlusUtils.sendMessage(token, refresh, key, url, type);
				} else {
					logger.log(Level.SEVERE, "Message type undefinied!");
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else {
				logger.log(Level.SEVERE, "Access token and refresh token is null!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}*/

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
		return "Gl Send Servlet";
	}// </editor-fold>
}
