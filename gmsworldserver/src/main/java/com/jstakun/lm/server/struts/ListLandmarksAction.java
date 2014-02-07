package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ListLandmarksAction extends Action {
    /**This is the main action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException,
                                                                      ServletException {

        List<Landmark> landmarkList = LandmarkPersistenceUtils.selectNewestLandmarks();     
        
        new ArrayList<Landmark>();
        request.setAttribute("landmarkList", landmarkList);

        return mapping.findForward( "success");
    }
}
