/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.persistence.Screenshot;
import com.jstakun.lm.server.utils.DateUtils;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.StringUtil;

/**
 *
 * @author jstakun
 */
public class ScreenshotPersistenceUtils {

    private static final Logger logger = Logger.getLogger(ScreenshotPersistenceUtils.class.getName());

    public static String persistScreenshot(String username, double latitude, double longitude, String filename)
    {
    	String key = null;
    	
        try {
        	String landmarksUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "addItem";
        	String params = "filename=" + filename + "&latitude=" + latitude + "&longitude=" + longitude + "&type=screenshot";
        	if (username != null) {
        		params += "&username=" + username;
        	}
        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
        	String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl), "POST", null, params, Commons.RH_GMS_USER);
        	logger.log(Level.INFO, "Received response: " + landmarksJson);
        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
        		JSONObject resp = new JSONObject(landmarksJson);
        		key = resp.getString("id");
        	}	
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return key;
    }

    public static long deleteScreenshotsOlderThanDate(Date day) {
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
    }

    public static Screenshot selectScreenshot(String k) {
    	if (StringUtils.isNumeric(k)){
    		return getRemoteScreenshot(k);
    	} else {
    		return getLocalScreenshot(k);
    	}
    }
    
    private static Screenshot getLocalScreenshot(String k) {
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
    }
    
    private static Screenshot getRemoteScreenshot(String k)
    {
    	Screenshot s = null;
    	try {
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "itemProvider";
        	String params = "type=screenshot&id=" + k;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.RH_GMS_USER);
        	//logger.log(Level.INFO, "Received response: " + gJson);
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
