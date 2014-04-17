/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.FileUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class ImageUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ImageUploadServlet.class.getName());
    private static final int ONE_MB = 1024 * 1024;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            double lat = NumberUtils.getDouble(request.getHeader("X-GMS-Lat"), Double.NaN);
            double lng = NumberUtils.getDouble(request.getHeader("X-GMS-Lng"), Double.NaN);
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (!Double.isNaN(lat) && !Double.isNaN(lng) && isMultipart) {

                ServletFileUpload upload = new ServletFileUpload();
                upload.setSizeMax(ONE_MB); //1 MB

                FileItemIterator iter = upload.getItemIterator(request);

                String output = null;

                if (iter != null && iter.hasNext()) {

                    FileItemStream item = iter.next();

                    String itemName = item.getName();

                    logger.log(Level.INFO, "Found file {0} with type {1}", new Object[]{itemName, item.getContentType()});

                    if (StringUtils.startsWith(itemName, "screenshot")) {

                        FileUtils.saveFileV2(itemName, item.openStream(), lat, lng);
                        
                        String username = StringUtil.getUsername(request.getAttribute("username"),request.getHeader("username"));
                        
                        String key = ScreenshotPersistenceUtils.persistScreenshot(username, lat, lng, itemName);
                        
                        if (key != null) {
                            //String imageUrl = FileUtils.getImageUrlV2(itemName);
                        	String imageUrl = ConfigurationManager.SERVER_URL + "image/" + key;
                        	String showImageUrl = UrlUtils.getShortUrl(ConfigurationManager.SERVER_URL + "showImage/" + key);
                    		
                            Map<String, String> params = new ImmutableMap.Builder<String, String>().
                            put("showImageUrl", showImageUrl).
                            put("imageUrl", imageUrl).
                            put("lat", Double.toString(lat)).
                            put("lng", Double.toString(lng)).
                            put("username", StringUtils.isNotEmpty(username) ? username : "").build();
                    		NotificationUtils.createImageCreationNotificationTask(params);
                    		
                    		output = "File saved with key " + key;
                        } else {
                        	output = "Key is empty!";
                            logger.log(Level.SEVERE, "Key is empty!");
                            logger.log(Level.INFO, "Deleted file " + FileUtils.deleteFileV2(itemName));
                        }
                    } else {
                        output = "File is not a screenshot.";
                    }
                } else {
                    output = "No file uploaded.";
                }
                out.print(output);
                logger.log(Level.INFO, output);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ImageUploadServlet.processRequest() exception", e);
            out.print("Failed to save file: " + e.getMessage() + ".");
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Screenshot Upload Servlet";
    }
}
