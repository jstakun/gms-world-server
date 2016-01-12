package com.jstakun.lm.server.struts;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.HttpUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.DynaValidatorForm;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

/**
 *
 * @author jstakun
 */
public class UserForm extends DynaValidatorForm {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(UserForm.class.getName());

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

        //Re-Captcha verification
        String uresponse = request.getParameter("g-recaptcha-response");
        String remoteAddr = request.getRemoteAddr();
        String urlParams = "secret=" + Commons.RECAPTCHA_PRIVATE_KEY +"&response=" + uresponse + "&remoteip=" + remoteAddr;
        
        try {
 			String response = HttpUtils.processFileRequest(new URL("https://www.google.com/recaptcha/api/siteverify"), "POST", null, urlParams);
 			JSONObject json = new JSONObject(response);
 			if (!json.getBoolean("success") == true) {
 				logger.log(Level.SEVERE, "Recaptcha verification error", response);
 				errors.add("userForm", new ActionMessage("errors.captcha"));
 			}
 	    } catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			errors.add("userForm", new ActionMessage("errors.captcha"));
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
