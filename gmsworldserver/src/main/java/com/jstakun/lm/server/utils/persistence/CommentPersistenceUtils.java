package com.jstakun.lm.server.utils.persistence;

import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
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
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Comment;

/**
 *
 * @author jstakun
 */
public class CommentPersistenceUtils implements Serializable {

    private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CommentPersistenceUtils.class.getName());

    public static void persist(String username, int landmarkKey, String message) {
        try {
        	final String landmarksUrl = ConfigurationManager.getBackendUrl() + "/addItem";
        	final String params = "username=" + URLEncoder.encode (username, "UTF-8") + "&landmarkId=" + landmarkKey + "&message=" + URLEncoder.encode(message, "UTF-8") 
        	                   + "&type=comment" + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);;
        	final String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl + "?" + params));
        	logger.log(Level.INFO, "Received response: " + landmarksJson);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static List<Comment> selectCommentsByLandmark(int landmarkKey){
    	List<Comment> results = new ArrayList<Comment>();
    	
    	try {
        	final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
        	final String params = "type=comment&landmarkId=" + landmarkKey + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
        	
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray arr = new JSONArray(gJson);
    		    for (int i=0;i<arr.length();i++) {
    		    	JSONObject commentJSon = arr.getJSONObject(i);
    		    	Comment c = new Comment();
    				Map<String, String> cMap = new HashMap<String, String>();
    				for(Iterator<String> iter = commentJSon.keys();iter.hasNext();) {
    					String name = iter.next();
    					Object value = commentJSon.get(name);
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
