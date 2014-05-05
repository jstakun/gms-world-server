package com.jstakun.lm.server.social;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;

public class LinkedInUtils {
	
	private static final Logger logger = Logger.getLogger(LinkedInUtils.class.getName());
	
	protected static void sendPost(String url, String title, int type, String token) {
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
}
