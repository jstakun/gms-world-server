package com.jstakun.lm.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Screenshot;
import com.jstakun.lm.server.utils.FileUtils;

/**
 *
 * @author jstakun
 */
public class ScreenshotPersistenceUtils {

    private static final Logger logger = Logger.getLogger(ScreenshotPersistenceUtils.class.getName());

    public static String persist(String username, double latitude, double longitude, String filename)
    {
    	String key = null;
    	
        try {
        	final String landmarksUrl = ConfigurationManager.getBackendUrl() + "/addItem";
        	String params = "filename=" + filename + "&latitude=" + latitude + "&longitude=" + longitude + "&type=screenshot" + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
        	if (username != null) {
        		params += "&username=" + URLEncoder.encode(username, "UTF-8");
        	}
        	final String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl + "?" + params));
        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
        		JSONObject resp = new JSONObject(landmarksJson);
        		key = resp.optString("id");
        	}	
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return key;
    }
       
    public static int deleteScreenshotsOlderThanDate(int ndays) {
    	 int result = 0;
    	 
    	 try {
         	final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
         	final String params = "type=screenshot&ndays=" + ndays + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
         	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
         	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
         		JSONArray root = new JSONArray(gJson);
         		final int size = root.length();
         		logger.log(Level.INFO, size + " screenshots will be deleted...");
             	for (int i=0;i<size; i++) {
         			JSONObject screenshot = root.getJSONObject(i);
         			String filename = screenshot.getString("filename");
         			int id = screenshot.getInt("id");
         			if (deleteScreenshot(filename, id)) {
         				result++;
         			}
         		}
         	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
         	   	 
    	return result;
    }
    
    public static boolean deleteScreenshot(String filename, int id) {
    	boolean deleted = false;
    	try {
    		if (!FileUtils.deleteFileV2(null, filename)) {
    			logger.log(Level.SEVERE, "Failed to delete file {0} from screeshot {1}", new Object[] {filename, id});
    		}
    		final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
            final String params = "type=screenshot&id=" + id + "&action=remove" + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
    		final String response = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
    		logger.log(Level.INFO, "Deleting screenshot " + id + " response: " + response);
    		Integer responseCode = HttpUtils.getResponseCode(gUrl);
    		if (responseCode != null && responseCode == 200) {
    			deleted = true;    
    		}
    	} catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return deleted;
    }
    
    public static Screenshot selectScreenshot(String k)
    {
    	Screenshot s = null;
    	try {
        	final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
        	final String params = "type=screenshot&id=" + k + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		JSONObject root = new JSONObject(gJson);
        		if (root.has("latitude") && root.has("longitude")) {
        			s = jsonToScreenshot(root);
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return s;
    }
    
    private static Screenshot jsonToScreenshot(JSONObject geocode) throws IllegalAccessException, InvocationTargetException {
		Screenshot s = new Screenshot();
		   
		Map<String, String> geocodeMap = new HashMap<String, String>();
		for(Iterator<String> iter = geocode.keys();iter.hasNext();) {
				String key = iter.next();
				Object value = geocode.get(key);
				geocodeMap.put(key, value.toString());
		}
		   
		ConvertUtils.register(DateUtils.getRHCloudDateConverter(), Date.class);
		BeanUtils.populate(s, geocodeMap);
		   
		return s;
	}
}
