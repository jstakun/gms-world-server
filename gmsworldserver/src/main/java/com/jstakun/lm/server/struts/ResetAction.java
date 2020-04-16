package com.jstakun.lm.server.struts;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

public class ResetAction extends Action {
	
	private static final Logger logger = Logger.getLogger(ResetAction.class.getName());
	
	@Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        UserForm userForm = (UserForm)form;
        String login = StringUtils.trimToEmpty((String) userForm.get("login"));
        String email = StringUtils.trimToEmpty((String) userForm.get("email"));
        String password = StringUtils.trimToEmpty((String) userForm.get("password"));
        
        String status = "success";
        try
        {
        	String secret = UserPersistenceUtils.persist(login, password, email, null, null);
            request.setAttribute("login", login);
            if (StringUtils.isEmpty(secret)) {
            	status = "failure";
            }
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            status = "failure";
        }

        return mapping.findForward(status);

	}
}
