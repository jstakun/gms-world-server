package com.jstakun.lm.server.struts;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.layers.FreebaseUtils;
import com.jstakun.lm.server.layers.LayerHelperFactory;

public class FreebaseViewAction extends Action {
	private static final String SUCCESS = "success";
	private static final String MOBILE = "mobile";
	private static final Logger logger = Logger.getLogger(FreebaseViewAction.class.getName());

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

        String mid = (String) request.getParameter("mid");

        if (StringUtils.isNotEmpty(mid)) {
        	request.setAttribute("mid", mid);
        	FreebaseUtils freebaseUtils = LayerHelperFactory.getFreebaseUtils();
        	List<String> filter = Arrays.asList("(all mid:/m/" + mid + ")");
        	List<ExtendedLandmark> landmarks = freebaseUtils.search(filter, null, 1, -1, request.getLocale());
        	if (!landmarks.isEmpty()) {
        		request.setAttribute("landmark", landmarks.get(0));
        	}
        }
        
        return mapping.findForward(MOBILE);
    }
}
