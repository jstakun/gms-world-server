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

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.layers.FacebookUtils;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.persistence.OAuthToken;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.OAuthTokenPersistenceUtils;
import com.restfb.exception.FacebookOAuthException;

/**
 * 
 * @author jstakun
 */
public class FBSendMessageServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FBSendMessageServlet.class.getName());

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
	    	
		logger.log(Level.SEVERE, "Oops !!! Somebody called " + FBSendMessageServlet.class.getName());

	}
	
	/*protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			String token = request.getParameter("token");
			if (token == null) {
				String username = (String) request.getSession().getAttribute("token");
				String password = (String) request.getSession().getAttribute("password");
				OAuthToken stoken = OAuthTokenPersistenceUtils.selectOAuthTokenByService(username, password, Commons.FACEBOOK);
				token = stoken.getToken();
			} 
			
			if (token != null) {
				int type = -1;
				String key = request.getParameter("key");
				if (key != null) {
					Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
					if (landmark != null) {
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
				}
				
				if (type != -1) {
					FacebookUtils.sendMessageToUserFeed(token, key, type);
				} else {
					logger.log(Level.SEVERE, "Message type undefinied!");
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else {
				logger.log(Level.SEVERE, "Access token is null!");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			
		} catch (Exception ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
			if (ex instanceof FacebookOAuthException) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
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
		return "FB Send Message";
	}// </editor-fold>
}
