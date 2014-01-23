/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.bitwalker.useragentutils.OperatingSystem;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author jstakun
 */
public class ShowUserAction extends org.apache.struts.action.Action {

    private static final int INTERVAL = 10;
    private static final Logger logger = Logger.getLogger(ShowUserAction.class.getName());
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

    	final int first = NumberUtils.getInt(request.getParameter("first"), 0);
        int next = -1;
        int prev = -1;

        String userStr = request.getParameter("user");

        int sid = userStr.indexOf(";jsessionid=");
        if (sid != -1) {
            userStr = userStr.substring(0, sid);
        }
        final String user = userStr;

        List<Landmark> userLandmarks = null;

        if (StringUtils.isNotEmpty(user)) {
        	CacheAction countCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
				@Override
				public Object executeAction() {
					return LandmarkPersistenceUtils.selectLandmarksByUserAndLayerCount(user, null);
				}
			});
        	Integer count = (Integer)countCacheAction.getObjectFromCache(user + "_count_key");
        	
        	request.setAttribute("user", user);

            if (count > 0) {

                final int nextCandidate = first + INTERVAL;

                //System.out.println("User: " + user + ", count: " + count + " , next: " + nextCandidate);

                CacheAction userLandmarksCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
    				@Override
    				public Object executeAction() {
    					return LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(user, null, first, nextCandidate);
    				}
    			});
                userLandmarks = (List<Landmark>)userLandmarksCacheAction.getObjectFromCache(user + "_" + first + "_" + nextCandidate);
                
                request.setAttribute("userLandmarks", userLandmarks);

                if (count > nextCandidate) {
                    next = nextCandidate;
                }
                if (first >= INTERVAL) {
                    prev = first - INTERVAL;
                }

                request.setAttribute("next", new Integer(next));
                request.setAttribute("prev", new Integer(prev));
            }
        }

        if (StringUtils.isNotEmpty(request.getParameter("fullScreenCollectionMap"))) {
            Double centerLat = 0.0;
            Double centerLon = 0.0;
            if (userLandmarks != null && !userLandmarks.isEmpty()) {
                for (Landmark landmark : userLandmarks) {
                    centerLat += landmark.getLatitude();
                    centerLon += landmark.getLongitude();
                }
                centerLat /= userLandmarks.size();
                centerLon /= userLandmarks.size();
            }
            request.setAttribute("centerLat", centerLat);
            request.setAttribute("centerLon", centerLon);

            return mapping.findForward("fullScreen");
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
