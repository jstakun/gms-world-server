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
import com.jstakun.lm.server.layers.FacebookUtils;
import com.jstakun.lm.server.utils.persistence.OAuthTokenPersistenceUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class FBAuthServlet extends HttpServlet {

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
            if (code != null && code.length() > 0) {
                
            	String username = "anonymous";
                String password = "anonymous";
                HttpSession session = request.getSession();
                if (session.getAttribute("token") != null) {
                    username = (String) session.getAttribute("token");
                }
                if (session.getAttribute("password") != null) {
                    password = (String) session.getAttribute("password");
                }
                
                String authURL = FBCommons.getAuthURL(code);
                
                URL url = new URL(authURL);

                String result = readURL(url);
                String accessToken = null;
                int expires = -1;
                String[] pairs = result.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length != 2) {
                        throw new RuntimeException("Unexpected auth response");
                    } else {
                        if (kv[0].equals("access_token")) {
                            accessToken = kv[1];
                        }
                        if (kv[0].equals("expires")) {
                            expires = Integer.parseInt(kv[1]);
                        }
                    }
                }
                if (accessToken != null) {
                    Map<String, String> userData = FacebookUtils.getMyData(accessToken);
                    OAuthTokenPersistenceUtils.persistOAuthToken(Commons.FACEBOOK, accessToken, username, password, userData.get(ConfigurationManager.FB_USERNAME));
                    userData.put("token", accessToken);
                    if (expires > 0) {
                    	userData.put(ConfigurationManager.FB_EXPIRES_IN, Integer.toString(expires));
                    }                 
                    
                    //move to task
                    //FacebookUtils.sendMessageToUserFeed(accessToken, null, Commons.LOGIN);
                    //MailUtils.sendUserCreationNotification("User " + ConfigurationManager.SERVER_URL + "socialProfile?uid=" + userData.get(ConfigurationManager.FB_USERNAME) + "@" + Commons.FACEBOOK + " logged in");
                    //if (userData.containsKey(ConfigurationManager.USER_EMAIL)) {
                    //	MailUtils.sendLoginNotification(userData.get(ConfigurationManager.USER_EMAIL), userData.get(ConfigurationManager.FB_NAME), "Facebook", getServletContext());
                    //}
                    
                    Queue queue = QueueFactory.getQueue("notifications");
                    queue.add(withUrl("/tasks/notificationTask").
                    		param("service", Commons.FACEBOOK).
                    		param("accessToken", accessToken).
                    		param("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
                    		param("username", userData.get(ConfigurationManager.FB_USERNAME)).
                    		param("name", userData.get(ConfigurationManager.FB_NAME)));                  
                    
                    out.print(OAuthCommons.getOAuthSuccessHTML(new JSONObject(userData).toString()));  
                } else {
                    response.sendRedirect("/m/oauth_logon_error.jsp");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            out.close();
        }
    }

    private String readURL(URL url) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        int r;
        while ((r = is.read()) != -1) {
            baos.write(r);
        }
        is.close();
        return new String(baos.toByteArray());
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
