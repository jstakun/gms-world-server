/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.servlet;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Date;
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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class ImageUploadServlet extends HttpServlet {

    /**
	 * 
	 */
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

                        InputStream is = item.openStream();

                        long creationDate = System.currentTimeMillis();
                        FileService fileService = FileServiceFactory.getFileService();
                        AppEngineFile file = fileService.createNewBlobFile("image/jpeg", itemName);
                        FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);

                        int nRead;
                        byte[] data = new byte[8192];
                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            writeChannel.write(ByteBuffer.wrap(data, 0, nRead));
                        }

                        writeChannel.closeFinally();
                        BlobKey blobKey = fileService.getBlobKey(file);

                        String username = StringUtil.getUsername(request.getAttribute("username"),request.getParameter("username"));
                        
                        boolean auth = false;
                        if (StringUtils.isNotEmpty(username)) {
                            auth = true;
                        }

                        String key = null;

                        if (blobKey != null) {
                            output = "Saved file: " + blobKey.getKeyString() + ".";
                            key = ScreenshotPersistenceUtils.persistScreenshot(username, auth, lat, lng, blobKey, new Date(creationDate));
                        } else {
                            output = "Failed to save file due to blobstore error.";
                        }
        
                        if (key != null) {
                            //This URL is served by a high-performance dynamic image serving infrastructure that is available globally. The URL returned by this method is always public, but not guessable; private URLs are not currently supported. If you wish to stop serving the URL, delete the underlying blob key. This takes up to 24 hours to take effect. The URL format also allows dynamic resizing and crop with certain restrictions. To get dynamic resizing and cropping simply append options to the end of the url obtained via this call. Here is an example: getServingUrl -> "http://lh3.ggpht.com/SomeCharactersGoesHere"
                            //To get a 32 pixel sized version (aspect-ratio preserved) simply append "=s32" to the url: "http://lh3.ggpht.com/SomeCharactersGoesHere=s32"
                            //To get a 32 pixel cropped version simply append "=s32-c": "http://lh3.ggpht.com/SomeCharactersGoesHere=s32-c"
                            //Valid sizes are any integer in the range [0, 1600] (maximum is available as SERVING_SIZES_LIMIT).

                            ImagesService imagesService = ImagesServiceFactory.getImagesService();
                            ServingUrlOptions sou = ServingUrlOptions.Builder.withBlobKey(blobKey);
                            String imageUrl = imagesService.getServingUrl(sou);

                            Queue queue = QueueFactory.getQueue("notifications");
                            queue.add(withUrl("/tasks/notificationTask").
                            		param("key", key).
                            		param("imageUrl", imageUrl).
                            		param("lat", Double.toString(lat)).
                            		param("lng", Double.toString(lng)).
                            		param("username", StringUtils.isNotEmpty(username) ? username : ""));       
                            
                            //String showImageUrl = UrlUtils.getShortUrl(ConfigurationManager.SERVER_URL + "showImage/" + key);                     
                            //FacebookUtils.sendImageMessage(imageUrl, showImageUrl, username);
                            //TwitterUtils.sendImageMessage(showImageUrl, username, lat, lng);
                            //GoogleBloggerUtils.sendImageMessage(showImageUrl, username, imageUrl);
                            //GooglePlusUtils.sendImageMessage(showImageUrl, username, imageUrl);

                        } else {
                            logger.log(Level.SEVERE, "Key is null!");
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
            logger.log(Level.SEVERE, "ImageUploadServlet.processRequest() exception: ", e);
            out.print("Failed to save file: " + e.getMessage() + ".");
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        return "Image Upload Servlet";
    }// </editor-fold>
}
