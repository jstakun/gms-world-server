package com.jstakun.lm.server.utils.memcache;

public class GoogleCacheProvider implements CacheProvider {

	@Override
	public void put(String key, Object value) {
		CacheUtil.put(key, value);
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
