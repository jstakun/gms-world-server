/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.layers.GooglePlusUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.persistence.OAuthTokenPersistenceUtils;
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

/**
 *
 * @author jstakun
 */
public class GlAuthServlet extends HttpServlet {

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
            String username = (String) request.getSession().getAttribute("token");
            String password = (String) request.getSession().getAttribute("password");

            if (username == null) {
                username = "anonymous";
            }
            if (password == null) {
                password = "anonymous";
            }
            
            //String token = (String) request.getSession().getAttribute("gltoken");
            //String secret = (String) request.getSession().getAttribute("glsecret");
            
            String code = request.getParameter("code");

            URL url = new URL("https://accounts.google.com/o/oauth2/token");

            String result = HttpUtils.processFileRequest(url, "POST", null, "code=" + code + "&client_id=" + Commons.GL_PLUS_KEY + "&client_secret=" + Commons.GL_PLUS_SECRET + "&redirect_uri=" + GlCommons.CALLBACK_URI + "&grant_type=authorization_code");
            String accessToken = null, refreshToken = null;
            long expires_in = -1;
            
            if (StringUtils.startsWith(result, "{")) {
                JSONObject resp = new JSONObject(result);
                accessToken = resp.optString("access_token");
                refreshToken = resp.optString("refresh_token");
                expires_in = resp.optInt("expires_in", -1);
            }

            if (accessToken != null) {

                Map<String, String> userData = GooglePlusUtils.getUserData(accessToken, refreshToken);
                
                String token = accessToken;
                if (refreshToken != null) {
                    token = token + " " + refreshToken;
                    userData.put("refresh_token", refreshToken);
                }
                
                userData.put("token", accessToken);
                if (expires_in > -1) {
                	userData.put(ConfigurationManager.GL_EXPIRES_IN, Long.toString(expires_in));
                }
               
                OAuthTokenPersistenceUtils.persistOAuthToken(Commons.GOOGLE_PLUS, token, username, password, userData.get(ConfigurationManager.GL_USERNAME));          

                //move to task
                //GooglePlusUtils.sendMessage(accessToken, refreshToken, null, ConfigurationManager.SERVER_URL, Commons.LOGIN);
                //MailUtils.sendUserCreationNotification("User " + ConfigurationManager.SERVER_URL + "socialProfile?uid=" + userData.get(ConfigurationManager.GL_USERNAME) + "@" + Commons.GOOGLE + " logged in");
                //if (userData.containsKey(ConfigurationManager.USER_EMAIL)) {
                //	MailUtils.sendLoginNotification(userData.get(ConfigurationManager.USER_EMAIL), userData.get(ConfigurationManager.GL_NAME), "Google", getServletContext());
                //}
                //
                
                Queue queue = QueueFactory.getQueue("notifications");
                queue.add(withUrl("/tasks/notificationTask").
                		param("service", Commons.GOOGLE_PLUS).
                		param("accessToken", accessToken).
                		param("refreshToken", refreshToken).
                		param("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
                		param("username", userData.get(ConfigurationManager.GL_USERNAME)).
                		param("name", userData.get(ConfigurationManager.GL_NAME)));         

                out.print(OAuthCommons.getOAuthSuccessHTML(new JSONObject(userData).toString()));
            } else {
                //out.print("Access token not found!");
                response.sendRedirect("/m/oauth_logon_error.jsp");
            }
        } catch (Exception ex) {
            Logger.getLogger(GlAuthServlet.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            response.sendRedirect("/m/oauth_logon_error.jsp");
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
