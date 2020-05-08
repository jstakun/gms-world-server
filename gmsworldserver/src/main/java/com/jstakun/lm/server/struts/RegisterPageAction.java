package com.jstakun.lm.server.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.UserAgentUtils;

public class RegisterPageAction extends Action {
	
	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		saveToken(request);
		
		if (UserAgentUtils.isMobile(request.getHeader("User-Agent"))) {
			return mapping.findForward("mobile");
    	} else {
    		return mapping.findForward("success");
    	}
	}	

}
