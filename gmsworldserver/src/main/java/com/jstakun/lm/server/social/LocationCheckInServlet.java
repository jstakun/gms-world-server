package com.jstakun.lm.server.social;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.CheckinPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.CommonPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class LocationCheckInServlet extends HttpServlet {

	private Logger logger = Logger.getLogger(LocationCheckInServlet.class.getName());
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
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject resp = new JSONObject();
        try {
            String key = request.getParameter("key");
            logger.log(Level.INFO, "Check-in at: " + key);
            if (CommonPersistenceUtils.isKeyValid(key)) {
                String username = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                Landmark landmark = null;
                if (StringUtils.startsWith(key, UrlUtils.BITLY_URL)) {
                	String extractedKey = StringUtils.remove(key, UrlUtils.BITLY_URL);
                	logger.log(Level.INFO, "Key is: " + extractedKey);
                    landmark = LandmarkPersistenceUtils.selectLandmarkByHash(extractedKey);
                } else if (StringUtils.startsWith(key, ConfigurationManager.SERVER_URL)) {
                	int index = StringUtils.lastIndexOfAny(key, new String[]{",","/"});
                	if (index > 0 && index < key.length()) {
                	   String extractedKey = key.substring(index+1);	
                	   logger.log(Level.INFO, "Key is: " + extractedKey);
                	   if (CommonPersistenceUtils.isKeyValid(extractedKey)) {
                		   landmark = LandmarkPersistenceUtils.selectLandmarkById(extractedKey, GoogleCacheProvider.getInstance());
                		   logger.log(Level.INFO, "Wrong key format " + extractedKey);
                	   }
                	} else {
                	    logger.log(Level.INFO, "No key found!");
                	}
                } else {
                	logger.log(Level.INFO, "Key is: " + key);
                    landmark = LandmarkPersistenceUtils.selectLandmarkById(key, GoogleCacheProvider.getInstance());
                } 
                
                if (landmark != null && landmark.getName() != null) {
                    boolean status = CheckinPersistenceUtils.persistCheckin(username, null, landmark.getId(), 1);  
                    response.setHeader("name", URLEncoder.encode(landmark.getName(), "UTF-8"));
                    if (status) {
                    	resp.put("status", "ok");
                    } else {
                    	resp.put("status", "failed").putOnce("message", "server error");
                    }
                } else {
                    //response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                	resp.put("status", "failed").put("message","landmark is null");
                }
            } else {
                //response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            	resp.put("status", "failed").put("message","wrong landmark key");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	out.print(resp.toString());
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
        return "Location Checkin Servlet";
    }
}
