package net.gmsworld.server.utils.memcache;

public interface CacheProvider {
	
	public void put(String key, Object value);
	
	public String getString(String key);

	public Object getObject(String key);

	public boolean containsKey(String key);
}
