package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.gdata.util.common.util.Base64;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.social.NotificationUtils;

import net.gmsworld.server.utils.CryptoTools;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
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
                boolean isSimilarToNewest = false;
                if (CacheUtil.containsKey(name + "_" + lat + "_" + lng)) {
                	isSimilarToNewest = true;
                	logger.log(Level.WARNING, "This landmark is similar to newest: " + name + "_" + lat + "_" + lng);
                } else {
                	CacheAction newestLandmarksAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
                		@Override
                		public Object executeAction() {
                			return LandmarkPersistenceUtils.selectNewestLandmarks();
                		}
                	});
                	
                	List<Landmark> landmarkList = (List<Landmark>)newestLandmarksAction.getObjectFromCache("newestLandmarks", CacheType.FAST);
                	if (!landmarkList.isEmpty()) {
                		Landmark newestLandmark = landmarkList.get(0);
                		logger.log(Level.INFO, "Newest landmark: " + newestLandmark.getName() + ", " + newestLandmark.getLatitude() + ", " + newestLandmark.getLongitude());
                		if (StringUtils.equals(newestLandmark.getName(), name) && StringUtils.equals(StringUtil.formatCoordE2(newestLandmark.getLatitude()), lat)
                			 && StringUtils.equals(StringUtil.formatCoordE2(newestLandmark.getLongitude()), lng)) {
                			logger.log(Level.WARNING, "This landmark is similar to newest: " + name + ", " + latitude + ", " + longitude);
                			isSimilarToNewest = true;
                		} else {
                			logger.log(Level.INFO, "This landmark is not similar to newest: " + name + ", " + latitude + ", " + longitude);
                		}
                	}
                }
                
                if (!isSimilarToNewest) {
                	Map<String, String> peristResponse = LandmarkPersistenceUtils.persistLandmark(name, description, latitude, longitude, altitude, username, validityDate, layer, email);

                	id = peristResponse.get("id");
                	hash = peristResponse.get("hash");
                
                	if (StringUtils.isNumeric(id)) {	
                    	//After adding landmark remove from cache layer list for the location
                    	//in order to make it visible immediately.
                    	int radius = NumberUtils.getRadius(request.getParameter("radius"), 3, 6371);
                    	String layerKey = JSON_LAYER_LIST + "_" + StringUtil.formatCoordE2(latitude) + "_" + StringUtil.formatCoordE2(longitude) + "_" + radius;
                    	logger.log(Level.INFO, "Removed from cache layer list {0}: {1}", new Object[]{layerKey, CacheUtil.remove(layerKey)});           
                	
                    	CacheUtil.put(name + "_" + lat + "_" + lng, "1", CacheType.FAST);
                    	//social notifications
                    
                    	String landmarkUrl = ConfigurationManager.SERVER_URL + "showLandmark/" + id;
                    	if (StringUtils.isNotEmpty(hash)) {
                    		landmarkUrl = UrlUtils.BITLY_URL + hash;
                    	} 
                                        
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
                    
                    	Map<String, String> params = new ImmutableMap.Builder<String, String>().
                            put("key", id).
                    		put("landmarkUrl", landmarkUrl).
                    		put("email", StringUtils.isNotEmpty(email) ? email : "").
                    		put("title", title).
                    		put("userUrl", userUrl).
                    		put("username", username).
                    		put("body", body).build();  
                    
                    	NotificationUtils.createLadmarkCreationNotificationTask(params);
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
