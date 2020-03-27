/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.utils.MailUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

/**
 *
 * @author jstakun
 */
public class ContactAction extends org.apache.struts.action.Action {

    private static final Logger logger = Logger.getLogger(ContactAction.class.getName());
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
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        DynaActionForm contactForm = (DynaActionForm) form;

        final String subject = (String) contactForm.get("subject");
        final String name = (String) contactForm.get("name");
        final String email = (String) contactForm.get("email");
        final String message = (String) contactForm.get("message");

        try {
            MailUtils.sendContactMessage(email, name, subject, message);
            request.setAttribute("status","success");
            contactForm.reset(mapping, request);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            request.setAttribute("status","failed");
        }

        return mapping.findForward("success");
    }
}
