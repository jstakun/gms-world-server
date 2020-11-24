package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Notification;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.RoutesUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceWebUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

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
		 GeocodeHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
		 LayerHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
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
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			if (HttpUtils.isEmptyAny(request, "type")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				JSONObject reply = new JSONObject();
				final String type = request.getParameter("type");
				final int appId = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), -1);
				final String routeId = request.getHeader(Commons.ROUTE_ID_HEADER);
				final int appVersion = NumberUtils.getInt(request.getHeader(Commons.APP_VERSION_HEADER), -1);
				final String deviceId = request.getHeader(Commons.DEVICE_ID_HEADER);
				final String deviceName = request.getHeader(Commons.DEVICE_NAME_HEADER);
				String username = null;
				if (appId == Commons.DL_ID && appVersion >= 78) {
					username = request.getParameter("username");
				}

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
	            
	            final Locale locale = request.getLocale();
				String language  = "en";
				if (locale != null) {
					language = locale.getLanguage();
				}
				
	            boolean routePointAdded = false;
	            logger.log(Level.INFO, "Request type " + type);
	            if (language != null) {
	            	logger.log(Level.INFO, "Request language " + language);
	            }
	            
	            if (GeocodeUtils.isValidLatitude(latitude) && GeocodeUtils.isValidLongitude(longitude) && appId >= 0) {
					if (StringUtils.isEmpty(routeId)) {
						//create new landmark but skip dl route points
						logger.log(Level.INFO, "Creating landmark...");
						try {
							Landmark l = new Landmark();
							l.setLatitude(latitude);
							l.setLongitude(longitude);
							l.setName(Commons.MY_POSITION_LAYER);
						
							String userStr = null;
							if (appId == Commons.DL_ID && StringUtils.isNotEmpty(deviceId)) {
								userStr = deviceId;
							} else {
								userStr = StringUtil.getUsername(request.getAttribute("username"), request.getParameter("username"));
							}
							//in LM from v1086, DA from v86 username is Base64 encoded string
							if (((appId == Commons.LM_ID && appVersion >= 1086) || (appId == Commons.DA_ID && appVersion >= 86)) 
									&& StringUtils.isNotEmpty(userStr) && !StringUtils.equalsIgnoreCase(userStr, "mypos") && Base64.isArrayByteBase64(userStr.getBytes())) {
								try {
									userStr = new String(Base64.decodeBase64(userStr));
								} catch (Exception e) {
									logger.log(Level.SEVERE, " Username " + userStr + " failed Base64 decoding appId: " + appId + ", version: " + appVersion + ", error: " + e.getMessage());
								}
							}
							if (StringUtils.isEmpty(userStr)) {
								throw new Exception("Username can't be null!");
							} 
							l.setUsername(userStr);
						
							if (!LandmarkPersistenceWebUtils.isSimilarToNewest(l, 10)) {
								String socialIds = request.getParameter("socialIds");
								LandmarkPersistenceWebUtils.setFlex(l, request);
								l.setLayer(Commons.MY_POS_CODE);
								LandmarkPersistenceUtils.persistLandmark(l, GoogleCacheProvider.getInstance());
								if (l.getId() > 0) {
									LandmarkPersistenceWebUtils.notifyOnLandmarkCreation(l, request.getHeader("User-Agent"), socialIds, null, null, appId);
								}
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
						}
					}
				   
					if (StringUtils.isNotEmpty(routeId)) {
						logger.log(Level.INFO, "Sending route " + routeId + " point...");
						//add route point to cache
						routePointAdded = RoutesUtils.addRoutePointToCache(routeId, latitude, longitude);
					}
					
					if (StringUtils.isNotEmpty(deviceId)) {
						//add device location to cache and update geo
						final String acc =  request.getHeader(Commons.ACC_HEADER);
			   	   		Double[] coords = CacheUtil.getDeviceLocation(deviceId);
			   	   		if (coords == null || (coords != null && NumberUtils.distanceInKilometer(latitude, longitude, coords[0], coords[1]) >= DeviceManagerServlet.CACHE_DEVICE_DISTANCE)) {
			   	   			String geo = "geo:" + latitude + " " + longitude;
			   	   			if (StringUtils.isNotEmpty(acc)) {
			   	   				geo += " " + acc;
			   	   			}
			   	   			final int status = DevicePersistenceUtils.setupDevice(deviceId, deviceName, username, null, geo);
			   	   			logger.log(Level.INFO, "Saved device " + deviceId + " configuration with status " + status + "\nNew configuration - name:" + deviceName + ", username:" + username + ", " + geo);						
			   	   			if (status == 1) {
			   	   				CacheUtil.cacheDeviceLocation(deviceId, latitude, longitude, acc);
			   	   			}
			   	   		} else {
			   	   		logger.log(Level.INFO, "Device " + deviceId + " location is already saved");
			   	   		}
					}
	            } else if (latitude != null || longitude != null) {
					logger.log(Level.SEVERE, "Invalid request: latitude " + latitude + ", longitude: " + longitude + ", appId: " + appId);
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
					//email engagement
					String email = request.getParameter("e");
					long lastStartupTime = NumberUtils.getLong(request.getParameter("lst"), -1);
					String useCount = request.getParameter("uc");
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(lastStartupTime);
					logger.log(Level.INFO, "Received usage notification from " + (email != null ? email : "guest")
									+ " last startup time: " + DateFormat.getDateTimeInstance().format(cal.getTime())	 + ", use count: " + useCount);
					int minInterval = NumberUtils.getInt(ConfigurationManager.getParam(net.gmsworld.server.config.ConfigurationManager.NOTIFICATIONS_INTERVAL, "14"), 14);
					int maxInterval = 31;
					long interval = System.currentTimeMillis() - lastStartupTime;
					if (interval > (minInterval * ONE_DAY) && interval < (maxInterval * ONE_DAY) && email != null) {
						//send email notification if lastStartupTime > week ago
						//send not more that once a week
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
						final String message = request.getParameter("message");
						final String telegramId = request.getParameter("chatId");
						if (StringUtils.equals(telegramId, "@dlcorrelationId")) {
			            	//message is correlationId
			            	String val = CacheUtil.getString(message); //telegramId _+_ deviceid  _+_ command
			            	if (val != null) {
			            		String[] data = StringUtils.split(val, "_+_");
			            		if (data != null && data.length == 3 && TelegramUtils.isValidTelegramId(data[0])) {
			            			String authStatus = request.getHeader(Commons.AUTH_HEADER);
			            			if (StringUtils.equals(authStatus, "failed")) {
			            				TelegramUtils.sendTelegram(data[0], "Command " + data[2] + " has been rejected by device " + data[1] + "!");
			            			} else {
			            				TelegramUtils.sendTelegram(data[0], "Command " + data[2] + " has been received by device " + data[1] + ".");
			            			}
			            		} else {
			            			logger.log(Level.SEVERE, "Invalid " +  message + " entry " + val); 
			            		}
			            	} else {
			            		logger.log(Level.SEVERE, "No entry found " +  message);
			            	}
			            } else if (TelegramUtils.isValidTelegramId(telegramId) && StringUtils.isNotEmpty(message)) {
			            	if (StringUtils.isNotEmpty(routeId) && !routePointAdded) {
								//route point has not been added skipping sending notification
								logger.log(Level.WARNING, "Skipping sending telegram notification with route point update");
								logger.log(Level.INFO, "Message:\n" + message);
								reply = new JSONObject().put("status", "skipped");
							} else {
								// check if chat id is on white list
								if (NotificationPersistenceUtils.isVerified(telegramId)) {
									if (!CacheUtil.containsKey(telegramId + ":blocked")) {
										int status = TelegramUtils.sendTelegram(telegramId, message);
										boolean blocked = false;
										if (status == 403) {
											logger.log(Level.SEVERE, "Our bot has been blocked by user " + telegramId);
											blocked = true;
											CacheUtil.put(telegramId + ":blocked", "1", CacheType.NORMAL);
											reply = new JSONObject().put("status", "failed");
										}
										if (!blocked && GeocodeUtils.isValidLatitude(latitude) && GeocodeUtils.isValidLongitude(longitude)) {
											status = TelegramUtils.sendLocationTelegram(telegramId, latitude, longitude);
											if (status == 403) {
												logger.log(Level.SEVERE, "Our bot has been blocked by user " + telegramId);
												blocked = true;
												CacheUtil.put(telegramId + ":blocked", "1", CacheType.NORMAL);
												reply = new JSONObject().put("status", "failed");
											}
										}
										if (!blocked) {
											reply = new JSONObject().put("status", "sent");
										}
									}
								}  else {
									logger.log(Level.SEVERE, "Telegram chat or channel Id " + telegramId + " is not on the whitelist!");
									logger.log(Level.WARNING, "Message won't be delivered to device " + deviceId + ":\n" + message);
									reply = new JSONObject().put("status", "unverified");
								}
							}
						} else {
							logger.log(Level.SEVERE, "Wrong message, chat or channel id " + telegramId);
							reply = new JSONObject().put("status", "failed");
						}
					} else {
						logger.log(Level.SEVERE, "Wrong application " + appId);
					}
				} else if (StringUtils.equals(type, "m_dl")) {
					//email notification
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String message = request.getParameter("message");
						try {
							if (StringUtils.isNotEmpty(message)) {
								message = URLDecoder.decode(message, "UTF-8");
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);
						}
						final String title = request.getParameter("title");
						final String emailTo = request.getParameter("emailTo");
						if (StringUtils.isNotEmpty(emailTo) && (StringUtils.isNotEmpty(title) || StringUtils.isNotEmpty(message))) {
							if (StringUtils.isNotEmpty(routeId) && !routePointAdded) {
								//route point has not been added skipping sending notification
								logger.log(Level.WARNING, "Skipping sending email notification with route point update");
								logger.log(Level.INFO, "Message:\n" + message);
								reply = new JSONObject().put("status", "skipped");
							} else {
								//check if email is on white list
								if (NotificationPersistenceUtils.isVerified(emailTo)) {
									MailUtils.sendDeviceLocatorMessage(emailTo, message, title);
									reply = new JSONObject().put("status", "sent");	
								} else {
									logger.log(Level.SEVERE, "Email address " + emailTo + " is not on the whitelist!");
									//logger.log(Level.WARNING, "Message won't be delivered to device " + deviceId + ":\n" + message);
									//reply = new JSONObject().put("status", "unverified");
									logger.log(Level.WARNING, "Sending email registration request");
									reply = registerEmail(emailTo, false, appVersion, deviceName, deviceId, language);
								}
							}
						} else {
							logger.log(Level.SEVERE, "Wrong email to " + emailTo);
							reply = new JSONObject().put("status", "failed");
						}
					} else {
						logger.log(Level.SEVERE, "Wrong application id " + appId);
					}
				} else if (StringUtils.equals(type, "register_t")) {
					//register for telegram notifications
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						String telegramId = request.getParameter("chatId");
						reply = NotificationPersistenceUtils.registerTelegram(telegramId, appVersion, GoogleCacheProvider.getInstance());
					} else {
						logger.log(Level.SEVERE, "Wrong application id " + appId);
					}
				} else if (StringUtils.equals(type, "register_m")) {
					//register for email notifications
					if (appId == Commons.DL_ID && StringUtils.startsWith(request.getRequestURI(), "/s/")) {
						reply = registerEmail(request.getParameter("email"), StringUtils.equalsIgnoreCase(request.getParameter("validate"), "false"), appVersion, deviceName, deviceId, language);
					} else {
						logger.log(Level.SEVERE, "Wrong application id " + appId);
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
							logger.log(Level.SEVERE, "Wrong application id " + appId);
					}
				} else if (StringUtils.equals(type, "reset")) {
					String login = request.getParameter("login");
					if (StringUtils.isNotEmpty(login)) {
						User u = UserPersistenceUtils.selectUserByLogin(login, null);
						if (u != null) {
							String nick = u.getFirstname();
							if (StringUtils.isEmpty(nick)) {
								nick = u.getLogin();
							}
							String result = MailUtils.sendResetPassword(u.getEmail(), nick, u.getSecret(), getServletContext());
							reply = new JSONObject().put("status", result);
						} else {
							reply = new JSONObject().put("status", "invalid login");
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						}
					} else {
						reply = new JSONObject().put("status", "failed");
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					}
				}
				//else if (StringUtils.equals(type, "register_fbm")) {
				//} else if (StringUtils.equals(type, "fbm_dl")) {
				//}
				
				if (reply != null) {
					out.print(reply.toString());
					if (reply.has("code")) {
						response.setStatus(reply.getInt("code"));
					}
				} else {
					//reply is empty
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				}
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

	private JSONObject registerEmail(String email, boolean skipVerify, int appVersion, String deviceName, String deviceId, String language) throws IOException {
		JSONObject reply = null;
		if (StringUtils.endsWithIgnoreCase(email, "@cloudtestlabaccounts.com")) {
			reply = new JSONObject().put("status", "blacklisted").put("code", HttpServletResponse.SC_BAD_REQUEST);
		} else if (StringUtils.isNotEmpty(email)) {
			if (NotificationPersistenceUtils.isVerified(email)) {
				if (CacheUtil.containsKey("mailto:"+email+":verified")) {
					logger.log(Level.INFO, "Skipping sending registration notification...");
				} else {
					final Notification n = NotificationPersistenceUtils.setVerified(email, true);
					if (n != null) {
						final String status = MailUtils.sendDeviceLocatorRegistrationNotification(email, email, n.getSecret(), this.getServletContext(), deviceName, deviceId);
						if (StringUtils.equalsIgnoreCase(status, MailUtils.STATUS_OK)) {
							CacheUtil.put("mailto:"+email+":verified", n.getSecret(), CacheType.FAST);
						}
					}
				}
				reply = new JSONObject().put("status", "registered");
			} else if (appVersion >= 30) {
				if (CacheUtil.containsKey("mailto:"+email+":sent")) {
					reply = new JSONObject().put("status", "unverified").put("secret", CacheUtil.getObject("mailto:"+email+":sent"));
					logger.log(Level.INFO, "Skipping sending registration request...");
				} else if (!CacheUtil.containsKey("mailto:"+email+":invalid")) {
					int verificationStatus;
					if (skipVerify) {
						verificationStatus = 200;
					} else {
						verificationStatus = MailUtils.emailAccountExists(email);
					}
					if (verificationStatus == 200) {
						Notification n = NotificationPersistenceUtils.setVerified(email, false);
						String status = null;
						if (appVersion >= 69) {
							if (StringUtils.isNotEmpty(deviceName)) {
								status = MailUtils.sendDeviceLocatorVerificationRequest(email, email, n.getSecret(), this.getServletContext(), 4, deviceName, deviceId, language);
							} else {
								status = MailUtils.sendDeviceLocatorVerificationRequest(email, email, n.getSecret(), this.getServletContext(), 3, null, null, language);
							}
						} else {
							status = MailUtils.sendDeviceLocatorVerificationRequest(email, email, n.getSecret(), this.getServletContext(), 2, null, null, language);
						}
						if (StringUtils.equals(status, "ok")) {
							reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
							CacheUtil.put("mailto:"+email+":sent", n.getSecret(), CacheType.FAST);
						} else {
							reply = new JSONObject().put("status", status);
						}
					} else if (verificationStatus >= 500) {
						logger.log(Level.SEVERE, email + " verification failed with code " + verificationStatus);
						reply = new JSONObject().put("status", "failed").put("code", verificationStatus); 
					} else if (verificationStatus >= 400) {
						logger.log(Level.SEVERE, email + " verification failed with code " + verificationStatus);
						reply = new JSONObject().put("status", "failed").put("code", verificationStatus);
						CacheUtil.put("mailto:"+email+":invalid", verificationStatus, CacheType.NORMAL);
					} else {
						logger.log(Level.SEVERE, email + " verification failed with code " + verificationStatus);
						reply = new JSONObject().put("status", "failed").put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					}
				} else {
					final Integer code = (Integer) CacheUtil.getObject("mailto:"+email+":invalid");
					logger.log(Level.SEVERE, email + " verification failed with code " + code + ". Check older logs for root cause.");
					reply = new JSONObject().put("status", "failed").put("code", code);
				}
			} else {
				Notification n = NotificationPersistenceUtils.setVerified(email, false);
				String status = MailUtils.sendDeviceLocatorVerificationRequest(email, email, n.getSecret(), this.getServletContext(), 0, null, null, language);
				if (StringUtils.equals(status, "ok")) {
					reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
					CacheUtil.put("mailto:"+email+":sent", n.getSecret(), CacheType.FAST);
				} else {
					reply = new JSONObject().put("status", status);
				}
			} 
		} else {
			reply = new JSONObject().put("status", "failed").put("code", HttpServletResponse.SC_BAD_REQUEST);
			logger.log(Level.SEVERE, "Email is empty!"); 
		}
		return reply;
	}
}
