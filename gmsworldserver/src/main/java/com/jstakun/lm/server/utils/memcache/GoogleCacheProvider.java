package com.jstakun.lm.server.utils.memcache;

import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import net.gmsworld.server.utils.memcache.CacheProvider;

public class GoogleCacheProvider implements CacheProvider {

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

}
