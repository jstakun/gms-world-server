package com.jstakun.lm.server.struts;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

import net.gmsworld.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class RegistrationConfirmationAction extends Action {

    /* forward name="success" path="" */
    private static final String SUCCESS_REG = "success_reg";
    private static final String SUCCESS_UNREG = "success_unreg";
    private static final String FAILURE_REG = "failure_reg";
    private static final String FAILURE_UNREG = "failure_unreg";
    
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
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        Boolean confirm = Boolean.FALSE;
        if (StringUtils.equals(request.getParameter("s"), "1")) {
            confirm = Boolean.TRUE;
        }
        boolean result = false;
        
        if (!HttpUtils.isEmptyAny(request, "k", "s")) {
            String login = URLDecoder.decode(request.getParameter("k"),"UTF-8");
            
            result = UserPersistenceUtils.confirmUserRegistration(login);
            if (result) {
               User user = UserPersistenceUtils.selectUserByLogin(login);
               if (user != null) {
                    MailUtils.sendRegistrationNotification(user.getEmail(), user.getLogin(), getServlet().getServletContext());
               }
            }
        } else if (!HttpUtils.isEmptyAny(request, "m", "s")) {
        	String email = request.getParameter("m");
        	
        	if (!NotificationPersistenceUtils.isWhitelistedEmail(email)) {
        		NotificationPersistenceUtils.addToWhitelistEmail(email, true);
            }
        	result = true;
        }

        if (result) {
            if (confirm) {
                return mapping.findForward(SUCCESS_REG);
            } else {
                return mapping.findForward(SUCCESS_UNREG);
            }
        } else {
            if (confirm) {
                return mapping.findForward(FAILURE_REG);
            } else {
                return mapping.findForward(FAILURE_UNREG);
            }
        }
    }
}
