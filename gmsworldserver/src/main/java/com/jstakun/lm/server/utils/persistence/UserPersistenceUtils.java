package com.jstakun.lm.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
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
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.config.Commons.Property;

import com.jstakun.lm.server.persistence.User;

import net.gmsworld.server.utils.CryptoTools;
import net.gmsworld.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class UserPersistenceUtils {

    private static final Logger logger = Logger.getLogger(UserPersistenceUtils.class.getName());

    public static void persistUser(String login, String password, String email, String firstname, String lastname, boolean local) {
        
        try {
        	String landmarksUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "addItem";
        	String params = "login=" + URLEncoder.encode(login, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&type=user";
        	
        	if (firstname != null) {
        	   params += "&firstname=" + URLEncoder.encode(firstname, "UTF-8");
        	}
        	if (lastname != null) {
        		params += "&lastname=" + URLEncoder.encode(lastname, "UTF-8");
        	}
            if (email != null) {
        	   params += "&email=" + URLEncoder.encode(email, "UTF-8");
            }
            
        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
        	String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	logger.log(Level.INFO, "Received response: " + landmarksJson);
        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
        		JSONObject resp = new JSONObject(landmarksJson);
        		if (resp.optString("login") == null) {
        			logger.log(Level.SEVERE, "Failed to save user: " + landmarksJson);
        		}
        	}	
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        /*if (local) {
        	//User user = new User(login, password, email, firstname, lastname);
            //PersistenceManager pm = PMF.get().getPersistenceManager();
            try {
        		user = pm.makePersistent(user);
        	} catch (Exception ex) {
        		logger.log(Level.SEVERE, ex.getMessage(), ex);
        	} finally {
        		pm.close();
        	}
        }*/
    }

    public static User selectUserByLogin(String username) {
    	User user = null;
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(User.class, "login == username");
            query.setUnique(true);
            query.declareParameters("String username");
            user = (User) query.execute(username);          
            if (user != null) {
            	user.setLastLogonDate(new Date());
                pm.makePersistent(user);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
        */
        
        try {
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "itemProvider";
        	String params = "type=user&login=" + username;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
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

    /*
    public static boolean confirmUserRegistration(String login, Boolean confirmation) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        User user = selectUserByLogin(login);
        boolean result = false;

        try {
            if (user != null) {
                user.setConfirmed(confirmation);
                user.setConfirmDate(new Date());
                pm.makePersistent(user);
                result = true;
                confirmRemoteRegistration(user.getLogin());
            } else {
                logger.log(Level.INFO, "User with key: {0} is null!. User wanted to confirm his account: {1}", new Object[]{login, confirmation});
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;
    }*/
    
    public static boolean confirmUserRegistration(String login) {
    	boolean confirmed = false;
    	try {
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "itemProvider";
        	String params = "type=user&confirm=1&login=" + URLEncoder.encode(login, "UTF-8");			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
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
        return (selectUserByLogin(username) != null);
    }

    /*public static List<User> selectUsers(int first, int last) {
        List<User> results = new ArrayList<User>();
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(User.class);
            query.setRange(first, last);
            query.setOrdering("regDate desc");

            results = (List<User>) query.execute();
            //results = (List<User>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }*/
    
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
                     logger.log(Level.SEVERE, "User {0} decrypt failed with {1}", new Object[]{username, passwordString});
                 }
            }        		
        	if (loginRemote(username, passwordString)) {
        		auth = true;
        	} else {
        		logger.log(Level.SEVERE, "User {0} authn failed with {1}", new Object[]{username, passwordString});
        	}
    	}
    	
    	return auth;
    }
    
    private static boolean loginRemote(String login, String password) {
    	boolean auth = false;
    	try {
    		String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "itemProvider";
    		String params = "type=user&login=" + URLEncoder.encode(login, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");			 
    		String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
    		if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
    			JSONObject root = new JSONObject(gJson);
    			auth = root.optBoolean("auth", false);
    			//TODO add request attributes
    			//"email",
    			//"name" = "firstname" + "lastname"
    		} else {
    			logger.log(Level.SEVERE, "Received following server response: " + gJson);
    		}
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    	return auth;
    }
}
