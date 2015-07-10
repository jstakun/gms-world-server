package net.gmsworld.server.layers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.StringUtil;

import com.google.appengine.api.ThreadManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

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
    			
    			LandmarkPersistenceUtils.persistLandmark(l);
    			if (l.getId() > 0) {
    				//LandmarkPersistenceUtils.notifyOnLandmarkCreation(l, request.getHeader("User-Agent"), null);
    				List<String> layers = Arrays.asList(new String[]{Commons.FACEBOOK_LAYER, Commons.FOURSQUARE_LAYER, Commons.HOTELS_LAYER});	
    				LayersLoader loader = new LayersLoader(ThreadManager.currentRequestThreadFactory() , layers);
    				loader.loadLayers(l.getLatitude(), l.getLongitude(), null, 20, 1132, 30, StringUtil.getStringLengthLimit("l"), null, null, Locale.US, false);
    				response.sendRedirect("/showLandmark/" + l.getId());
    			} else {
    				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			}
			}
		} catch (Exception e) {
	    	logger.log(Level.SEVERE, e.getMessage(), e);
	    } 
		
	}

}
