package com.jstakun.lm.server.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;

public class UserAgentUtils {

	private static final Logger logger = Logger.getLogger(UserAgentUtils.class.getName());
	
	public static boolean isMobile(String userAgent) {
		OperatingSystem os = OperatingSystem.parseUserAgentString(userAgent);
        return os.getDeviceType().equals(DeviceType.MOBILE);
	}
	
	public static boolean isUnknown(String userAgent) {
		OperatingSystem os = OperatingSystem.parseUserAgentString(userAgent);
        boolean isUnknown = os.getDeviceType().equals(DeviceType.UNKNOWN);
        if (isUnknown) {
        	logger.log(Level.WARNING, "Unknown user agent device type: " + os.getDeviceType().getName());
        }
        return isUnknown;
	}
	
	public static boolean isBot(String userAgent) {
		Browser browser = Browser.parseUserAgentString(userAgent);
		boolean isBot = (browser.getGroup() == Browser.BOT || browser.getGroup() == Browser.BOT_MOBILE || browser.getGroup() == Browser.UNKNOWN);
		if (isBot) {
			logger.log(Level.WARNING, "Bot user agent device type: " + browser.getName());
        }
		return isBot;
	}
}
