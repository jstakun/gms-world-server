/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils;

import com.rosaloves.bitlyj.Url;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import static com.rosaloves.bitlyj.Bitly.*;

/**
 *
 * @author jstakun
 */
public class UrlUtils {

    private static final Logger logger = Logger.getLogger(UrlUtils.class.getName());
    private static final Provider bitly = as(Commons.BITLY_USERNAME, Commons.BITLY_APIKEY);
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
                        url = "http://www.linkedin.com/x/profile/" + Commons.LN_API_KEY + "/" + id;
                    }
                } else if (user.endsWith("@gl")) {
                    url = "http://www.blogger.com/feeds/" + id + "/posts/default";
                } else if (user.endsWith("@gg")) {
                    //url = "http://profiles.google.com/" + id;
                    url = "https://plus.google.com/" + id;
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
                    username = "Google Blogger User";
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

    public static String getLandmarkUrl(Landmark landmark) {
        String hash = landmark.getHash();
        Date creationDate = landmark.getCreationDate();
        //hash not empty and not /showLandmark/null
        if ((creationDate == null || creationDate.getTime() > DB_MIGRATION_DATE) && StringUtils.isNotEmpty(hash) && !StringUtils.equals(hash, "12wsNzG")) { 
            //logger.log(Level.INFO, "Landmark " + landmark.getName() + " created after migration");
            //logger.log(Level.INFO, landmark.getCreationDate().getTime() + " > " + DB_MIGRATION_DATE);
        	return BITLY_URL + hash;
        } else {
        	//logger.log(Level.INFO, "Landmark " + landmark.getName() + " created before migration");
        	return ConfigurationManager.SERVER_URL + "showLandmark/" + landmark.getId();
        }
    }

    public static String getShortUrl(String url) {
        String respUrl = url;
        if (!StringUtils.startsWith(url, BITLY_URL)) {
            try {
                Url shortUrl = bitly.call(shorten(url));
                respUrl = shortUrl.getShortUrl();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Bitly API exception: ", e);
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
}
