package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
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
import com.jstakun.lm.server.persistence.Notification;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.RoutesUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceWebUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;

import net.gmsworld.server.config.Commons;
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
				String routeId = request.getHeader(Commons.ROUTE_ID_HEADER);
				JSONObject reply = new JSONObject();
				int appVersion = NumberUtils.getInt(request.getHeader(Commons.APP_VERSION_HEADER), -1);

				Double latitude = null;
	            if (request.getParameter("lat") != null) {
	                 latitude = GeocodeUtils.getLatitude(request.getParameter("lat"));
	            } else if (request.getHeader(Commons.LAT_HEADER) != null) {
	                 latitude = GeocodeUtils.getLatitude(request.getHeader(Commons.LAT_HEADER));
	            }
	            
	            Double longitude = null;
	            if (request.getParameter("lng") != null) {
	                 longitude = GeocodeUtils.getLongitude(request.getParameter("lng"));
	            } else if (request.getHeader(Commons.LNG_HEADER) != null) {
	                 longitude = GeocodeUtils.getLongitude(request.getHeader(Commons.LNG_HEADER));
	            }
	            
	            //create new landmark but skip dl route points
				if (StringUtils.isEmpty(routeId) && GeocodeUtils.isValidLatitude(latitude) && GeocodeUtils.isValidLongitude(longitude)) {
					try {
						Landmark l = new Landmark();
						l.setLatitude(latitude);
						l.setLongitude(longitude);
						//logger.log(Level.INFO, "User location is " + Double.toString(latitude) + "," + Double.toString(longitude));
						l.setName(Commons.MY_POSITION_LAYER);
						
						String u = StringUtil.getUsername(request.getAttribute("username"),
								request.getParameter("username"));
						if (u != null && u.length() % 4 == 0) {
							try {
								u = new String(Base64.decode(u));
							} catch (Exception e) {
								// from version 1086, 86 username is Base64 encoded string
							}
						}
						if (u == null) {
							throw new Exception("Username can't be null!");
						} //else {
						//	logger.log(Level.INFO, "Username is " + u);
						//}
						l.setUsername(u);
						
						if (!LandmarkPersistenceWebUtils.isSimilarToNewest(l)) {
							String socialIds = request.getParameter("socialIds");

							LandmarkPersistenceWebUtils.setFlex(l, request);
							l.setLayer(Commons.MY_POS_CODE);

							LandmarkPersistenceUtils.persistLandmark(l, GoogleCacheProvider.getInstance());

							if (l.getId() > 0) {
								LandmarkPersistenceWebUtils.notifyOnLandmarkCreation(l, request.getHeader("User-Agent"), socialIds);
							}
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				} //else {
					//logger.log(Level.INFO, "No user location provided");
				//}
				
				//add route point to cache
				if (StringUtils.isNotEmpty(routeId) && GeocodeUtils.isValidLatitude(latitude) && GeocodeUtils.isValidLongitude(longitude)) {
        			RoutesUtils.addRoutePointToCache(routeId, latitude, longitude);
        		}

				if (StringUtils.equals(type, "v")) {
					// check for version
					reply.put("type", type);
					if (appId == Commons.LM_ID) {
						// LM
						String version = ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.LM_VERSION, "0");
						reply.put("value", version);
					} else if (appId == Commons.DA_ID) {
						// DA
						String version = ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.DA_VERSION, "0");
						reply.put("value", version);
					} else if (appId == Commons.DL_ID) {
						// DL
						String version = ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.DL_VERSION, "0");
						reply.put("value", version);
					}
				} else if (StringUtils.equals(type, "u")) {
					//mail engagement
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
					//telegram notification
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String message = request.getParameter("message");
						String telegramId = request.getParameter("chatId");
						if (StringUtils.equals(telegramId, "@dlcorrelationId")) {
			            	//message is correlationId
			            	String val = CacheUtil.getString(message); //telegramId _+_ deviceid  _+_ command
			            	if (val != null) {
			            		String[] data = StringUtils.split(val, "_+_");
			            		if (data != null && data.length == 3 && TelegramUtils.isValidTelegramId(data[0])) {
			            			String authStatus = request.getHeader("X-GMS-AuthStatus");
			            			if (StringUtils.equals(authStatus, "failed")) {
			            				TelegramUtils.sendTelegram(data[0], "Command " + data[2] + " has been rejected by device " + data[1] + ".");
			            			} else {
			            				TelegramUtils.sendTelegram(data[0], "Command " + data[2] + " has been received by device " + data[1] + ".");
			            			}
			            		} else {
			            			logger.log(Level.WARNING, "Invalid " +  message + " entry value " + val); 
			            		}
			            	} else {
			            		logger.log(Level.WARNING, "No entry found " +  message);
			            	}
			            } else if (TelegramUtils.isValidTelegramId(telegramId) && StringUtils.isNotEmpty(message)) {
							// check if chat id is on white list
							if (NotificationPersistenceUtils.isWhitelistedTelegramId(telegramId)) {
				            	TelegramUtils.sendTelegram(telegramId, message);
				            	if (GeocodeUtils.isValidLatitude(latitude) && GeocodeUtils.isValidLongitude(longitude)) {
				            		TelegramUtils.sendLocationTelegram(telegramId, latitude, longitude);
				            	}
				            	reply = new JSONObject().put("status", "sent");
				            }  else {
				            	logger.log(Level.WARNING, "Telegram chat or channel Id " + telegramId + " is not on whitelist!");
				            	reply = new JSONObject().put("status", "unverified");
				            }
						} else {
							logger.log(Level.WARNING, "Wrong message or chat/channel id " + telegramId);
							reply = new JSONObject().put("status", "failed");
						}
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "m_dl")) {
					//email notification
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String message = request.getParameter("message");
						String title = request.getParameter("title");
						String emailTo = request.getParameter("emailTo");
						if (StringUtils.isNotEmpty(emailTo) && (StringUtils.isNotEmpty(title) || StringUtils.isNotEmpty(message))) {
							//check if email is on white list
							if (NotificationPersistenceUtils.isWhitelistedEmail(emailTo)) {
				            	MailUtils.sendDeviceLocatorMessage(emailTo, message, title);
				            	reply = new JSONObject().put("status", "sent");	
				            } else {
				            	logger.log(Level.WARNING, "Email address " + emailTo + " is not on whitelist!");
				            	reply = new JSONObject().put("status", "unverified");
				            }
						} else {
							logger.log(Level.WARNING, "Wrong email to  " + emailTo);
							reply = new JSONObject().put("status", "failed");
						}
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "register_t")) {
					//register telegram
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String telegramId = request.getParameter("chatId");
						reply = registerTelegram(telegramId, appVersion, response);
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "register_m")) {
					//register mail
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						reply = registerEmail(request.getParameter("email"), appVersion, response);
					} else {
						logger.log(Level.WARNING, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "unregister")) {
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String id = request.getParameter("id");
						if (StringUtils.isNotEmpty(id) && NotificationPersistenceUtils.remove(id)) {
							reply = new JSONObject().put("status", "removed");
						} else {
							reply = new JSONObject().put("status", "failed");
						}
					} else {
							logger.log(Level.WARNING, "Wrong application " + appId);
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
	}

	private JSONObject registerEmail(String email, int appVersion, HttpServletResponse response) throws IOException {
		JSONObject reply = null;
		if (StringUtils.isNotEmpty(email)) {
			if (NotificationPersistenceUtils.isWhitelistedEmail(email)) {
				Notification n = NotificationPersistenceUtils.addToWhitelistEmail(email, true);
				MailUtils.sendDeviceLocatorRegistrationNotification(email, email, n.getSecret(), this.getServletContext());
				reply = new JSONObject().put("status", "registered");
			} else if (MailUtils.emailAccountExists(email)) {
				Notification n = NotificationPersistenceUtils.addToWhitelistEmail(email, false);
				int version = 0;
				if (appVersion >= 30) {
					version = 2;
				}
				String status = MailUtils.sendDeviceLocatorVerificationRequest(email, email, n.getSecret(), this.getServletContext(), version);
				if (StringUtils.equals(status, "ok")) {
					reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
				} else {
					reply = new JSONObject().put("status", status);
				}
			} else {
				reply = new JSONObject().put("status", "failed");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);   
			}
		} else {
			logger.log(Level.WARNING, "Email is empty!"); 
		}
		return reply;
	}
	
	private JSONObject registerTelegram(String telegramId, int appVersion, HttpServletResponse response) throws IOException {
		JSONObject reply = null;
		if (TelegramUtils.isValidTelegramId(telegramId)) {
			if (NotificationPersistenceUtils.isWhitelistedTelegramId(telegramId)) {
				if (StringUtils.isNumeric(telegramId)) {
					TelegramUtils.sendTelegram(telegramId, "You've been already registered to Device Locator notifications.\n"
						+ "You can unregister at any time by sending /unregister command message to @device_locator_bot");
				} else {
					TelegramUtils.sendTelegram(telegramId, "You've been already registered to Device Locator notifications.\n"
							+ "You can unregister at any time by sending /unregister " + telegramId + " command message to @device_locator_bot");
				}
				reply = new JSONObject().put("status", "registered");
			} else if (StringUtils.isNumeric(telegramId)) {
				Integer responseCode =  TelegramUtils.sendTelegram(telegramId, "We've received Device Locator notifications registration request from you.");
				if (responseCode != null && responseCode == 200) {
					Notification n = NotificationPersistenceUtils.addToWhitelistTelegramId(telegramId, false);
					if (appVersion >= 30) {
						String tokens[] = StringUtils.split(n.getSecret(), ".");
	            		if (tokens.length == 2 && tokens[1].length() == 4 && StringUtils.isNumeric(tokens[1])) {
	            			String activationCode = tokens[1];
	            			TelegramUtils.sendTelegram(telegramId, "If this is correct here is your activation code: " + activationCode + ", otherwise please ignore this message.");
	            			reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
	            		} else {
	            			reply = new JSONObject().put("status", "internalError");
	    					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            		}
					} else {
						TelegramUtils.sendTelegram(telegramId, "If this is correct please send us back /register command message, otherwise please ignore this message.");
						reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
					}				
				} else if (responseCode != null && responseCode == 400) {
				    reply = new JSONObject().put("status", "failed");
				} else {
					reply = new JSONObject().put("status", "internalError");
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else if ((StringUtils.startsWithAny(telegramId, new String[]{"@","-100"}))) {
				Integer responseCode = TelegramUtils.sendTelegram(telegramId, "We've received Device Locator notifications registration request for this Channel.");
				if (responseCode != null && responseCode == 200) {
					Notification n = NotificationPersistenceUtils.addToWhitelistTelegramId(telegramId, false);
					if (appVersion >= 30) {
						String tokens[] = StringUtils.split(n.getSecret(), ".");
	            		if (tokens.length == 2 && tokens[1].length() == 4 && StringUtils.isNumeric(tokens[1])) {
	            			String activationCode = tokens[1];
	            			TelegramUtils.sendTelegram(telegramId, "If this is correct here is your activation code: " + activationCode +  ", otherwise please ignore this message.");
	            			reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
	            		} else {
	            			reply = new JSONObject().put("status", "internalError");
	    					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            		}
					} else {
						TelegramUtils.sendTelegram(telegramId, "If this is correct please contact us via email at: device-locator@gms-world.net and send your Channel ID: " + telegramId + ", otherwise please ignore this message.");
						reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
					}
				} else if (responseCode != null && responseCode == 400) {
					reply = new JSONObject().put("status", "badRequestError");
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				} else if (responseCode != null && responseCode == 403) {
					reply = new JSONObject().put("status", "permissionDenied");
					response.sendError(HttpServletResponse.SC_FORBIDDEN);	
				} else {
					logger.log(Level.WARNING, "Received response code " + responseCode + " for channel " + telegramId);
					reply = new JSONObject().put("status", "internalError");
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		} else {
			logger.log(Level.WARNING, "Wrong chat id " + telegramId);
		}
		return reply;
	}
}
