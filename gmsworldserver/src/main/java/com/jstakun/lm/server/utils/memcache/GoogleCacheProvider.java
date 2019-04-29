package com.jstakun.lm.server.utils.memcache;

import java.util.List;

import net.gmsworld.server.utils.memcache.CacheProvider;

import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

public class GoogleCacheProvider implements CacheProvider {

	//private static final Logger logger = Logger.getLogger(GoogleCacheProvider.class.getName());
	
	private static GoogleCacheProvider instance = new GoogleCacheProvider();
	
	private GoogleCacheProvider() {
		
	}
	
	public static GoogleCacheProvider getInstance() {
		return instance;
	}
	
	public void put(String key, Object value) {
		CacheUtil.put(key, value, CacheType.NORMAL);
	}
	
	public void put(String key, Object value, int options) {
		if (options == -1) {
			CacheUtil.put(key, value, CacheType.FAST);
		} else if (options == 1) {
			CacheUtil.put(key, value, CacheType.LONG);
		} else {
			CacheUtil.put(key, value, CacheType.NORMAL);
		}
	}

	public String getString(String key) {
		return CacheUtil.getString(key);
	}

	public Object getObject(String key) {
		return CacheUtil.getObject(key);
	}

	public boolean containsKey(String key) {
		return CacheUtil.containsKey(key);
	}

	public <T> T getObject(Class<T> type, String key) {
		return CacheUtil.getObject(type, key);
	}
		
	public <T> List<T> getList(Class<T> type, String key) {
		return CacheUtil.getList(type, key);
	}
	
	public Object remove(String key) {
		return CacheUtil.remove(key);
	}
}
