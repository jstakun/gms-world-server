package net.gmsworld.server.layers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

import com.google.appengine.api.ThreadManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import com.openlapi.AddressInfo;

/**
 * Servlet implementation class BrowserLandmarkServlet
 */
public class BrowserLandmarkServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(BrowserLandmarkServlet.class.getName());
    
	/**
     * @see HttpServlet#HttpServlet()
     */
    public BrowserLandmarkServlet() {
        super();
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        GeocodeHelperFactory.setCacheProvider(new GoogleCacheProvider());
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
	
	private void processRequest(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (HttpUtils.isEmptyAny(request, "latitude", "longitude") && HttpUtils.isEmptyAny(request, "lat", "lng")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
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
	            Landmark l = new Landmark();
	    		l.setLatitude(latitude);
    			l.setLongitude(longitude);
                l.setName(Commons.MY_POSITION_LAYER);
    			l.setLayer(Commons.MY_POSITION_LAYER);
    			l.setUsername(Commons.getProperty(Commons.Property.MYPOS_USER));
    			
    			AddressInfo addressInfo = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(l.getLatitude(), l.getLongitude());
    			LandmarkPersistenceUtils.setFlex(l, addressInfo, request);
        		l.setDescription(addressInfo.getField(AddressInfo.EXTENSION)); 
    			
    			LandmarkPersistenceUtils.persistLandmark(l);
    			if (l.getId() > 0) {
    				LandmarkPersistenceUtils.notifyOnLandmarkCreation(l, request.getHeader("User-Agent"), null);
    				LayersLoader loader = new LayersLoader(ThreadManager.currentRequestThreadFactory() , Arrays.asList(Commons.getLayers()));
    				loader.loadLayers(l.getLatitude(), l.getLongitude(), null, 20, 1132, 30, StringUtil.getStringLengthLimit("l"), null, null, Locale.US, true);
    				response.setContentType("text/javascript;charset=UTF-8");
    				response.getWriter().println("{\"id\": " + l.getId() +"}");
    				response.getWriter().close();
    			} else {
    				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			}
    			//response.setContentType("text/javascript;charset=UTF-8");
				//response.getWriter().println("{\"id\": 44306}");
				//response.getWriter().close();
			}
		} catch (Exception e) {
	    	logger.log(Level.SEVERE, e.getMessage(), e);
	    } 
		
	}

}
