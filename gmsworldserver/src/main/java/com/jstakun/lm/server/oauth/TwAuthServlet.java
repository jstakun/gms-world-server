/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.oauth;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.persistence.OAuthTokenPersistenceUtils;


/**
 *
 * @author jstakun
 */
public class TwAuthServlet extends HttpServlet {

     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TwAuthServlet.class.getName());
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
        	Twitter twitter = new TwitterFactory().getInstance();
    		twitter.setOAuthConsumer(Commons.TW_CONSUMER_KEY, Commons.TW_CONSUMER_SECRET);
    		RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
            String username = (String) request.getSession().getAttribute("token");
            String password = (String) request.getSession().getAttribute("password");
            String verifier = request.getParameter("oauth_verifier");
            try {
            	AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                request.getSession().removeAttribute("requestToken");
                User me = twitter.showUser(twitter.getId());
                OAuthTokenPersistenceUtils.persistOAuthToken(Commons.TWITTER, accessToken.getToken() + " " + accessToken.getTokenSecret(), username, password, me.getScreenName());
                
                Map<String, String> userData = new HashMap<String, String>();
                userData.put("token", accessToken.getToken());
                userData.put("secret", accessToken.getTokenSecret());
                userData.put(ConfigurationManager.TWEET_USERNAME, me.getScreenName());
                userData.put(ConfigurationManager.TWEET_NAME, me.getName());
                
                //move to task
                //TwitterUtils.sendMessage(null, ConfigurationManager.SERVER_URL, accessToken.getToken(), accessToken.getTokenSecret(), Commons.LOGIN);
                //MailUtils.sendUserCreationNotification("User " + ConfigurationManager.SERVER_URL + "socialProfile?uid=" + userData.get(ConfigurationManager.TWEET_USERNAME) + "@" + Commons.TWITTER + " logged in");
                //if (userData.containsKey(ConfigurationManager.USER_EMAIL)) {
                //	MailUtils.sendLoginNotification(userData.get(ConfigurationManager.USER_EMAIL), userData.get(ConfigurationManager.TWEET_NAME), "Twitter", getServletContext());
                //}
                //
                
                Queue queue = QueueFactory.getQueue("notifications");
                queue.add(withUrl("/tasks/notificationTask").
                		param("service", Commons.TWITTER).
                		param("accessToken", accessToken.getToken()).
                		param("tokenSecret", accessToken.getTokenSecret()).
                		//param("email", userData.containsKey(ConfigurationManager.USER_EMAIL) ? userData.get(ConfigurationManager.USER_EMAIL) : "").
                		param("username", userData.get(ConfigurationManager.TWEET_USERNAME)).
                		param("name", userData.get(ConfigurationManager.TWEET_NAME)));         
                
                out.print(OAuthCommons.getOAuthSuccessHTML(new JSONObject(userData).toString()));             
            } catch (TwitterException e) {
                 logger.log(Level.SEVERE, e.getMessage(), e);
                //out.print("AccessToken is null!");
                response.sendRedirect("/m/oauth_logon_error.jsp");
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
