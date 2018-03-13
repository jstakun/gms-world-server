package com.jstakun.lm.server.social;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.CommentPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.CommonPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class AddCommentServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(AddCommentServlet.class.getName());
    
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
            String key = request.getParameter("key");
            logger.log(Level.INFO, "Adding comment to: " + key);
            if (HttpUtils.isEmptyAny(request, "key", "message") || !CommonPersistenceUtils.isKeyValid(key)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String username = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                String message = request.getParameter("message");
                Landmark landmark = null;
                if (StringUtils.startsWith(key, UrlUtils.BITLY_URL)) {
                    String hash = StringUtils.remove(key, UrlUtils.BITLY_URL);
                    landmark = LandmarkPersistenceUtils.selectLandmarkByHash(hash, GoogleCacheProvider.getInstance());
                } else if (StringUtils.startsWith(key, ConfigurationManager.SERVER_URL)) {
                	int index = StringUtils.lastIndexOfAny(key, new String[]{",","/"});
                	if (index > 0 && index < key.length()) {
                	   String extractedKey = key.substring(index+1);	
                 	   logger.log(Level.INFO, "Key is: " + extractedKey);
                 	   if (CommonPersistenceUtils.isKeyValid(extractedKey)) {
                 		   landmark = LandmarkPersistenceUtils.selectLandmarkById(extractedKey, GoogleCacheProvider.getInstance());
                 	   } else {
                 		   logger.log(Level.INFO, "Wrong key format " + extractedKey);
                 	   }
                	}                	 
                } else {
                    landmark = LandmarkPersistenceUtils.selectLandmarkById(key, GoogleCacheProvider.getInstance());
                } 
                
                if (landmark != null) {
                    CommentPersistenceUtils.persist(username, landmark.getId() + "", message);
                    out.println("Comment saved");
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        return "Add Comment Servlet";
    }
}
