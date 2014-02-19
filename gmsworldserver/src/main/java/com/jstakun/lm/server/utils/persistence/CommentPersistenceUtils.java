/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

import com.jstakun.lm.server.persistence.Comment;
import com.jstakun.lm.server.utils.DateUtils;
import com.jstakun.lm.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class CommentPersistenceUtils implements Serializable {

    private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CommentPersistenceUtils.class.getName());

    public static void persistComment(String username, String landmarkKey, String message) {
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(new Comment(username, landmarkKey, message));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	try {
        	String landmarksUrl = "http://landmarks-gmsworld.rhcloud.com/actions/addItem";
        	String params = "username=" + username + "&landmarkId=" + landmarkKey + "&message=" + URLEncoder.encode(message, "UTF-8") + "&type=comment";
        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
        	String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl), "POST", null, params);
        	logger.log(Level.INFO, "Received response: " + landmarksJson);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static List<Comment> selectCommentsByLandmark(String landmarkKey){
    	List<Comment> results = new ArrayList<Comment>();
    	
    	/*PersistenceManager pm = PMF.get().getPersistenceManager();
        
        try {
            Query query = pm.newQuery(Comment.class);
            query.setOrdering("creationDate desc");
            query.setFilter("landmarkKey == lk");
            query.declareParameters("String lk");
            results = (List<Comment>) query.execute(landmarkKey);
            results = (List<Comment>) pm.detachCopyAll(results);
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	
    	try {
        	String gUrl = "http://landmarks-gmsworld.rhcloud.com/actions/itemProvider";
        	String params = "type=comment&landmarkId=" + landmarkKey;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequest(new URL(gUrl), "POST", null, params);
        	//logger.log(Level.INFO, "Received response: " + gJson);
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
