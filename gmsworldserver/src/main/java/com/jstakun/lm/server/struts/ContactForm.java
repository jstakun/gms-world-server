package com.jstakun.lm.server.struts;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jstakun.lm.server.utils.MailUtils;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.validator.DynaValidatorForm;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class ContactForm extends DynaValidatorForm {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(UserForm.class.getName());
	
	/**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param request The HTTP Request we are processing.
     * @return
     */
    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        final String email = (String) get("email");
        if (StringUtils.isEmpty(email) || !MailUtils.isValidEmailAddress(email) || MailUtils.emailAccountExists(email) != 200) {
            errors.add("contactForm", new ActionMessage("errors.email"));
            logger.log(Level.WARNING, "Invalid email address " + email);
        }
        if (StringUtils.isEmpty((String) get("message"))) {
            errors.add("contactForm", new ActionMessage("errors.required", "Message"));
        }
        
        if (errors.isEmpty()) {
        	//Re-Captcha verification
        	final String uresponse = request.getParameter("g-recaptcha-response");
        	final String remoteAddr = request.getRemoteAddr();
        	final String urlParams = "secret=" + Commons.getProperty(Property.RECAPTCHA_PRIVATE_KEY) +"&response=" + uresponse + "&remoteip=" + remoteAddr;
        
        	try {
        		String response = HttpUtils.processFileRequest(new URL("https://www.google.com/recaptcha/api/siteverify"), "POST", null, urlParams);
        		JSONObject json = new JSONObject(response);
        		if (json.getBoolean("success") == false) {
        			logger.log(Level.WARNING, "Recaptcha verification error", response);
        			errors.add("userForm", new ActionMessage("errors.captcha"));
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        		errors.add("userForm", new ActionMessage("errors.captcha"));
        	}
        }

        return errors;
    }
    
    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
    	super.reset(mapping, request);
    	getMap().clear();
    }
}
