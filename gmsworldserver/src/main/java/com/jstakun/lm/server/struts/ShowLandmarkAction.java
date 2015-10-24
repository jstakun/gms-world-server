package com.jstakun.lm.server.struts;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.persistence.Checkin;
import com.jstakun.lm.server.persistence.Comment;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.HtmlUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.CheckinPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.CommentPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.CommonPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;

/**
 *
 * @author jstakun
 */
public class ShowLandmarkAction extends Action {

    private static final Logger logger = Logger.getLogger(ShowLandmarkAction.class.getName());
    
    public ShowLandmarkAction() {
    	super();
    	GeocodeHelperFactory.setCacheProvider(new GoogleCacheProvider());
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            final HttpServletRequest request,
            HttpServletResponse response) throws IOException,
            ServletException {

        final String key = (String) request.getParameter("key");
        Landmark landmark = null;
        if (StringUtils.isNotEmpty(key)) {
            try {
                //if (CommonPersistenceUtils.isKeyValid(key)) {
            	    request.setAttribute("key", key);
            	    logger.log(Level.INFO, "Searching for key: " + key);
            	    
            	    CacheAction landmarkCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
        				@Override
        				public Object executeAction() {
        					Browser browser = Browser.parseUserAgentString(request.getHeader("User-Agent"));
        		            if (browser.getGroup() == Browser.BOT || browser.getGroup() == Browser.BOT_MOBILE || browser.getGroup() == Browser.UNKNOWN) {
        		            	logger.log(Level.WARNING, "User agent: " + browser.getName() + ", " + request.getHeader("User-Agent"));
        		            	return null;
        		            } else if (CommonPersistenceUtils.isKeyValid(key)) {
        		            	return LandmarkPersistenceUtils.selectLandmarkById(key);
        		            } else {
        		            	logger.log(Level.SEVERE, "Wrong key format " + key);
        		            	return null;
        		            }
        				}
        			});
            	    landmark = (Landmark) landmarkCacheAction.getObjectFromCache(key, CacheType.NORMAL);
                    
            	    if (landmark != null) {
                        request.setAttribute("landmark", landmark);
                        //String address = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(landmark.getLatitude(),landmark.getLongitude()).getField(AddressInfo.EXTENSION);
                        //if (StringUtils.isNotEmpty(address)) {
                        //   request.setAttribute("address", address);
                        //}

                        //List<Comment> comments = CommentPersistenceUtils.selectCommentsByLandmark(key);
                        CacheAction commentsCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
            				@Override
            				public Object executeAction() {
            					return CommentPersistenceUtils.selectCommentsByLandmark(key);
            				}
            			});
                        List<Comment> comments = (List<Comment>)commentsCacheAction.getObjectFromCache("comments_" + key, CacheType.NORMAL);
                        if (comments != null && !comments.isEmpty()) {
                            request.setAttribute("comments", comments);
                        }
                        
                        if (!landmark.isSocial()) {
                            //List<Checkin> checkins = CheckinPersistenceUtils.selectAllLandmarkCheckins(key);
                        	CacheAction checkinCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
                				@Override
                				public Object executeAction() {
                					return CheckinPersistenceUtils.selectCheckinsByLandmark(key);
                				}
                			});
                        	List<Checkin> checkins = (List<Checkin>)checkinCacheAction.getObjectFromCache("checkins_" + key, CacheType.NORMAL);
                        	
                        	if (checkins != null && !checkins.isEmpty()) {
                                Checkin lastCheckin = checkins.get(0);
                                String username = lastCheckin.getUsername();
                                //List<OAuthToken> tokens = OAuthTokenPersistenceUtils.selectOAuthTokenByUser(lastCheckin.getUsername());
                                //if (tokens != null && !tokens.isEmpty()) {
                                //    OAuthToken userToken = tokens.get(0);
                                //    username = userToken.getUserId() + "@" + userToken.getService();
                                //}
                                request.setAttribute("lastCheckinUsername", username);
                                request.setAttribute("lastCheckinDate", lastCheckin.getCreationDate());
                                if (checkins.size() == 100) {
                                	request.setAttribute("checkinsCount", "100+");
                                } else {
                                	request.setAttribute("checkinsCount", checkins.size());	
                                }                               
                            }
                        }
                    }
                //}
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        if (landmark != null && System.currentTimeMillis() - landmark.getCreationDate().getTime() < CacheUtil.LONG_CACHE_LIMIT) {
        	OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
        	request.setAttribute("lat", StringUtil.formatCoordE6(landmark.getLatitude()));
        	request.setAttribute("lng", StringUtil.formatCoordE6(landmark.getLongitude()));
        	request.setAttribute("landmarkDesc", HtmlUtils.buildLandmarkDesc(landmark, request.getAttribute("address"), request.getLocale()));
        	request.setAttribute("landmarkName", "'" + landmark.getName() + "'");
        	if (os.isMobileDevice()) {
                return mapping.findForward("landmarksMobile");
            } else {
            	return mapping.findForward("landmarks");
            } 
        } else if (landmark != null && StringUtils.isNotEmpty(request.getParameter("fullScreenLandmarkMap"))) {
        	request.setAttribute("lat", StringUtil.formatCoordE6(landmark.getLatitude()));
        	request.setAttribute("lng", StringUtil.formatCoordE6(landmark.getLongitude()));
        	request.setAttribute("landmarkDesc", HtmlUtils.buildLandmarkDesc(landmark, request.getAttribute("address"), request.getLocale()));
        	request.setAttribute("landmarkName", "'" + landmark.getName() + "'");
        	//return mapping.findForward("fullScreen");
            return mapping.findForward("landmarks");
        } else {
        	OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));
            if (os.isMobileDevice()) {
                return mapping.findForward("mobile");
            } else {
            	return mapping.findForward("success");
            }
        }
    }
}
