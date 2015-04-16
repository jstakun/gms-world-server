package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.CryptoTools;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
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
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        GeocodeHelperFactory.setCacheProvider(new GoogleCacheProvider());
    }
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
        String id = null, hash = null;
        PrintWriter out = response.getWriter();

        try {
            if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "name", "username")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                double latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
                double longitude = GeocodeUtils.getLongitude(request.getParameter("longitude"));
                double altitude = NumberUtils.getDouble(request.getParameter("altitude"), 0.0);
                boolean anonymous = StringUtil.getStringParam(request.getParameter("anonymous"), "1").equals("0");

                String name = request.getParameter("name");
                String description = request.getParameter("description");

                Date validityDate = null;
                String validityStr = request.getParameter("validityDate");
                if (StringUtils.isNotEmpty(validityStr)) {
                    long validity = Long.parseLong(validityStr);
                    Date current = new Date();
                    validityDate = new Date(current.getTime() + validity);
                } 

                String layer = StringUtil.getStringParam(request.getParameter("layer"), "Public");
                logger.log(Level.INFO, "Creating new landmark in layer: " + layer);
                if (layer.equals(Commons.MY_POS_CODE)) {
                    description = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(latitude, longitude);
                }
               
                String username = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                if (username != null && username.length() % 4 == 0) {
                	try {
                		username = new String(Base64.decode(username));
                	} catch (Exception e) {
                			//from version 1086, 86 username is Base64 encoded string
                	}
                }	
                
                String email = null;
                if (!anonymous) {
                	email = request.getParameter("email");
                	if (StringUtils.isNotEmpty(email)) {
                		try {
                			email = new String(CryptoTools.decrypt(Base64.decode(email.getBytes())));
                		} catch (Exception e) {
                			//logger.log(Level.SEVERE, e.getMessage(), e);
                		}
                	}
                }

                //check if this landmark has the same name and location as newest (last saved) landmark
                String lat = StringUtil.formatCoordE2(latitude);
                String lng = StringUtil.formatCoordE2(longitude);
            	boolean isSimilarToNewest = LandmarkPersistenceUtils.isSimilarToNewest(name, lat, lng);
            	if (!isSimilarToNewest) {
            		int useCount = NumberUtils.getInt(request.getHeader("X-GMS-UseCount"), 1);
                	Map<String, String> peristResponse = LandmarkPersistenceUtils.persistLandmark(name, description, latitude, longitude, altitude, username, validityDate, layer, email, "{useCount:"+useCount+"}");

                	id = peristResponse.get("id");
                	hash = peristResponse.get("hash");
                
                	if (StringUtils.isNumeric(id)) {	
                    	//After adding landmark remove from cache layer list for the location
                    	//in order to make it visible immediately.
                    	int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);
                    	String layerKey = JSON_LAYER_LIST + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude) + "_" + radius;
                    	logger.log(Level.INFO, "Removed from cache layer list {0}: {1}", new Object[]{layerKey, CacheUtil.remove(layerKey)});           
                	    //
                    	String userAgent = request.getHeader("User-Agent");
                    	LandmarkPersistenceUtils.notifyOnLandmarkCreation(name, lat, lng, id, hash, layer, username, email, userAgent, useCount);
                	} 
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (id != null) {
                response.setHeader("key", id);
            }
            if (hash != null) {
                response.setHeader("hash", hash);
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
