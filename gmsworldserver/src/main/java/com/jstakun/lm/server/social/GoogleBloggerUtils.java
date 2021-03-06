package com.jstakun.lm.server.social;

import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.lang.StringUtils;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.blogger.Blogger;
import com.google.api.services.blogger.model.Blog;
import com.google.api.services.blogger.model.BlogList;
import com.google.api.services.blogger.model.Post;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

/**
 *
 * @author jstakun
 */
public class GoogleBloggerUtils {

    private static final Logger logger = Logger.getLogger(GoogleBloggerUtils.class.getName());
    protected static final String USAGE_LIMIT_MARKER = "BloggerUsageLimitsMarker";
    
    protected static String sendMessage(String url, String token, String secret, String username, String name, String imageUrl, String layer, Double lat, Double lng, String desc, int type) {
        if (type == Commons.SERVER) {
        	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            if (token != null && secret != null && url != null) {
        		String message = null;
        		
        		String userMask = UrlUtils.createUsernameMask(username);
        		String profileUrl = UrlUtils.createUserProfileUrl(username);
        		if (username != null) {
        			if (StringUtils.equals(profileUrl, "#")) {
            			profileUrl = ConfigurationManager.SERVER_URL + "showUser/" + username;
            		}
        			userMask = "<a href=\"" + profileUrl + "\">" + userMask + "</a>";
        		}
        		
                String prefix = "";
        		if (imageUrl != null) {
        			prefix = "<a href=\"" + imageUrl + "\" "
                        + "imageanchor=\"1\" style=\"clear: left; cssfloat: left; float: left; margin-bottom: 1em; margin-right: 1em;\">"
                        + "<img border=\"0\" src=\"" + imageUrl + "\" ya=\"true\" /></a>"; 
        		}
        		if (StringUtils.equals(layer, Commons.SOCIAL)) { 
                    message = String.format(rb.getString("Social.gl.server.blogeo"), prefix + userMask, url);
        		} else if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
        			message = String.format(rb.getString("Social.gl.hotels"), prefix + name, url);
        		} else {
                    message = String.format(rb.getString("Social.gl.server.landmark"), prefix + userMask, name, url);
        		}  
        		
        		if (message != null) {
        			String lname = name;
        			if (StringUtils.equals(name, Commons.MY_POSITION_LAYER) && StringUtils.isNotEmpty(desc)) {
            			lname = desc;
            		} else if (StringUtils.equals(name, Commons.MY_POSITION_LAYER) && StringUtils.isEmpty(desc)) {
            			lname = "See location on the map";
            		} else {
            			lname = "See " + lname + " on the map"; 
            		}
                    return createPost(getBlogger(), name, message, lat, lng, lname);
                } else {
                	return null;
                }
        	} else {
        		logger.log(Level.SEVERE, "Something is empty! token: {0}, secret: {1}, url: {2}", new Object[]{token, secret, url});
        		return null;
        	}
        } else if (type == Commons.CHECKIN) { 
        	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            String message = String.format(rb.getString("Social.gl.message.checkin"), username, url, name);
        	if (message != null) { 
                return createPost(getBlogger(), String.format(rb.getString("Social.gl.title.checkin"), username, name), message, lat, lng, "See " + name + " on the map");
            } else {
            	return null; 
            }
        } else {
        	return null;
        }      
    }

    protected static String sendImageMessage(String showImageUrl, String username, String imageUrl, String flex, Double lat, Double lng, int type) {
    	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
        String userMask = UrlUtils.createUsernameMask(username);
        if (StringUtils.isNotEmpty(username)) {
            userMask = "<a href=\"" + ConfigurationManager.SERVER_URL + "showUser/" + username + "\">" + userMask + "</a>";
        }
        String message = null;
        String title = null;
        if (type == Commons.SCREENSHOT) {
        	String prefix = "<a href=\"" + showImageUrl + "\" "
                + "imageanchor=\"1\" style=\"clear: left; cssfloat: left; float: left; margin-bottom: 1em; margin-right: 1em;\">"
                + "<img border=\"0\" src=\"" + imageUrl + "\" ya=\"true\" /></a>"; 
        	 message = String.format(rb.getString("Social.gl.server.screenshot"), prefix + userMask, showImageUrl);
        	 title = "GMS World screenshot";
        } else if (type == Commons.ROUTE) {
        	String prefix = "<a href=\"" + showImageUrl + "\" "
                    + "imageanchor=\"1\" style=\"clear: left; cssfloat: left; float: left; margin-bottom: 1em; margin-right: 1em;\">"
                    + "<img border=\"0\" src=\"" + imageUrl + "\" ya=\"true\" /></a>"; 
            message = String.format(rb.getString("Social.gl.server.route"), prefix + userMask, flex, showImageUrl);
            title = "GMS World route";
        }        
        
        if (message != null && title != null) {
        	return createPost(getBlogger(), title, message, lat, lng, "See location on the map");
        } else {
        	return null;
        }
    }

    private static String createPost(Blogger blogger, String title, String content, Double lat, Double lng, String lname) {
        try {
            BlogList blogList = blogger.blogs().listByUser("self").execute();
            List<Blog> blogs = blogList.getItems();
            Blog blog = null;
            if (!blogs.isEmpty()) {
                blog = blogs.get(0);
            }

            if (blog != null) {
                if (!CacheUtil.containsKey(USAGE_LIMIT_MARKER)) {
                	Post post = new Post();
                	post.setTitle(title);
                	post.setContent(content);
                	if (lat != null && lng != null) {
                		Post.Location location = new Post.Location();
                		location.setLat(lat);
                		location.setLng(lng);
                		location.setName(lname); 
                		post.setLocation(location);
                	}
                	Post postResp = blogger.posts().insert(blog.getId(), post).setFields("id").execute();
                	logger.log(Level.INFO, "Successfully created post: {0} at blog {1}", new Object[]{postResp.getId(), blog.getId()});
                	return postResp.getId();
                } else {
                	logger.log(Level.WARNING, "Blogger Rate Limit Exceeded");
                	return USAGE_LIMIT_MARKER;
                }
            } else {
                logger.log(Level.INFO, "No blogs found for the user!");
                return null;
            }
        } catch (GoogleJsonResponseException ex) {
        	int status = ex.getStatusCode();
        	if (status == 403) {
        		CacheUtil.put(USAGE_LIMIT_MARKER, "1", CacheType.NORMAL);
        		logger.log(Level.WARNING, "GoogleBloggerUtils.createPost() exception " + status);
            } else if (status == 500) {
            	logger.log(Level.WARNING, "GoogleBloggerUtils.createPost() exception " + status);
        	} else {
        		logger.log(Level.SEVERE, "GoogleBloggerUtils.createPost() exception " + status, ex);
        	}
        	return null;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "GoogleBloggerUtils.createPost() exception", ex);
            return null;
        }
    }

    private static Blogger getBlogger() {
        HttpTransport httpTransport = new UrlFetchTransport();
    	JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential requestInitializer = new GoogleCredential.Builder().setClientSecrets(Commons.getProperty(Property.GL_PLUS_KEY), Commons.getProperty(Property.GL_PLUS_SECRET)).setJsonFactory(jsonFactory).setTransport(httpTransport).build();
        requestInitializer.setAccessToken(Commons.getProperty(Property.gl_plus_token)).setRefreshToken(Commons.getProperty(Property.gl_plus_refresh));
        Blogger blogger = new Blogger.Builder(httpTransport, jsonFactory, requestInitializer).setApplicationName("GMS World").build();
        return blogger;
    }
}
