package com.jstakun.lm.server.utils.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.ConfigurationManager;

public class NotificationPersistenceUtils {
	
	public static boolean isWhitelistedTelegramId(String telegramId) {
		 return telegramId != null && ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, telegramId);
	}

	public static boolean isWhitelistedEmail(String email) {
		 return email != null && ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST, email);
	}
	
	public static void addToWhitelistEmail(String user, String email) {
		if (email != null && user != null) {
			List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST)));
			whitelistList.add(user + ":" + email );
			ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST,  StringUtils.join(whitelistList, "|"));
		}
	}
	
	public static void addToWhitelistTelegramId(String telegramId) {
		if (telegramId != null) {
			List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST)));
			whitelistList.add(telegramId);
			ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST,  StringUtils.join(whitelistList, "|"));
		}
	}
	
	public static boolean removeFromWhitelistTelegramId(String telegramId) {
		boolean removed = false;
		if (telegramId != null) {
			List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST)));
			removed = whitelistList.remove(telegramId); 
			if (removed) {
				ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST,  StringUtils.join(whitelistList, "|"));
			}	 
		}
		return removed;
	}
}
