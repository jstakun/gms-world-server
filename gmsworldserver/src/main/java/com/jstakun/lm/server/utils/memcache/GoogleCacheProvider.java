package com.jstakun.lm.server.utils.memcache;

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.memcache.CacheProvider;

public class GoogleCacheProvider implements CacheProvider {

	private static final Logger logger = Logger.getLogger(GoogleCacheProvider.class.getName());
	
	private static GoogleCacheProvider instance = new GoogleCacheProvider();
	
	private GoogleCacheProvider() {
		
	}
	
	public static GoogleCacheProvider getInstance() {
		return instance;
	}
	
	@Override
	public void put(String key, Object value) {
		CacheUtil.put(key, value, CacheType.NORMAL);
	}
	
	@Override
	public void put(String key, Object value, int options) {
		if (options == -1) {
			CacheUtil.put(key, value, CacheType.FAST);
		} else if (options == 1) {
			CacheUtil.put(key, value, CacheType.LONG);
		} else {
			CacheUtil.put(key, value, CacheType.NORMAL);
		}
	}

	@Override
	public String getString(String key) {
		return CacheUtil.getString(key);
	}

	@Override
	public Object getObject(String key) {
		return CacheUtil.getObject(key);
	}

	@Override
	public boolean containsKey(String key) {
		return CacheUtil.containsKey(key);
	}
	
	@Override
	public void putToSecondLevelCache(String key, String value) {
		try {
			URL cacheUrl = new URL("http://cache-gmsworld.rhcloud.com/rest/cache/" + key);
			String resp = HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, "POST", null, value, "application/json; charset=utf-8", Commons.getProperty(Property.RH_GMS_USER));
			logger.log(Level.INFO, "Cache response: " + resp);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@Override
	public String getFromSecondLevelCache(String key) {
		try {
			URL cacheUrl = new URL("http://cache-gmsworld.rhcloud.com/rest/cache/" + key);
			return HttpUtils.processFileRequestWithBasicAuthn(cacheUrl, Commons.getProperty(Property.RH_GMS_USER), false);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public <T> T getObject(Class<T> type, String key) {
		return CacheUtil.getObject(type, key);
	}
		
	@Override
	public <T> List<T> getList(Class<T> type, String key) {
		return CacheUtil.getList(type, key);
	}
}
