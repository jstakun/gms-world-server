/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.util.ArrayList;
import java.util.List;
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
public class ShowLayerAction extends org.apache.struts.action.Action {

    private static final int INTERVAL = 10;
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

        int first = NumberUtils.getInt(request.getParameter("first"), 0);
        int next = -1;
        int prev = -1;

        String layer = null;
        if (request.getParameter("layer") != null) {
            layer = request.getParameter("layer");
        }

        List<Landmark> layerLandmarks = new ArrayList<Landmark>();

        if (layer != null) {
            int count = LandmarkPersistenceUtils.selectLandmarksByUserAndLayerCount(null, layer);

            request.setAttribute("layer", layer);

            if (count > 0) {
                layerLandmarks = LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(null, layer, first, first + INTERVAL);
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
            if (!layerLandmarks.isEmpty()) {
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

            if (os.isMobileDevice()) {
                return mapping.findForward("mobile");
            } else {
                return mapping.findForward("success");
            }
        }
    }
}
