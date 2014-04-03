/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.struts;

import com.jstakun.lm.server.personalization.ReCaptchaUtils;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;
import org.apache.struts.action.ActionMessage;

/**
 *
 * @author jstakun
 */
public class UserForm extends DynaValidatorForm {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
    }

    /**
     * Validate all properties to their default values.
     * @param mapping The ActionMapping used to select this instance.
     * @param request The HTTP Request we are processing.
     * @return ActionErrors A list of all errors found.
     */
    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        String login = StringUtils.trimToEmpty((String) get("login"));
        if (StringUtils.isEmpty(login)) {
            errors.add("userForm", new ActionMessage("errors.login"));
        } else if (!regexLoginValidate(login)) {
            errors.add("userForm", new ActionMessage("errors.login.regex"));
        } else if (UserPersistenceUtils.userExists(login)) {
            errors.add("userForm", new ActionMessage("errors.uniqueLogin"));
        } 

        String password = StringUtils.trimToEmpty((String) get("password"));
        if (StringUtils.isEmpty(password)) {
            errors.add("userForm", new ActionMessage("errors.password"));
        }

        String repassword = StringUtils.trimToEmpty((String) get("repassword"));
        if (StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(repassword)) {
            if (!StringUtils.equals(password, repassword)) {
                errors.add("userForm", new ActionMessage("errors.repassword"));
            }

            if (!regexPasswordValidate(password)) {
                errors.add("userForm", new ActionMessage("errors.password.regex"));
            }
        }

        String email = StringUtils.trimToEmpty((String) get("email"));
        if (StringUtils.isEmpty(email) || !MailUtils.isValidEmailAddress(email)) {
            errors.add("userForm", new ActionMessage("errors.email"));
        }

        //Captcha verification
        String challenge = request.getParameter("recaptcha_challenge_field");
        String uresponse = request.getParameter("recaptcha_response_field");

        if (challenge == null || uresponse == null) {
            errors.add("userForm", new ActionMessage("errors.captcha"));
        } else {
            String remoteAddr = request.getRemoteAddr();
            if (!ReCaptchaUtils.checkAnswer(remoteAddr, challenge, uresponse)) {
                errors.add("userForm", new ActionMessage("errors.captcha"));
            }
        }

        return errors;
    }

    private static boolean regexPasswordValidate(String password) {
        String regex = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9_-]{6,24}$";

        return (password.matches(regex));
    }
    
    private static boolean regexLoginValidate(String login) {
        String regex = "^[a-zA-Z0-9_-]{4,24}$";

        return (login.matches(regex));
    }
}
