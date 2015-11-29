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

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

import eu.bitwalker.useragentutils.OperatingSystem;

public class WelcomeAction extends org.apache.struts.action.Action {
	
	private static final Logger logger = Logger.getLogger(WelcomeAction.class.getName());

	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		CacheAction newestLandmarksAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
			@Override
			public Object executeAction() {
				return LandmarkPersistenceUtils.selectNewestLandmarks();
			}
		});
				
		List<Landmark> landmarkList = (List<Landmark>)newestLandmarksAction.getObjectFromCache("newestLandmarks", CacheType.FAST);
        request.setAttribute("newestLandmarkList", landmarkList);   
        
        //handle different request urls
        //http://m.
        //http://www.
		//http://hotels.
        //http://landmarks.
        //http://hotelsonmap.net
        
        logger.log(Level.INFO, "Received request to " + request.getRequestURL() + " from locale " + request.getLocale().toString());
        
        String url = request.getRequestURL().toString();
        if (StringUtils.startsWith(url, "http://hotels.") || StringUtils.contains(url, "hotelsonmap.net")) {
        	return mapping.findForward("hotels");
        } else if (StringUtils.startsWith(url, "http://landmarks.")) {
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
