package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.ImageUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.persistence.Screenshot;
import com.jstakun.lm.server.utils.FileUtils;

/**
 * Servlet implementation class ImageServlet
 */
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ImageServlet.class.getName());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageServlet() {
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
	
	/**
	 * @see HttpServlet#process(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String key = request.getParameter("key");
		String imageUrl = null;
		boolean thumbnail = true;
		if (StringUtils.equals(request.getParameter("thumbnail"), "false")) {
			thumbnail = false;
		}
		if (StringUtils.isNotEmpty(key)) {
			Screenshot s = FileUtils.getScreenshot(key, thumbnail, request.isSecure());
			if (s != null) {
				imageUrl = s.getUrl();
			} 
		} else if (StringUtils.isNotEmpty(request.getParameter("lat")) && StringUtils.isNotEmpty(request.getParameter("lng"))) {
			try {
				final double lat = Double.valueOf(request.getParameter("lat")).doubleValue();
				final double lng = Double.valueOf(request.getParameter("lng")).doubleValue();
				String image = "landmark_" + StringUtil.formatCoordE6(lat) + "_" + StringUtil.formatCoordE6(lng) + ".jpg";
				imageUrl = FileUtils.getImageUrlV2(null, image, thumbnail, request.isSecure());
				if (imageUrl == null) {					
					imageUrl = ImageUtils.getImageUrl(lat, lng, "170x170", 9, thumbnail, ConfigurationManager.MAP_PROVIDER.OSM_MAPS, request.isSecure());
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else if (StringUtils.isNotEmpty(request.getParameter("lat_start")) && StringUtils.isNotEmpty(request.getParameter("lng_start")) &&
				   StringUtils.isNotEmpty(request.getParameter("lat_end")) && StringUtils.isNotEmpty(request.getParameter("lng_end"))) {
			try {
				final double lat_start = Double.valueOf(request.getParameter("lat_start")).doubleValue();
				final double lng_start = Double.valueOf(request.getParameter("lng_start")).doubleValue();
				final double lat_end = Double.valueOf(request.getParameter("lat_end")).doubleValue();
				final double lng_end = Double.valueOf(request.getParameter("lng_end")).doubleValue();
			
				String image = "path_" + StringUtil.formatCoordE6(lat_start) + "_" + StringUtil.formatCoordE6(lng_start) + "_" + StringUtil.formatCoordE6(lat_end) + "_" + StringUtil.formatCoordE6(lng_end) + ".jpg";
				imageUrl = FileUtils.getImageUrlV2(null, image, thumbnail, request.isSecure());
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}	
		}
		
		if (imageUrl != null) {
			logger.log(Level.INFO, "Loading image " + imageUrl);			
            response.setContentType("image/jpg");
            response.sendRedirect(imageUrl);
		} else {
			response.setContentType("image/png");
			imageUrl = "/images/location.png";
			request.getRequestDispatcher(imageUrl).include(request, response);
		}
	}
}
