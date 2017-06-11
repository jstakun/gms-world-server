package net.gmsworld.server.layers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceWebUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class NotificationsServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(NotificationsServlet.class.getName());
	private static final long ONE_DAY = 1000 * 60 * 60 * 24;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (HttpUtils.isEmptyAny(request, "type")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				String type = request.getParameter("type");
				int appId = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), -1);
				JSONObject reply = new JSONObject();

				String latStr = request.getParameter("lat");
				String lngStr = request.getParameter("lng");
				if (StringUtils.isNotEmpty(latStr) && StringUtils.isNotEmpty(lngStr)) {
					try {
						Landmark l = new Landmark();
						l.setLatitude(GeocodeUtils.getLatitude(latStr));
						l.setLongitude(GeocodeUtils.getLongitude(lngStr));
						logger.log(Level.INFO, "User location is " + latStr + "," + lngStr);
						// persist location

						l.setName(Commons.MY_POSITION_LAYER);
						boolean isSimilarToNewest = LandmarkPersistenceWebUtils.isSimilarToNewest(l);
						if (!isSimilarToNewest) {
							String u = StringUtil.getUsername(request.getAttribute("username"),
									request.getParameter("username"));
							if (u != null && u.length() % 4 == 0) {
								try {
									u = new String(Base64.decode(u));
								} catch (Exception e) {
									// from version 1086, 86 username is Base64
									// encoded string
								}
							}
							l.setUsername(u);
							String socialIds = request.getParameter("socialIds");

							LandmarkPersistenceWebUtils.setFlex(l, request);
							l.setLayer(Commons.MY_POS_CODE);

							LandmarkPersistenceUtils.persistLandmark(l, GoogleCacheProvider.getInstance());

							if (l.getId() > 0) {
								LandmarkPersistenceWebUtils.notifyOnLandmarkCreation(l, request.getHeader("User-Agent"),
										socialIds);
							}
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				} else {
					logger.log(Level.INFO, "No user location provided");
				}

				if (StringUtils.equals(type, "v")) {
					// check for version
					reply.put("type", type);
					if (appId == 0) {
						// LM
						String version = ConfigurationManager
								.getParam(net.gmsworld.server.config.ConfigurationManager.LM_VERSION, "0");
						reply.put("value", version);
					} else if (appId == 1) {
						// DA
						String version = ConfigurationManager
								.getParam(net.gmsworld.server.config.ConfigurationManager.DA_VERSION, "0");
						reply.put("value", version);
					} else if (appId == 2) {
						// DL
						reply.put("value", "0");
					}
				} else if (StringUtils.equals(type, "u")) {
					// engagement
					String email = request.getParameter("e");
					long lastStartupTime = NumberUtils.getLong(request.getParameter("lst"), -1);
					String useCount = request.getParameter("uc");
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(lastStartupTime);
					logger.log(Level.INFO,
							"Received usage notification from " + (email != null ? email : "guest")
									+ " last startup time: " + DateFormat.getDateTimeInstance().format(cal.getTime())
									+ ", use count: " + useCount);
					int minInterval = NumberUtils.getInt(ConfigurationManager.getParam(
							net.gmsworld.server.config.ConfigurationManager.NOTIFICATIONS_INTERVAL, "14"), 14);
					int maxInterval = 31;
					long interval = System.currentTimeMillis() - lastStartupTime;
					if (interval > (minInterval * ONE_DAY) && interval < (maxInterval * ONE_DAY) && email != null) {
						// send email notification if lastStartupTime > week ago
						// send not more that once a week
						logger.log(Level.WARNING, email + " should be engaged to run Landmark Manager!");
						MailUtils.sendEngagementMessage(email, getServletContext());
						reply = new JSONObject().put("status", "engaged").put("timestamp", System.currentTimeMillis());
					} else {
						response.setStatus(HttpServletResponse.SC_ACCEPTED);
						reply = new JSONObject().put("status", "accepted");
					}
				} else if (StringUtils.equals(type, "t_dl")) {
					// telegram
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String message = request.getParameter("message");
						long telegramId = NumberUtils.getLong(request.getParameter("chatId"), -1L);
						if (telegramId > 0 && StringUtils.isNotEmpty(message)) {
							// check if chat id is on white list
							if (ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, Long.toString(telegramId))) {
				            	TelegramUtils.sendTelegram(Long.toString(telegramId), message);
				            } else {
				            	logger.log(Level.WARNING, "Telegram chat id " + telegramId + " is not on whitelist!");
				            }
						} else {
							logger.log(Level.WARNING, "Wrong message to chat " + telegramId);
						}
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "m_dl")) {
					// mail
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String message = request.getParameter("message");
						String title = request.getParameter("title");
						String emailTo = request.getParameter("emailTo");
						if (StringUtils.isNotEmpty(emailTo) && (StringUtils.isNotEmpty(title) || StringUtils.isNotEmpty(message))) {
							//check if email is on white list
							if (ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST, emailTo)) {
				            	MailUtils.sendDeviceLocatorMessage(emailTo, message, title);
				            } else {
				            	logger.log(Level.WARNING, "Email address " + emailTo + " is not on whitelist!");
				            }
						} else {
							logger.log(Level.WARNING, "Wrong email to  " + emailTo);
						}
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "register_t")) {
					// telegram
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						long telegramId = NumberUtils.getLong(request.getParameter("chatId"), -1L);
						if (telegramId > 0) {
							if (ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, Long.toString(telegramId))) {
								TelegramUtils.sendTelegram(Long.toString(telegramId), "You've been already registered to Device Locator notifications.\n"
										+ "You can unregister at any time by sending /unregister command message.");
							} else {
								TelegramUtils.sendTelegram(Long.toString(telegramId), "We've received Device Locator registration request from you.\n"
										+ "If this is correct please send us back /register command message, otherwise please ignore this message.");
							}
						} else {
							logger.log(Level.WARNING, "Wrong chat id " + telegramId);
						}
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "register_m")) {
					// mail
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String email = request.getParameter("email");
						String user = request.getParameter("user");
						if (StringUtils.isNotEmpty(email) && StringUtils.isNotEmpty(user)) {
							if (ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST, email)) {
								MailUtils.sendDlRegistrationNotification(email, email, this.getServletContext());
							} else {
								List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST)));
								whitelistList.add(user + ":" + email );
								ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST,  StringUtils.join(whitelistList, "|"));
								MailUtils.sendDlVerificationRequest(email, email, user, this.getServletContext());
							}
						} else {
							logger.log(Level.WARNING, "Wrong email  " + email + " or user " + user);
						}
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, Commons.getProperty(Property.TELEGRAM_TOKEN))) {
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
						long telegramId= messageJson.getJSONObject("chat").getLong("id");
						if (StringUtils.equals(message, "/register")) {
							//add chat id to white list
							if (!ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, Long.toString(telegramId))) {
								List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST)));
								whitelistList.add(Long.toString(telegramId));
								ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST,  StringUtils.join(whitelistList, "|"));
				            } else {
				            	logger.log(Level.WARNING, "Telegram chat id " + telegramId + " already exists in the whitelist!");
				            }								
							TelegramUtils.sendTelegram(Long.toString(telegramId), "You've been registered to Device Locator notifications.\n"
									+ "You can unregister at any time by sending /unregister command message.");
						} else if (StringUtils.equals(message, "/unregister")) {
							//remove chat id from white list
							if (ConfigurationManager.listContainsValue(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST, Long.toString(telegramId))) {
								List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST)));
								if (whitelistList.remove(Long.toString(telegramId))) {
									ConfigurationManager.setParam(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST,  StringUtils.join(whitelistList, "|"));
								} else {
									logger.log(Level.SEVERE, "Unable to remove Telegram chat id " + telegramId + " from the whitelist!");
								}
								TelegramUtils.sendTelegram(Long.toString(telegramId), "You've been unregistered from Device Locator notifications.");
				            } else {
				            	logger.log(Level.WARNING, "Telegram chat id " + telegramId + " doesn't exists in the whitelist!");
				            }
						} else {
							 TelegramUtils.sendTelegram(Long.toString(telegramId), "I've received unrecognised message " + message);
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
				out.print(reply.toString());
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on
	// the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws ServletException
	 *             if a servlet-specific error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * 
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Notifications servlet";
	}// </editor-fold>

}
