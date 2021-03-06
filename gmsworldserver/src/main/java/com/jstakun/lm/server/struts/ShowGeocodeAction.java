package com.jstakun.lm.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.HtmlUtils;
import com.jstakun.lm.server.utils.UserAgentUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.CommonPersistenceUtils;

import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.GeocodeCache;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;

/**
 *
 * @author jstakun
 */
public class ShowGeocodeAction extends org.apache.struts.action.Action {

    private static final Logger logger = Logger.getLogger(ShowGeocodeAction.class.getName());

    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

    	GeocodeCache gc = null;
    	
        if (request.getParameter("key") != null) {
            try {
                String key = (String) request.getParameter("key");
                CacheAction geocodeCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
    				public Object executeAction() {
    					if (UserAgentUtils.isBot(request.getHeader("User-Agent"))) {
    		            	return null;
    		            } else if (CommonPersistenceUtils.isKeyValid(key)) {
    		            	return GeocodeCachePersistenceUtils.selectGeocodeCacheById(key);
    		            } else {
    		            	logger.log(Level.SEVERE, "Wrong key format " + key);
    		            	return null;
    		            }
    				}
                });
                gc = (GeocodeCache) geocodeCacheAction.getObjectFromCache("geocode-" + key, CacheType.NORMAL);
                           
                if (gc != null) {
                	request.setAttribute("geocodeCache", gc);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        boolean isMobile = UserAgentUtils.isMobile(request.getHeader("User-Agent"));
        if (StringUtils.isNotEmpty(request.getParameter("fullScreenGeocodeMap"))) {
            if (gc != null) {    	
            	request.setAttribute("lat", StringUtil.formatCoordE6(gc.getLatitude()));
            	request.setAttribute("lng", StringUtil.formatCoordE6(gc.getLongitude()));
            	request.setAttribute("landmarkDesc", HtmlUtils.buildGeocodeDescV2(gc, request.getAttribute("address"), request.getLocale(), isMobile));
            	request.setAttribute("landmarkName", "'" + gc.getLocation() + "'");
            	if (isMobile) {
            		return mapping.findForward("landmarksMobile");
            	} else {
            		return mapping.findForward("landmarks");
            	}
            } else {
            	return mapping.findForward("fullScreen");
            }
        } else {
            if (isMobile) {
                return mapping.findForward("mobile");
            } else {
                return mapping.findForward("success");
            }
        }
    }
}
