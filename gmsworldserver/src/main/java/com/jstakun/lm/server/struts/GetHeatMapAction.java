/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.utils.DateUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author jstakun
 */
public class GetHeatMapAction extends org.apache.struts.action.Action {

    /* forward name="success" path="" */
    private static final String SUCCESS = "success";

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

        int nDays = 365;

        try {
            String param = request.getParameter("days");
            if (param != null) {
                nDays = Integer.parseInt(param);
            }
        } catch (Exception e) {
        }

        boolean inBackground = StringUtils.endsWithIgnoreCase(request.getParameter("inBackground"), "true");

        String cacheKey = DateUtils.getDay(new Date()) + "_" + nDays + "_heatMap";

        Map<String, Integer> heatMapData = (Map<String, Integer>) CacheUtil.getObject(cacheKey);

        if (heatMapData == null) {
            heatMapData = LandmarkPersistenceUtils.getHeatMap(nDays);
        }

        request.setAttribute("heatMapData", heatMapData);

        if (inBackground) {
            return null;
        } else {
            return mapping.findForward(SUCCESS);
        }
    }
}
