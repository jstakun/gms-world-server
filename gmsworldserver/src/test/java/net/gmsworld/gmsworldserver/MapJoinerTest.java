package net.gmsworld.gmsworldserver;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.base.Joiner;

public class MapJoinerTest {
	@Test
	public void test() {
		Joiner.MapJoiner mapJoiner = Joiner.on(',').withKeyValueSeparator("=");
		
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("keyone", new String[]{"valueone, valuetwo"});
		params.put("keytwo", new String[]{"valuetwo"});
		
		System.out.println(mapJoiner.join(params));
		
		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("keyone", "valueone");
		params2.put("keytwo", "valuetwo");
		
		System.out.println(mapJoiner.join(params2));
		
		//assertEquals(mapJoiner.join(params), "")
	}
}
