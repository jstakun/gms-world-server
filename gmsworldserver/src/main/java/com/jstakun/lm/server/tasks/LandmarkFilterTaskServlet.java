/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.tasks;

import com.jstakun.lm.server.utils.DateUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
public class LandmarkFilterTaskServlet extends HttpServlet {

    // /admin/taskExecute?action=filter&filterProperty=username&pattern=@fb&resultProperty=username

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LandmarkFilterTaskServlet.class.getName());
    private static final long WEEK = 1000 * 60 * 60 * 24 * 7;

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
            if (HttpUtils.isEmptyAny(request, "filterProperty", "pattern", "resultProperty")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String filterProperty = request.getParameter("filterProperty");
                String pattern = request.getParameter("pattern");
                String resultProperty = request.getParameter("resultProperty");

                Date nDaysAgo = null;
                int nDays = NumberUtils.getInt(request.getParameter("forDays"), 0);
                if (nDays > 0) {
                    nDaysAgo = DateUtils.getDayInPast(nDays, true);
                }

                String[] patternArr = StringUtils.split(pattern, ",");

                Map<String, Integer> beforeCreated = new HashMap<String, Integer>();
                for (int i=0;i<patternArr.length;i++) {
                    beforeCreated.put(patternArr[i], 0);
                }

                //Map<String,Collection<String>> filter = LandmarkPersistenceUtils.filterLandmarks(filterProperty, patternArr, resultProperty, true, System.currentTimeMillis() - WEEK, beforeCreated, nDaysAgo);
                //MailUtils.sendList("Report: " + resultProperty + " meeting patterns " + filterProperty + "=" + pattern, filter, beforeCreated);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
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
