/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.tasks;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.ServiceLogPersistenceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;

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
	private static final String domainName = "gmsworld";

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
                        Date nDaysAgo = DateUtils.getDayInPast(Integer.parseInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.LOG_OLDER_THAN_DAYS, "60")), false);
                        long count = ServiceLogPersistenceUtils.deleteLogsOlderThanDate(nDaysAgo);
                        logger.log(Level.INFO, "Deleted {0} logs.", count);
                    } else if (entity.equalsIgnoreCase("screenshot")) {
                    	int ndays = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.SCREENSHOT_OLDER_THAN_DAYS, "90"), 90);
                        //removed on 02/07/14
                    	//Date nDaysAgo = DateUtils.getDayInPast(ndays, true);
                        //long count = ScreenshotPersistenceUtils.deleteScreenshotsOlderThanDate(nDaysAgo);
                        //
                        long count = ScreenshotPersistenceUtils.deleteScreenshotsOlderThanDate(ndays);
                        //
                        logger.log(Level.INFO, "Deleted {0} screenshots.", count);
                    } else {
                        logger.log(Level.INFO, "Wrong parameter entity: {0}", entity);
                    }
                } else if (action.equalsIgnoreCase("rhcloud")) {
                	rhcloudHealthCheck("hotels", "http://hotels-gmsworld.rhcloud.com/snoop.jsp");
                	rhcloudHealthCheck("landmarks", "http://landmarks-gmsworld.rhcloud.com/snoop.jsp");
                	logger.log(Level.INFO, "Done");
                } else {
                    logger.log(Level.SEVERE, "Wrong parameter action: {0}", action);
                }
            }
        } finally {
            out.close();
        }
    }

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
        return "Cron Tasks Servlet";
    }
    
    private static void rhcloudHealthCheck(String appname, String healthCheckUrl) throws IOException {
    	logger.log(Level.INFO, "Checking if {0} app is running...", appname);
    	URL rhcloudUrl = new URL(healthCheckUrl);
    	HttpUtils.processFileRequest(rhcloudUrl);
    	Integer status = HttpUtils.getResponseCode(rhcloudUrl.toExternalForm()); 
    	if (status != null && status == 503) {
    		logger.log(Level.SEVERE, "Received Service Unavailable error response!");
    		logger.log(Level.INFO, "Trying to start the application {0}...", appname);
    		URL apiUrl = new URL("https://openshift.redhat.com/broker/rest/domains/" + domainName + "/applications/" + appname + "/events");
        	String response = HttpUtils.processFileRequestWithBasicAuthn(apiUrl, "POST", "application/json", "event=start", Commons.getProperty(Property.RH_ACCOUNT));
        	status = HttpUtils.getResponseCode(apiUrl.toExternalForm());
        	if (status != null && status != 200) {
        		logger.log(Level.SEVERE, "Received server response code " + status);
        	}
        	logger.log(Level.INFO, "Received following server response: {0}", response);
    	} else {
    		logger.log(Level.INFO, "Received server response code " + status);
    	}
    }
}
