package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.utils.NumberUtils;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.MailUtils;

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
    private static final String[] titleParams = new String[]{"PACKAGE_NAME", "APP_VERSION_CODE", "APP_VERSION_NAME"};
    private static final String[] bodyParams = new String[] {"PHONE_MODEL", "BRAND", "ANDROID_VERSION", "USER_APP_START_DATE", 
    		"USER_CRASH_DATE","SHARED_PREFERENCES","STACK_TRACE", "LOGCAT"};

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
            String title = "New crash report";
            String titleSuffix = "";
            int versionCode = 0;
            
            Map<String, String[]> requestParams = request.getParameterMap();

            if (!requestParams.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n");
                for (Iterator<Map.Entry<String, String[]>> iter = requestParams.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry<String, String[]> entry = iter.next();
                    final String key = entry.getKey();
                    final String[] value = entry.getValue();
                    
                    if (StringUtils.indexOfAny(key, titleParams) >= 0 && value.length > 0) {
                        titleSuffix += " " + key + ": " + value[0];
                    }
                    
                    if (StringUtils.indexOfAny(key, bodyParams) >= 0 && value.length > 0) {
                    	sb.append("Parameter: ").append(key).append("\n");
                    	for (String v : value) {
                            sb.append("Value: ").append(v).append("\n");
                        }
                    }
                }
                
                if (requestParams.containsKey("APP_VERSION_CODE")) {
                	String[] versionStr = requestParams.get("APP_VERSION_CODE");
                	if (versionStr.length > 0) {
                		versionCode = NumberUtils.getInt(versionStr[0], 0);
                	}
                }

                if (StringUtils.isNotEmpty(titleSuffix)) {
                    title += titleSuffix;
                }

                int lmVersion = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.LM_VERSION, "0"), 0);
                int daVersion = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.DA_VERSION, "0"), 0);
                int dlVersion = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.DL_VERSION, "0"), 0);
                
                if (versionCode >= (lmVersion-3) || (versionCode >= (daVersion-3) && versionCode < 500) || (versionCode > (dlVersion-3))) {
                	if (!StringUtils.equals(MailUtils.sendCrashReport(title, sb.toString()), "ok")) {
                		logger.log(Level.WARNING, "App version code: " + versionCode);
                    	logger.log(Level.SEVERE, sb.toString());
                	} else {
                		logger.log(Level.WARNING, "App version code: " + versionCode);
                    	logger.log(Level.WARNING, sb.toString());
                	}
                } else {
                	logger.log(Level.INFO, "App version code: " + versionCode);
                	logger.log(Level.INFO, sb.toString());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
