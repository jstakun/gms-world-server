/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.utils.MailUtils;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.DynaValidatorForm;

/**
 *
 * @author jstakun
 */
public class ContactForm extends DynaValidatorForm {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param request The HTTP Request we are processing.
     * @return
     */
    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (!MailUtils.isValidEmailAddress((String) get("email"))) {
            errors.add("contactForm", new ActionMessage("errors.email"));
        }
        if (StringUtils.isEmpty((String) get("message"))) {
            errors.add("contactForm", new ActionMessage("errors.login"));
        }

        return errors;
    }
}
