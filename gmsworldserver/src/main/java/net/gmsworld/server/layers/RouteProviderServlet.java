package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.RoutesUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

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
    
	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        GeocodeHelperFactory.setCacheProvider(GoogleCacheProvider.getInstance());
    }
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (HttpUtils.isEmptyAny(request, "lat_start", "lng_start", "lat_end", "lng_end", "type", "username") && HttpUtils.isEmptyAny(request, "route")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else if (!HttpUtils.isEmptyAny(request, "lat_start", "lng_start", "lat_end", "lng_end", "type", "username")) {
                String username = request.getParameter("username");
                String type = request.getParameter("type");
                double lat_start = NumberUtils.getDouble(request.getParameter("lat_start"), 0d);
                double lng_start = NumberUtils.getDouble(request.getParameter("lng_start"), 0d);
                double lat_end = NumberUtils.getDouble(request.getParameter("lat_end"), 0d);
                double lng_end = NumberUtils.getDouble(request.getParameter("lng_end"), 0d);
                
                String lat_startStr = StringUtil.formatCoordE6(lat_start) ;
                String lng_startStr = StringUtil.formatCoordE6(lng_start);
                String lat_endStr = StringUtil.formatCoordE6(lat_end);
                String lng_endStr = StringUtil.formatCoordE6(lng_end);

                JSONObject route = RoutesUtils.getFromServer(lat_startStr, lng_startStr, lat_endStr, lng_endStr, type, username); 

                if (route == null) {
                  	response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);                
                } else {
            		String pathKey = "path_" + StringUtil.formatCoordE6(lat_start) + "_" + StringUtil.formatCoordE6(lng_start) + "_" + StringUtil.formatCoordE6(lat_end) + "_" + StringUtil.formatCoordE6(lng_end);
                	
                	try {
                			
                		RoutesUtils.saveImage(pathKey, route, request.isSecure(), lat_start, lng_start) ;	
                			
                		//send route creation social notification
                		String imageUrl = ConfigurationManager.SERVER_URL + "image?lat_start=" + lat_startStr + "&lng_start=" + lng_startStr + "&lat_end=" + lat_endStr + "&lng_end=" + lng_endStr;
                		String showRouteUrl = UrlUtils.getShortUrl(ConfigurationManager.SERVER_URL + "showRoute/" + lat_startStr + "/" + lng_startStr + "/" + lat_endStr + "/" + lng_endStr);
                			
                		String routeType = StringUtils.split(type,"/")[0];
                			
                		Map<String, String> params = new ImmutableMap.Builder<String, String>().
                		            put("username", username).
                		    		put("routeType", routeType).
                		    		put("lat", Double.toString(lat_start)).
                		    		put("lng", Double.toString(lng_start)).
                		    		put("showRouteUrl", showRouteUrl).
                		    		put("imageUrl", imageUrl).build();  
                			
                		NotificationUtils.createRouteCreationNotificationTask(params);
                		            		
                	} catch (Exception e) {
                		logger.log(Level.SEVERE, e.getMessage(), e);
                	}
                	
                	if (route != null) {
                		route.put("name", pathKey + "_" + username + "_" + type);
                		RoutesUtils.cache(route.toString());
                		//GoogleCacheProvider.getInstance().putToSecondLevelCache(pathKey, output.toString());
                	}
                    
                    out.print(route.toString());
                } 
            } else if (!HttpUtils.isEmptyAny(request, "route")) {
                 //Load route from cache
            	String json = RoutesUtils.loadFromCache(request.getParameter("route"), request.getParameter("now"));
            	if (!StringUtils.startsWith(json, "{")) {
            		json = "{\"features\":[]}";
            		//response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            	}
            	String callBackJavaScripMethodName = request.getParameter("callback");
            	if (StringUtils.isNotEmpty(callBackJavaScripMethodName)) {
                		json = callBackJavaScripMethodName + "("+ json + ");";
                }
            	out.println(json);
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
         //json route upload
    	if (HttpUtils.isEmptyAny(request, "route")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
        	PrintWriter out = response.getWriter();
    		try {
    			final String routeStr = request.getParameter("route");
    			final String deviceName =  request.getHeader(Commons.DEVICE_NAME_HEADER);
        		if (StringUtils.startsWith(routeStr, "{")) {
        			JSONObject root = new JSONObject(routeStr);
        			if (root.has("name") && root.has("features")) {
        				String[] resp = RoutesUtils.cache(routeStr);
        				if (StringUtils.equals(resp[1], "200") && StringUtils.isNotEmpty(resp[0])) {
        					try {
        						final String routeName = root.getString("name");
        						final String[] tokens = StringUtils.split(routeName, "_");
        						String message;
        						if (tokens.length == 5) {
        							message = "New route saved: " + routeName + "\n" + ConfigurationManager.SERVER_URL + "dlr/" + tokens[3] + "/" + tokens[4];
        						} else {
        							message = "New route saved: " + routeName;
        						}
        						try {
        							final JSONObject route = root.getJSONArray("features").getJSONObject(0);
        							message += "\nDescription: " + route.getJSONObject("properties").getString("description");
        							message += "\nRoute waypoints count: " + route.getJSONObject("geometry").getJSONArray("coordinates").length();  
        						} catch (Exception e) {
        							logger.log(Level.SEVERE, e.getMessage(), e);
        						}
        						String title = "New route";
        						if (StringUtils.isNotEmpty(deviceName)) {
        							title += " from device " + deviceName;
        						}
        						MailUtils.sendAdminMail(title, message);
        					} catch (Exception e) {
        						logger.log(Level.SEVERE, e.getMessage(), e);
        					}
        					out.println(resp[0]);
        					out.close();
        				} else {
        					logger.log(Level.SEVERE, "Server error: " + resp[1] + " " + resp[0]);
        					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        				}
        			} else {
        				logger.log(Level.WARNING, "Wrong route format: " + routeStr);
            	 		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        			}
        			root = null;
        		} else {
        			logger.log(Level.WARNING, "Wrong json format: " + routeStr);
        	 		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        	}
        }
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
