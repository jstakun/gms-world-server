package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class TelegramUtils {
	
	private static final Logger logger = Logger.getLogger(TelegramUtils.class.getName());
	
	public static Integer sendTelegram(final String telegramId, final String message) throws IOException {
		Integer responseCode = null;
		if (telegramId != null) {
        	String urlStr = "https://api.telegram.org/bot" + Commons.getProperty(Property.TELEGRAM_TOKEN) + "/sendMessage"; 
        	URL url = new URL(urlStr);
            String response = HttpUtils.processFileRequest(url, "POST", null, "text=" + message + "&chat_id=" + telegramId + "&parse_mode=HTML");
            responseCode = HttpUtils.getResponseCode(urlStr);
            if (responseCode == null || responseCode != 200) {
            	logger.log(Level.SEVERE,  "Received server response: " + responseCode + " - " + response);
            	if (responseCode != null && responseCode == 400) {
            		logger.log(Level.SEVERE, "Telegram chat or channel id: " + telegramId + ", message: " + message);    		
            	}
            }
        }
		return responseCode;
    }
	
	public static Integer verifyTelegramChat(final String telegramId) throws IOException {
		Integer responseCode = null;
		if (telegramId != null) {
        	String urlStr = "https://api.telegram.org/bot" + Commons.getProperty(Property.TELEGRAM_TOKEN) + "/getChat"; 
        	URL url = new URL(urlStr);
            String response = HttpUtils.processFileRequest(url, "POST", null, "chat_id=" + telegramId);
            responseCode = HttpUtils.getResponseCode(urlStr);
            if (responseCode == null || responseCode != 200) {
            	logger.log(Level.SEVERE,  "Received server response: " + responseCode + " - " + response);
            }
        }
		return responseCode;
    }
	
	public static Integer sendLocationTelegram(final String telegramId, final Double latitude, final Double longitude) throws IOException {
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
	
    public static boolean isValidTelegramId(String telegramId) {
        //channel id could be negative number starting from -100 with length > 13 or string starting with @
        //chat id must be positive integer with length > 5
        if (StringUtils.startsWith(telegramId, "@") && telegramId.length() > 1 && !containsWhitespace(telegramId)) {
            return true;
        } else  {
            if (StringUtils.isNotEmpty(telegramId)) {
                try {
                    long id = Long.parseLong(telegramId);
                    if (id < 0) {
                        return StringUtils.startsWith(telegramId, "-100") && telegramId.length() > 13;
                    } else {
                        return telegramId.length() > 5;
                    }
                } catch (Exception e) {
                	logger.log(Level.SEVERE, "Invalid telegram chat or channel id: " + telegramId);
                }
            }
        }
        return false;
    }
    
    private static boolean containsWhitespace(final String testCode){
        if(testCode != null){
            for(int i = 0; i < testCode.length(); i++){
                if(Character.isWhitespace(testCode.charAt(i))){
                    return true;
                }
            }
        }
        return false;
    }
    
}
