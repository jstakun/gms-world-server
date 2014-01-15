/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.persistence.GeocodeCache;
import com.jstakun.lm.server.utils.persistence.GeocodeCachePersistenceUtils;
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

        if (request.getParameter("key") != null) {
            try {
                String key = (String) request.getParameter("key");
                GeocodeCache gc = GeocodeCachePersistenceUtils.selectGeocodeCache(key);
                request.setAttribute("geocodeCache", gc);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        if (StringUtils.isNotEmpty(request.getParameter("fullScreenGeocodeMap"))) {
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
