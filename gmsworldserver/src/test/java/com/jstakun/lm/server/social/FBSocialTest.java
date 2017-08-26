package com.jstakun.lm.server.social;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.config.Commons.Property;

import org.junit.Test;

public class FBSocialTest {

	@Test
	public void test() {
		FacebookSocialUtils.sendMessageToPageFeed(ConfigurationManager.SERVER_URL, "test", "GMS World Test Message", ConfigurationManager.SERVER_URL + "images/hotel2.jpg", Commons.CHECKIN, Commons.getProperty(Property.FB_GMS_WORLD_PAGE_TOKEN));
	}

}
