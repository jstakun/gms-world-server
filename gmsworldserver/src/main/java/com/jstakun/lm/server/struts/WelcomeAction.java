package com.jstakun.lm.server.struts;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.UserAgentUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import net.gmsworld.server.layers.GeocodeHelperFactory;
import net.gmsworld.server.layers.LayerHelperFactory;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

public class WelcomeAction extends org.apache.struts.action.Action {
	
	private static final Logger logger = Logger.getLogger(WelcomeAction.class.getName());

	public WelcomeAction() {
		 super();
		 logger.log(Level.INFO, "Initializing layer providers...");
		 LayerHelperFactory.getInstance();
		 logger.log(Level.INFO, "Initializing geocode providers...");
		 GeocodeHelperFactory.getInstance();
		 logger.log(Level.INFO, "Initializing cache provider...");
	     LayerHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
		 GeocodeHelperFactory.getInstance().setCacheProvider(GoogleCacheProvider.getInstance());
	}
	
	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//handle different request urls
        //http://m.
        //http://www.
		//http://hotels.
        //http://landmarks.
		
		logger.log(Level.INFO, "Received request to " + request.getRequestURL() + " from locale " + request.getLocale().toString());
        
        String url = request.getRequestURL().toString();
        
        if (StringUtils.contains(url, "://hotels.") || StringUtils.contains(url, "://www.hotels")) {
        	return mapping.findForward("hotels");
        } else if (StringUtils.contains(url, "://landmarks.")) {
        	return mapping.findForward("landmarks");
        } else {
        	CacheAction newestLandmarksAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
    			public Object executeAction() {
    				return LandmarkPersistenceUtils.selectNewestLandmarks();
    			}
    		});
    				
    		List<Landmark> landmarkList = newestLandmarksAction.getListFromCache(Landmark.class, "newestLandmarks", CacheType.FAST);
            request.setAttribute("newestLandmarks", landmarkList);   
            
        	if (UserAgentUtils.isMobile(request.getHeader("User-Agent"))) {
        		return mapping.findForward("mobile");
        	} else {
        		return mapping.findForward("success");
        	}
        }
	}
}
