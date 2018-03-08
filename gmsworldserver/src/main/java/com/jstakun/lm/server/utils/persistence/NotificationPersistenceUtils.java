package com.jstakun.lm.server.utils.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.config.ConfigurationManager;

public class NotificationPersistenceUtils {
	
	//TODO use persistence manager
	
	//telegram
	
	public static boolean isWhitelistedTelegramId(String telegramId) {
		 return telegramId != null && ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, telegramId);
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
	
	//email
	
	public static boolean isWhitelistedEmail(String email) {
		 return email != null && ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST, email);
	}
	
	public static void addToWhitelistEmail(String user, String email, boolean isRegistered) {
		if (email != null && user != null) {
			List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST)));
			if (!isRegistered) {
				whitelistList.add(user + ":" + email );
			} else {
				//remove all occurences of email
				for (String emailVal : whitelistList) {
					 if (emailVal.endsWith(":" + email)) {
						 whitelistList.remove(emailVal);
					 }
				}
				if (!isWhitelistedEmail(email)) {
					 whitelistList.add(email);
				}
			}
			ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST,  StringUtils.join(whitelistList, "|"));
		}
	}
}
