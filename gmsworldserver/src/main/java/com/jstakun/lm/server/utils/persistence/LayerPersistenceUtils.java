/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.util.LabelValueBean;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Layer;
import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

/**
 *
 * @author jstakun
 */
public class LayerPersistenceUtils {

    private static final Logger logger = Logger.getLogger(LayerPersistenceUtils.class.getName());

    public static List<LabelValueBean> selectAllLayers() {
        List<LabelValueBean> results = new ArrayList<LabelValueBean>();
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(Layer.class);
            List<Layer> result = (List<Layer>) query.execute();
            for (Layer l : result) {
                results.add(new LabelValueBean(l.getFormatted(), l.getName()));
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
        List<Layer> allLayers = getAllLayers();
		for (Layer l : allLayers) {
			results.add(new LabelValueBean(l.getFormatted(), l.getName()));
		}
        
        return results;
    }

    public static List<Layer> listAllLayers(final int version) {
    	final String key = LayerPersistenceUtils.class.getName() + "_listAllLayers_" + version;
    	CacheAction layersCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			@Override
			public Object executeAction() {
				List<Layer> result = new ArrayList<Layer>();
				List<Layer> allLayers = getAllLayers();
	    		for (Layer l : allLayers) {
	    			if (l.geVersion() > 0 && l.geVersion() <= version) {
	    				result.add(l);
	    			}
	    		}	        
				return result;
			}
		});
	    return (List<Layer>) layersCacheAction.getObjectFromCache(key);
    }

    public static void persistLayer(String name, String desc, boolean enabled, boolean manageable, boolean checkinable, String formatted) {
        /*Layer layer = new Layer(name, desc, enabled, manageable, checkinable, formatted);
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(layer);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
    	try {
        	String landmarksUrl = "http://landmarks-gmsworld.rhcloud.com/actions/addItem";
        	String params = "name=" + name + "&desc=" + URLEncoder.encode(desc, "UTF-8") + 
        			        "&formatted=" + URLEncoder.encode(formatted, "UTF-8") + "&type=layer" +
        			        "&e=" + enabled + "&m=" + manageable + "&c=" + checkinable;
        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
        	String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl), "POST", null, params);
        	logger.log(Level.INFO, "Received response: " + landmarksJson);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static String getLayerFormattedName(final String name) {
    	/*String response = CacheUtil.getString(name);
        if (response == null) {
        	response = name;
        	PersistenceManager pm = PMF.get().getPersistenceManager();
        	try {
        		Layer layer = pm.getObjectById(Layer.class, name);
        		if (layer != null) {
        			response = layer.getFormatted();
        			CacheUtil.put(name, response);
        		}
        	} catch (Exception ex) {
        		logger.log(Level.SEVERE, ex.getMessage(), ex);
        	} finally {
        		pm.close();
        	}
        }*/
    	final String key = "layer_" + name;
    	CacheAction layersCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			@Override
			public Object executeAction() {
				List<Layer> allLayers = getAllLayers();
	    		for (Layer l : allLayers) {
	    		    if (StringUtils.equals(l.getName(), name)) {
	    		    	return l.getFormatted();
	    		    }
	    		}
	    		return null;
			}
    	});	   	
    	
        return (String) layersCacheAction.getObjectFromCache(key);
    }
    
    private static List<Layer> getAllLayers() {
    	final String key = LayerPersistenceUtils.class.getName() + "_getAllLayers";
    	CacheAction layersCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			@Override
			public Object executeAction() {
				List<Layer> layers = new ArrayList<Layer>();
				try {
					String gUrl = "http://landmarks-gmsworld.rhcloud.com/actions/itemProvider";
					String params = "type=layer";			 
					//logger.log(Level.INFO, "Calling: " + gUrl);
					String gJson = HttpUtils.processFileRequest(new URL(gUrl), "POST", null, params);
					//logger.log(Level.INFO, "Received response: " + gJson);
					if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
						JSONArray root = new JSONArray(gJson);
						for (int i=0;i<root.length();i++) {
							JSONObject layer = root.getJSONObject(i);
							Layer l = new Layer();
							Map<String, String> layerMap = new HashMap<String, String>();
							for(Iterator<String> iter = layer.keys();iter.hasNext();) {
								String key = iter.next();
								Object value = layer.get(key);
								layerMap.put(key, value.toString());
							}
        			
							BeanUtils.populate(l, layerMap);
							layers.add(l);
						}
					} else {
						logger.log(Level.SEVERE, "Received following server response: " + gJson);
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}		
				return layers;
			}
		});
	    return (List<Layer>) layersCacheAction.getObjectFromCache(key);
    }
    	
}
