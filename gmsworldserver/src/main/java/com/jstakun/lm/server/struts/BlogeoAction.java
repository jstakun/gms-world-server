package com.jstakun.lm.server.struts;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author jstakun
 */
public class BlogeoAction extends org.apache.struts.action.Action {

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


        String user = null;

        if (request.getParameter("user") != null)
        {
            user = request.getParameter("user");
        }

        if (user != null)
        {
            request.setAttribute("user", user);
            List<Landmark> userLandmarks = LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(user, Commons.SOCIAL, first, first+INTERVAL);
            request.setAttribute("userLandmarks", userLandmarks);

            int count = LandmarkPersistenceUtils.countLandmarksByUserAndLayer(user, Commons.SOCIAL);

            if (count - first - INTERVAL > 0) {
                next = first + INTERVAL;
            }
            if (first - INTERVAL >= 0) {
                prev = first - INTERVAL;
            }

            request.setAttribute("next", new Integer(next));
            request.setAttribute("prev", new Integer(prev));
        }
        
        return mapping.findForward("success");
    }
}
