package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.ConfigurationManager;

import net.gmsworld.server.utils.HttpUtils;

/**
 * Servlet implementation class HotelsBookingServlet
 */
public class HotelsBookingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static final Logger logger =  Logger.getLogger(HotelsBookingServlet.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HotelsBookingServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String key = request.getParameter("key");
		if (StringUtils.isEmpty(key)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
        	String bookingUrl = null;
        	String cc = request.getParameter("cc");
        	String city = request.getParameter("city");
        	if (StringUtils.isNotEmpty(cc) && StringUtils.isNotEmpty(city)) {
        	
        		String normalizedCity = city.toLowerCase(Locale.US).replaceAll(" ", "-");
        		if (normalizedCity.equals("new-york-city")) {
        			normalizedCity = "new-york";
        		} else if (normalizedCity.endsWith("buenos-aires")) {
        			normalizedCity = "buenos-aires";
        		}
        		bookingUrl = String.format(ConfigurationManager.BOOKING_URL, cc.toLowerCase(Locale.US), normalizedCity);
        	
        		try {
        			HttpUtils.processFileRequest(new URL(bookingUrl));
        	
        			if (HttpUtils.getResponseCode(bookingUrl) == 404) {
        				logger.log(Level.WARNING, "Wrong url: " + bookingUrl);
        			    bookingUrl = null;
        			}
        	
        		} catch (Exception e) {
        			logger.log(Level.SEVERE, e.getMessage(), e);
        			bookingUrl = null;
        		}
        	
        	}
        	
        	if (bookingUrl != null) {
        		response.sendRedirect(bookingUrl);
        	} else {
        		response.sendRedirect("/showLandmark/" + key + "?enabled=Hotels&fullScreenLandmarkMap=true");
        	}
        }
	}

}
