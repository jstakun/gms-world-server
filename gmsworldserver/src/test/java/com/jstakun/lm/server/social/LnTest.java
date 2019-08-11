package com.jstakun.lm.server.social;

import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;

import com.jstakun.lm.server.oauth.LnCommons;

public class LnTest {
	
	private static final String TOKEN = "PUT_YOUR_TOKEN_HERE";

	@Test
	public void test() {
		Map<String, String> userData = LnCommons.getUserDate(TOKEN);
		if (userData != null) {
			System.out.println(new JSONObject(userData).toString());
		}
	}

}
