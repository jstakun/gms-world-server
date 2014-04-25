/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.config.Commons.Property;
import com.jstakun.lm.server.persistence.Checkin;
import com.jstakun.lm.server.utils.DateUtils;
import com.jstakun.lm.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class CheckinPersistenceUtils {

    private static final Logger logger = Logger.getLogger(CheckinPersistenceUtils.class.getName());

    public static void persistCheckin(String username, String landmarkKey, Integer type) {
    	/*PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(new Checkin(username, landmarkKey, type));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	
    	try {
        	String landmarksUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "addItem";
        	String params = "username=" + username + "&landmarkId=" + landmarkKey + "&itemType=" + type + "&type=checkin";
        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
        	String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	logger.log(Level.INFO, "Received response: " + landmarksJson);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static List<Checkin> selectCheckinsByLandmark(String key) {
    	List<Checkin> results = new ArrayList<Checkin>();
        /*PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Query query = pm.newQuery(Checkin.class);
            query.setFilter("landmarkKey == key");
            query.setOrdering("creationDate desc");
            query.setRange(0, 100);
            query.declareParameters("String key");
            results = (List<Checkin>) query.execute(key);
            //pm.retrieveAll(results);
            results = (List<Checkin>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	
    	try {
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "itemProvider";
        	String params = "type=checkin&landmarkId=" + key;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray arr = new JSONArray(gJson);
    		    for (int i=0;i<arr.length();i++) {
    		    	JSONObject checkinJSon = arr.getJSONObject(i);
    		    	Checkin c = new Checkin();
    				Map<String, String> cMap = new HashMap<String, String>();
    				for(Iterator<String> iter = checkinJSon.keys();iter.hasNext();) {
    					String name = iter.next();
    					Object value = checkinJSon.get(name);
    					cMap.put(name, value.toString());
    				}   		    	
    				
    				ConvertUtils.register(DateUtils.getRHCloudDateConverter(), Date.class);
    				BeanUtils.populate(c, cMap);
    				
    		    	results.add(c);
    		    }
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return results;
    }
}
