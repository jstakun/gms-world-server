package com.jstakun.lm.server.utils.memcache;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

public class CacheAction {

	private CacheActionExecutor executor;
	
	private static final Logger logger = Logger.getLogger(CacheAction.class.getName());
	
	public interface CacheActionExecutor {
		public Object executeAction();
	}
	
	public CacheAction(CacheActionExecutor executor) {
		this.executor = executor;
	}
	
	public Object getObjectFromCache(String key, CacheType cacheType) {
		Object o = CacheUtil.getObject(key);
		if (o != null) {
			logger.log(Level.INFO, "Found object {0} in cache", key);
		} else {
			o = executor.executeAction();
			logger.log(Level.INFO, "Executing action for key {0}", key);
			if (o != null) {
				CacheUtil.put(key, o, cacheType);
			}
		}
		return o;
	}
	
	public <T> List<T> getListFromCache(Class<T> type, String key, CacheType cacheType) {
		List<T> l = CacheUtil.getList(type, key);
		if (l != null) {
			logger.log(Level.INFO, "Found object {0} in cache", key);
		} else {
			l = (List<T>) executor.executeAction();
			logger.log(Level.INFO, "Execution action for {0}", key);
			if (l != null) {
				CacheUtil.put(key, l, cacheType);
			}
		}
		return l;
	}
	
	public Integer getIntFromCache(String key, CacheType cacheType) {
		Integer i = CacheUtil.getObject(Integer.class, key);
		if (i != null) {
			logger.log(Level.INFO, "Found object {0} in cache", key);
		} else {
			i = (Integer)executor.executeAction();
			logger.log(Level.INFO, "Execution action for {0}", key);
			if (i > 0) {
				CacheUtil.put(key, i, cacheType);
			}
		}
		return i;
	}
}
