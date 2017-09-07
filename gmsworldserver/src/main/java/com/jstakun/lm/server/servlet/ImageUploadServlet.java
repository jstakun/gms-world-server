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

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.ImageUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.FileUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
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
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/plain;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			double lat = NumberUtils.getDouble(request.getHeader(Commons.LAT_HEADER), Double.NaN);
			double lng = NumberUtils.getDouble(request.getHeader(Commons.LNG_HEADER), Double.NaN);
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			String myPosKey = request.getHeader(Commons.MYPOS_KEY_HEADER);
			String cacheKey = "screenshot_" + StringUtil.formatCoordE2(lat) + "_" + StringUtil.formatCoordE2(lng);
			boolean silent = StringUtils.equals(request.getHeader("X-GMS-Silent"), "true");
			String bucketName =  request.getHeader("X-GMS-BucketName");

			if (CacheUtil.containsKey(cacheKey)) {
				logger.log(Level.WARNING, "This screenshot is similar to newest: " + cacheKey);
			} else if (!Double.isNaN(lat) && !Double.isNaN(lng) && isMultipart) {

				ServletFileUpload upload = new ServletFileUpload();
				upload.setSizeMax(ONE_MB); // 1 MB

				FileItemIterator iter = upload.getItemIterator(request);

				String output = null;

				if (iter != null && iter.hasNext()) {

					FileItemStream item = iter.next();

					String itemName = item.getName();

					logger.log(Level.INFO, "Found file {0} with type {1}",
							new Object[] { itemName, item.getContentType() });

					if (StringUtils.startsWith(itemName, "screenshot")) {

						byte[] screenshot = IOUtils.toByteArray(item.openStream());

						if (screenshot != null && screenshot.length > 3) {

							itemName = System.currentTimeMillis() + "_" + itemName;

							if (ImageUtils.isBlackImage(screenshot)) {
								logger.log(Level.SEVERE, "This image is black and won't be saved.");
								output = "Image is black.";
							} else {

								FileUtils.saveFileV2(bucketName, itemName, screenshot, lat, lng);

								if (!silent) {
									String username = StringUtil.getUsername(request.getAttribute("username"),
											request.getHeader("username"));

									String key = ScreenshotPersistenceUtils.persistScreenshot(username, lat, lng, itemName);

									if (key != null) {
										String imageUrl = ConfigurationManager.SERVER_URL + "image/" + key;
										String showImageUrl = ConfigurationManager.SERVER_URL + "showImage/" + key;
										if (StringUtils.isNotEmpty(myPosKey)) {
											showImageUrl += "/" + myPosKey;
											logger.log(Level.INFO,
													"This screenshot is linked with landmark " + myPosKey);
										} else {
											logger.log(Level.INFO, "This screenshot is not linked with any landmark");
										}
										showImageUrl = UrlUtils.getShortUrl(showImageUrl);

										CacheUtil.put(cacheKey, "1", CacheType.FAST);

										// load image from imageUrl and check if
										// it is black
										// sometimes uploaded image is black
										try {
											byte[] uploadedImage = ImageUtils.loadImage(imageUrl);
											if (uploadedImage != null && uploadedImage.length > 3
													&& ImageUtils.isBlackImage(uploadedImage)) {
												logger.log(Level.SEVERE,
														"Uploaded image " + key + " is black: " + imageUrl);
												output = "Uploaded image is black!";
											} else {
												Map<String, String> params = new ImmutableMap.Builder<String, String>()
														.put("showImageUrl", showImageUrl).put("imageUrl", imageUrl)
														.put("lat", Double.toString(lat))
														.put("lng", Double.toString(lng))
														.put("username",
																StringUtils.isNotEmpty(username) ? username : "")
														.build();
												NotificationUtils.createImageCreationNotificationTask(params);
												output = "File saved with key " + key;
											}
										} catch (Exception e) {
											logger.log(Level.SEVERE, "ImageUploadServlet.processRequest() exception",
													e);
											output = "Image upload failed!";
										}
										//
									} else {
										output = "Key is empty!";
										logger.log(Level.SEVERE, "Key is empty!");
										logger.log(Level.INFO, "Deleted file " + FileUtils.deleteFileV2(bucketName, itemName));
									}
								} else {
									output = UrlUtils.getShortUrl(FileUtils.getImageUrlV2(bucketName, itemName, false, true));
								}
							}
						} else {
							output = "Empty screenshot found.";
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
				logger.log(Level.WARNING, "Latitude is NaN: " + Double.isNaN(lat) +  ", Longitude is NaN: " + Double.isNaN(lng) + ", Is multipart: " + isMultipart);
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
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * 
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Screenshot Upload Servlet";
	}
}
