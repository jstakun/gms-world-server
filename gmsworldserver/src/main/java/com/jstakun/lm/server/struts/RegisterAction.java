package com.jstakun.lm.server.struts;

import net.gmsworld.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;
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

/**
 *
 * @author jstakun
 */
public class RegisterAction extends Action {

    private static final Logger logger = Logger.getLogger(RegisterAction.class.getName());

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	//String status = "failure";
    	//if (isTokenValid(request)) { //requires session
    	String status = "success";	
    		UserForm userForm = (UserForm)form;
        
    		final String login = StringUtils.trimToEmpty((String) userForm.get("login"));
    		final String email = StringUtils.trimToEmpty((String) userForm.get("email"));
    		final String password = StringUtils.trimToEmpty((String) userForm.get("password"));
    		final String firstname = StringUtils.trimToEmpty((String) userForm.get("firstname"));
    		final String lastname = StringUtils.trimToEmpty((String) userForm.get("lastname"));

    		try
    		{
    			final String secret = UserPersistenceUtils.persist(login, password, email, firstname, lastname);
    			if (StringUtils.isNotEmpty(secret)) {
    				MailUtils.sendVerificationRequest(email, login, secret, getServlet().getServletContext());
    				MailUtils.sendUserCreationNotification("User " + ConfigurationManager.SERVER_URL + "showUser/" + login + " created");
    				request.setAttribute("email", email);
    			} else {
    				status = "failure";
    			}
    		}
    		catch (Exception e)
    		{
    			logger.log(Level.SEVERE, e.getMessage(), e);
    			status = "failure";
    		}
        //}
        return mapping.findForward(status);
    }

}
