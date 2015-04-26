package net.gmsworld.server.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;

public class ImageUtils {

	private static final double BLACK_FACTOR = 0.75;
	private static final Logger logger = Logger.getLogger(ImageUtils.class.getName());
    
	public static boolean isBlackImage(BufferedImage image) {
		boolean isBlack = false;
		int blackPixelsCount = 0;
		int w = image.getWidth();
	    int h = image.getHeight();
	    int totalPixels = w * h;
	    int blackFactor = (int)(totalPixels * BLACK_FACTOR);
	    
	    for (int i = 0; i < h; i++) {
	    	for (int j = 0; j < w; j++) {
	    		int pixel = image.getRGB(j, i);
	    		if (((pixel & 0x00FFFFFF) == 0)) {
	    			blackPixelsCount++;
	    			if (blackPixelsCount > blackFactor) {
	    				isBlack = true;
	    				break;
	    			}
	    		}
	    	}
	    }  
		
	    logger.log(Level.INFO, "Image has " + String.format("%1.4f", ((double)blackPixelsCount/(totalPixels))) + " black factor.");
	    
	    return isBlack;
	}
	
	public static boolean isBlackImage(byte[] imageData) {
		boolean isBlack = false;
		
		int blackPixelsCount = 0;
		int totalPixels = imageData.length / 3;	  
		int blackFactor = (int)(totalPixels * BLACK_FACTOR);
	    
		for (int i = 0; i < totalPixels; i++) {
		    int pixel = 0xFF000000 | 
		        ((imageData[3 * i + 0] & 0xFF) << 16) |
		        ((imageData[3 * i + 1] & 0xFF) << 8) |
		        ((imageData[3 * i + 2] & 0xFF));
		    
		    if ((pixel & 0x00FFFFFF) == 0) {
    			blackPixelsCount++;
    			if (blackPixelsCount > blackFactor) {
    				isBlack = true;
    				break;
    			}
    		}
		}
		logger.log(Level.INFO, "Image has " + String.format("%1.4f", ((double)blackPixelsCount/(totalPixels))) + " black factor.");	    
		return isBlack;
	}
	
	public static String getGoogleMapsImageUrl(double latitude, double longitude, String size, int zoom) {
		String lat = StringUtil.formatCoordE6(latitude);
		String lng = StringUtil.formatCoordE6(longitude);
		return "http://maps.google.com/maps/api/staticmap?center=" + lat + "," + lng + "&zoom=" + zoom + "&size=" + size + "&markers=icon:http://gms-world.appspot.com/images/flagblue.png|" + lat + "," + lng; // + "&key=" + Commons.getProperty(Commons.Property.GOOGLE_API_KEY);
	}
	
	public static String getGoogleMapsPathUrl(List<Double> path, String size, int zoom) {
		//TODO not yet implemented
		//path=color:0xff0000ff|weight:5|40.737102,-73.990318|40.749825,-73.987963|40.752946,-73.987384|40.755823,-73.986397
	    return null;
	}
	
	public static byte[] loadImage(String imageUrl) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		HttpUtils.processImageFileRequest(out, imageUrl);
		return out.toByteArray();
	}
	
	public static byte[] loadImage(double latitude, double longitude, String size, int zoom) throws IOException {
		return loadImage(getGoogleMapsImageUrl(latitude, longitude, size, zoom));
	}
}
