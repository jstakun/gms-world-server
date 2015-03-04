package net.gmsworld.server.utils.memcache;

public interface CacheProvider {
	
	public void put(String key, Object value);
	
	public void put(String key, Object value, int options);
	
	public String getString(String key);

	public Object getObject(String key);

	public boolean containsKey(String key);
}
