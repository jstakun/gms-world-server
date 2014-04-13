/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import java.net.URLDecoder;

import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

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
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Boolean confirm = Boolean.FALSE;
        boolean result = false;

        if (!HttpUtils.isEmptyAny(request, "k", "s")) {
            String login = URLDecoder.decode(request.getParameter("k"),"UTF-8");
            String s = request.getParameter("s");

            if (s.equals("1")) {
                confirm = Boolean.TRUE;
            }

            result = UserPersistenceUtils.confirmUserRegistration(login, confirm);
            if (result) {
               User user = UserPersistenceUtils.selectUserByLogin(login);
               if (user != null) {
                    MailUtils.sendRegistrationNotification(user.getEmail(), user.getLogin(), getServlet().getServletContext());
               }
            }

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
