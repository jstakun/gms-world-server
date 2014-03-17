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

import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.TokenUtil;

/**
 *
 * @author jstakun
 */
public class AuthnServlet extends HttpServlet {
   
	private static final Logger logger = Logger.getLogger(AuthnServlet.class.getName());
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
        response.setContentType("text/json");
        PrintWriter out = response.getWriter();
        JSONObject resp = new JSONObject();
        try {
            String password = (String)request.getAttribute("password");
            if (password != null) {
           		resp.put("password", password);
           		String key = TokenUtil.generateToken("lm", (String)request.getAttribute("username"));
        		resp.put("gmsToken", key);   
           		String email = (String) request.getAttribute("email");
           		if (email != null) {
           			resp.put(ConfigurationManager.USER_EMAIL, email);
           			String name = (String)request.getAttribute("name");
           			if (name != null) {
           				resp.put(ConfigurationManager.GMS_NAME, name);
           			}
           			MailUtils.sendLoginNotification(email, name, "GMS World", getServletContext());
           		} 
           		out.print(resp.toString());  	
            } else {
            	resp.put("message", "No password encrypted");
            }
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        	resp.put("message", e.getMessage());
        } finally { 
        	out.print(resp.toString());  
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
        return "Authentication servlet";
    }// </editor-fold>

}
