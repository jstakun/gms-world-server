package com.jstakun.lm.server.utils.memcache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

public class CacheAction {

	private CacheActionExecutor executor;
	
	private static final Logger logger = Logger.getLogger(CacheAction.class.getName());
	
	private static final Map<String, Object> lockSyncMap = new HashMap<String, Object>();
	
	public interface CacheActionExecutor {
		public Object executeAction();
	}
	
	public CacheAction(CacheActionExecutor executor) {
		this.executor = executor;
	}
	
	public Object getObjectFromCache(String key, CacheType cacheType) {
		Object o = null;
		final Object lockSync = getSyncLock(key);
		synchronized (lockSync) {
			o = CacheUtil.getObject(key);
			if (o != null) {
				logger.log(Level.INFO, "Found object {0} in cache", key);
			} else {
				o = executor.executeAction();
				logger.log(Level.INFO, "Executing action for key {0}", key);
				if (o != null) {
					CacheUtil.put(key, o, cacheType);
				}
			}
			lockSyncMap.remove(key);
		}
		return o;
	}
	
	public <T> List<T> getListFromCache(Class<T> type, String key, CacheType cacheType) {
		List<T> l = null;
		final Object lockSync = getSyncLock(key);
		synchronized (lockSync) {
			l = CacheUtil.getList(type, key);
			if (l != null) {
				logger.log(Level.INFO, "Found object {0} in cache", key);
			} else {
				l = (List<T>) executor.executeAction();
				logger.log(Level.INFO, "Execution action for key {0}", key);
				if (l != null) {
					CacheUtil.put(key, l, cacheType);
				}
			}
			lockSyncMap.remove(key);
		}
		return l;
	}
	
	public Integer getIntFromCache(String key, CacheType cacheType) {
		Integer i = null;
		final Object lockSync = getSyncLock(key);
		synchronized (lockSync) {
			i = CacheUtil.getObject(Integer.class, key);
			if (i != null) {
				logger.log(Level.INFO, "Found object {0} in cache", key);
			} else {
				i = (Integer)executor.executeAction();
				logger.log(Level.INFO, "Execution action for key {0}", key);
				if (i > 0) {
					CacheUtil.put(key, i, cacheType);
				}
			}
			lockSyncMap.remove(key);
		}
		return i;
	}
	
	private Object getSyncLock(final String key) {
		Object lockSync = null; 
		synchronized (lockSyncMap) {
			if (lockSyncMap.containsKey(key)) {
				lockSync = lockSyncMap.get(key);
			}else {
				lockSync = new Object();
				lockSyncMap.put(key, lockSync);
			}
		}
		return lockSync;
	}
}
