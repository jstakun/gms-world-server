/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.tasks;

import com.google.appengine.api.datastore.KeyFactory;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class PersonalizeTaskServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2625922137820909647L;
	private static final Logger logger = Logger.getLogger(PersonalizeTaskServlet.class.getName());

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
        int first = NumberUtils.getInt(request.getParameter("first"), 0);
        int last = NumberUtils.getInt(request.getParameter("last"), 100);
        
        try {
            List<User> users = UserPersistenceUtils.selectUsers(first, last);
            logger.log(Level.INFO, "Found " + users.size() + " users");
            for (User user : users) {
                /*if (StringUtils.isEmpty(user.getPersonalInfo())) {
                    String key = KeyFactory.keyToString(user.getKey());
                    UserPersistenceUtils.setPersonalInfo(key);
                    logger.log(Level.INFO, "Requesting personal info for user {0}", user.getEmail());
                }*/
            	try {
            		 UserPersistenceUtils.persistUser(user.getLogin(), user.getPassword(), user.getEmail(), user.getFirstname(), user.getLastname(), false);
            		 if (user.getConfirmed()) {
            			 UserPersistenceUtils.confirmRemoteRegistration(user.getLogin());
            		 }
            		 logger.log(Level.INFO, "Migrated user " + user.getLogin());
            	} catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
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
