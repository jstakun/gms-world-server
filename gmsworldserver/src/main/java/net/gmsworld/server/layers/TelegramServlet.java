package net.gmsworld.server.layers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
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
					StringBuffer jb = new StringBuffer();
					String line = null;
					try {
						BufferedReader reader = request.getReader();
						while ((line = reader.readLine()) != null)
							jb.append(line);
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}

					JSONObject jsonObject = new JSONObject(jb.toString());
					JSONObject messageJson = jsonObject.optJSONObject("message");
					if (messageJson != null && messageJson.has("text") && messageJson.has("chat")) {
						String message = messageJson.getString("text");
						Long telegramId= messageJson.getJSONObject("chat").getLong("id");
						if (StringUtils.equalsIgnoreCase(message, "/register") || StringUtils.equalsIgnoreCase(message, "register")) {
							//add chat id to white list
							if (!NotificationPersistenceUtils.isWhitelistedTelegramId(Long.toString(telegramId))) {
								NotificationPersistenceUtils.addToWhitelistTelegramId(Long.toString(telegramId), true);
							} else {
								logger.log(Level.WARNING, "Telegram chat id " + telegramId + " already exists in the whitelist!");
							}		
							if (telegramId > 0) {
									TelegramUtils.sendTelegram(Long.toString(telegramId), "You've been registered to Device Locator notifications.\n"
									+ "You can unregister at any time by sending /unregister command message to @device_locator_bot");
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
							if (NotificationPersistenceUtils.isWhitelistedTelegramId(id)) {
								if (!NotificationPersistenceUtils.remove(id)) {
									logger.log(Level.SEVERE, "Unable to remove Telegram chat or channel Id " + id + " from the whitelist!");
								}
								TelegramUtils.sendTelegram(id, "You've been unregistered from Device Locator notifications.");
							} else if (id == null) {
								TelegramUtils.sendTelegram(Long.toString(telegramId), "I've received unrecognised message " + message);
							} else {
								logger.log(Level.WARNING, "Telegram chat or channel Id " + id + " doesn't exists in the whitelist!");
							}
						} else if (StringUtils.equalsIgnoreCase(message, "/getmyid") || StringUtils.equalsIgnoreCase(message, "getmyid") || StringUtils.equalsIgnoreCase(message, "/myid") || StringUtils.equalsIgnoreCase(message, "myid") || StringUtils.equalsIgnoreCase(message, "/id") || StringUtils.equalsIgnoreCase(message, "id")) { 
							String id = Long.toString(telegramId);
							TelegramUtils.sendTelegram(id, id);
							TelegramUtils.sendTelegram(id, "Please click on message above containing your chat Id and select copy. Then come back to Device Locator and "
							 		+ "paste your chat Id to Telegram Messenger chat Id form field. If you are lucky your chat Id will be pasted automatically :)");
						} else {
							TelegramUtils.sendTelegram(Long.toString(telegramId), "I've received unrecognised message " + message);
						}
					}	else {
						logger.log(Level.SEVERE, "Received invalid json: " + jb.toString());
						response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					}	
				} else if (StringUtils.equals(type, Commons.getProperty(Property.TELEGRAM_COMMANDS_TOKEN))) {
					StringBuffer jb = new StringBuffer();
					String line = null;
					try {
						BufferedReader reader = request.getReader();
						while ((line = reader.readLine()) != null)
							jb.append(line);
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
					
					JSONObject jsonObject = new JSONObject(jb.toString());
					JSONObject messageJson = jsonObject.optJSONObject("message");
					if (messageJson != null && messageJson.has("text") && messageJson.has("chat")) {
						Long telegramId= messageJson.getJSONObject("chat").getLong("id");
						//command imei pin -p args 
						//command name pin username -p args 
						String[] tokens = StringUtils.split(messageJson.getString("text"), " ");
						String reply = "";
						if (tokens.length >= 3 && StringUtils.isAlpha(tokens[0]) && StringUtils.isNumeric(tokens[2])) {
							try {
								String command = tokens[0];
								String deviceId = tokens[1];
								Integer pin = Integer.valueOf(tokens[2]);	
								
								int argsIndex = 4;
								String username = null;
								if (tokens.length > 3 && !StringUtils.equals(tokens[3], "-p")) {
									argsIndex = 5;
									username = tokens[3];
								}
								
								String args = null; 
								if (tokens.length == argsIndex+1) {
									 args = tokens[argsIndex];
								} else if (tokens.length > argsIndex+1) {
									StringUtils.join(Arrays.copyOfRange(tokens, argsIndex, tokens.length-1), " ");
								}
								
								reply = "Command " +  command + " has been sent to the cloud! You should get notified when device " + deviceId + " will receive this command.";
								
								if (StringUtils.startsWith(command, "/")) {
									command = command.substring(1);
								}
								if (! StringUtils.endsWithIgnoreCase(command, "dlt")) {
									command += "dlt";
								}
								
								final String correlationId = RandomStringUtils.randomAlphabetic(16) + System.currentTimeMillis();
								final String commandName =  command.substring(0, command.length()-3);
							
								int status;
								if (username == null) {
									status = DevicePersistenceUtils.sendCommand(deviceId, pin, null, null, command, args, correlationId, null);
								} else {
									status = DevicePersistenceUtils.sendCommand(null, pin, deviceId, username, command, args, correlationId, null);
								}
								
								if (status == 1)  {
									CacheUtil.put(correlationId, telegramId + "_+_" + deviceId + "_+_" + commandName, CacheType.LANDMARK);
								} else if (status == -2) { //400
									reply = "Invalid command " + commandName + " or pin";
								} else if (status == -4) { //404
									reply = "Device " + deviceId + " not found";
								} else  {
									reply = "Failed to send command " + commandName + " to the device " + deviceId;
								}  
							} catch (Exception e) {
								reply = "Failed to send command: " + e.getMessage();
							}
						} else {
							reply = "Invalid command!";
						}
						TelegramUtils.sendTelegram(Long.toString(telegramId), reply);
					} else {
						logger.log(Level.SEVERE, "Received invalid json: " + jb.toString());
						response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					}	
				}	else {
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
