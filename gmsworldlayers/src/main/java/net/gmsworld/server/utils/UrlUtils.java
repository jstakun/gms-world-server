package net.gmsworld.server.utils;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.shorten;

import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.persistence.Landmark;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.rosaloves.bitlyj.Bitly.Provider;
import com.rosaloves.bitlyj.Url;

/**
 *
 * @author jstakun
 */
public class UrlUtils {

    private static final Logger logger = Logger.getLogger(UrlUtils.class.getName());
    private static final Provider bitly = as(Commons.getProperty(Property.BITLY_USERNAME), Commons.getProperty(Property.BITLY_APIKEY));
    public final static String BITLY_URL = "http://bit.ly/";
    private static final long DB_MIGRATION_DATE = 1373846399000L; //14-07-13 23:59:59
    
    //http://www.facebook.com/profile.php?id=uid
    //http://twitter.com/uid
    //http://www.linkedin.com/profile/view?id=uid
    //http://www.blogger.com/feeds/uid/posts/default
    //http://profiles.google.com/uid
    //https://plus.google.com/uid
    //http://foursquare.com/user/uid
    //http://gowalla.com/users/uid
    //https://twitter.com/account/redirect_by_id?id=%THE_ID%
    public static String createUserProfileUrl(String user) {
        String url = "#";

        try {
            if (StringUtils.isNotEmpty(user) && user.length() > 3 && user.charAt(user.length() - 3) == '@') {
                String id = user.substring(0, user.length() - 3);
                if (user.endsWith("@fb")) {
                    url = "http://www.facebook.com/profile.php?id=" + id;
                } else if (user.endsWith("@tw")) {
                    try {
                        url = "https://twitter.com/account/redirect_by_id?id=" + Long.parseLong(id);
                    } catch (NumberFormatException e) {
                        url = "http://twitter.com/" + id;
                    }
                } else if (user.endsWith("@ln")) {
                    if (id.contains(".")) {
                        url = "http://www.linkedin.com/profile/view?id=" + id;
                    } else {
                        url = "http://www.linkedin.com/x/profile/" + Commons.getProperty(Property.LN_API_KEY) + "/" + id;
                    }
                } else if (user.endsWith("@gl")) {
                	url = "https://plus.google.com/" + id;
                	//url = "http://www.blogger.com/feeds/" + id + "/posts/default";
                } else if (user.endsWith("@gg")) {
                    url = "https://plus.google.com/" + id;
                    //url = "http://profiles.google.com/" + id;
                } else if (user.endsWith("@fs")) {
                    url = "http://foursquare.com/user/" + id;
                } else if (user.endsWith("@gw")) {
                    url = "http://gowalla.com/users/" + id;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return url;
    }

    public static String createUsernameMask(String user) {
        String username = "GMS World User";

        if (StringUtils.isNotEmpty(user)) {
            try {
                //String id = user.substring(0, user.length() - 3);
                if (user.endsWith("@fb")) {
                    username = "Facebook User";
                } else if (user.endsWith("@tw")) {
                    username = "Twitter User";
                } else if (user.endsWith("@ln")) {
                    username = "LinkedIn User";
                } else if (user.endsWith("@gl")) {
                    username = "Google User";
                } else if (user.endsWith("@gg")) {
                    username = "Google+ User";
                } else if (user.endsWith("@fs")) {
                    username = "Foursquare User";
                } else if (user.endsWith("@gw")) {
                    username = "Gowalla User";
                }
            } catch (Exception ex) {
                Logger.getLogger(UrlUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return username;
    }

    public static String forXML(String aText) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '&') {
                result.append("&amp;");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }

    public static String getShortUrl(String longUrl) {
    	String respUrl = longUrl;
        if (!StringUtils.startsWith(longUrl, BITLY_URL)) {
            try {
                Url shortUrl = bitly.call(shorten(longUrl));
                respUrl = shortUrl.getShortUrl();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Bitly API exception: ", e);
                respUrl = getGoogleShortUrl(longUrl);
            }
        }
        return respUrl;
    }

    public static String getHash(String url) {
        try {
            Url shortUrl = bitly.call(shorten(url));
            return shortUrl.getUserHash();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Bitly API exception: ", ex);
            return null;
        }
    }
    
    public static String getLandmarkUrl(Landmark landmark) {
        String hash = landmark.getHash();
        Date creationDate = landmark.getCreationDate();
        //hash not empty and not /showLandmark/null
        if ((creationDate == null || creationDate.getTime() > DB_MIGRATION_DATE) && StringUtils.isNotEmpty(hash) && !StringUtils.equals(hash, "12wsNzG")) { 
            return BITLY_URL + hash;
        } else {
        	return ConfigurationManager.SERVER_URL + "showLandmark/" + landmark.getId();
        }
    }
  
    public static String getGoogleShortUrl(String longUrl) {
    	if (!StringUtils.startsWith(longUrl, BITLY_URL)) {
    		try {
    			URL url = new URL("https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + Commons.getProperty(Property.FCM_LM_WEB_API_KEY));
        		JSONObject request = new JSONObject();
        		request.put("longDynamicLink", "https://nm3n7.app.goo.gl/?link=" + longUrl + "&apn=com.jstakun.gms.android.ui");
        		request.put("suffix", new JSONObject().put("option", "SHORT")); //UNGUESSABLE"));
        		String reply = HttpUtils.processFileRequest(url, "POST", "application/json", request.toString(), "application/json");
        		if (StringUtils.startsWith(reply, "{")) {
        			 JSONObject replyJson = new JSONObject(reply);
        			 if (replyJson.has("shortLink")) {
        				 return replyJson.getString("shortLink");
        			 } else {
        				 logger.log(Level.WARNING, "Received following reply: " + reply);
        				 return longUrl;
        			 }
        		} else {
        			 logger.log(Level.WARNING, "Received following reply: " + reply);
        			 return longUrl;
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
            	return longUrl;
        	}	
    	} else {
    		return longUrl;
    	}
    }
    
    public static String getLandmarkUrl(String hash, int id, Date creationDate) {
    	//hash not empty and not /showLandmark/null
    	if ((creationDate == null || creationDate.getTime() > DB_MIGRATION_DATE) && StringUtils.isNotEmpty(hash) && !StringUtils.equals(hash, "12wsNzG")) {
    		return BITLY_URL + hash;
    	} else {
    		return ConfigurationManager.SERVER_URL + "showLandmark/" + id;
    	}
    }

}
