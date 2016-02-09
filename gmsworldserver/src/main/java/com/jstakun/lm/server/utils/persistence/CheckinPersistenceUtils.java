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

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;

import com.jstakun.lm.server.persistence.Checkin;

/**
 *
 * @author jstakun
 */
public class CheckinPersistenceUtils {

    private static final Logger logger = Logger.getLogger(CheckinPersistenceUtils.class.getName());

    /*
     * type 0 - qrcode, 1 - GMS world landmark, 2 - social checkin (fb, fs, gg)
     */
    public static boolean persistCheckin(String username, String venueId, int landmarkKey, Integer type) {
    	boolean result = true;
    	try {
        	String landmarksUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "addItem?";
        	String params = "username=" + username + "&itemType=" + type + "&type=checkin";
        	if (landmarkKey > 0) {
        		params += "&landmarkId=" + landmarkKey;
        	}
        	if (StringUtils.isNotEmpty(venueId)) {
        		params += "&venueId=" + venueId;
        	}
        	//logger.log(Level.INFO, "Calling " + landmarksUrl + params);
        	String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl + params), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	logger.log(Level.INFO, "Received following server response: " + landmarksJson);
            if (StringUtils.contains(landmarksJson, "error")) {
            	result = false;
            }
    	} catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return result;
    }

    public static List<Checkin> selectCheckinsByLandmark(String landmarkid) {
    	List<Checkin> results = new ArrayList<Checkin>();
        
    	try {
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "itemProvider";
        	String params = "type=checkin&landmarkId=" + landmarkid;			 
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
    				
    				try {
    					Date d = new Date(Long.parseLong(cMap.get("creationDateLong")));
    					c.setCreationDate(d);
    				} catch (Exception e) {
    					logger.log(Level.SEVERE, e.getMessage(), e);
    				}
    				
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
