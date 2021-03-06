package com.jstakun.lm.server.struts;

import java.net.URLDecoder;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.jstakun.lm.server.persistence.Notification;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.MailUtils;
import com.jstakun.lm.server.utils.UserAgentUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class AccountAction extends Action {

    private static final String SUCCESS_REG = "success_reg";
    private static final String SUCCESS_UNREG = "success_unreg";
    private static final String FAILURE_REG = "failure_reg";
    private static final String FAILURE_UNREG = "failure_unreg";
    
    private static final String SUCCESS_REG_MOBILE = "success_reg_mobile";
    private static final String SUCCESS_UNREG_MOBILE = "success_unreg_mobile";
    private static final String FAILURE_REG_MOBILE = "failure_reg_mobile";
    private static final String FAILURE_UNREG_MOBILE = "failure_unreg_mobile";
    
    private static final String CONFIRM_UNREG_MOBILE = "confirm_unreg_mobile";
    private static final String CONFIRM_UNREG = "confirm_unreg";
    
    private static final Logger logger = Logger.getLogger(AccountAction.class.getName());
    
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
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

    	final boolean isMobile = UserAgentUtils.isMobile(request.getHeader("User-Agent"));
        String deviceName = request.getHeader(Commons.DEVICE_NAME_HEADER);
        if (StringUtils.isEmpty(deviceName)) {
        	deviceName = request.getParameter("dn");
        }
        String deviceId = request.getHeader(Commons.DEVICE_ID_HEADER);
        if (StringUtils.isEmpty(deviceId)) {
        	deviceId = request.getParameter("di");
        }
        
        Boolean confirm = Boolean.FALSE;
        if (StringUtils.equals(request.getParameter("s"), "1")) {
            confirm = Boolean.TRUE;
        }
        
        Boolean confirmUnreg = Boolean.FALSE;

        final Locale locale = request.getLocale();
		String language  = "en";
		if (locale != null) {
			language = locale.getLanguage();
		}
        
        boolean result = false;

        boolean api = false;
        String output = null;
        if (StringUtils.startsWith(request.getHeader("User-Agent"), "Device Locator")) {
        	api = true;
        	output = "{\"status\":\"unknown\"}";
        }
        
        if (!HttpUtils.isEmptyAny(request, "k", "s")) {
        	//register user to GMS World
            final String login = URLDecoder.decode(request.getParameter("k"),"UTF-8");
            result = UserPersistenceUtils.confirmUserRegistration(login);
            if (result) {
               User user = UserPersistenceUtils.selectUserByLogin(login, null);
               if (user != null) {
                    MailUtils.sendRegistrationNotification(user.getEmail(), user.getLogin(), user.getSecret(), getServlet().getServletContext());
                    request.setAttribute("login", user.getLogin());
                    request.setAttribute("email", user.getEmail());
               } else {
               		logger.log(Level.SEVERE, "Account with login " + login + " not found!");
               }
            } else {
           		logger.log(Level.SEVERE, "Failed to confirm " + login + " registration!");
            }
        } else if (!HttpUtils.isEmptyAny(request, "sc", "s")) {
        	//register to DL notifications
        	final String secret = request.getParameter("sc");
        	Notification n = NotificationPersistenceUtils.verifyWithSecret(secret);
        	if (n != null) {
        		final String email = n.getId(); 
        		if (MailUtils.isValidEmailAddress(email)) {
        			if (!CacheUtil.containsKey("mailto:"+email+":verified")) {
    					final String status = MailUtils.sendDeviceLocatorRegistrationNotification(email, email, secret, getServlet().getServletContext(), deviceName, deviceId, language);
    					if (StringUtils.equalsIgnoreCase(status, MailUtils.STATUS_OK)) {
							CacheUtil.put("mailto:"+email+":verified", secret, CacheType.FAST);
						}
    				}
        			request.setAttribute("email", email);
        		} 
        		if (api) {
    				output = "{\"status\":\"ok\"}";
    			}
        		result = true;
        	} else {
            	logger.log(Level.SEVERE, "Notification with secret " + secret + " not found!");
            } 
        } else if (!HttpUtils.isEmptyAny(request, "k", "sc", "u")) {
        	//unregister from DL notifications using email
        	final String secret = request.getParameter("sc");
        	final String email = URLDecoder.decode(request.getParameter("k"),"UTF-8").trim();
        	request.setAttribute("email", email);
			Notification n = NotificationPersistenceUtils.findBySecret(secret);
        	if (n != null && StringUtils.equals(n.getId(), email)) {
        		if (NotificationPersistenceUtils.remove(email)) {
        			CacheUtil.remove("mailto:"+email+":verified");
        			if (MailUtils.isValidEmailAddress(email)) {
        				MailUtils.sendUnregisterNotification(email, "", getServlet().getServletContext());
            		} else {
        				MailUtils.sendAdminMail("Notifications Service unregistration", email + " has unregistered from Notifications service.");
        			}
        			if (api) {
        				output = "{\"status\":\"ok\"}";
        			}
        		}
        	} else if (n != null && n.getId() == null) {
        		request.setAttribute("email", "has been");
                result = true;
        	} else {
            	logger.log(Level.SEVERE, "Notification -" + email + "- with secret -" + secret + "- not found!");
            }
        } else if (!HttpUtils.isEmptyAny(request, "u", "sc")) {
        	//unregister from DL notifications using secret
        	final String secret = request.getParameter("sc");
        	Notification n = NotificationPersistenceUtils.findBySecret(secret);
        	if (n != null && n.getId() != null) {
            	request.setAttribute("secret", secret);
            	request.setAttribute("email", n.getId());
            	confirmUnreg = true;
            } else {
            	logger.log(Level.SEVERE, "Notification with secret " + secret + " not found!");
            }
        } else if (!HttpUtils.isEmptyAny(request, "k", "u", "se")) {
        	//unregister user from GMS World using login
            final String login = URLDecoder.decode(request.getParameter("k"),"UTF-8");
            final String secret = request.getParameter("se");
            final User user = UserPersistenceUtils.selectUserByLogin(login, null);
            request.setAttribute("login", login);
            if (user != null && StringUtils.equals(secret, user.getSecret())) {
            	UserPersistenceUtils.removeUser(user.getSecret());
                MailUtils.sendAdminMail("GMS World User Unregistration", user.getLogin() + " has unregistered from GMS World.");
                request.setAttribute("email", user.getEmail());
                result = true;
            } else {
            	logger.log(Level.SEVERE, "Failed to remove account " + login + " with secret " + secret);
            }
        } else if (!HttpUtils.isEmptyAny(request, "se", "u")) {
        	//unregister user from GMS World using secret
            final String secret = request.getParameter("se");
            final User user = UserPersistenceUtils.selectUserByLogin(null, secret);
            if (user != null) {
            	request.setAttribute("secret", secret);
            	request.setAttribute("login", user.getLogin());
            	confirmUnreg = true;
            } else {
            	logger.log(Level.SEVERE, "Account with secret " + secret + " not found!");
            }
        } else if (!HttpUtils.isEmptyAny(request, "se", "s")) {
        	//register user to GMS World
            final String secret = request.getParameter("se");
            final User user = UserPersistenceUtils.selectUserByLogin(null, secret);
            if (user != null) {
            	result = UserPersistenceUtils.confirmUserRegistration(user.getLogin());
                MailUtils.sendRegistrationNotification(user.getEmail(), user.getLogin(), secret, getServlet().getServletContext());
                request.setAttribute("login", user.getLogin());
                request.setAttribute("email", user.getEmail());
            } else {
            	logger.log(Level.SEVERE, "Account with secret " + secret + " not found!");
            }
        } 

        if (api) {
        	request.setAttribute("output", output);
        	return mapping.findForward("api");   
        } else if (isMobile) {
        	if (confirmUnreg) {
        		return mapping.findForward(CONFIRM_UNREG_MOBILE);
            } else if (result) {
        		if (confirm) {
        			return mapping.findForward(SUCCESS_REG_MOBILE);
        		} else {
        			return mapping.findForward(SUCCESS_UNREG_MOBILE);
        		}
        	} else {
        		if (confirm) {
        			return mapping.findForward(FAILURE_REG_MOBILE);
        		} else {
        			return mapping.findForward(FAILURE_UNREG_MOBILE);
        		}
        	}
        } else {
        	if (confirmUnreg) {
        		return mapping.findForward(CONFIRM_UNREG);
            } else if (result) {
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
}
