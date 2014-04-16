package com.jstakun.lm.server.social;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.HttpUtils;

public class LinkedInUtils {
	
	private static final Logger logger = Logger.getLogger(LinkedInUtils.class.getName());
	
	public static void sendPost(String url, String title, int type, String token, String secret) {
		ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
		InputStream is = null;
		try {
            String message = null;
            if (type == Commons.BLOGEO) {
            	message = rb.getString("Social.ln.message.blogeo");
            } else if (type == Commons.LANDMARK) {
                message = rb.getString("Social.ln.message.landmark");
            } else if (type == Commons.MY_POS) {
                message = rb.getString("Social.ln.message.mypos");
            } else if (type == Commons.LOGIN) {
                message = rb.getString("Social.login");
            }
            
            URL shareUrl = new URL("https://api.linkedin.com/v1/people/~/shares?oauth2_access_token=" + token);
            
            HashMap<String, Object> jsonMap = new HashMap<String, Object>();
            jsonMap.put("comment", message);
             
            JSONObject contentObject = new JSONObject();
            contentObject.put("title", title);
            contentObject.put("description", rb.getString("Social.login.desc"));
            contentObject.put("submitted-url", url);
            contentObject.put("submitted-image-url", ConfigurationManager.SERVER_URL + "images/poi_j.png");
             
            jsonMap.put("content", contentObject);
             
            JSONObject visibilityObject = new JSONObject();
            visibilityObject.put("code", "anyone");
             
            jsonMap.put("visibility", visibilityObject);
            
            String data = new JSONObject(jsonMap).toString();
            
            //logger.log(Level.INFO, data);
             
            HttpURLConnection conn = (HttpURLConnection) shareUrl.openConnection();
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-li-format", "json");
            conn.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            
            IOUtils.write(data, conn.getOutputStream());
            
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpServletResponse.SC_OK || responseCode == 201) {
                is = conn.getInputStream();
               
            } else {
                is = conn.getErrorStream();
                
            }          
            
            String response = null; 
            if (is != null) {
                response = IOUtils.toString(is, "UTF-8");
            } else {
            	response = Integer.toString(responseCode);
            }
            
            logger.log(Level.INFO, "Status update sent to LinkedIn: " + response);
            //return MessageFormat.format(rb.getString("Social.send.post.success"), "LinkedIn");
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "LinkedInUtils.sendPost exception", e);
        	//return MessageFormat.format(rb.getString("Social.send.post.failure"), "LinkedIn");
        } finally {
            if (is != null) {
            	try {
                is.close();
            	} catch (Exception e) {
            		
            	}
            }
        }
    }
	
	public static Map<String, String> getUserDate(String accessToken) {
		Map<String, String> userData = new HashMap<String, String>();
		
		try {
			URL profileUrl = new URL("https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address)?oauth2_access_token=" + accessToken + "&format=json");
			
			String response = HttpUtils.processFileRequest(profileUrl, "GET", null, null);
			
			//logger.log(Level.INFO, response);
			
			JSONObject json = new JSONObject(response);
		    String id = json.optString("id");
		    if (id != null) {
		    	userData.put(ConfigurationManager.LN_USERNAME, id);
		    }
		    String fn = json.optString("firstName");
		    String ln = json.optString("lastName");
		    if (StringUtils.isNotEmpty(fn) && StringUtils.isNotEmpty(ln)) {
		    	userData.put(ConfigurationManager.LN_NAME, fn + " " + ln);
		    }
		  
		    String email = json.optString("emailAddress"); 
		    if (StringUtils.isNotEmpty(email)) {
		        userData.put(ConfigurationManager.USER_EMAIL, email);
		    }
			
		} catch (Exception e) {
        	logger.log(Level.SEVERE, "LinkedInUtils.getUserData exception", e);
        	//return MessageFormat.format(rb.getString("Social.send.post.failure"), "LinkedIn");
        }
        
        return userData;
	}
}
