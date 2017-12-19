package net.gmsworld.server.layers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;

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

					try {
						JSONObject jsonObject = new JSONObject(jb.toString());
						JSONObject messageJson = jsonObject.getJSONObject("message");
						String message = messageJson.getString("text");
						String telegramId= Long.toString(messageJson.getJSONObject("chat").getLong("id"));
						if (StringUtils.equalsIgnoreCase(message, "/register") || StringUtils.equalsIgnoreCase(message, "register")) {
							//add chat id to white list
							if (!ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, telegramId)) {
								List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST)));
								whitelistList.add(telegramId);
								ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST,  StringUtils.join(whitelistList, "|"));
				            } else {
				            	logger.log(Level.WARNING, "Telegram chat id " + telegramId + " already exists in the whitelist!");
				            }								
							TelegramUtils.sendTelegram(telegramId, "You've been registered to Device Locator notifications.\n"
									+ "You can unregister at any time by sending /unregister command message.");
						} else if (StringUtils.equalsIgnoreCase(message, "/unregister") || StringUtils.equalsIgnoreCase(message, "unregister")) {
							//remove chat id from white list
							if (ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, telegramId)) {
								List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST)));
								if (whitelistList.remove(telegramId)) {
									ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST,  StringUtils.join(whitelistList, "|"));
								} else {
									logger.log(Level.SEVERE, "Unable to remove Telegram chat id " + telegramId + " from the whitelist!");
								}
								TelegramUtils.sendTelegram(telegramId, "You've been unregistered from Device Locator notifications.");
				            } else {
				            	logger.log(Level.WARNING, "Telegram chat id " + telegramId + " doesn't exists in the whitelist!");
				            }
						} else if (StringUtils.equalsIgnoreCase(message, "/getmyid") || StringUtils.equalsIgnoreCase(message, "getmyid")) { 
							 TelegramUtils.sendTelegram(telegramId, telegramId);
							 TelegramUtils.sendTelegram(telegramId, "Please click on message above containing your chat id and select copy. Then come back to Device Locator and "
							 		+ "paste your chat id to Telegram Messenger chat id form field. If you are lucky your chat id will be pasted automatically :)");
						} else {
							 TelegramUtils.sendTelegram(telegramId, "I've received unrecognised message " + message);
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
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
					JSONObject messageJson = jsonObject.getJSONObject("message");
					String message = messageJson.getString("text");
					String telegramId= Long.toString(messageJson.getJSONObject("chat").getLong("id"));
					//command imei pin args
					String[] tokens = StringUtils.split(message, " ");
					String reply = "Command sent";
					if (tokens.length >= 3) {
						try {
							String command = tokens[0];
							Long imei = Long.valueOf(tokens[1]);
							Integer pin = Integer.valueOf(tokens[2]);		
							String args = tokens.length > 3 ? tokens[3] : null;
							int status = DevicePersistenceUtils.sendCommand(imei, pin, command, args);
							if (status == -1) {
								reply = "Failed to send command";
							}
						} catch (Exception e) {
							reply = "Failed to send command: " + e.getMessage();
						}
					} else {
						reply = "Wrong command";
					}
					TelegramUtils.sendTelegram(telegramId, reply);
				}	else {
					logger.log(Level.SEVERE, "Received wrong paramter: " + type);
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
