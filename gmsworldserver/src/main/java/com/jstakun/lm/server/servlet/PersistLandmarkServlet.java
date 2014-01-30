package com.jstakun.lm.server.servlet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.CryptoTools;
import com.jstakun.lm.server.utils.GeocodeUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class PersistLandmarkServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PersistLandmarkServlet.class.getName());
    private static final String JSON_LAYER_LIST = "jsonLayersList";
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request 
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    //http://localhost:8080/services/persistLandmark?name=Test&description=none&longitude=0.0&latitude=0.0&username=jstakun&layer=Social&validityDate=10000000
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Landmark landmark = null;
        String key = null;
        PrintWriter out = response.getWriter();

        try {
            if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "name", "username")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                double latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
                double longitude = GeocodeUtils.getLongitude(request.getParameter("longitude"));
                double altitude = NumberUtils.getDouble(request.getParameter("altitude"), 0.0);

                String name = request.getParameter("name");
                String description = request.getParameter("description");

                String username = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                
                String layer = StringUtil.getStringParam(request.getParameter("layer"), "Public");

                Date validityDate = null;

                String validityStr = request.getParameter("validityDate");
                if (StringUtils.isNotEmpty(validityStr)) {
                    long validity = Long.parseLong(validityStr);
                    Date current = new Date();
                    validityDate = new Date(current.getTime() + validity);
                } 

                if (layer.equals(Commons.MY_POS_CODE)) {
                    description = GeocodeUtils.processGoogleReverseGeocode(latitude + "," + longitude);
                    //description = GeocodeUtils.processYahooReverseGeocode(latitude + "," + longitude);
                }

                String email = request.getParameter("email");
                
                try {
                	String landmarksUrl = "http://landmarks-gmsworld.rhcloud.com/actions/addLandmark";
                	String params = "latitude=" + latitude + "&longitude=" + longitude + "&name=" + URLEncoder.encode(name, "UTF-8") + 
                			"&altitude=" + altitude + "&username=" + username + "&layer=" + layer;			 
                	if (validityStr != null) {
                		params +=	"&validityDate=" + validityStr;
                	}	
                	if (description != null) {
                		params += "&description=" + URLEncoder.encode(description, "UTF-8"); 
                	}
                	if (email != null) {
                		params += "&email=" + email;
                	}
                	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
                	String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl), "POST", null, params);
                	logger.log(Level.INFO, "Received response: " + landmarksJson);
                } catch (Exception e) {
                	logger.log(Level.SEVERE, e.getMessage(), e);
                }
                
                if (username.length() % 4 == 0) {
                	try {
                		username = new String(Base64.decode(username));
                	} catch (Exception e) {
                		//from version 1086, 86 username is Base64 encoded string
                	}
                }	
                
                if (StringUtils.isNotEmpty(email)) {
                    try {
                        email = new String(CryptoTools.decrypt(Base64.decode(email.getBytes())));
                    } catch (Exception e) {
                        //logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }

                landmark = LandmarkPersistenceUtils.persistLandmark(name, description, latitude, longitude, altitude, username, validityDate, layer, email);

                if (landmark != null) {	
                    //After adding landmark remove from cache layer list for the location
                    //in order to make it visible immediately.
                    int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);
                    String layerKey = JSON_LAYER_LIST + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude) + "_" + radius;
                    logger.log(Level.INFO, "Removed from cache layer list {0}: {1}", new Object[]{layerKey, CacheUtil.remove(layerKey)});           
                	
                    //social notifications
                    String landmarkUrl = UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark));
                    key = landmark.getKeyString();
                    
                    String titleSuffix = "";
                    String userAgent = request.getHeader("User-Agent");
                    String[] tokens = StringUtils.split(userAgent, ",");
                    if (tokens != null) {
                        for (int i = 0; i < tokens.length; i++) {
                            String token = StringUtils.trimToEmpty(tokens[i]);
                            if (token.startsWith("Package:") || token.startsWith("Version:") || token.startsWith("Version Code:")) {
                                titleSuffix += " " + token;
                            }
                        }
                    }

                    String useCount = request.getHeader("X-GMS-UseCount");
                    String messageSuffix = "";
                    if (useCount != null) {
                        messageSuffix = " User has opened LM " + useCount + " times.";
                    }

                    String title = "New landmark";
                    if (StringUtils.isNotEmpty(titleSuffix)) {
                        title += titleSuffix;
                    }

                    String body = "Landmark: " + name + " has been created by user " + ConfigurationManager.SERVER_URL + "socialProfile?uid=" + username + "." + messageSuffix;
                    
                    String userUrl = ConfigurationManager.SERVER_URL;
                    if (StringUtils.equals(layer, "Social")) {
                    	userUrl += "blogeo/" + username;
                    } else {
                    	userUrl += "showUser/" + username;
                    }
                    
                    Queue queue = QueueFactory.getQueue("notifications");
                    queue.add(withUrl("/tasks/notificationTask").
                    		param("key", key).
                    		param("landmarkUrl", landmarkUrl).
                    		param("email", StringUtils.isNotEmpty(email) ? email : "").
                    		param("title", title).
                    		param("userUrl", userUrl).
                    		param("username", username).
                    		param("body", body));              
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (key != null) {
                response.setHeader("key", key);
            }
            if (landmark != null && landmark.getHash() != null) {
                response.setHeader("hash", landmark.getHash());
            }
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("Landmark created.");
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
        return "Persist Landmark Servlet";
    }
}
