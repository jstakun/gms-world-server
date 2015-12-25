package net.gmsworld.server.utils.memcache;

import java.util.HashMap;
import java.util.List;
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
	
    public void putToSecondLevelCache(String key, String value) {
    	
    }
	
	public String getFromSecondLevelCache(String key) {
		return null;
	}

	@Override
	public <T> T getObject(Class<T> type, String key) {
		Object o = cache.get(key);
		if (o != null && type.isAssignableFrom(o.getClass())) {
			return type.cast(o);
	    } else {
	    	return null;
	    }
	}

	@Override
	public <T> List<T> getList(Class<T> type, String key) {
		//TODO fix warning
		return getObject(List.class, key);
	}
}	
