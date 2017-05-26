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
	
	public static void sendTelegram(final String telegramId, final String message) throws IOException {
        if (StringUtils.isNotEmpty(telegramId)) {
        	String urlStr = "https://api.telegram.org/bot" + Commons.getProperty(Property.TELEGRAM_TOKEN) + "/sendMessage?text=" + message + "&chat_id=" + telegramId; 
        	URL url = new URL(urlStr);
            String response = HttpUtils.processFileRequest(url, "POST", null, null);
            Integer responseCode = HttpUtils.getResponseCode(urlStr);
            if (responseCode != 200) {
            	logger.log(Level.SEVERE,  "Received following server response: " + responseCode + " - " + response);
            }
        }
    }
}
