package net.gmsworld.gmsworldserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.jstakun.lm.server.utils.HtmlUtils;

import net.gmsworld.server.layers.TelegramUtils;

public class StringTest {

	private static volatile Map<String, Object> objectCache = new ConcurrentHashMap<String, Object>();
    
	//@Test
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

	@Test
	public void test2() {
		String lng = StringUtils.split("mxvtrwsqs;jsessionid=1s85bi1qlwymbba2gkgy427gm", ";")[0];
		System.out.println(lng + " " + HtmlUtils.decodeDouble(lng));
	}
}
