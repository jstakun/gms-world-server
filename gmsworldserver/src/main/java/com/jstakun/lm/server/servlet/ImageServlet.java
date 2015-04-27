package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		if (StringUtils.isNotEmpty(key)) {
			boolean thumbnail = true;
			if (StringUtils.equals(request.getParameter("thumbnail"), "false")) {
				thumbnail = false;
			}
			Screenshot s = FileUtils.getScreenshot(key, thumbnail);
			if (s != null) {
				imageUrl = s.getUrl();
				response.sendRedirect(imageUrl);
			} 
		} else {
			try {
				final double lat = Double.valueOf(request.getParameter("lat")).doubleValue();
				final double lng = Double.valueOf(request.getParameter("lng")).doubleValue();
				try {
					String image = "landmark_" + StringUtil.formatCoordE6(lat) + "_" + StringUtil.formatCoordE6(lng) + ".jpg";
					imageUrl = FileUtils.getImageUrlV2(image, true);
					logger.log(Level.INFO, "Loading image " + image + " from cache.");
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage());
					imageUrl = ImageUtils.getGoogleMapsImageUrl(lat, lng, "128x128", 9, false);
				}
				response.sendRedirect(imageUrl);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		} 
		
		if (imageUrl == null) {
			response.setContentType("image/png");
			imageUrl = "/images/location.png";
			request.getRequestDispatcher(imageUrl).include(request, response);
		}
	}
}
