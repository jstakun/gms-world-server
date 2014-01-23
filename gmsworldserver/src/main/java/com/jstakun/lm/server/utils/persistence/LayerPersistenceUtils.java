/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.struts.util.LabelValueBean;

import com.jstakun.lm.server.persistence.Layer;
import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

/**
 *
 * @author jstakun
 */
public class LayerPersistenceUtils {

    private static final Logger logger = Logger.getLogger(LayerPersistenceUtils.class.getName());

    public static List<LabelValueBean> selectAllLayers() {
        ArrayList<LabelValueBean> results = new ArrayList<LabelValueBean>();
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(Layer.class);
            List<Layer> result = (List<Layer>) query.execute();
            for (Layer l : result) {
                results.add(new LabelValueBean(l.getName(), l.getName()));
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
        return results;
    }

    /*public static List<Layer> listAllLayers(int version) {
        final String key = LayerPersistenceUtils.class.getName() + "_listAllLayers_" + version;
    	List<Layer> result = (List<Layer>)CacheUtil.getObject(key);

    	if (result == null) {
    	
    		PersistenceManager pm = PMF.get().getPersistenceManager();
        
    		try {
    			Query query = pm.newQuery(Layer.class);
    			query.declareParameters("Integer v");
    			query.setFilter("version > 0 && version <= v");
    			result = (List<Layer>) query.execute(version); //name != 'MyPos' and
    			pm.retrieveAll(result);
    			result = (List<Layer>) pm.detachCopyAll(result);
    			CacheUtil.put(key, result);
    			logger.log(Level.INFO, "Adding layers list to cache "  + key);
    		} catch (Exception ex) {
    			logger.log(Level.SEVERE, ex.getMessage(), ex);
    		} finally {
    			pm.close();
    		}
        
    	} else {
    		logger.log(Level.INFO, "Reading layers list from cache "  + key);
    	}
        return result;
    }*/
    
    public static List<Layer> listAllLayers(final int version) {
    	final String key = LayerPersistenceUtils.class.getName() + "_listAllLayers_" + version;
    	CacheAction layersCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			@Override
			public Object executeAction() {
				PersistenceManager pm = PMF.get().getPersistenceManager();
				List<Layer> result = null;
	    		try {
	    			Query query = pm.newQuery(Layer.class);
	    			query.declareParameters("Integer v");
	    			query.setFilter("version > 0 && version <= v");
	    			result = (List<Layer>) query.execute(version); //name != 'MyPos' and
	    			//pm.retrieveAll(result);
	    			result = (List<Layer>) pm.detachCopyAll(result);
	    			CacheUtil.put(key, result);
	    			logger.log(Level.INFO, "Adding layers list to cache "  + key);
	    		} catch (Exception ex) {
	    			logger.log(Level.SEVERE, ex.getMessage(), ex);
	    		} finally {
	    			pm.close();
	    		}
	        
				return result;
			}
		});
	    return (List<Layer>) layersCacheAction.getObjectFromCache(key);
    }

    public static void persistLayer(String name, String desc, boolean enabled, boolean manageable, boolean checkinable, String formatted) {
        Layer layer = new Layer(name, desc, enabled, manageable, checkinable, formatted);
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(layer);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static void deleteLayer(String name) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        
        try {
            Layer layer = pm.getObjectById(Layer.class, name);
            pm.deletePersistent(layer);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static String getLayerFormattedName(String name) {
        String response = CacheUtil.getString(name);
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
        }
        return response;
    }
}
