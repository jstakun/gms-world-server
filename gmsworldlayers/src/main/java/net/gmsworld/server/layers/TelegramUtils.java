package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class TelegramUtils {
	
	private static final Logger logger = Logger.getLogger(TelegramUtils.class.getName());
	
	public static Integer sendTelegram(final Long telegramId, final String message) throws IOException {
		Integer responseCode = null;
		if (telegramId != null) {
        	String urlStr = "https://api.telegram.org/bot" + Commons.getProperty(Property.TELEGRAM_TOKEN) + "/sendMessage"; 
        	URL url = new URL(urlStr);
            String response = HttpUtils.processFileRequest(url, "POST", null, "text=" + message + "&chat_id=" + telegramId);
            responseCode = HttpUtils.getResponseCode(urlStr);
            if (responseCode == null || responseCode != 200) {
            	logger.log(Level.SEVERE,  "Received following server response: " + responseCode + " - " + response);
            	if (responseCode != null && responseCode == 400) {
            		logger.log(Level.SEVERE, "Telegram chat or channel id: " + telegramId + ", message: " + message);
            		
            	}
            }
        }
		return responseCode;
    }
	
	public static Integer sendLocationTelegram(final Long telegramId, final Double latitude, final Double longitude) throws IOException {
		Integer responseCode = null;
		if (telegramId != null) {
        	String urlStr = "https://api.telegram.org/bot" + Commons.getProperty(Property.TELEGRAM_TOKEN) + "/sendLocation"; 
        	URL url = new URL(urlStr);
            String response = HttpUtils.processFileRequest(url, "POST", null, "latitude=" + latitude + "&longitude=" + longitude + "&chat_id=" + telegramId);
            responseCode = HttpUtils.getResponseCode(urlStr);
            if (responseCode == null || responseCode != 200) {
            	logger.log(Level.SEVERE,  "Received following server response: " + responseCode + " - " + response);
            	if (responseCode != null && responseCode == 400) {
            		logger.log(Level.SEVERE, "Telegram chat or channel id: " + telegramId + ", lat: " + latitude + ", lng: " + longitude);           		
            	}
            }
        }
		return responseCode;
    }
}
