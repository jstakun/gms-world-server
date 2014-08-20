package net.gmsworld.server.utils.memcache;

public class MockCacheProvider implements CacheProvider {

	@Override
	public void put(String key, Object value) {
	}

	@Override
	public String getString(String key) {
		return null;
	}

	@Override
	public Object getObject(String key) {
		return null;
	}

	@Override
	public boolean containsKey(String key) {
		return false;
	}

}
