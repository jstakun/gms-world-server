package net.gmsworld.server.utils.memcache;

public class MockCacheProvider implements CacheProvider {

	public void put(String key, Object value) {
	}

	public String getString(String key) {
		return null;
	}

	public Object getObject(String key) {
		return null;
	}

	public boolean containsKey(String key) {
		return false;
	}

}
