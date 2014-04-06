package com.jstakun.lm.server.servlet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.layers.FacebookUtils;
import com.jstakun.lm.server.layers.LayerHelperFactory;
import com.jstakun.lm.server.utils.GeocodeUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.restfb.exception.FacebookOAuthException;



/**
 *
 * @author jstakun
 */
public class FBPhotosServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FBPhotosServlet.class.getName());
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    	String format = StringUtil.getStringParam(request.getParameter("format"), "json");
    	int version = NumberUtils.getVersion(request.getParameter("version"), 1);
        PrintWriter out = null;
    	//ObjectOutputStream outObj = null;
    	if (format.equals("bin")) {
    		if (version >= 12) {
        		response.setContentType("deflate");
        	} else {
        		response.setContentType("application/x-java-serialized-object"); 
        	} 
    		//outObj = new ObjectOutputStream(response.getOutputStream());	
    	} else {
    		response.setContentType("text/json;charset=UTF-8");
            out = response.getWriter();
    	}
        try {
            if (HttpUtils.isEmptyAny(request,"lat","lng","token") && HttpUtils.isEmptyAny(request,"latitude","longitude","token")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String token = URLDecoder.decode(request.getParameter("token"), "UTF-8");
                double latitude;
                if (request.getParameter("lat") != null) {
                    latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
                } else {
                    latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
                }

                double longitude;
                if (request.getParameter("lng") != null) {
                    longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
                } else {
                    longitude = GeocodeUtils.getLongitude(request.getParameter("longitude"));
                }
                int limit = NumberUtils.getInt(request.getParameter("limit"), 30);
                int stringLimit = StringUtil.getStringLengthLimit(request.getParameter("display"));
                
                if (format.equals("bin")) {
                	List<ExtendedLandmark> landmarks = FacebookUtils.getFriendsPhotosToLandmark(latitude, longitude, version, limit, stringLimit, token, request.getLocale());
                	//outObj.writeObject(landmarks);
            		//outObj.flush();
                	LayerHelperFactory.getFacebookUtils().serialize(landmarks, response.getOutputStream(), version);
                } else {
                	String json = FacebookUtils.getFriendsPhotosToJSon(latitude, longitude, version, limit, stringLimit, token);              
                	//String json = new JSONObject().put("ResultSet", landmarks).toString();
                	out.print(json);
                }	
            }
        } catch (FacebookOAuthException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            if (out != null) {
            	out.print("{\"error\":{\"message\":\"Facebook authentication error\"}}");
            } else {
            	response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
        	if (format.equals("bin")) {
        		//if (outObj != null) {
        		//	outObj.close();
        		//}
        	} else {
        		if (out != null) {
        			out.close();
        		}
        	}
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
