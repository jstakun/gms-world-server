package net.gmsworld.server.layers;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.FileUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.ImageUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;
import net.gmsworld.server.utils.memcache.CacheProvider;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class RouteProviderServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RouteProviderServlet.class.getName());
    private static final int LIMIT = 64;
    private CacheProvider cacheProvider = null;
	
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        cacheProvider = new GoogleCacheProvider();
        GeocodeHelperFactory.setCacheProvider(cacheProvider);
    }
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (HttpUtils.isEmptyAny(request, "lat_start", "lng_start", "lat_end", "lng_end", "type", "username")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String username = request.getParameter("username");
                String type = request.getParameter("type");
                double lat_start = NumberUtils.getDouble(request.getParameter("lat_start"), 0d);
                double lng_start = NumberUtils.getDouble(request.getParameter("lng_start"), 0d);
                double lat_end = NumberUtils.getDouble(request.getParameter("lat_end"), 0d);
                double lng_end = NumberUtils.getDouble(request.getParameter("lng_end"), 0d);

                JSONObject output = GeocodeHelperFactory.getMapQuestUtils().getRoute(lat_start, lng_start, lat_end, lng_end, type, username);

                if (output == null) {
                  	response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);                
                } else {
                	
                	try {
                		JSONArray route_geometry = output.optJSONArray("route_geometry");
                		if (route_geometry != null && route_geometry.length() > 1) {
                			int filter = route_geometry.length() / LIMIT + 1;
                			List<Double[]> path = new ArrayList<Double[]>(LIMIT);
                			
                			//remove every nth points to have up to LIMIT points
                			for (int i=0;i<route_geometry.length();i++) {
                	            if (i != 0 && i != (route_geometry.length()-1) && i % filter == 0) { 
                	            	JSONArray point = route_geometry.getJSONArray(i);	
                	            	path.add(new Double[]{point.getDouble(0), point.getDouble(1)});
                	            }
                			}
                			logger.log(Level.INFO, "Path has " + path.size() + " points");
                    
                			byte[] pathImage = ImageUtils.loadPath(path, "640x256");
                			String pathKey = "path_" + StringUtil.formatCoordE6(lat_start) + "_" + StringUtil.formatCoordE6(lng_start) + "_" + StringUtil.formatCoordE6(lat_end) + "_" + StringUtil.formatCoordE6(lng_end);
                			FileUtils.saveFileV2(pathKey + ".jpg", pathImage, lat_start, lng_start);
                			
                			//send route creation social notification
                			String imageUrl = ConfigurationManager.SERVER_URL + "image?lat_start=" + lat_start + "&lng_start=" + lng_start + "&lat_end=" + lat_end + "&lng_end=" + lng_end;
                			String showRouteUrl = UrlUtils.getShortUrl(ConfigurationManager.SERVER_URL + "showRoute/" + + lat_start + "/" + lng_start + "/" + lat_end + "/" + lng_end);
                			
                			String routeType = StringUtils.split(type,"/")[0];
                			
                			Map<String, String> params = new ImmutableMap.Builder<String, String>().
                		            put("username", username).
                		    		put("routeType", routeType).
                		    		put("lat", Double.toString(lat_start)).
                		    		put("lng", Double.toString(lng_start)).
                		    		put("showRouteUrl", showRouteUrl).
                		    		put("imageUrl", imageUrl).build();  
                			
                			NotificationUtils.createRouteCreationNotificationTask(params);
                			
                			if (cacheProvider != null) {
                				cacheProvider.putToSecondLevelCache(pathKey, output.toString());
                			}
                		}
                	} catch (Exception e) {
                		logger.log(Level.SEVERE, e.getMessage(), e);
                	}
                    
                    out.print(output.toString());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } finally {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Routes provider";
    }
}
