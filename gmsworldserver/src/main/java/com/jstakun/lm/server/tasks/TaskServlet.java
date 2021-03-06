package com.jstakun.lm.server.tasks;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.FileUtils;
import com.jstakun.lm.server.utils.RHCloudUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;

import net.gmsworld.server.layers.ExchangeRatesApiUtils;
import net.gmsworld.server.layers.GeocodeUtils;
import net.gmsworld.server.utils.ImageUtils;
import net.gmsworld.server.utils.NumberUtils;

/**
 *
 * @author jstakun
 */
public class TaskServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TaskServlet.class.getName());
	
	/** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            String entity = request.getParameter("entity");
            String action = request.getParameter("action");
            
            if (request.getQueryString() != null) {
            	logger.log(Level.INFO, "Running " + request.getRequestURI() + "?" + request.getQueryString());
            }
            
            if (StringUtils.equalsIgnoreCase(action, "purge")) {
                if (StringUtils.equalsIgnoreCase(entity, "log")) {
                    //long count = ServiceLogPersistenceUtils.deleteAllLogs();
                    //Date nDaysAgo = DateUtils.getDayInPast(Integer.parseInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.LOG_OLDER_THAN_DAYS, "60")), false);                        
                    //long count = ServiceLogPersistenceUtils.deleteLogsOlderThanDate(nDaysAgo);
                    //logger.log(Level.INFO, "Deleted {0} logs.", count);
                } else if (StringUtils.equalsIgnoreCase(entity, "screenshot")) {
                  	int ndays = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.SCREENSHOT_OLDER_THAN_DAYS, "90"), 90);
                    long count = ScreenshotPersistenceUtils.deleteScreenshotsOlderThanDate(ndays);
                    logger.log(Level.INFO, "Deleted {0} screenshots.", count);
                } else {
                    logger.log(Level.INFO, "Wrong parameter entity: {0}", entity);
                }
             } else if (StringUtils.equalsIgnoreCase(action, "rhcloud")) {
               RHCloudUtils.rhcloudHealthCheck();
               logger.log(Level.INFO, "Done");
             } else if (StringUtils.equalsIgnoreCase(action, "loadImage")) {
                //save map image thumbnail
                final Double latitude = GeocodeUtils.getLatitude(request.getParameter("latitude"));
                final Double longitude = GeocodeUtils.getLongitude(request.getParameter("longitude"));
                if (latitude != null && longitude != null) {
                	final String imageName = FileUtils.getLocationImageName(latitude, longitude);
        			final String imageUrl = FileUtils.getImageUrlV2(null, imageName, true, request.isSecure());
        			if (imageUrl == null) {					
        				byte[] thumbnail = ImageUtils.loadImage(latitude, longitude, "170x170", 9, net.gmsworld.server.config.ConfigurationManager.MAP_PROVIDER.OSM_MAPS, request.isSecure()); 
        				if (thumbnail != null && thumbnail.length > 0) {
        					FileUtils.saveFileV2(null, imageName, thumbnail, latitude, longitude);
        				} else {
        					thumbnail = ImageUtils.loadImage(latitude, longitude, "170x170", 9, net.gmsworld.server.config.ConfigurationManager.MAP_PROVIDER.GOOGLE_MAPS, request.isSecure()); 
            				if (thumbnail != null && thumbnail.length > 0) {
            					FileUtils.saveFileV2(null, imageName, thumbnail, latitude, longitude);
            				}
        					logger.log(Level.WARNING, "Failed to load map image ");
        				}
        			} else {
        				logger.log(Level.INFO, "Image {0} found in the storage.", imageUrl);
        			}
                } else {
                	logger.log(Level.SEVERE, "Wrong latitude and/or longitude parameter(s) value.");
                }   
             } else if (StringUtils.equalsIgnoreCase(action, "currency")) {
            	 ExchangeRatesApiUtils.loadAllCurrencies(GoogleCacheProvider.getInstance());
             } else {
            	 logger.log(Level.SEVERE, "Wrong parameter action: {0}", action);
             }            
        } catch (Exception e) {
    	    logger.log(Level.SEVERE, e.getMessage(), e);
    	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        return "Cron Tasks Servlet";
    }
}
