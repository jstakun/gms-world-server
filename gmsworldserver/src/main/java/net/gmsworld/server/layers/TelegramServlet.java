package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
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
	
	private static final String INVALID_COMMAND = "Oops! I didn't understand your message. Please check available commands <a href=\"https://www.gms-world.net/dl\">here</a>.";
	
	@Override
	public void init(ServletConfig config) throws ServletException {
			super.init(config);
			GeocodeHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
	}
	
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
					JSONObject messageJson = null;
					if (jsonObject.has("message")) {
						messageJson = jsonObject.optJSONObject("message");
					} else if (jsonObject.has("edited_message")) {
						messageJson = jsonObject.optJSONObject("edited_message");
					} else if (jsonObject.has("channel_post")) {
						messageJson = jsonObject.optJSONObject("channel_post");
					} 
					if (messageJson != null && messageJson.has("text") && messageJson.has("chat")) {
						String message = messageJson.getString("text");
						Long telegramId= messageJson.getJSONObject("chat").getLong("id");
						logger.log(Level.INFO, "Received message " + message + " from " + telegramId);
						if (StringUtils.contains(message, "OsmAnd")) { //blacklisted
							logger.log(Level.WARNING, "This message is invalid!");
						} else if (StringUtils.startsWithIgnoreCase(message, "/register") || StringUtils.startsWithIgnoreCase(message, "register")) {
							//add chat or channel id to white list
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
								TelegramUtils.sendTelegram(Long.toString(telegramId), INVALID_COMMAND);
							} else {
								logger.log(Level.WARNING, "Telegram chat or channel Id " + id + " doesn't exists in the whitelist!");
								TelegramUtils.sendTelegram(id, "You are not registered for Device Locator notifications.");
							}
						} else if (StringUtils.startsWithIgnoreCase(message, "/getmyid") || StringUtils.startsWithIgnoreCase(message, "getmyid") || StringUtils.startsWithIgnoreCase(message, "/myid") || StringUtils.startsWithIgnoreCase(message, "myid") || StringUtils.startsWithIgnoreCase(message, "/id") || StringUtils.startsWithIgnoreCase(message, "id")) { 
							final String id = Long.toString(telegramId);
							TelegramUtils.sendTelegram(id, id);
							TelegramUtils.sendTelegram(id, "Please click on message above containing your chat id and select copy. Next please come back to Device Locator and paste it to \"Telegram id\" notification field.");
						} else if (StringUtils.equalsIgnoreCase(message, "/hello") ||  StringUtils.equalsIgnoreCase(message, "hello")) {
							TelegramUtils.sendTelegram(Long.toString(telegramId), "Hello there!");
						} else if (StringUtils.startsWithIgnoreCase(message, "/start ") && StringUtils.split(message, " ").length == 2) {
							//add chat or channel id to white list
							JSONObject reply = NotificationPersistenceUtils.registerTelegram(Long.toString(telegramId), 45, GoogleCacheProvider.getInstance());
							final String telegramSecret = StringUtils.split(message, " ")[1];
							reply.put("chatId", telegramId);
							GoogleCacheProvider.getInstance().put(telegramSecret, reply.toString());
							logger.info("Cached "  + telegramSecret + ": " + reply);
						} else if (StringUtils.startsWithIgnoreCase(message, "/help") ||  StringUtils.startsWithIgnoreCase(message, "help")) {
							InputStream is = null;
							try {
								is= getServletContext().getResourceAsStream("/WEB-INF/emails/bot-dl.txt");
								String helpMessage = String.format(IOUtils.toString(is, "UTF-8"));
								TelegramUtils.sendTelegram(Long.toString(telegramId), helpMessage);
							} catch (IOException ex) {
					            logger.log(Level.SEVERE, ex.getMessage(), ex);
					        } finally {
					            if (is != null) {
					                try {
					                    is.close();
					                } catch (IOException ex) {
					                    logger.log(Level.SEVERE, null, ex);
					                }
					            }
					        }
					    } else if (DevicePersistenceUtils.isValidCommand(message) || (StringUtils.startsWith(message, "/") && DevicePersistenceUtils.isValidCommand(message.substring(1)))) {
							final String reply = DevicePersistenceUtils.sendCommand(message, Long.toString(telegramId), "telegram"); 
							TelegramUtils.sendTelegram(Long.toString(telegramId), reply);
						} else {
							TelegramUtils.sendTelegram(Long.toString(telegramId), INVALID_COMMAND);
							logger.log(Level.SEVERE, "This message is invalid");
						}
					} else if (messageJson != null && messageJson.has("chat")) {
						Long telegramId= messageJson.getJSONObject("chat").getLong("id");
						logger.log(Level.INFO, "Received message " + messageJson + " from " + telegramId);
						TelegramUtils.sendTelegram(Long.toString(telegramId), INVALID_COMMAND);
				    } else {
						logger.log(Level.SEVERE, "Received invalid json: " + content);
						response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					}	
				} else if (StringUtils.equals(type, "getTelegramChatId")) {
					final String telegramSecret = request.getParameter("telegramSecret");
					if (StringUtils.isNotEmpty(telegramSecret) && GoogleCacheProvider.getInstance().containsKey(telegramSecret)) {
						out.println(GoogleCacheProvider.getInstance().getObject(telegramSecret));
					} else {
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				} else {
					logger.log(Level.SEVERE, "Received wrong parameter: " + type);
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
