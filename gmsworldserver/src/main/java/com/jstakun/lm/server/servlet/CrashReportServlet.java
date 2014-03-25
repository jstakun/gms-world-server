/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.NumberUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
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
public class CrashReportServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CrashReportServlet.class.getName());
    private static final String[] params = new String[]{"PACKAGE_NAME", "APP_VERSION_CODE", "APP_VERSION_NAME"};

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
            String title = "New crash report";
            String titleSuffix = "";
            int versionCode = 0;
            
            Map<String, String[]> requestParams = request.getParameterMap();

            if (!requestParams.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                for (Iterator<Map.Entry<String, String[]>> iter = requestParams.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry<String, String[]> entry = iter.next();
                    String key = entry.getKey();
                    //logger.log(Level.INFO, "Parameter: {0}", key);
                    sb.append("Parameter: ").append(key).append("\n");
                    String[] value = entry.getValue();
                    for (String v : value) {
                        //logger.log(Level.INFO, "Value: {0}", v);
                        sb.append("Value: ").append(v).append("\n");
                    }
                    if (StringUtils.indexOfAny(key, params) >= 0 && value.length > 0) {
                        titleSuffix += " " + key + ": " + value[0];
                    }
                    
                    if (key.equals("APP_VERSION_CODE")) {
                    	versionCode = NumberUtils.getInt(value[0], 0);
                    }
                }

                if (StringUtils.isNotEmpty(titleSuffix)) {
                    title += titleSuffix;
                }

                int lmVersion = NumberUtils.getInt(ConfigurationManager.getParam(ConfigurationManager.LM_VERSION, "0"), 0);
                int daVersion = NumberUtils.getInt(ConfigurationManager.getParam(ConfigurationManager.DA_VERSION, "0"), 0);
                
                if (versionCode >= lmVersion || (versionCode >= daVersion && versionCode < 500)) {
                	MailUtils.sendCrashReport(title, sb.toString());
                } else {
                	logger.log(Level.INFO, sb.toString());
                }
                
                out.println("<html>");
                out.println("<head>");
                out.println("<title>CrashReport Servlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("Servlet CrashReport Servlet executed.");
                out.println("</body>");
                out.println("</html>");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
        return "Crash Report Servlet";
    }
}
