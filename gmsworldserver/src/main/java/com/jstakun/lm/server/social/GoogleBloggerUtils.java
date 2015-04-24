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
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class GoogleBloggerUtils {

    private static final Logger logger = Logger.getLogger(GoogleBloggerUtils.class.getName());
    private static final String CACHE_KEY = "BloggerUsageLimitsMarker";
    
    protected static void sendMessage(String key, String url, String token, String secret, String user, String name, int type) {
        if (key != null && type == Commons.SERVER) {
        	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            Landmark landmark = LandmarkPersistenceUtils.selectLandmarkById(key);
        	if (landmark != null && token != null && secret != null) {
        		String message = null;
        		if (url == null) {
        			url = UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark.getHash(), landmark.getId(), landmark.getCreationDate()));
        		}

        		String username = landmark.getUsername();
        		String userMask = UrlUtils.createUsernameMask(username);
        		if (username != null) {
        			userMask = "<a href=\"" + ConfigurationManager.SERVER_URL + "showUser/" + username + "\">" + userMask + "</a>";
        		}
    
        		if (landmark.isSocial()) { 
                    message = String.format(rb.getString("Social.gl.server.blogeo"), userMask, url);
        		} else {
                    message = String.format(rb.getString("Social.gl.server.landmark"), userMask, landmark.getName(), url);
        		}  
        		
        		if (message != null) {
                    createPost(getBlogger(), landmark.getName(), message);
                }
        	} else {
        		logger.log(Level.SEVERE, "Landmark or token is empty! Key: {0}, token: {1}, secret: {2}", new Object[]{key, token, secret});
        	}
        } else if (type == Commons.CHECKIN) { 
        	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
            String message = String.format(rb.getString("Social.gl.message.checkin"), user, url, name);
        	if (message != null) { 
                createPost(getBlogger(), String.format(rb.getString("Social.gl.title.checkin"), user, name), message);
            }
        }
        
        
    }

    protected static void sendImageMessage(String showImageUrl, String username, String imageUrl) {
    	ResourceBundle rb = ResourceBundle.getBundle("com.jstakun.lm.server.struts.ApplicationResource");
        String userMask = UrlUtils.createUsernameMask(username);
        if (StringUtils.isNotEmpty(username)) {
            userMask = "<a href=\"" + ConfigurationManager.SERVER_URL + "showUser/" + username + "\">" + userMask + "</a>";
        }
        String prefix = "<a href=\"" + showImageUrl + "\" "
                + "imageanchor=\"1\" style=\"clear: left; cssfloat: left; float: left; margin-bottom: 1em; margin-right: 1em;\">"
                + "<img border=\"0\" src=\"" + imageUrl + "\" ya=\"true\" /></a>"; 
        String message = String.format(rb.getString("Social.gl.server.screenshot"), prefix + userMask, showImageUrl);
        createPost(getBlogger(), "GMS World screenshot", message);
    }

    private static void createPost(Blogger blogger, String title, String content) {
        try {
            BlogList blogList = blogger.blogs().listByUser("self").execute();
            List<Blog> blogs = blogList.getItems();
            Blog blog = null;
            if (!blogs.isEmpty()) {
                blog = blogs.get(0);
            }

            if (blog != null) {
                if (!CacheUtil.containsKey(CACHE_KEY)) {
                	Post post = new Post();
                	post.setTitle(title);
                	post.setContent(content);
                	Post postResp = blogger.posts().insert(blog.getId(), post).execute();
                	logger.log(Level.INFO, "Successfully created post: {0} at blog {1}", new Object[]{postResp.getId(), blog.getId()});
                } else {
                	logger.log(Level.WARNING, "Blogger Rate Limit Exceeded");
                }
            } else {
                logger.log(Level.INFO, "No blogs found for the user!");
            }
        } catch (GoogleJsonResponseException ex) {
        	int status = ex.getStatusCode();
        	if (status == 403) {
        		CacheUtil.put(CACHE_KEY, "1", CacheType.NORMAL);
        	}
        	logger.log(Level.SEVERE, "GoogleBloggerUtils.createPost() exception with error " + status, ex);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "GoogleBloggerUtils.createPost() exception", ex);
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
