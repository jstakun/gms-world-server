package net.gmsworld.server.utils.memcache;

import java.util.HashMap;
import java.util.Map;

public class MockCacheProvider implements CacheProvider {

	private Map<String, Object> cache = new HashMap<String, Object>();
	
	public void put(String key, Object value) {
		cache.put(key, value);
	}
	
	public void put(String key, Object value, int options) {
		put(key, value);
	}

	public String getString(String key) {
		Object o = getObject(key);
		if (o instanceof String) {
			return (String)o;
		} else if (o != null) {
			return o.toString();
		} else {
			return null;
		}
	}

	public Object getObject(String key) {
		return cache.get(key);
	}

	public boolean containsKey(String key) {
		return cache.containsKey(key);
	}

}
