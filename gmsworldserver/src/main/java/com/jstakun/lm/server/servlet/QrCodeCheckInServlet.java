/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.servlet;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.persistence.CheckinPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.CommonPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jstakun
 */
public class QrCodeCheckInServlet extends HttpServlet {
   
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
            String key = request.getParameter("key");
            if (CommonPersistenceUtils.isKeyValid(key)) {
                String username = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
                if (landmark != null)
                {
                    CheckinPersistenceUtils.persistCheckin(username, key, 0);
                    response.setHeader("name", URLEncoder.encode(landmark.getName(),"UTF-8"));
                    out.println("Checkin done");
                }
                else
                {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception ex) {
            Logger.getLogger(QrCodeCheckInServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
