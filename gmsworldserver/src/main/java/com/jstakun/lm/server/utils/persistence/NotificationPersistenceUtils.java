package com.jstakun.lm.server.utils.persistence;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Notification;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.layers.TelegramUtils;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.memcache.CacheProvider;

public class NotificationPersistenceUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationPersistenceUtils.class.getName());

	private static Notification persist(String id, Notification.Status status) {
		Notification n = null;
		if (StringUtils.isNotEmpty(id)) {
			try {
				final String landmarksUrl = ConfigurationManager.getBackendUrl() + "/addItem";
	        	String params = "id=" + id + "&type=notification&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
	        	if (status.equals(Notification.Status.VERIFIED)) {
	        		params += "&status=1";
	        	}
	        	final String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl + "?" + params));
	        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
	        		JSONObject resp = new JSONObject(landmarksJson);
	        		if (resp.has("id")) {
	        			n = jsonToNotification(resp);
	        		}
	        	}	
			} catch (Exception ex) {
				logger.log(Level.SEVERE, ex.getMessage(), ex);
			}
		} 
		return n;
	}
	
	public static boolean remove(String id) {
		boolean removed = false;
		if (StringUtils.isNotEmpty(id)) {
			try {
	        	final String gUrl = ConfigurationManager.getBackendUrl() + "/deleteItem";
	        	final String params = "type=notification&id=" + id + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
	        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
	        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
	        		JSONObject root = new JSONObject(gJson);
	        		if  (StringUtils.equals(root.optString("status"), "ok")) {
	        			removed = true;
	        		}
	        		logger.log(Level.INFO, "Notification removal status: " + gJson);
	        	} else {
	        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
	        	}
	        } catch (Exception e) {
	        	logger.log(Level.SEVERE, e.getMessage(), e);
	        }
		}	
        return removed;
    }
	
	private static Notification findById(String id) {
		Notification n = null;
		if (StringUtils.isNotEmpty(id)) {
       		try {
       			final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
       			final String params = "type=notification&id=" + URLEncoder.encode(id, "UTF-8") + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
       			final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
       			if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
       				JSONObject root = new JSONObject(gJson);
       				if (root.has("id")) {
       					n = jsonToNotification(root);
       				}
       			} else {
       				logger.log(Level.SEVERE, "Received following server response: " + gJson);
       			}
       		} catch (Exception e) {
       			logger.log(Level.SEVERE, e.getMessage(), e);
       		}
		}
		return n;
	 }
	
	private static Notification findBySecret(String secret) {
		Notification n = null;
		if (StringUtils.isNotEmpty(secret)) {
			try {
	        	final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
	        	final String params = "type=notification&secret=" + secret + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
	        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
	        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
	        		JSONObject root = new JSONObject(gJson);
	        		if (root.has("id")) {
	        			n = jsonToNotification(root);
	        		}
	        	} else {
	        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
	        	}
	        } catch (Exception e) {
	        	logger.log(Level.SEVERE, e.getMessage(), e);
	        }
		}
		return n;
	}
	
	public static boolean isVerified(String id) {
		boolean verified = false;
		if (StringUtils.isNotEmpty(id)) {
			Notification n = findById(id);
			if (n != null && n .getStatus() == Notification.Status.VERIFIED) {
				verified = true;
			}
		}
		return verified;
	}

	public static synchronized Notification setVerified(String notification, boolean isRegistered) {
		Notification.Status status =  isRegistered ?  Notification.Status.VERIFIED : Notification.Status.UNVERIFIED;
		return persist(notification, status);
	}
	
	public static synchronized Notification verifyWithSecret(String secret) {
		Notification n = findBySecret(secret);
		if (n != null) {
			if (n.getStatus() == Notification.Status.UNVERIFIED) {
				persist(n.getId(), Notification.Status.VERIFIED);
			}
		} 
		return n;
	}
	
	public static JSONObject registerTelegram(String telegramId, int appVersion, CacheProvider cacheProvider) throws IOException {
		JSONObject reply = null;
		if (TelegramUtils.isValidTelegramId(telegramId)) {
			if (isVerified(telegramId)) {
				if (StringUtils.isNumeric(telegramId)) {
					TelegramUtils.sendTelegram(telegramId, "You've been already registered to Device Locator notifications.\n"
						+ "You can unregister at any time by sending /unregister command message to @device_locator_bot");
				} else {
					TelegramUtils.sendTelegram(telegramId, "You've been already registered to Device Locator notifications.\n"
							+ "You can unregister at any time by sending /unregister " + telegramId + " command message to @device_locator_bot");
				}
				reply = new JSONObject().put("status", "registered");
			} else if (StringUtils.isNumeric(telegramId)) {
				if (!cacheProvider.containsKey("telegramId:"+telegramId +":invalid")) {
					Integer responseCode =  TelegramUtils.verifyTelegramChat(telegramId);
					if (responseCode != null && responseCode == 200) {
						Notification n = setVerified(telegramId, false);
						if (appVersion >= 30) {
							String tokens[] = StringUtils.split(n.getSecret(), ".");
							if (tokens.length == 2 && tokens[1].length() == 4 && StringUtils.isNumeric(tokens[1])) {
								String activationCode = tokens[1];
								TelegramUtils.sendTelegram(telegramId, "Welcome to Device Locator!\n"
										+ "Here is your activation code: <b>" + activationCode + "</b>.\n"
										+ "Please come back to Device Locator mobile application and enter this code when prompted.\n"
										+ "Only after you confirm your registration, you will start receiving notifications from Device Locator. "
										+ "If you didn\'t ask, please ignore this message.\n"
										+ "Thank you\n"
										+ "Device Locator Team");
								reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
							} else {
								reply = new JSONObject().put("status", "internalError").put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
							}
						} else {
							TelegramUtils.sendTelegram(telegramId, "If this is correct please send us back /register command message, otherwise please ignore this message.");
							reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
						}				
					} else if (responseCode != null && responseCode == 400) {
						reply = new JSONObject().put("status", "failed").put("code", HttpServletResponse.SC_BAD_REQUEST);
						cacheProvider.put("telegramId:"+telegramId +":invalid", 400);
					} else {
						reply = new JSONObject().put("status", "internalError").put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					} 
				} else {
					reply = new JSONObject().put("status", "failed").put("code", HttpServletResponse.SC_BAD_REQUEST);
				}
			} else if ((StringUtils.startsWithAny(telegramId, new String[]{"@","-100"}))) {
				if (!cacheProvider.containsKey("telegramId:"+telegramId +":invalid")) {
					Integer responseCode = TelegramUtils.sendTelegram(telegramId, "We've received Device Locator notifications registration request for this Channel.");
					if (responseCode != null && responseCode == 200) {
						Notification n = setVerified(telegramId, false);
						if (appVersion >= 30) {
							String tokens[] = StringUtils.split(n.getSecret(), ".");
							if (tokens.length == 2 && tokens[1].length() == 4 && StringUtils.isNumeric(tokens[1])) {
								String activationCode = tokens[1];
								TelegramUtils.sendTelegram(telegramId, "If this is correct here is your activation code: " + activationCode +  ", otherwise please ignore this message.");
								reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
							} else {
								reply = new JSONObject().put("status", "internalError").put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
							}
						} else {
							TelegramUtils.sendTelegram(telegramId, "If this is correct please contact us via email at: device-locator@gms-world.net and send your Channel ID: " + telegramId + ", otherwise please ignore this message.");
							reply = new JSONObject().put("status", "unverified").put("secret", n.getSecret());
						}
					} else if (responseCode != null && responseCode == 400) {
						logger.log(Level.SEVERE, "Received response code " + responseCode + " for channel " + telegramId);
						reply = new JSONObject().put("status", "badRequestError").put("code", HttpServletResponse.SC_BAD_REQUEST);
						cacheProvider.put("telegramId:"+telegramId +":invalid", 400);
					} else if (responseCode != null && responseCode == 403) {
						logger.log(Level.SEVERE, "Received response code " + responseCode + " for channel " + telegramId);
						reply = new JSONObject().put("status", "permissionDenied").put("code", HttpServletResponse.SC_FORBIDDEN);	
					} else {
						logger.log(Level.SEVERE, "Received response code " + responseCode + " for channel " + telegramId);
						reply = new JSONObject().put("status", "internalError").put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					}
				} else {
					logger.log(Level.SEVERE, "Telegram id " + telegramId + " is invalid");
					reply = new JSONObject().put("status", "badRequestError").put("code", HttpServletResponse.SC_BAD_REQUEST);
				}
			}
		} else {
			logger.log(Level.SEVERE, "Cache response: Telegram id: " + telegramId + "is invalid");
			reply = new JSONObject().put("status", "invalidTelegramId").put("code", HttpServletResponse.SC_BAD_REQUEST);
		}
		return reply;
	}
	
	private static Notification jsonToNotification(JSONObject notification) throws IllegalAccessException, InvocationTargetException {
		Notification n= new Notification();
		   
		Map<String, String> geocodeMap = new HashMap<String, String>();
		for(Iterator<String> iter = notification.keys();iter.hasNext();) {
			String key = iter.next();
			Object value = notification.get(key);
			geocodeMap.put(key, value.toString());
		}
		   
		ConvertUtils.register(new EnumConverter(), Notification.Status.class);
		ConvertUtils.register(DateUtils.getRHCloudDateConverter(), Date.class);
		BeanUtils.populate(n, geocodeMap);
		   
		return n;
	}
	
	/*public static void migrate() {
		List<Notification> verified = findByStatus(Notification.Status.VERIFIED);
		logger.log(Level.INFO, "Found " + verified.size() + " notifications...");
		for (Notification n : verified) {
			persist(n.getId(), Notification.Status.VERIFIED);
		}
		List<Notification> unverified = findByStatus(Notification.Status.UNVERIFIED);
		logger.log(Level.INFO, "Found " + unverified.size() + " notifications...");
		for (Notification n : unverified) {
			persist(n.getId(), Notification.Status.UNVERIFIED);
		}
	}*/
	
	private static class EnumConverter implements Converter {    
	    
		@Override
		public Object convert(Class tClass, Object o) {
			String enumValName = (String) o;
	        Enum[] enumConstants = (Enum[]) tClass.getEnumConstants();
	        
	        for (Enum enumConstant : enumConstants) {
	            if (enumConstant.name().equals(enumValName)) {
	                return enumConstant;
	            }
	        }

	        throw new ConversionException(String.format("Failed to convert %s value to %s class", enumValName, tClass.toString()));
		}
	}
}
