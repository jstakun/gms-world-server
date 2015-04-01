/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.MailUtils;

/**
 *
 * @author jstakun
 */
public class NotificationsServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(NotificationsServlet.class.getName());
    private static final long ONE_DAY = 1000 * 60 * 60 * 24;
	
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
        try {
            if (HttpUtils.isEmptyAny(request, "type", "appId")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String type = request.getParameter("type");
                String appId = request.getParameter("appId");
                JSONObject reply = new JSONObject();
                
                String lat = request.getParameter("lat");
                String lng = request.getParameter("lng");
                if (StringUtils.isNotEmpty(lat) && StringUtils.isNotEmpty(lng)) {
                	logger.log(Level.INFO, "User location is " + lat + "," + lng);
                	//TODO persist location
                	
                	/*
                	String name = "My location";
                	String lat = StringUtil.formatCoordE2(latitude);
                	String lng = StringUtil.formatCoordE2(longitude);
            		boolean isSimilarToNewest = LandmarkPersistenceUtils.isSimilarToNewest(name, lat, lng);
                	if (!isSimilarToNewest) {
                		String username = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                		if (username != null && username.length() % 4 == 0) {
                			try {
                				username = new String(Base64.decode(username));
                			} catch (Exception e) {
                				//from version 1086, 86 username is Base64 encoded string
                			}
                		}	
                		String description = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(latitude, longitude);            
                		Map<String, String> peristResponse = LandmarkPersistenceUtils.persistLandmark(name, description, latitude, longitude, 0, username, null, Commons.MY_POS_CODE, null);

                		String id = peristResponse.get("id");
                		String hash = peristResponse.get("hash");
                
                		if (StringUtils.isNumeric(id)) {	
                        	String userAgent = request.getHeader("User-Agent");
                    		int useCount = NumberUtils.getInt(request.getHeader("X-GMS-UseCount"), 1);
                    		LandmarkPersistenceUtils.notifyOnLandmarkCreation(name, lat, lng, id, hash, Commons.MY_POS_CODE, username, null, userAgent, useCount);
                        }
                    } */   
                	
                } else {
                	logger.log(Level.INFO, "No user location provided");
                }
                
                if (StringUtils.equals(type, "v")) {
                    //check for version
                    reply.put("type", type);
                    if (StringUtils.equalsIgnoreCase(appId,"0")) {
                        //LM
                        String version = ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.LM_VERSION, "0");
                        reply.put("value", version);
                    } else if (StringUtils.equalsIgnoreCase(appId,"1")) {
                        //DA
                        String version = ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.DA_VERSION, "0");
                        reply.put("value", version);
                    }
                } else if (StringUtils.equals(type, "u")) {
                	//engagement
                	String email = request.getParameter("e");
                	long lastStartupTime = NumberUtils.getLong(request.getParameter("lst"), -1);
                	String useCount = request.getParameter("uc");
                	Calendar cal = Calendar.getInstance();
                	cal.setTimeInMillis(lastStartupTime);
                	logger.log(Level.INFO, "Received usage notification from " + (email != null ? email : "guest") + 
                			" last startup time: " + DateFormat.getDateTimeInstance().format(cal.getTime()) + 
                			", use count: " + useCount);
                	int minInterval = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.NOTIFICATIONS_INTERVAL, "14"), 14);
                	int maxInterval = 31;
                	long interval = System.currentTimeMillis() - lastStartupTime;
                	if (interval > (minInterval * ONE_DAY) && interval < (maxInterval * ONE_DAY) && email != null) {
                		//send email notification if lastStartupTime > week ago 
                    	//send not more that once a week
                		logger.log(Level.WARNING, email + " should be engaged to run Landmark Manager!");
                		MailUtils.sendEngagementMessage(email, getServletContext());
                		reply = new JSONObject().put("status", "engaged").put("timestamp", System.currentTimeMillis()); 
                	} else {
                		response.setStatus(HttpServletResponse.SC_ACCEPTED);
                		reply = new JSONObject().put("status", "accepted"); 
                	}	
                }
                out.print(reply.toString());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
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
        return "Notifications servlet";
    }// </editor-fold>

}
