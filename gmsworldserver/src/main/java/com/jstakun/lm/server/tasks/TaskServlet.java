package com.jstakun.lm.server.tasks;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.FileUtils;
import com.jstakun.lm.server.utils.RHCloudUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;

import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.ImageUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;

/**
 *
 * @author jstakun
 */
public class TaskServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TaskServlet.class.getName());
	
	private static final String[] currencies = {"EUR", "HUF","MXN","SEK","CHF","ILS","ZAR","MYR","CAD",
		"TRY","DKK","SGD","BRL","USD","IDR","RON","KRW","NOK","HKD","CZK","AUD","PHP",
		"CNY","HRK","BGN","NZD","JPY","INR","PLN","GBP","THB","RUB"};
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
                double latitude = NumberUtils.getDouble(request.getParameter("latitude"), -200d);
                double longitude = NumberUtils.getDouble(request.getParameter("longitude"), -200d);
                if (latitude > -200d && longitude > -200d) {
                	String image = "landmark_" + StringUtil.formatCoordE6(latitude) + "_" + StringUtil.formatCoordE6(longitude) + ".jpg";
        			String imageUrl = FileUtils.getImageUrlV2(null, image, true, request.isSecure());
        			if (imageUrl == null) {					
        				byte[] thumbnail = ImageUtils.loadImage(latitude, longitude, "170x170", 9, net.gmsworld.server.config.ConfigurationManager.MAP_PROVIDER.OSM_MAPS, request.isSecure()); 
        				if (thumbnail != null && thumbnail.length > 0) {
        					FileUtils.saveFileV2(null, image, thumbnail, latitude, longitude);
        				} else {
        					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        				}
        			} else {
        				logger.log(Level.INFO, "Image {0} found in the storage.", imageUrl);
        			}
                } else {
                	logger.log(Level.SEVERE, "Wrong latitude and/or longitude parameter(s) value.");
                }   
             } else if (StringUtils.equalsIgnoreCase(action, "currency")) {
            	 //for (Currency currency : Currency.getAvailableCurrencies()) {
            	 //	 loadCurrency(currency.getCurrencyCode());
            	 //}
            	 for (int i=0;i<currencies.length;i++) {
            		 loadCurrency(currencies[i]);
            	 }
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
    
    private void loadCurrency(String fromcc) {
    	final String currencyUrl = "http://api.fixer.io/latest?base=" + fromcc;
    	Map<String, Double> ratesMap = new HashMap<String, Double>();
		try {
			logger.log(Level.INFO, "Calling " + currencyUrl + "...");
			String resp = HttpUtils.processFileRequest(new URL(currencyUrl));							
			if (StringUtils.startsWith(resp, "{")) {
				JSONObject root = new JSONObject(resp);
				if (root.has("error")) {
					logger.log(Level.WARNING, "Currency " + fromcc + " response error: " + root.getString("error"));
				} else {
					JSONObject rates = root.getJSONObject("rates");
					for (Iterator<String> keys=rates.keys();keys.hasNext();) {
						String key = keys.next();
						ratesMap.put(key, rates.getDouble(key));
					}
				}
				logger.log(Level.INFO, "Saving to cache " + currencyUrl + "...");
				GoogleCacheProvider.getInstance().put(currencyUrl, ratesMap, 1);
			} else {
				logger.log(Level.WARNING, currencyUrl + " received following response from the server: " + resp);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
    }
}
