/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.memcache;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import org.json.JSONObject;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;

/**
 * 
 * @author jstakun
 */
public class CacheUtil {

	private static Cache cache = null;
	private static final Logger logger = Logger.getLogger(CacheUtil.class.getName());
	private static final Expiration ONE_HOUR_EXPIRATION = Expiration.byDeltaSeconds(60 * 60);
    private static final int TWO_HOURS = 3600 * 2;
    private static final Expiration ONE_MINUTE_EXPIRATION = Expiration.byDeltaSeconds(60);
    //private static final MyCacheListener listener = new MyCacheListener();
	
	private static Cache getCache() {
		if (cache == null) {
			try {
				Map props = new HashMap();
				props.put(GCacheFactory.EXPIRATION_DELTA, TWO_HOURS); 
				CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
				cache = cacheFactory.createCache(props);
				//cache.addListener(listener);
			} catch (CacheException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		return cache;
	}

	public static void put(String key, Object value) {
		//logger.log(Level.INFO, "put " + key);
		try {
			getCache().put(key, value);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		//logger.log(Level.INFO, "Cache size is: " + getCache().getCacheStatistics().getObjectCount());
	}

	public static String getString(String key) {
		return getObject(String.class, key);
	}

	public static Object getObject(String key) {
		//logger.log(Level.INFO, "getObject " + key);
		return getCache().get(key);
	}
	
	protected static <T> T getObject(Class<T> type, String key) {
		Object o = getCache().get(key);
		if (o != null && type.isAssignableFrom(o.getClass())) {
			return type.cast(o);
	    } else {
	    	return null;
	    }
	}
	
	public static boolean containsKey(String key) {
		return getCache().containsKey(key);
	}

	public static boolean remove(String key) {
		return (getCache().remove(key) != null);
	}
	
	public static void putToShortCache(String key, Object value) {
		//logger.log(Level.INFO, "putToShortCache " + key);
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.put(key, value, ONE_MINUTE_EXPIRATION);
	}
	
	public static void updateJSONObjectHashMap(String key, String layer, JSONObject value) {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.increment(key, 1, 0L);
		syncCache.put(key + "_" + layer, value, ONE_HOUR_EXPIRATION);
	}
	
	public static void increment(String key) {
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.increment(key, 1);
	}
	
	/*private static class MyCacheListener implements CacheListener {

		@Override
		public void onClear() {
			logger.log(Level.INFO, "onClear");			
		}

		@Override
		public void onEvict(Object arg0) {
			logger.log(Level.INFO, "onEvict " + arg0);			
		}

		@Override
		public void onLoad(Object arg0) {
			logger.log(Level.INFO, "onLoad " + arg0);	
		}

		@Override
		public void onPut(Object arg0) {
			logger.log(Level.INFO, "onPut " + arg0);	
		}

		@Override
		public void onRemove(Object arg0) {
			logger.log(Level.INFO, "onRemove " + arg0);	
		}
		
	}*/
}
