package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
import org.json.JSONObject;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.persistence.Landmark;
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
        PrintWriter out = response.getWriter();
        Landmark l = new Landmark();           	
         
        try {
            if (HttpUtils.isEmptyAny(request, "latitude", "longitude", "name", "username")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
            	l.setLatitude(GeocodeUtils.getLatitude(request.getParameter("latitude")));
                l.setLongitude(GeocodeUtils.getLongitude(request.getParameter("longitude")));
                l.setAltitude(NumberUtils.getDouble(request.getParameter("altitude"), 0.0));
                l.setName(request.getParameter("name"));
                String desc = request.getParameter("description");
                if (StringUtils.isNotEmpty(desc)) {
                	l.setDescription(desc);
                }

                String validityStr = request.getParameter("validityDate");
                if (StringUtils.isNotEmpty(validityStr)) {
                    long validity = Long.parseLong(validityStr);
                    Date current = new Date();
                    l.setValidityDate(new Date(current.getTime() + validity));
                } 
                
                String layer = StringUtil.getStringParam(request.getParameter("layer"), Commons.LM_SERVER_LAYER);
                logger.log(Level.INFO, "Creating new landmark in layer: " + layer);
                if (layer.equals(Commons.MY_POS_CODE)) {
                    l.setDescription(GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(l.getLatitude(), l.getLongitude()));
                }
                l.setLayer(layer);
               
                String u = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                if (u != null && u.length() % 4 == 0) {
                	try {
                		u = new String(Base64.decode(u));
                	} catch (Exception e) {
                			//from version 1086, 86 username is Base64 encoded string
                	}
                }	
                l.setUsername(u);
                
                String socialIds = request.getParameter("socialIds");
                
                boolean anonymous = StringUtil.getStringParam(request.getParameter("anonymous"), "1").equals("0");
                if (!anonymous) {
                	String email = request.getParameter("email");
                	if (StringUtils.isNotEmpty(email)) {
                		try {
                			email = new String(CryptoTools.decrypt(Base64.decode(email.getBytes())));
                		} catch (Exception e) {
                			email = "";
                		}
                		l.setEmail(email);
                	}            	
                }

                //check if this landmark has the same name and location as newest (last saved) landmark
                boolean isSimilarToNewest = LandmarkPersistenceUtils.isSimilarToNewest(l);
            	if (!isSimilarToNewest) {
            		int useCount = NumberUtils.getInt(request.getHeader(Commons.USE_COUNT_HEADER), 1);
            		int appId = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), -1);
            		int version = NumberUtils.getInt(request.getHeader(Commons.APP_VERSION_HEADER), -1);
            		
            		JSONObject flex = new JSONObject();
            		flex.put("useCount", useCount);
            		if (appId > -1) {
            			flex.put("appId", appId);
            		}
            		if (version > 0) {
            			flex.put("version", version);
            		}
            		l.setFlex(flex.toString());
            		
                	LandmarkPersistenceUtils.persistLandmark(l);

                	if (l.getId() > 0) {	
                    	//After adding landmark remove from cache layer list for the location
                    	//in order to make it visible immediately.
                    	int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);
                    	String layerKey = JSON_LAYER_LIST + "_" + StringUtil.formatCoordE2(l.getLatitude()) + "_" + StringUtil.formatCoordE2(l.getLongitude()) + "_" + radius;
                    	logger.log(Level.INFO, "Removed from cache layer list {0}: {1}", new Object[]{layerKey, CacheUtil.remove(layerKey)});           
                	    
                    	//send notification to social networks
                    	LandmarkPersistenceUtils.notifyOnLandmarkCreation(l, request.getHeader("User-Agent"), socialIds);
                	} 
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (l.getId() > 0) {
                response.setHeader("key", Integer.toString(l.getId()));
            }
            if (StringUtils.isNotEmpty(l.getHash())) {
                response.setHeader("hash", l.getHash());
            }
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("Landmark " + l.getId() + " created.");
            out.close();
        }
    }

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
