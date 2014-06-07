/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.tasks;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.DateUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.ServiceLogPersistenceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
public class TaskServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TaskServlet.class.getName());

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
            String entity = request.getParameter("entity");
            String action = request.getParameter("action");
            //String time = request.getParameter("time");

            if (StringUtils.isNotEmpty(action)) {
                if (action.equalsIgnoreCase("purge")) {
                    if (entity.equalsIgnoreCase("log")) {
                        //long count = ServiceLogPersistenceUtils.deleteAllLogs();
                        Date nDaysAgo = DateUtils.getDayInPast(Integer.parseInt(ConfigurationManager.getParam(ConfigurationManager.LOG_OLDER_THAN_DAYS, "60")), false);
                        long count = ServiceLogPersistenceUtils.deleteLogsOlderThanDate(nDaysAgo);
                        logger.log(Level.INFO, "Deleted {0} logs.", count);
                    } else if (entity.equalsIgnoreCase("screenshot")) {
                    	int ndays = NumberUtils.getInt(ConfigurationManager.getParam(ConfigurationManager.SCREENSHOT_OLDER_THAN_DAYS, "90"), 90);
                        //
                    	Date nDaysAgo = DateUtils.getDayInPast(ndays, true);
                        long count = ScreenshotPersistenceUtils.deleteScreenshotsOlderThanDate(nDaysAgo);
                        //TODO replace on 01/07/14
                        count += ScreenshotPersistenceUtils.deleteScreenshotsOlderThanDate(ndays);
                        //
                        logger.log(Level.INFO, "Deleted {0} screenshots.", count);
                    } else {
                        logger.log(Level.INFO, "Wrong parameter entity: {0}", entity);
                    }
                } else {
                    logger.log(Level.INFO, "Wrong parameter action: {0}", action);
                }
            }
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
