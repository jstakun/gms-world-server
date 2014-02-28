/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.social;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.ItemScope;
import com.google.api.services.plus.model.Moment;
import com.google.api.services.plus.model.Person;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class GooglePlusUtils {

    private static final Logger logger = Logger.getLogger(GooglePlusUtils.class.getName());
    private static final Random random = new Random();

    public static Map<String,String> getUserData(String accessToken, String refreshToken) {
        Map<String, String> userData = new HashMap<String, String>();

        try {
            Person person = getPlus(accessToken, refreshToken).people().get("me").execute();

            userData.put(ConfigurationManager.GL_USERNAME,person.getId());
            userData.put(ConfigurationManager.GL_NAME, person.getDisplayName());
            userData.put(ConfigurationManager.GL_GENDER, person.getGender());
            userData.put(ConfigurationManager.GL_BIRTHDAY, person.getBirthday());
            //System.out.println(person.getId() + " " + person.getNickname() + " "
            //        + person.getGender() + " " + person.getDisplayName() + " "
            //        + person.getName() + " " + person.getBirthday());
            String email = getUserEmail(accessToken, refreshToken);
            if (email != null) {
            	userData.put(ConfigurationManager.USER_EMAIL, email);
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "GooglePlusUtils.getUserId() exception: ", ex);
        }

        return userData;
    }
    
    private static String getUserEmail(String accessToken, String refreshToken) {
        String email = null;
        try {
            HttpTransport httpTransport = new UrlFetchTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            GoogleCredential requestInitializer = new GoogleCredential.Builder().
                    setClientSecrets(Commons.GL_PLUS_KEY, Commons.GL_PLUS_SECRET).
                    setJsonFactory(jsonFactory).
                    setTransport(httpTransport).build();

            requestInitializer.setAccessToken(accessToken).setRefreshToken(refreshToken);

            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v1/userinfo?alt=json");
            HttpRequest request = httpTransport.createRequestFactory(requestInitializer).buildGetRequest(url);

            String response = request.execute().parseAsString();
            //System.out.println(response);
            //logger.log(Level.INFO, response);
            JSONObject json = new JSONObject(response);
            if (json.has("email")) {
                email = json.getString("email");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "GoogglePlusUtils.getUserEmail exception", e);
        }

        return email;
    }

    public static void sendMessage(String accessToken, String refreshToken, String key, String url, int type) {
        if (accessToken != null || refreshToken != null) {
            
        	Landmark landmark = null; 
        	if (key != null) {
        		landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
        	}
        	
        	final String[] images = {"blogeo_j.png", "blogeo_a.png", "poi_j.png", "poi_a.png"};
            int imageId = 2;
            try {
                imageId = random.nextInt(4);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
            //logger.log(Level.INFO, "Image id: {0}", imageId);
            if (imageId > 3 || imageId < 0) {
                imageId = 2;
            }

            String message = null;
            ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            if (type == Commons.SERVER) {
                String username = landmark.getUsername();
                String userMask = UrlUtils.createUsernameMask(username);
                message = String.format(rb.getString("Social.gl.server"), userMask, landmark.getName(), url);
            } else if (type == Commons.BLOGEO) {
                message = String.format(rb.getString("Social.gl.message.blogeo"), url);
            } else if (type == Commons.LANDMARK) {
                message = String.format(rb.getString("Social.gl.message.landmark"), landmark.getName(), url);
            } else if (type == Commons.MY_POS) {
            	message = String.format(rb.getString("Social.gl.message.mypos"), url);
            } else if (type == Commons.LOGIN) {
            	message = rb.getString("Social.login");
            }           

            if (message != null) {
            	double lat = -1, lng = -1;
                if (landmark != null) {
                	lat = landmark.getLatitude();
                	lng = landmark.getLongitude();
                }
                
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

    public static void sendImageMessage(String showImageUrl, String username, String imageUrl) {
        String userMask = UrlUtils.createUsernameMask(username);
        String message = userMask + " has just posted new screenshot to GMS World. Check it out: " + showImageUrl;
        Plus plus = getPlus(null, null);
        sendMoment(plus, message, "Message from GMS World", imageUrl + "=s128", -1, -1);
        sendUrlMoment(plus, showImageUrl);
    }

    private static Plus getPlus(String accessToken, String refreshToken) {
        HttpTransport httpTransport = new UrlFetchTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        GoogleCredential requestInitializer = new GoogleCredential.Builder().setClientSecrets(Commons.GL_PLUS_KEY, Commons.GL_PLUS_SECRET).
                setJsonFactory(jsonFactory).
                setTransport(httpTransport).build();

        if (accessToken == null && refreshToken == null) {
            requestInitializer.setAccessToken(Commons.gl_plus_token).setRefreshToken(Commons.gl_plus_refresh);
        } else {
            requestInitializer.setAccessToken(accessToken).setRefreshToken(refreshToken);
        }

        Plus plus = new Plus.Builder(httpTransport, jsonFactory, requestInitializer).setApplicationName("Landmark Manager").build();
        return plus;
    }

    private static void sendUrlMoment(Plus plus, String url) {
        if (url != null) {
            Moment moment1 = new Moment();
            moment1.setType("http://schemas.google.com/AddActivity");
            ItemScope itemScope1 = new ItemScope();
            itemScope1.setUrl(url);
            moment1.setTarget(itemScope1);
            try {
                Moment momentResult1 = plus.moments().insert("me", "vault", moment1).execute();
                logger.log(Level.INFO, "Created activity with id: {0}", momentResult1.getId());
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "GooglePlusUtils.sendUrlMoment() exception", ex);
            }
        } else {
            logger.log(Level.SEVERE, "Url is null!");
        }
    }

    private static void sendMoment(Plus plus, String name, String desc, String image, double lat, double lng) {
        try {
            Moment moment = new Moment();
            moment.setType("http://schemas.google.com/AddActivity");
            ItemScope itemScope = new ItemScope();
            itemScope.setId("gms-world-msg-" + System.currentTimeMillis());
            itemScope.setType("http://schemas.google.com/AddActivity");
            itemScope.setName(name);
            itemScope.setDescription(desc);
            itemScope.setImage(image);
            if (lat != -1) {
                itemScope.setLatitude(lat);
            }
            if (lng != -1) {
                itemScope.setLongitude(lng);
            }
            moment.setTarget(itemScope);
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
                        new JacksonFactory(), refreshToken, Commons.GL_PLUS_KEY, Commons.GL_PLUS_SECRET).execute();
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
