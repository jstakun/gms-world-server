/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

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
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class GoogleBloggerUtils {

    private static final Logger logger = Logger.getLogger(GoogleBloggerUtils.class.getName());
    
    public static void sendMessage(String key, String landmarkUrl, String token, String secret, boolean isServer) {
        Landmark landmark = LandmarkPersistenceUtils.selectLandmark(key);
        if (landmark != null && token != null && secret != null) {
            String message = null;
            String url = landmarkUrl;
            if (url == null) {
                url = UrlUtils.getShortUrl(UrlUtils.getLandmarkUrl(landmark));
            }

            if (isServer) {
                String username = landmark.getUsername();
                String userMask = UrlUtils.createUsernameMask(username);
                if (username != null) {
                    userMask = "<a href=\"" + ConfigurationManager.SERVER_URL + "showUser/" + username + "\">" + userMask + "</a>";
                }
                message = userMask + " has just posted new point of interest " + landmark.getName() + " to GMS World. <a href=\"" + url + "\">Check it out</a>.";
            } else {
                if (landmark.getLayer().equals("Social")) {
                    message = "I've just posted new geo message to Blogeo. <a href=\"" + url + "\">Check it out</a>.";
                } else {
                    message = "I've just posted point of interest " + landmark.getName() + " to GMS World. Please check <a href=\"" + url + "\">here</a>.";
                }
            }
            if (message != null) {
                //createPost(getBloggerService(), landmark.getName(), message, false);

                createPost(getBlogger(), landmark.getName(), message);
            }
        } else {
            logger.log(Level.SEVERE, "Landmark or token is null! Key: {0}, token: {1}, secret: {2}", new Object[]{key, token, secret});
        }
    }

    public static void sendImageMessage(String showImageUrl, String username, String imageUrl) {
        String userMask = UrlUtils.createUsernameMask(username);
        if (StringUtils.isNotEmpty(username)) {
            userMask = "<a href=\"" + ConfigurationManager.SERVER_URL + "showUser/" + username + "\">" + userMask + "</a>";
        }
        String prefix = "<a href=\"" + showImageUrl + "\" "
                + "imageanchor=\"1\" style=\"clear: left; cssfloat: left; float: left; margin-bottom: 1em; margin-right: 1em;\">"
                + "<img border=\"0\" src=\"" + imageUrl + "=s128\" ya=\"true\" /></a>";
        String message = prefix + userMask + " has just posted new screenshot to GMS World. <a href=\"" + showImageUrl + "\">Check it out</a>.";

        //createPost(getBloggerService(), "GMS World screenshot", message, false);

        createPost(getBlogger(), "GMS World screenshot", message);
    }

    private static void createPost(Blogger blogger, String title, String content) {
        try {
            BlogList blogList = blogger.blogs().listByUser("self").execute();
            List<Blog> blogs = blogList.getItems();
            Blog blog = null;
            if (!blogs.isEmpty()) {
                blog = blogs.get(0);
                logger.log(Level.INFO, "Primary blogId: {0}", blog.getId());
            }

            if (blog != null) {
                if (!CacheUtil.containsKey("BloggerUsageLimits")) {
                	Post post = new Post();
                	post.setTitle(title);
                	post.setContent(content);
                	Post postResp = blogger.posts().insert(blog.getId(), post).execute();
                	logger.log(Level.INFO, "Successfully created post: {0}", postResp.getId());
                } else {
                	logger.log(Level.WARNING, "Blogger Rate Limit Exceeded");
                }
            } else {
                logger.log(Level.INFO, "No blogs found for the user!");
            }
        } catch (GoogleJsonResponseException ex) {
        	CacheUtil.put("BloggerUsageLimits", "rateLimitExceeded");
        	logger.log(Level.SEVERE, "GoogleBloggerUtils.createPost() exception with status " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "GoogleBloggerUtils.createPost() exception", ex);
        }
    }

    private static Blogger getBlogger() {
        HttpTransport httpTransport = new UrlFetchTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential requestInitializer = new GoogleCredential.Builder().setClientSecrets(Commons.GL_PLUS_KEY, Commons.GL_PLUS_SECRET).setJsonFactory(jsonFactory).setTransport(httpTransport).build();
        requestInitializer.setAccessToken(Commons.gl_plus_token).setRefreshToken(Commons.gl_plus_refresh);
        Blogger blogger = new Blogger.Builder(httpTransport, jsonFactory, requestInitializer).setApplicationName("Landmark Manager").build();

        return blogger;
    }

    /*private static void createPost(BloggerService myService, String title, String content, Boolean isDraft) {
        try {
            // Create the entry to insert
            Entry myEntry = new Entry();
            myEntry.setTitle(new PlainTextConstruct(title));
            myEntry.setContent(new PlainTextConstruct(content));
            //Person author = new Person(authorName, null, userName);
            //myEntry.getAuthors().add(author);
            myEntry.setDraft(isDraft);

            // Ask the service to insert the new entry
            String url = GlCommons.BLOGGER_SCOPE + getBlogId(myService) + GlCommons.POSTS_FEED_URI_SUFFIX;
            URL postUrl = new URL(url);
            Entry post = myService.insert(postUrl, myEntry);
            logger.log(Level.INFO, "Successfully created draft post: {0}", post.getTitle().getPlainText());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "GoogleBloggerUtils.createPost() exception", ex);
        }
    }*/

    /*private static String getBlogId(BloggerService myService)
            throws ServiceException, IOException {
        // Get the metafeed
        final URL feedUrl = new URL(GlCommons.METAFEED_URL);
        Feed resultFeed = myService.getFeed(feedUrl, Feed.class);

        // If the user has a blog then return the id (which comes after 'blog-')
        if (resultFeed.getEntries().size() > 0) {
            Entry entry = resultFeed.getEntries().get(0);
            return entry.getId().split("blog-")[1];
        }
        throw new IOException("User has no blogs!");
    }*/

    /*private static BloggerService getBloggerService() {
        BloggerService myService = new BloggerService("GMS World");

        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
        oauthParameters.setOAuthConsumerKey(GlCommons.BLOGGER_KEY);
        oauthParameters.setOAuthConsumerSecret(GlCommons.BLOGGER_SECRET);
        oauthParameters.setOAuthToken(GlCommons.blogger_token);
        oauthParameters.setOAuthTokenSecret(GlCommons.blogger_secret);
        try {
            myService.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
        } catch (OAuthException ex) {
            logger.log(Level.SEVERE, "GoogleBloggerUtils.getBloggerService() exception: ", ex);
        }
        return myService;
    }*/

}
