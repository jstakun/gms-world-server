package com.jstakun.lm.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;

public class NotificationPersistenceUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationPersistenceUtils.class.getName());

	private static Notification persist(String id, Notification.Status status) {
		Notification n = null;
		if (StringUtils.isNotEmpty(id)) {
			try {
				String landmarksUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "addItem";
	        	String params = "id=" + id + "&type=notification";
	        	if (status.equals(Notification.Status.VERIFIED)) {
	        		params += "&status=1";
	        	}
	        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
	        	String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
	        	logger.log(Level.INFO, "Received response: " + landmarksJson);
	        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
	        		JSONObject resp = new JSONObject(landmarksJson);
	        		if (resp.has("id")) {
	        			n = jsonToNotification(resp);
	        		}
	        		//logger.log(Level.INFO, "Received response: " + landmarksJson);
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
	        	String gUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "deleteItem";
	        	String params = "type=notification&id=" + id;			 
	        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
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
		try {
        	String gUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "itemProvider";
        	String params = "type=notification";
        	if (StringUtils.isNotEmpty(id)) {
       		 	 params += "&id=" + id;
        	} 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
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
		return n;
	 }
	
	private static Notification findBySecret(String secret) {
		Notification n = null;
		if (StringUtils.isNotEmpty(secret)) {
			try {
	        	String gUrl = ConfigurationManager.getParam(ConfigurationManager.GMS_LANDMARK_URL, ConfigurationManager.BACKEND_SERVER_URL) + "itemProvider";
	        	String params = "type=notification";
	        	if (StringUtils.isNotEmpty(secret)) {
	       		 	 params += "&secret=" + secret;
	        	} 
	        	//logger.log(Level.INFO, "Calling: " + gUrl);
	        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
	        	//logger.log(Level.INFO, "Received response: " + gJson);
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
