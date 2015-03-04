package com.jstakun.lm.server.utils.memcache;

import net.gmsworld.server.utils.memcache.CacheProvider;

public class GoogleCacheProvider implements CacheProvider {

	@Override
	public void put(String key, Object value) {
		CacheUtil.put(key, value);
	}
	
	@Override
	public void put(String key, Object value, int options) {
		if (options == -1) {
			CacheUtil.putToFastCache(key, value);
		} else if (options == 1) {
			CacheUtil.putToLongCache(key, value);
		} else {
			CacheUtil.put(key, value);
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

}
