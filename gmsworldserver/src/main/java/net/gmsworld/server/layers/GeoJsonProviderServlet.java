package net.gmsworld.server.layers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.utils.memcache.CacheUtil;

import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

/**
 * Servlet implementation class geoJsonProviderServlet
 */
public class GeoJsonProviderServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(GeoJsonProviderServlet.class.getName());
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeoJsonProviderServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String json = null;
		try {
			if (HttpUtils.isEmptyAny(request, "lat", "lng", "layer")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				response.setContentType("text/javascript;charset=UTF-8");
				double lat = GeocodeUtils.getLatitude(request.getParameter("lat"));
			    double lng = GeocodeUtils.getLongitude(request.getParameter("lng"));
			    String layer = request.getParameter("layer");
			    String key = "geojson_" + StringUtil.formatCoordE2(lat) + "_" + StringUtil.formatCoordE2(lng) + "_" + layer;
				json = CacheUtil.getString(key);
			}	
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
        	if (! StringUtils.startsWith(json, "{")) {
				json = "{}";
			}
        	String callBackJavaScripMethodName = request.getParameter("callback");
        	if (StringUtils.isNotEmpty(callBackJavaScripMethodName)) {
        		json = callBackJavaScripMethodName + "("+ json + ");";
        	}
        	response.getWriter().write(json);
        	response.getWriter().close();
        }
	}
}
