/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import org.scribe.builder.ServiceBuilder;
//import org.scribe.builder.api.Google2Api;
//import org.scribe.model.Token;
//import org.scribe.oauth.OAuthService;

import com.jstakun.lm.server.config.Commons;

/**
 *
 * @author jstakun
 */
public class GlLoginServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

            /*if (request.getParameter(Commons.OAUTH_USERNAME) != null) {
                String userPass = request.getParameter(Commons.OAUTH_USERNAME);

                String[] unPw = CommonUtils.userPass(userPass);
                if (unPw != null) {
                    request.getSession().setAttribute("token", unPw[0]);
                    request.getSession().setAttribute("password", unPw[1]);
                }
            }*/

            response.sendRedirect(GlCommons.getAuthorizationUrl());

        } catch (Exception ex) {
            Logger.getLogger(GlLoginServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
