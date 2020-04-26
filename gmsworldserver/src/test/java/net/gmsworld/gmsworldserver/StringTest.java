package net.gmsworld.gmsworldserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.junit.Test;

import com.jstakun.lm.server.utils.HtmlUtils;

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

	//@Test
	public void test2() {
		String lng = StringUtils.split("mxvtrwsqs;jsessionid=1s85bi1qlwymbba2gkgy427gm", ";")[0];
		System.out.println(lng + " " + HtmlUtils.decodeDouble(lng));
	}
	
	@Test
	public void test3() {
		String u = "mypos";
		if (Base64.isArrayByteBase64(u.getBytes())) {
			System.out.println("Decoded base64: " + new String(Base64.decodeBase64(u)));
		}
		u = "bXlwb3M=";
		if (Base64.isArrayByteBase64(u.getBytes())) {
			 System.out.println("Decoded base64: " + new String(Base64.decodeBase64(u)));
		}
		
	}
}
