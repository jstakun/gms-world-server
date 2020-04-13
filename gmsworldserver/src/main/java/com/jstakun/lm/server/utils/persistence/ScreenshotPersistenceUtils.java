package com.jstakun.lm.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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
        	final String landmarksUrl = ConfigurationManager.getBackendUrl(ConfigurationManager.GMS_LANDMARK_URL) + "/addItem";
        	String params = "filename=" + filename + "&latitude=" + latitude + "&longitude=" + longitude + "&type=screenshot";
        	if (username != null) {
        		params += "&username=" + username;
        	}
        	final String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
        		JSONObject resp = new JSONObject(landmarksJson);
        		key = resp.optString("id");
        	}	
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return key;
    }

    /*public static long deleteScreenshotsOlderThanDate(Date day) {
        int result = 0;
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("Screenshot");
        //query.setKeysOnly();
        Filter loginFilter =  new FilterPredicate("creationDate", FilterOperator.LESS_THAN, day);
        query.setFilter(loginFilter);
        final int chunk = 128;
        int count = 128;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        //5 mins limit
        while (count > 0 && (currentTime - startTime) < Commons.FIVE_MINS) {
            count = 0;
            for (Entity entity : ds.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunk))) {

                BlobKey blobKey = (BlobKey)entity.getProperty("blobKey");

                //delete blob with blobkey
                blobstoreService.delete(blobKey);

                ds.delete(entity.getKey());
                count++;
            }
            result += count;
            currentTime = System.currentTimeMillis();
        }

        return result;
    }*/
    
    public static int deleteScreenshotsOlderThanDate(int ndays) {
    	 int result = 0;
    	 
    	 try {
         	final String gUrl = ConfigurationManager.getBackendUrl(ConfigurationManager.GMS_LANDMARK_URL) + "/itemProvider";
         	final String params = "type=screenshot&ndays=" + ndays;			 
         	final String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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
    		final String gUrl = ConfigurationManager.getBackendUrl(ConfigurationManager.GMS_LANDMARK_URL) + "/itemProvider";
            final String params = "type=screenshot&id=" + id + "&action=remove";
    		final String response = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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

    /*public static Screenshot selectScreenshot(String k) {
    	if (StringUtils.isNumeric(k)){
    		return getRemoteScreenshot(k);
    	} else {
    		return getLocalScreenshot(k);
    	}
    }*/
    
    /*private static Screenshot getLocalScreenshot(String k) {
        Screenshot s = null;

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
        	if (StringUtil.isAllLowerCaseAndDigit(k)) {
        		Query query = pm.newQuery(Screenshot.class, "keyString == :k");
        		query.setUnique(true);
        		//query.declareParameters("String k");
        		s = (Screenshot) query.execute(k);
        	} else {
        		Key key = KeyFactory.stringToKey(k);
        		s = pm.getObjectById(Screenshot.class, key);
        		if (s != null) {
                    s.setKeyString(k.toLowerCase());
                    pm.makePersistent(s);
                }
        	}
        } catch (JDOObjectNotFoundException ex) {
        	logger.log(Level.SEVERE, ex.getMessage());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return s;
    }*/
    
    public static Screenshot selectScreenshot(String k)
    {
    	Screenshot s = null;
    	try {
        	final String gUrl = ConfigurationManager.getBackendUrl(ConfigurationManager.GMS_LANDMARK_URL) + "/itemProvider";
        	final String params = "type=screenshot&id=" + k;			 
        	final String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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
