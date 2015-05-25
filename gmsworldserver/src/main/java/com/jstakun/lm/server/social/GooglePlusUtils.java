package com.jstakun.lm.server.social;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.UrlUtils;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.ItemScope;
import com.google.api.services.plus.model.Moment;

/**
 *
 * @author jstakun
 */
public class GooglePlusUtils {

    private static final Logger logger = Logger.getLogger(GooglePlusUtils.class.getName());
    private static final Random random = new Random();

    protected static void sendMessage(String accessToken, String refreshToken, String key, String url, String user, String name, Double lat, Double lng, int type) {
        if (accessToken != null || refreshToken != null) {
            
        	final String[] images = {"blogeo_j.png", "blogeo_a.png", "poi_j.png", "poi_a.png"};
        	int imageId = NumberUtils.normalizeNumber(random.nextInt(4), 0, 3);

            String message = null;
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            if (type == Commons.SERVER) {
                
                message = String.format(rb.getString("Social.gl.server"), user, name, url);
            } else if (type == Commons.BLOGEO) {
                message = String.format(rb.getString("Social.gl.message.blogeo"), url);
            } else if (type == Commons.LANDMARK) {
                message = String.format(rb.getString("Social.gl.message.landmark"), user, url);
            } else if (type == Commons.MY_POS) {
            	message = String.format(rb.getString("Social.gl.message.mypos"), url);
            } else if (type == Commons.LOGIN) {
            	message = rb.getString("Social.login");
            } else if (type == Commons.CHECKIN) {
            	message = String.format(rb.getString("Social.gp.checkin"), user, name , url);
            }

            if (message != null) {
            	String token = accessToken;
                if (token == null) {
                	//logger.log(Level.INFO, "RefreshToken: " + refreshToken);
                	Map<String, String> refresh =  requestAccessToken(refreshToken);
                	if (refresh.containsKey("token")) {
                	   token = refresh.get("token");
                	}
                }
                
                if (token != null) {
                	Plus plus = getPlus(token, refreshToken);
                
                	sendMoment(plus, message, "Message from GMS World", ConfigurationManager.SERVER_URL + "images/" + images[imageId], lat, lng);

                	sendUrlMoment(plus, url);
                } else {
                	logger.log(Level.SEVERE, "Token is null!");
                }
            }
        } else {
            logger.log(Level.SEVERE, "Token is null!");
        }
    }

    protected static void sendImageMessage(String showImageUrl, String username, String imageUrl, String flex, int type) {
        String userMask = UrlUtils.createUsernameMask(username); 
        ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
        Plus plus = getPlus(null, null);
        if (type == Commons.SCREENSHOT) {
        	String message = String.format(rb.getString("Social.gp.screenshot"), userMask , showImageUrl);
            sendMoment(plus, message, "Message from GMS World", imageUrl, null, null);
        	sendUrlMoment(plus, showImageUrl);
        } else if (type == Commons.ROUTE) {
        	String url = UrlUtils.getGoogleShortUrl(imageUrl);
        	String message = String.format(rb.getString("Social.gp.route"), userMask, flex, url);
        	sendMoment(plus, message, "Message from GMS World", imageUrl, null, null);
        	sendUrlMoment(plus, showImageUrl);
        }
    }

    public static Plus getPlus(String accessToken, String refreshToken) {
        HttpTransport httpTransport = new UrlFetchTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        GoogleCredential requestInitializer = new GoogleCredential.Builder().setClientSecrets(Commons.getProperty(Property.GL_PLUS_KEY), Commons.getProperty(Property.GL_PLUS_SECRET)).
                setJsonFactory(jsonFactory).
                setTransport(httpTransport).build();

        if (accessToken == null && refreshToken == null) {
            requestInitializer.setAccessToken(Commons.getProperty(Property.gl_plus_token)).setRefreshToken(Commons.getProperty(Property.gl_plus_refresh));
        } else {
            requestInitializer.setAccessToken(accessToken).setRefreshToken(refreshToken);
        }

        Plus plus = new Plus.Builder(httpTransport, jsonFactory, requestInitializer).setApplicationName("Landmark Manager").build();
        return plus;
    }

    private static void sendUrlMoment(Plus plus, String url) {
        if (url != null) {
        	Moment moment = new Moment();
        	moment.setType("http://schema.org/AddAction");
        	ItemScope itemScope = new ItemScope();
        	itemScope.setUrl(url);
        	moment.setObject(itemScope);
            try {
                Moment momentResult1 = plus.moments().insert("me", "vault", moment).execute();
                logger.log(Level.INFO, "Created activity with id: {0}", momentResult1.getId());
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "GooglePlusUtils.sendUrlMoment() exception", ex);
            }
        } else {
            logger.log(Level.SEVERE, "Url is null!");
        }
    }

    private static void sendMoment(Plus plus, String name, String desc, String image, Double lat, Double lng) {
        try {
            Moment moment = new Moment();
        	moment.setType("http://schema.org/AddAction");
            ItemScope itemScope = new ItemScope();
            itemScope.setId("gms-world-msg-" + System.currentTimeMillis());
            itemScope.setType("http://schema.org/AddAction");
            itemScope.setName(name);
            itemScope.setDescription(desc);
            itemScope.setImage(image);
            if (lat != null) {
                itemScope.setLatitude(lat);
            }
            if (lng != null) {
                itemScope.setLongitude(lng);
            }
            moment.setObject(itemScope);
            Moment momentResult = plus.moments().insert("me", "vault", moment).execute();
            logger.log(Level.INFO, "Created activity with id: {0}", momentResult.getId());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "GooglePlusUtils.sendMoment() exception", ex);
        }
    }
    
    private static Map<String, String> requestAccessToken(String refreshToken) {
    	Map<String, String> result = new HashMap<String, String>();
    	if (refreshToken != null) {
            try {
                logger.log(Level.INFO, "GooglePlusUtils.requestAccessToken(): renewing access token...");
                TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(),
                        new JacksonFactory(), refreshToken, Commons.getProperty(Property.GL_PLUS_KEY), Commons.getProperty(Property.GL_PLUS_SECRET)).execute();
                String accessToken = response.getAccessToken();
                Long expires_in = response.getExpiresInSeconds();
                if (accessToken != null) {
                	result.put("token", accessToken);
                	logger.log(Level.INFO, "GooglePlusUtils.requestAccessToken(): new token received.");
                }
                if (expires_in != null) {
                	result.put("expires_in", expires_in.toString());
                }
            } catch (TokenResponseException e) {
                if (e.getDetails() != null) {
                	logger.log(Level.SEVERE, "GooglePlusCommons.requestAccessToken() exception: " + e.getDetails().getError(), e);
                    if (e.getDetails().getErrorDescription() != null) {
                    	logger.log(Level.SEVERE, "GooglePlusCommons.requestAccessToken() exception: " + e.getDetails().getErrorDescription(), e);
                    }
                    if (e.getDetails().getErrorUri() != null) {
                    	logger.log(Level.SEVERE, "GooglePlusCommons.requestAccessToken() exception: " + e.getDetails().getErrorUri(), e);
                    }
                } else {
                	logger.log(Level.SEVERE, "GooglePlusCommons.requestAccessToken() exception: " + e.getMessage(), e);
                }
            } catch (Exception e) {
            	logger.log(Level.SEVERE, "GooglePlusCommons.requestAccessToken() exception: ", e);
            }
        } else {
        	logger.log(Level.SEVERE, "GooglePlusCommons.requestAccessToken() exception: refreshToken is empty!");
        }
    	return result;
    }
}
