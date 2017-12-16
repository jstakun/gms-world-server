package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author jstakun
 */
public class ShowLayerAction extends org.apache.struts.action.Action {

    private static final int INTERVAL = 10;
    private static final Logger logger = Logger.getLogger(ShowLayerAction.class.getName());
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
            final HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        final int first = NumberUtils.getInt(request.getParameter("first"), 0);
        int next = -1;
        int prev = -1;

        final String layer = request.getParameter("layer");
        List<Landmark> layerLandmarks = null;

        if (layer != null) {
            //int count = LandmarkPersistenceUtils.selectLandmarksByUserAndLayerCount(null, layer);
        	CacheAction countCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
				public Object executeAction() {
					Browser browser = Browser.parseUserAgentString(request.getHeader("User-Agent"));
		            if (browser.getGroup() == Browser.BOT || browser.getGroup() == Browser.BOT_MOBILE || browser.getGroup() == Browser.UNKNOWN) {
		            	logger.log(Level.WARNING, "User agent: " + browser.getName() + ", " + request.getHeader("User-Agent"));
		            	return 0;
		            } else {
		            	return LandmarkPersistenceUtils.countLandmarksByUserAndLayer(null, layer);
		            }
				}
			});
        	Integer count = countCacheAction.getIntFromCache(layer + "_count_key", CacheType.NORMAL);

            request.setAttribute("layer", layer);

            if (count > 0) {
            	final int nextCandidate = first + INTERVAL;
                //layerLandmarks = LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(null, layer, first, nextCandidate);
            	CacheAction userLandmarksCacheAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
    				public Object executeAction() {
    					return LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(null, layer, first, nextCandidate);
    				}
    			});
            	layerLandmarks = userLandmarksCacheAction.getListFromCache(Landmark.class, layer + "_" + first + "_" + nextCandidate, CacheType.NORMAL);
                
                request.setAttribute("layerLandmarks", layerLandmarks);
                request.setAttribute("collectionAttributeName", "layerLandmarks");

                if (count - first - INTERVAL > 0) {
                    next = first + INTERVAL;
                }
                if (first - INTERVAL >= 0) {
                    prev = first - INTERVAL;
                }

                request.setAttribute("next", new Integer(next));
                request.setAttribute("prev", new Integer(prev));
            }
        }
        
        if (StringUtils.isNotEmpty(request.getParameter("fullScreenCollectionMap"))) {
            Double centerLat = 0.0;
            Double centerLon = 0.0;
            if (layerLandmarks != null && !layerLandmarks.isEmpty()) {
                for (Landmark landmark : layerLandmarks) {
                    centerLat += landmark.getLatitude();
                    centerLon += landmark.getLongitude();
                }
                centerLat /= layerLandmarks.size();
                centerLon /= layerLandmarks.size();
            }
            request.setAttribute("centerLat", centerLat);
            request.setAttribute("centerLon", centerLon);

            return mapping.findForward("fullScreen");
        } else {
            OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));

            if (os.getDeviceType().equals(DeviceType.MOBILE)) {
                return mapping.findForward("mobile");
            } else {
                return mapping.findForward("success");
            }
        }
    }
}
