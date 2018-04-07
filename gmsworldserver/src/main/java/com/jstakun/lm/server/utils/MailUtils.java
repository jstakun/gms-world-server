package com.jstakun.lm.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class MailUtils {

    private static final Logger logger = Logger.getLogger(MailUtils.class.getName());

    private static String sendLocalMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromA, fromP));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toA, toP));
            if (ccA != null && ccP != null) {
            	msg.addRecipient(Message.RecipientType.CC, new InternetAddress(ccA, ccP));
            } else if (ccA != null) {
            	msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccA));
            }
            msg.setSubject(subject);
            msg.setContent(content, contentType);
            Transport.send(msg);
            return "ok";
        } catch (Exception ex) {
        	logger.log(Level.SEVERE, ex.getMessage(), ex);
            return "failed";
        }
    }
    
    private static String sendRemoteMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP)  {
    	 final String MAILER_SERVER_URL = "https://openapi-landmarks.b9ad.pro-us-east-1.openshiftapps.com/actions/emailer"; 
    	 
     	 String params = "from=" + fromA +
    	                                "&password=" + Commons.getProperty(Property.RH_MAILER_PWD) +
    			                        "&to=" + toA;
    	 if (subject != null) {
    		  params +=  "&subject=" + subject;
    	 }
    	 if (content != null) {
    			params += "&body=" + content;
    			if (contentType != null) {
    				 params += "&contentType=" + contentType;
    			}
    	 }		                        
    	 if (fromP != null) {
    		 	params +="&fromNick=" + fromP;
    	 }
    	 if (toP != null) {
    		 	params += "&toNick=" + toP;
    	 }	
    	 if (ccA != null) {
    		 	params  += "&cc=" + ccA;
         }
    	 if (ccP != null) {
    		 	params  += "&ccNick=" + ccP;
    	 }
    	 
    	 try {
    		 HttpUtils.processFileRequestWithBasicAuthn(new URL(MAILER_SERVER_URL), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
    		 Integer responseCode = HttpUtils.getResponseCode(MAILER_SERVER_URL);
    		 logger.log(Level.INFO, "Received response code: " + responseCode);
    		 if (responseCode != null && responseCode == 200) {
    			 return "ok";
    		 } else {
    			 return sendLocalMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
    		 } 
    	 } catch (Exception e) {
    		 logger.log(Level.SEVERE, e.getMessage(), e);
    		 return sendLocalMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
    	 }
    }
    
    public static void sendAdminMail(String message) {
    	sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, "Admin message", message, "text/plain", null, null);
    }

    public static void sendEmailingMessage(String toA, String nick, String message) {
        sendRemoteMail(ConfigurationManager.LM_MAIL, ConfigurationManager.LM_NICK, toA, nick, "Message from Landmark Manager", message, "text/html", null, null);
    }
    
    public static void sendDeviceLocatorMessage(String toA, String message, String title) {
    	//sendMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, toA, title, message, "text/plain");
    	sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, toA, title, message, "text/plain", null, null);
    }
    
    public static void sendDeviceLocatorRegistrationRequest(String email) {
    	sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, "Registration request", email, "text/plain", "jstakun.appspot@gmail.com", ConfigurationManager.DL_NICK);
    }

    public static String sendLandmarkCreationNotification(String title, String body) {
        return sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, body, "text/plain", null, null);
    }

    public static void sendList(String title, Map<String, Collection<String>> stringMap, Map<String, Integer> recentlyCreated) {
        String message = "";
        for (Map.Entry<String, Collection<String>> entry : stringMap.entrySet()) {
            message += "Report for " + entry.getKey() + "<br/><br/>";
            Collection<String> stringList = entry.getValue();
            for (String s : stringList) {
                message += s + "<br/>";
            }
            if (!stringList.isEmpty()) {
                message += "<br/>";
            }
            message += "Found " + stringList.size() + " record(s).";
            message += "<br/>Number of users last week: " + recentlyCreated.get(entry.getKey()) + ".<br/><br/>";
        }
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, message, "text/html", null, null);
    }

    public static void sendVerificationRequest(String toA, String nick, String key, ServletContext context) {
        InputStream is = null;
        try {
            String link = ConfigurationManager.SERVER_URL + "verify.do?k=" + URLEncoder.encode(key, "UTF-8") + "&s=1";
            is = context.getResourceAsStream("/WEB-INF/emails/verification.html");
            String message = String.format(IOUtils.toString(is, "UTF-8"), nick, link);
            sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "Welcome to GMS World", message, "text/html",  "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static String sendDlVerificationRequest(String toA, String nick, String secret, ServletContext context, boolean first) {
        InputStream is = null;
        String result = null; 
        try {
            String link = ConfigurationManager.SERVER_URL + "verify/" + secret + "/" + URLEncoder.encode(toA, "UTF-8");
            if (first) {
            	is = context.getResourceAsStream("/WEB-INF/emails/verification-dl.html");
            } else {
            	is = context.getResourceAsStream("/WEB-INF/emails/verification-next-dl.html");
            }
            String message = String.format(IOUtils.toString(is, "UTF-8"), link);
            result = sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  "jstakun.appspot@gmail.com", ConfigurationManager.DL_NICK);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            result = "failed";
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
        return result;
    }
    
    public static void sendRegistrationNotification(String toA, String nick, ServletContext context) {
        InputStream is = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/notification.html");
            if (StringUtils.isEmpty(nick)) {
            	nick = "GMS World User";
            }
            String message = String.format(IOUtils.toString(is, "UTF-8"), nick);
            sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Registration", message, "text/html",  "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static void sendDlRegistrationNotification(String toA, String nick, ServletContext context) {
        InputStream is = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/notification-dl.html");
            String message = IOUtils.toString(is, "UTF-8");
            sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  "jstakun.appspot@gmail.com", ConfigurationManager.DL_NICK);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static void sendLoginNotification(String toA, String nick, String layer, ServletContext context) {
        InputStream is = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/login.html");
            String message = String.format(IOUtils.toString(is, "UTF-8"), nick, layer);
            sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Login", message, "text/html",  "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static String sendLandmarkNotification(String toA, String userUrl, String nick, String landmarkUrl, ServletContext context) {
        InputStream is = null;
        String status = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/landmark.html");
            String message = String.format(IOUtils.toString(is, "UTF-8"), userUrl, nick, landmarkUrl, landmarkUrl);
            status = sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "Message from GMS World", message, "text/html", null, null);
            //remove after tests
            //sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, ConfigurationManager.SUPORT_MAIL, ConfigurationManager.ADMIN_NICK, "Copy of message to " + toA, message, "text/html", null, null);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        return status;
    }
    
    public static void sendUserCreationNotification(String body) {
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, "New user", body, "text/plain",  "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK);
    }
    
    public static void sendBlackScreenshotNotification(String body) {
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, "Black screenshot", body, "text/plain", null, null);
    }

    public static void sendCrashReport(String title, String body) {
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, body, "text/plain", null, null);
    }

    public static void sendContactMessage(String fromA, String nick, String subject, String body) {
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, subject, "Message from: " + nick + " " + fromA + "\n" + body, "text/plain", null, null);
    }

    public static boolean isValidEmailAddress(String aEmailAddress) {
        return EmailValidator.getInstance().isValid(aEmailAddress);
    }

    public static void sendSearchQueryNotification(String query, boolean isDeal, int counter, String uri) {
        String url = null;
        if (uri.startsWith("/services")) {
            url = ConfigurationManager.SSL_SERVER_URL + uri.substring(1);
        } else {
            url = ConfigurationManager.SERVER_URL + uri.substring(1);
        }
        String message = "New search query has been executed: " + query + "\nDeals: "
                + isDeal + "\nResponse contains " + counter + " landmarks.\nCheck it out: " + url;
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, "New search with " + counter + " results", message, "text/plain", null, null);
    }
    
    public static void sendEngagementMessage(String toA, ServletContext context) {
        InputStream is = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/engage.html");
            String message = IOUtils.toString(is, "UTF-8");
            
            if (com.jstakun.lm.server.config.ConfigurationManager.listContainsValue(ConfigurationManager.EXCLUDED, toA)) {
               sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.LM_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, "Copy of excluded engagement message to " + toA, message, "text/html", null, null);
               logger.log(Level.INFO, "Skipped sending engagement message to " + toA);
            } else {
               sendRemoteMail(ConfigurationManager.LM_MAIL, ConfigurationManager.LM_NICK, toA, "Landmark Manager User", "Message from Landmark Manager", message, "text/html", null, null);
               //remove after tests
               sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.LM_NICK,  "jstakun.appspot@gmail.com", ConfigurationManager.LM_NICK, "Copy of engagement message to " + toA, message, "text/html", null, null);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
