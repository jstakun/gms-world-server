package net.gmsworld.server.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;

import org.apache.commons.lang.StringUtils;

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
		
	    logger.log(Level.INFO, "Image has reached " + String.format("%1.4f", ((double)blackPixelsCount/(totalPixels))) + " black factor.");
	    
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
		logger.log(Level.INFO, "Image has reached " + String.format("%1.4f", ((double)blackPixelsCount/(totalPixels))) + " black factor.");	    
		return isBlack;
	}
		
	public static String getImageUrl(double latitude, double longitude, String size, int zoom, boolean anonymous, ConfigurationManager.MAP_PROVIDER mapProvider, boolean isSecure) {
		if (mapProvider == ConfigurationManager.MAP_PROVIDER.GOOGLE_MAPS) {
			return getGoogleMapsImageUrl(latitude, longitude, size, zoom, anonymous, isSecure);
		} else { //ConfigurationManager.MAP_PROVIDER.OSM_MAPS
			return getOpenStreetMapsImageUrl(latitude, longitude, size, zoom, isSecure);
		}
	}
	
	public static String getRouteUrl(List<Double[]> path, String size, boolean anonymous, boolean isSecure) throws UnsupportedEncodingException {
		String prefix = "http";
		if (isSecure) {
			prefix = "https";
		}
		String mapsUrl = prefix + "://maps.google.com/maps/api/staticmap?size=" + size; 
		if (!anonymous) {
			mapsUrl += "&key=" + Commons.getProperty(Commons.Property.GOOGLE_API_KEY);
		}
		if (!path.isEmpty()) {
			List<String> coords = new ArrayList<String>(path.size());
			for (Double[] point : path) {
				coords.add(StringUtil.formatCoordE6(point[0]) + "," + StringUtil.formatCoordE6(point[1])); 
			}
			mapsUrl += "&path=color:0xff0000ff" + URLEncoder.encode("|weight:5|" + StringUtils.join(coords, '|'), "UTF-8");
			mapsUrl += "&markers=color:green%7Clabel:S%7C" + coords.get(0);
			mapsUrl += "&markers=color:red%7Clabel:E%7C" + coords.get(coords.size()-1);
		}
		return mapsUrl;
	}
	
	public static byte[] loadImage(String imageUrl) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		HttpUtils.processImageFileRequest(out, imageUrl);
		return out.toByteArray();
	}
	
	public static byte[] loadImage(double latitude, double longitude, String size, int zoom, ConfigurationManager.MAP_PROVIDER mapProvider, boolean isSecure) throws IOException {
		return loadImage(getImageUrl(latitude, longitude, size, zoom, false, mapProvider, isSecure));
	}
	
	public static byte[] loadPath(List<Double[]> path, String size, boolean isSecure) throws IOException {
		return loadImage(getRouteUrl(path, size, false, isSecure));
	}
	
	//https://developers.google.com/maps/documentation/maps-static/start
	private static String getGoogleMapsImageUrl(double latitude, double longitude, String size, int zoom, boolean anonymous, boolean isSecure) {
		String lat = StringUtil.formatCoordE6(latitude);
		String lng = StringUtil.formatCoordE6(longitude);
		String prefix = "http";
		if (isSecure) {
			prefix = "https";
		}
		String mapsUrl = prefix + "://maps.google.com/maps/api/staticmap?center=" + lat + "," + lng + "&zoom=" + zoom + "&size=" + size + "&markers=icon:http://www.gms-world.net/images/flagblue.png|" + lat + "," + lng; 
		if (!anonymous) {
			mapsUrl += "&key=" + Commons.getProperty(Commons.Property.GOOGLE_API_KEY);
		}
		return mapsUrl;
	}
	
	//https://developer.mapquest.com/documentation/static-map-api/v5/map/
	private static String getOpenStreetMapsImageUrl(double latitude, double longitude, String size, int zoom, boolean isSecure) {
		String coords = latitude+","+longitude;
		String prefix = "http";
		if (isSecure) {
			prefix = "https";
		}
		//return prefix + "://www.mapquestapi.com/staticmap/v5/map?locations="+coords+"|http://www.gms-world.net/images/flagblue.png&zoom="+zoom+"&size="+size.replace('x', ',')+"&defaultMarker=marker-sm-3B5998&key="+Commons.getProperty(Property.MAPQUEST_APPKEY);
		return prefix + "://www.mapquestapi.com/staticmap/v5/map?center="+coords+"&locations="+coords+"&zoom="+zoom+"&size="+size.replace('x', ',')+"&defaultMarker=marker-sm-3B5998&key="+Commons.getProperty(Property.MAPQUEST_APPKEY);
	}
}
