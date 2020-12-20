package com.jstakun.lm.server.social;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.config.Commons.Property;

import org.junit.Test;

import com.fasterxml.jackson.databind.jsontype.impl.AsExistingPropertyTypeSerializer;

public class FBSocialTest {

	private static final String PAGE_ACCESS_TOKEN = Commons.getProperty(Property.FB_GMS_WORLD_PAGE_TOKEN);
	
	@Test
	public void test() {
		FacebookSocialUtils.sendMessageToPageFeed(ConfigurationManager.SERVER_URL, "test", "GMS World Test Message", ConfigurationManager.SERVER_URL + "images/hotel2.jpg", Commons.CHECKIN, PAGE_ACCESS_TOKEN);
	}

}
