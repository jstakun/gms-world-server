package com.jstakun.lm.server.utils.memcache;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheAction {

	private CacheActionExecutor executor;
	
	private static final Logger logger = Logger.getLogger(CacheAction.class.getName());
	
	public interface CacheActionExecutor {
		public Object executeAction();
	}
	
	public CacheAction(CacheActionExecutor executor) {
		this.executor = executor;
	}
	
	public Object getObjectFromCache(String key) {
		Object o = CacheUtil.getObject(key);
		if (o != null) {
			logger.log(Level.INFO, "Found object {0} in cache", key);
		} else {
			o = executor.executeAction();
			logger.log(Level.INFO, "Execution action for {0}", key);
			if (o != null) {
				CacheUtil.put(key, o);
			}
		}
		return o;
	}
	
	public Integer getIntFromCache(String key) {
		Integer i = (Integer)CacheUtil.getObject(key);
		if (i != null) {
			logger.log(Level.INFO, "Found object {0} in cache", key);
		} else {
			i = (Integer)executor.executeAction();
			logger.log(Level.INFO, "Execution action for {0}", key);
			if (i > 0) {
				CacheUtil.put(key, i);
			}
		}
		return i;
	}
}
