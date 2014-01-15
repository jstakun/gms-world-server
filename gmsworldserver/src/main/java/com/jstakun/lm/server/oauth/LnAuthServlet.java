/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.layers.LinkedInUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.persistence.OAuthTokenPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class LnAuthServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LnAuthServlet.class.getName());
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
                /*String token = (String) request.getSession().getAttribute("lntoken");
                String secret = (String) request.getSession().getAttribute("lnsecret");*/
                
                String username = (String)request.getSession().getAttribute("token");
                String password = (String)request.getSession().getAttribute("password");
                
                if (username == null) {
                    username = "anonymous";
                }
                if (password == null) {
                    password = "anonymous";
                }

                String code = request.getParameter("code");
                String state = request.getParameter("state");
                
                if (code != null && StringUtils.equals(state, Commons.LN_STATE)) {
            	
        		URL tokenUrl = new URL(LnCommons.getAccessTokenUrl(code));
        	
        		String result = HttpUtils.processFileRequest(tokenUrl, "POST", null, null);
        		String accessToken = null;
        		long expires_in = -1;
            
        		if (StringUtils.startsWith(result, "{")) {
        			JSONObject resp = new JSONObject(result);
        			accessToken = resp.optString("access_token");
        			expires_in = resp.optInt("expires_in", -1);
        		}
                
                if (StringUtils.isNotEmpty(accessToken))
                {
                	Map<String, String> userData = LinkedInUtils.getUserDate(accessToken);
                	
                	OAuthTokenPersistenceUtils.persistOAuthToken(Commons.LINKEDIN, accessToken, username, password, userData.get(ConfigurationManager.LN_USERNAME));

                    userData.put("token", accessToken);
                    
                    if (expires_in > -1) {
                    	userData.put(ConfigurationManager.LN_EXPIRES_IN, Long.toString(expires_in));
                    }

                    //move to task
                    //LinkedInUtils.sendPost(ConfigurationManager.SERVER_URL, "GMS World", Commons.LOGIN, accessToken, null);
                    //MailUtils.sendUserCreationNotification("User " + ConfigurationManager.SERVER_URL + "socialProfile?uid=" + userData.get(ConfigurationManager.LN_USERNAME) + "@" + Commons.LINKEDIN + " logged in");
                    //if (userData.containsKey(ConfigurationManager.USER_EMAIL)) {
                    //	MailUtils.sendLoginNotification(userData.get(ConfigurationManager.USER_EMAIL), userData.get(ConfigurationManager.LN_NAME), "LinkedIn", getServletContext());
                    //}
                    //
                    
                    Queue queue = QueueFactory.getQueue("notifications");
                    queue.add(withUrl("/tasks/notificationTask").
                    		param("service", Commons.LINKEDIN).
                    		param("accessToken", accessToken).
                    		param("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
                    		param("username", userData.get(ConfigurationManager.LN_USERNAME)).
                    		param("name", userData.get(ConfigurationManager.LN_NAME)));         
                    
                    out.print(OAuthCommons.getOAuthSuccessHTML(new JSONObject(userData).toString()));        
                }
                else {
                    //out.print("Access token not found!");
                    response.sendRedirect("/m/oauth_logon_error.jsp");
                }
                } else {
                	response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }

            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
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
