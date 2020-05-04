package com.jstakun.lm.server.utils.memcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;

import net.gmsworld.server.utils.StringUtil;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

/**
 * 
 * @author jstakun
 */
public class CacheUtil {

	public enum CacheType {FAST, NORMAL, LANDMARK, LONG};
	private static Cache cache = null;
	private static final Logger logger = Logger.getLogger(CacheUtil.class.getName());
	private static final Expiration ONE_HOUR_EXPIRATION = Expiration.byDeltaSeconds(60 * 60);
    private static final Expiration ONE_MINUTE_EXPIRATION = Expiration.byDeltaSeconds(60);
    private static final Expiration TEN_MINUTES_EXPIRATION = Expiration.byDeltaSeconds(10 * 60);
    private static final Expiration LONG_CACHE_EXPIRATION = Expiration.byDeltaSeconds(4 * 60 * 60);
    public static final int LONG_CACHE_LIMIT = 4 * 60 * 60 * 1000; //4h
     
	private static Cache getCache() {
		if (cache == null) {
			try {
				Map<String, Integer> props = new HashMap<String, Integer>();
				props.put(GCacheFactory.EXPIRATION_DELTA, LONG_CACHE_LIMIT); 
				CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
				cache = cacheFactory.createCache(props);
				//cache.addListener(new MyCacheListener());
			} catch (CacheException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		return cache;
	}

	//private static void putAsync(String key, Object value) {
	//	new Thread(new ThreadPut(key, value)).start();
	//}
	
	private static void put(String key, Object value) {
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
		try {
			return getCache().get(key);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static <T> T getObject(Class<T> type, String key) {
		try {
			Object o = getCache().get(key);
			if (o != null && type.isAssignableFrom(o.getClass())) {
				return type.cast(o);
			} 
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return null;
	}
	
	public static <T> List<T> getList(Class<T> type, String key) {
		try {
			Collection<?> c = (Collection<?>) getCache().get(key);
			if (c != null) {
				List<T> r = new ArrayList<T>(c.size());
				for (Object o : c) {
					if (type.isAssignableFrom(o.getClass())) {
						r.add(type.cast(o));
					}
				}
				return r;
			}
		} catch (Exception e) {
			 logger.log(Level.SEVERE, e.getMessage(), e);
		}
	    return null;
	}
	
	public static boolean containsKey(String key) {
		try {	
			return getCache().containsKey(key);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}

	public static Object remove(String key) {
		try {
			return getCache().remove(key) ;
		} catch (Exception e) {
			return null;
		}
	}
	
	private static void putToFastCache(String key, Object value) {
		MemcacheServiceFactory.getMemcacheService().put(key, value, ONE_MINUTE_EXPIRATION);
	}
	
	private static void putToNormalCache(String key, Object value) {
		MemcacheServiceFactory.getMemcacheService().put(key, value, TEN_MINUTES_EXPIRATION);
	}
	
	private static void putToLongCache(String key, Object value) {
		MemcacheServiceFactory.getMemcacheService().put(key, value, LONG_CACHE_EXPIRATION);
	}
	
	public static void updateJSONObjectHashMap(String key, String layer, JSONObject value) {
		MemcacheServiceFactory.getMemcacheService().increment(key, 1, 0L);
		MemcacheServiceFactory.getMemcacheService().put(key + "_" + layer, value, ONE_HOUR_EXPIRATION);
	}
	
	public static Long increment(String key) {	
		try {
			return MemcacheServiceFactory.getMemcacheService().increment(key, 1, 0L);
		} catch (Exception e) {
			return 0L;
		}
	}
	
	public static void cacheDeviceLocation(String deviceId, Double latitude, Double longitude, String accuracy) {
		if (StringUtils.isNotEmpty(deviceId) && latitude != null && longitude != null) {
			final String key = deviceId + "_location";
			String value = StringUtil.formatCoordE6(latitude) + "_" + StringUtil.formatCoordE6(longitude) + "_";
			if (StringUtils.isNotEmpty(accuracy)) {
				value += accuracy + "_";
			}
			value += Long.toString(System.currentTimeMillis());
			CacheUtil.put(key, value, CacheType.LONG);
		}
	}
	
	public static Double[] getDeviceLocation(String deviceId) {
		if (StringUtils.isNotEmpty(deviceId)) {
			final String key = deviceId + "_location";
			if (CacheUtil.containsKey(key)) {
				Double[] coords = new Double[2];
				String[] coordsStr = StringUtils.split(CacheUtil.getString(key), "_");
				if (coordsStr.length >= 2) {
					coords[0] = Double.valueOf(coordsStr[0]);
					coords[1] = Double.valueOf(coordsStr[1]);
					return coords;
				}
			}
		}
		return null;
	}
	
	
	public static void put(String key, Object value, CacheType type) {
		try {
			if (type == CacheType.NORMAL) {
				put(key, value); //putAsync(key, value);
			} else if (type == CacheType.FAST) {
				putToFastCache(key, value);
			} else if (type == CacheType.LONG) {
				putToLongCache(key, value);
			} else if (type == CacheType.LANDMARK) {
				putToNormalCache(key, value);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/*private static class MyCacheListener implements CacheListener {

		@Override
		public void onClear() {
			logger.log(Level.INFO, "onClear");			
		}

		@Override
		public void onEvict(Object key) {
			logger.log(Level.INFO, "onEvict " + key);			
		}

		@Override
		public void onLoad(Object key) {
			logger.log(Level.INFO, "onLoad " + key);	
		}

		@Override
		public void onPut(Object key) {
			logger.log(Level.INFO, "onPut " + key);	
		}

		@Override
		public void onRemove(Object key) {
			logger.log(Level.INFO, "onRemove " + key);	
		}	
	}*/
}
