package com.jstakun.lm.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.UserAgentUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.memcache.GoogleCacheProvider;
import com.jstakun.lm.server.utils.persistence.CommonPersistenceUtils;

import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

public class BookingAction extends Action {
	
	private static final Logger logger = Logger.getLogger(BookingAction.class.getName());
    
	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String key = (String) request.getParameter("key");
        Landmark landmark = null;
        
        if (StringUtils.isNotEmpty(key)) {
            try {
                  logger.log(Level.INFO, "Searching for key: " + key);
            	    
            	  CacheAction landmarkCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
        				public Object executeAction() {
        					if (UserAgentUtils.isBot(request.getHeader("User-Agent"))) {
        		            	return null;
        		            } else if (CommonPersistenceUtils.isKeyValid(key)) {
        		            	return LandmarkPersistenceUtils.selectLandmarkById(key, GoogleCacheProvider.getInstance());
        		            } else {
        		            	logger.log(Level.SEVERE, "Wrong key format " + key);
        		            	return null;
        		            }
        				}
        		 });
            	 landmark = (Landmark) landmarkCacheAction.getObjectFromCache(key, CacheType.NORMAL);
            } catch (Exception e) {
            	logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        if (landmark != null) {
        	request.setAttribute("lat", StringUtil.formatCoordE6(landmark.getLatitude()));
        	request.setAttribute("lng", StringUtil.formatCoordE6(landmark.getLongitude()));
        	request.setAttribute("address", landmark.getDescription());	
        }
		
		if (UserAgentUtils.isMobile(request.getHeader("User-Agent"))) {
			return mapping.findForward("mobile");
    	} else {
    		return mapping.findForward("success");
    	}
	}	

}
