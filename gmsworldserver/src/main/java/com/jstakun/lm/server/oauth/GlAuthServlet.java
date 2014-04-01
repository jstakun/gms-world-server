/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

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

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.social.GooglePlusUtils;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.TokenUtil;

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
               
                String key = TokenUtil.generateToken("lm", userData.get(ConfigurationManager.GL_USERNAME) + "@" + Commons.GOOGLE_PLUS);
                userData.put("gmsToken", key); 
                
                Map<String, String> params = new ImmutableMap.Builder<String, String>().
                   		put("service", Commons.GOOGLE_PLUS).
                		put("accessToken", accessToken).
                		put("refreshToken", refreshToken).
                		put("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
                		put("username", userData.get(ConfigurationManager.GL_USERNAME)).
                		put("name", userData.get(ConfigurationManager.GL_NAME)).build();
                NotificationUtils.createNotificationTask(params);

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
