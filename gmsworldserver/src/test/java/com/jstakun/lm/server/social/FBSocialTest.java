package com.jstakun.lm.server.social;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;

import org.junit.Test;

public class FBSocialTest {

	@Test
	public void test() {
		FacebookSocialUtils.sendMessageToPageFeed(ConfigurationManager.SERVER_URL, "test", "GMS World Test Message", null, Commons.CHECKIN, Commons.FB_GMS_WORLD_PAGE_TOKEN);
	}

}
