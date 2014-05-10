/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.layers.GeocodeHelperFactory;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.StringUtil;

/**
 *
 * @author jstakun
 */
public class ShowLocationAction extends org.apache.struts.action.Action {

    /* forward name="success" path="" */
    private static final Logger logger = Logger.getLogger(ShowLocationAction.class.getName());

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

        if (!HttpUtils.isEmptyAny(request, "lat", "lon")) {
            try {
                double lat = Double.parseDouble(request.getParameter("lat"));
                double lon = Double.parseDouble(request.getParameter("lon"));
                request.setAttribute("lat", StringUtil.formatCoordE6(lat));
                request.setAttribute("lon", StringUtil.formatCoordE6(lon));
                String address = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(lat, lon);
                if (StringUtils.isNotEmpty(address)) {
                    request.setAttribute("address", address);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        //this action is only for mobile devices
        //OperatingSystem os = OperatingSystem.parseUserAgentString(request.getHeader("User-Agent"));

        //if (os.isMobileDevice()) {
        //    return mapping.findForward("mobile");
        //} else {
        return mapping.findForward("mobile");
        //}
    }
}
