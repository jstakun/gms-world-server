package com.jstakun.lm.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.gdata.util.common.util.Base64;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;

import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.User;

import net.gmsworld.server.utils.CryptoTools;
import net.gmsworld.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class UserPersistenceUtils {

    private static final Logger logger = Logger.getLogger(UserPersistenceUtils.class.getName());

    public static String persist(String login, String password, String email, String firstname, String lastname) {
        //login, password, email are mandatory
        try {
        	final String landmarksUrl = ConfigurationManager.getBackendUrl() + "/addItem";
        	String params = "login=" + URLEncoder.encode(login, "UTF-8") + "&password=" + URLEncoder.encode(getHash(password), "UTF-8") 
        							+ "&type=user&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
        	
        	if (StringUtils.isNotEmpty(firstname)) {
        	   params += "&firstname=" + URLEncoder.encode(firstname, "UTF-8");
        	}
        	if (StringUtils.isNotEmpty(lastname)) {
        		params += "&lastname=" + URLEncoder.encode(lastname, "UTF-8");
        	}
            if (StringUtils.isNotEmpty(email)) {
        	   params += "&email=" + URLEncoder.encode(email, "UTF-8");
            }
            
        	final String landmarksJson = HttpUtils.processFileRequest(new URL(landmarksUrl + "?" + params));
        	
        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
        		JSONObject resp = new JSONObject(landmarksJson);
        		if (resp.optString("login") == null && resp.optString("secret") == null) {
        			logger.log(Level.SEVERE, "Failed to save user: " + landmarksJson);
        		}
        		return resp.optString("secret");
        	}	else {
        		logger.log(Level.SEVERE, "Received following response: " + landmarksJson);
        		return null;
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        	return null;
        }
    }

    public static User selectUserByLogin(String username, String secret) {
    	User user = null;
        
        try {
        	final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
        	String params = "type=user" + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
        	if (StringUtils.isNotEmpty(username)) {
       		 	 params += "&login=" + username;
        	} else if (StringUtils.isNotEmpty(secret)) {
        		 params += "&secret=" + secret;
        	}
        	
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
        	
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		JSONObject root = new JSONObject(gJson);
        		if (root.has("login") && root.has("password")) {
        			user = jsonToUser(root);
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return user;
    }
    
    public static boolean confirmUserRegistration(String login) {
    	boolean confirmed = false;
    	try {
        	final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
        	final String params = "type=user&confirm=1&login=" + URLEncoder.encode(login, "UTF-8") + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		JSONObject root = new JSONObject(gJson);
        		confirmed = root.optBoolean("confirmed", false);
        		logger.log(Level.INFO, "User {0} registration is confirmed {1}", new Object[]{login, confirmed});
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return confirmed;
    }
    
    public static boolean userExists(String username) {
        return (selectUserByLogin(username, null) != null);
    }
    
    public static void removeUser(String secret) {
    	try {
        	final String gUrl = ConfigurationManager.getBackendUrl() + "/deleteItem";
        	final String params = "type=user&secret=" + secret + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
        	final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		logger.log(Level.INFO, "User removal status: " + gJson);
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    private static User jsonToUser(JSONObject user) throws IllegalAccessException, InvocationTargetException {
		User u = new User();
		   
		Map<String, String> geocodeMap = new HashMap<String, String>();
		for(Iterator<String> iter = user.keys();iter.hasNext();) {
			String key = iter.next();
			Object value = user.get(key);
			geocodeMap.put(key, value.toString());
		}
		   
		BeanUtils.populate(u, geocodeMap);
		   
		return u;
	}
    
    public static boolean login(String username, byte[] password) {
    	boolean auth = false;
    	
    	if (StringUtils.equals(username, Commons.getProperty(Property.DEFAULT_USERNAME))) {      
        	try {                  		
        		String pwdStr = Base64.encode(password);
        		if (StringUtils.equals(pwdStr, Commons.getProperty(Property.DEFAULT_PASSWORD))) {
        			logger.log(Level.INFO, "User default authn succeded!");
        			auth = true;
        		} else {
        			logger.log(Level.SEVERE, "User default authn failed!");
        		}
    		} catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
    	} 
    	
    	if (!auth) {
    		String passwordString = new String(password);
        	if (password.length % 8 == 0) {
            	try {
            		byte[] pwd = CryptoTools.decrypt(password);
            		passwordString = new String(pwd);
                 } catch (Exception e) {
                     logger.log(Level.WARNING, "User {0} decrypt failed", username);
                 }
            }        		
        	if (loginRemote(username, passwordString)) {
        		auth = true;
        	} else {
        		logger.log(Level.SEVERE, "User {0} authn failed", username);
        	}
    	}
    	
    	return auth;
    }
    
    private static boolean loginRemote(String login, String password) {
    	boolean auth = false;
    	try {
    		final String gUrl = ConfigurationManager.getBackendUrl() + "/itemProvider";
    		final String passwordEnc = getHash(password);
    		final String params = "type=user&login=" + URLEncoder.encode(login, "UTF-8") + "&password=" + URLEncoder.encode(passwordEnc, "UTF-8") 
    											+ "&enc=1" + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);			 
    		final String gJson = HttpUtils.processFileRequest(new URL(gUrl + "?" + params));
    		if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
    			JSONObject root = new JSONObject(gJson);
    			auth = root.optBoolean("auth", false);
    		} else {
    			logger.log(Level.SEVERE, "Received following server response: " + gJson);
    		}
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    	return auth;
    }
    
    private static String getHash(String password) throws Exception {
		MessageDigest digester = MessageDigest.getInstance("SHA-256");
	    digester.update(password.getBytes());
	    return Base64.encode(digester.digest());
	}
}