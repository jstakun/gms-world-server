package net.gmsworld.server.utils.memcache;

import java.util.List;

public interface CacheProvider {
	
	public void put(String key, Object value);
	
	public void put(String key, Object value, int options);
	
	public String getString(String key);

	public Object getObject(String key);

	public <T> T getObject(Class<T> type, String key);
	
	public <T> List<T> getList(Class<T> clazz, String key);
	
	public boolean containsKey(String key);
	
	public Object remove(String key);
}
