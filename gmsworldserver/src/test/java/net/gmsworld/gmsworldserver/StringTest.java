package net.gmsworld.gmsworldserver;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

public class StringTest {

	private static volatile Map<String, Object> objectCache = new ConcurrentHashMap<String, Object>();
    
	@Test
	public void test() {
		objectCache.put("test", new Object());
		org.junit.Assert.assertTrue(containsObject("test", Object.class));
	}
	
	public boolean containsObject(String key, Class<? extends Object> c) {
        if (objectCache.containsKey(key)) {
            Object o = objectCache.get(key);
            return c.isAssignableFrom(o.getClass());
        } else {
            return false;
        }
    }

}
