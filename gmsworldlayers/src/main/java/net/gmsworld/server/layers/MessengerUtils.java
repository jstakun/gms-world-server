package net.gmsworld.server.layers;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

public class MessengerUtils {

	public static final String ACTION_MARK_SEEN = "mark_seen";
	public static final String ACTION_TYPING_ON = "typing_on";
	public static final String ACTION_TYPING_OFF = "typing_off";
	
	private static final String MESSENGER_URL = "https://graph.facebook.com/v2.6/me/messages?access_token=" + Commons.getProperty(Property.DL_PAGE_ACCESS_TOKEN);
	
	private static final Logger logger = Logger.getLogger(MessengerUtils.class.getName());
	
	public static void sendMessage(String psid, String action, String text) {
		if (isValidMessengerId(psid) && (StringUtils.isNotEmpty(action) || StringUtils.isNotEmpty(text))) {
			try {
				JSONObject recipient = new JSONObject();
				recipient.put("id", psid);			
				JSONObject content = new JSONObject();
				content.put("recipient", recipient);
				if (StringUtils.isNotEmpty(action)) {
					content.put("sender_action", action); 
				} else if (StringUtils.isNotEmpty(text)) {
					JSONObject message = new JSONObject();
					message.put("text", text);
					content.put("message", message);
					content.put("messaging_type", "UPDATE");
				}
				logger.log(Level.INFO, "Sending data: " + content.toString());
				String response = HttpUtils.processFileRequest(new URL(MESSENGER_URL), "POST", "application/json", content.toString(), "application/json");
			    if (StringUtils.startsWith(response, "{")) {
			    	 logger.log(Level.INFO, "Message sent: " + response);
			    } else {
			    	 logger.log(Level.SEVERE, "Received follwing server response: " + response);
			    }
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		} else {
			logger.log(Level.SEVERE, "Invalid paramters!");
		}
	}
	
	public static boolean isValidMessengerId(String id) {
		 return StringUtils.isNumeric(id) && id.length() > 0;
	}
}
