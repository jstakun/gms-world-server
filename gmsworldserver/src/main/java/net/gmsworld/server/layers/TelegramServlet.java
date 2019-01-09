package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

/**
 * Servlet implementation class TelegramServlet
 */
public class TelegramServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TelegramServlet.class.getName());
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (HttpUtils.isEmptyAny(request, "type")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				String type = request.getParameter("type");
				
				//device_locator_bot
				if (StringUtils.equals(type, Commons.getProperty(Property.TELEGRAM_TOKEN))) {
					String content = IOUtils.toString(request.getReader());
					JSONObject jsonObject = new JSONObject(content);
					JSONObject messageJson = jsonObject.optJSONObject("message");
					if (messageJson != null && messageJson.has("text") && messageJson.has("chat")) {
						String message = messageJson.getString("text");
						Long telegramId= messageJson.getJSONObject("chat").getLong("id");
						if (StringUtils.equalsIgnoreCase(message, "/register") || StringUtils.equalsIgnoreCase(message, "register")) {
							//add chat id to white list
							if (!NotificationPersistenceUtils.isVerified(Long.toString(telegramId))) {
								NotificationPersistenceUtils.setVerified(Long.toString(telegramId), true);
							} else {
								logger.log(Level.WARNING, "Telegram chat id " + telegramId + " is already verified!");
							}		
							if (telegramId > 0) {
									TelegramUtils.sendTelegram(Long.toString(telegramId), "You've been registered to Device Locator notifications.\n"
									+ "You can unregister at any time by sending /unregister message to @device_locator_bot");
							} else if (telegramId < 0) {
									TelegramUtils.sendTelegram(Long.toString(telegramId), "You've been registered to Device Locator notifications.\n"
											+ "You can unregister at any time by sending /unregister " + telegramId +  " command message to @device_locator_bot");
							}
						} else if (StringUtils.startsWithIgnoreCase(message, "/unregister") || StringUtils.startsWithIgnoreCase(message, "unregister")) {
								//remove chat or channel id from white list
							String[] tokens = StringUtils.split(message, " ");
							String id = null;
							if (tokens.length > 1 && ((StringUtils.startsWith(tokens[1], "-") && StringUtils.isNumeric(tokens[1].substring(1))) || StringUtils.startsWith(tokens[1], "@"))) {
								id = tokens[1]; //channel id
							} else if (tokens.length == 1) {
								id = Long.toString(telegramId); //chat id
							}
							if (NotificationPersistenceUtils.isVerified(id)) {
								if (!NotificationPersistenceUtils.remove(id)) {
									logger.log(Level.SEVERE, "Unable to remove Telegram chat or channel Id " + id + " from the whitelist!");
								}
								TelegramUtils.sendTelegram(id, "You've been unregistered from Device Locator notifications.");
							} else if (id == null) {
								TelegramUtils.sendTelegram(Long.toString(telegramId), "Oops! I didn't understand your message. Please check list of available commands.");
							} else {
								logger.log(Level.WARNING, "Telegram chat or channel Id " + id + " doesn't exists in the whitelist!");
							}
						} else if (StringUtils.equalsIgnoreCase(message, "/getmyid") || StringUtils.equalsIgnoreCase(message, "getmyid") || StringUtils.equalsIgnoreCase(message, "/myid") || StringUtils.equalsIgnoreCase(message, "myid") || StringUtils.equalsIgnoreCase(message, "/id") || StringUtils.equalsIgnoreCase(message, "id")) { 
							String id = Long.toString(telegramId);
							TelegramUtils.sendTelegram(id, id);
							TelegramUtils.sendTelegram(id, "Please click on message above containing your chat id, select copy and come back to Device Locator. "
									+ "Your chat id should be pasted automatically otherwise please paste it to \"Telegram id\" notification field.");
						} else if (StringUtils.equalsIgnoreCase(message, "/hello") ||  StringUtils.equalsIgnoreCase(message, "hello")) {
							TelegramUtils.sendTelegram(Long.toString(telegramId), "Hello there!");
						} else if (DevicePersistenceUtils.isValidCommand(message)) {
							final String reply = DevicePersistenceUtils.sendCommand(message, Long.toString(telegramId)); 
							TelegramUtils.sendTelegram(Long.toString(telegramId), reply);
						} else {
							TelegramUtils.sendTelegram(Long.toString(telegramId), "Oops! I didn't understand your message. Please check list of available commands.");
							logger.log(Level.SEVERE, "Received invalid message: " + message);
						}
					} else if (messageJson.has("chat")) {
						Long telegramId= messageJson.getJSONObject("chat").getLong("id");
						TelegramUtils.sendTelegram(Long.toString(telegramId), "Oops! I didn't understand your message. Please check list of available commands.");
				    } else {
						logger.log(Level.SEVERE, "Received invalid json: " + content);
						response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					}	
				} else {
					logger.log(Level.SEVERE, "Received wrong paramter: " + type);
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
			} 
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}
}
