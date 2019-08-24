package com.jstakun.lm.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import org.json.JSONObject;

import com.jstakun.lm.server.utils.memcache.CacheUtil;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.layers.AwsSesUtils;
import net.gmsworld.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class MailUtils {

    private static final Logger logger = Logger.getLogger(MailUtils.class.getName());
    private static final String VALIDATE_MAIL_URL = com.jstakun.lm.server.config.ConfigurationManager.BACKEND_SERVER_URL + "validateEmail";
    private static final String MAILER_SERVER_URL = com.jstakun.lm.server.config.ConfigurationManager.BACKEND_SERVER_URL + "emailer"; 
	 
	// ---------------------------------------------------------------------------------------------------------------------------
    
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
    	if (isValidEmailAddress(toA)) {
    		final long count  = CacheUtil.increment("mailto:" + toA);
        	if (count < 20) {
    			if (AwsSesUtils.sendEmail(fromA, fromP, toA, toP, ccA, ccP, content, contentType, subject)) {
    				return "ok";
    			} else {
    				logger.log(Level.SEVERE, "Failed to send mail with SES!");
    				//return "failed";
    				return sendJamesMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
    			}
    		} else {
    			if (count % 100 == 0) {
    				logger.log(Level.SEVERE, "James is sending " + count + " email " + subject + " to " + toA);
    			} else {
    				logger.log(Level.WARNING, "James is sending " + count + " email " + subject + " to " + toA);
    			}
    			//return "blocked";
    			return sendJamesMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
    		}
    	} else {
    		logger.log(Level.SEVERE, "Invalid email address " + toA);
    		return "failed";
    	}
    }
    
    private static String sendJamesMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP)  {
    	String status = "failed";   
    	
    	String recipients = addEmailAddress("to", toA, toP);
    	if (StringUtils.isEmpty(recipients)) {
    		return status;
    	}
    	if (StringUtils.isNotEmpty(ccA)) {
    		recipients += "|" + addEmailAddress("cc", ccA, ccP);
    	}	 
    	String params = "from=" + fromA +
    	                                "&password=" + Commons.getProperty(Property.RH_MAILER_PWD) +
    	                                "&recipients=" + recipients;
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
    	
    	 try {
    		 HttpUtils.processFileRequestWithBasicAuthn(new URL(MAILER_SERVER_URL), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
    		 Integer responseCode = HttpUtils.getResponseCode(MAILER_SERVER_URL);
    		 logger.log(Level.INFO, "Received response code: " + responseCode);
    		 if (responseCode != null && responseCode == 200) {
    			 status = "ok";
    		 } 
    	 } catch (Exception e) {
    		 logger.log(Level.SEVERE, e.getMessage(), e);
    	 }
   	    
    	 return status;
    }
    
    // ---------------------------------------------------------------------------------------------------------------
    
    public static void sendAdminMail(String message) {
    	sendAdminMail("Admin message", message) ;
    }
    
    public static void sendAdminMail(String title, String message) {
    	sendJamesMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, message, "text/plain", null, null);
    }

    public static String sendLandmarkCreationNotification(String title, String body) {
        return sendJamesMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, body, "text/plain", null, null);
    }

    public static void sendEmailingMessage(String toA, String nick, String message) {
        sendRemoteMail(ConfigurationManager.LM_MAIL, ConfigurationManager.LM_NICK, toA, nick, "Message from Landmark Manager", message, "text/html", null, null);
    }
    
    public static void sendDeviceLocatorMessage(String toA, String message, String title) {
    	sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, toA, title, message, "text/plain", null, null);
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

    public static void sendVerificationRequest(String toA, String nick, String secret, ServletContext context) {
        InputStream is = null;
        try {
            String link = ConfigurationManager.SSL_SERVER_URL + "verifyUser/" + secret;
            is = context.getResourceAsStream("/WEB-INF/emails/verification.html");
            String message = String.format(IOUtils.toString(is, "UTF-8"), nick, link);
            //String result = sendLocalMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "Welcome to GMS World", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
       		//if (! StringUtils.equalsIgnoreCase(result, "ok")) {
       			sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "Welcome to GMS World", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
       		//}
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
    
    public static String sendDeviceLocatorVerificationRequest(String toA, String nick, String secret, ServletContext context, int version) {
        InputStream is = null;
        String result = null; 
        try {
        	String message = "";
            String link = null; 
            if (context != null) {
            	if (version == 0) {
            		is = context.getResourceAsStream("/WEB-INF/emails/verification-dl.html");
            		link = ConfigurationManager.SSL_SERVER_URL + "verify/" + secret;
            	} else if (version == 1) {
            		is = context.getResourceAsStream("/WEB-INF/emails/verification-next-dl.html");
            		link = ConfigurationManager.SSL_SERVER_URL + "verify/" + secret;
            	} else if (version == 2) {
            		is = context.getResourceAsStream("/WEB-INF/emails/verification-dl-v2.html");
            		String tokens[] = StringUtils.split(secret, ".");
            		if (tokens.length == 2 && tokens[1].length() == 4 && StringUtils.isNumeric(tokens[1])) {
            			link = tokens[1];
            		}
            	}
            	message = String.format(IOUtils.toString(is, "UTF-8"), link);
            } 
            //result = sendLocalMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);
       		//logger.log(Level.INFO, "Email verification request status: " + result);
            //if (! StringUtils.equalsIgnoreCase(result, "ok")) {
            	return sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick,  "Device Locator Registration", message, "text/html", ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);
       		//}	
        } catch (Exception ex) {
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
    
    public static void sendRegistrationNotification(String toA, String nick, String secret, ServletContext context) {
        InputStream is = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/notification.html");
            if (StringUtils.isEmpty(nick)) {
            	nick = "GMS World User";
            }
            String link = ConfigurationManager.SSL_SERVER_URL + "unregisterUser/" + secret;
            String message = String.format(IOUtils.toString(is, "UTF-8"), nick, link);
            //String result = sendLocalMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Registration", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
       		//if (! StringUtils.equalsIgnoreCase(result, "ok")) {
       			sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Registration", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
       		//}
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
    
    public static String sendResetPassword(String toA, String nick, String secret, ServletContext context) {
        InputStream is = null;
        String result = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/reset.html");
            if (StringUtils.isEmpty(nick)) {
            	nick = "GMS World User";
            }
            String link = ConfigurationManager.SSL_SERVER_URL + "reset/" + secret;
            String message = String.format(IOUtils.toString(is, "UTF-8"), nick, link);
            //result = sendLocalMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Password Reset", message, "text/html",  null, null);
       		//if (! StringUtils.equalsIgnoreCase(result, "ok")) {
       			result = sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Password Reset", message, "text/html",  null, null);
       		//}
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
        return result;
    }
    
    public static String sendDeviceLocatorRegistrationNotification(String toA, String nick, String secret, ServletContext context) {
        InputStream is = null;
        String status = null;
        try {
        	String message = "";
        	String link = ConfigurationManager.SSL_SERVER_URL + "unregister/" + secret;
        	if (context != null) {
        		is = context.getResourceAsStream("/WEB-INF/emails/notification-dl.html");
        		message =String.format(IOUtils.toString(is, "UTF-8"), link);
        	}
            //status = sendLocalMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);
        	//if (! StringUtils.equalsIgnoreCase(status, "ok")) {
        		status = sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);	
        	//}
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
        return status;
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
        String status = sendJamesMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, title, body, "text/plain", null, null);
        if (! StringUtils.equalsIgnoreCase(status, "ok")) {
        	sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", null, title, body, "text/plain", null, null) ;
        }
    }

    public static void sendContactMessage(String fromA, String nick, String subject, String body) {
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, subject, "Message from: " + nick + " " + fromA + "\n" + body, "text/plain", null, null);
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
    
    // --------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    private static String addEmailAddress(String type, String email, String nick) {
    	  String emailAddress = "";
    	  if ((StringUtils.equals(type, "to") || StringUtils.equals(type, "cc") || StringUtils.equals(type, "bcc")) && StringUtils.isNotEmpty(email) && isValidEmailAddress(email)) {
    		  try { 
    			  InternetAddress.parse(email);   
    		      if (StringUtils.isNotEmpty(nick) && !StringUtils.equals(nick, email)) {
    		    	  emailAddress = type + ":" + nick + " <" + email + ">";
    		      } else {
    		    	  emailAddress = type + ":" + email;
    		      }
    		   } catch (Exception e) {
    			   logger.log(Level.SEVERE, "Invalid email: " + email, e);    
    		   }
    	  }
    	  return  emailAddress;
    }
    
    public static boolean isValidEmailAddress(String aEmailAddress) {
        return EmailValidator.getInstance().isValid(aEmailAddress);
    }
    
    public static JSONObject emailAccountExists( String address ) {
    	JSONObject reply = new JSONObject();
    	if (StringUtils.isEmpty(address)) {
    		reply.put("responseCode",500); 
    		return reply;
    	}
  
    	try {
    		final String url =  VALIDATE_MAIL_URL + "?to="+ address;
   	 		String response = HttpUtils.processFileRequestWithBasicAuthn(new URL(url), "GET", null, null, Commons.getProperty(Property.RH_GMS_USER));
   	 		Integer responseCode = HttpUtils.getResponseCode(url);
   	 		logger.log(Level.INFO, "Received response code: " + responseCode);
   	 		if (responseCode != null && responseCode == 200 && StringUtils.startsWith(response, "{")) {
   	 			JSONObject root = new JSONObject(response);
   	 		    reply.put("responseCode",responseCode);
   	 		    if (StringUtils.equals(root.optString("status"), "ok")) {
   	 		    	reply.put("status", "ok");
   	 		    } else {
   	 		    	reply.put("status", "failed");
   	 		    }
   	 		} else {
   	 			reply.put("status", "failed");
   	 			logger.log(Level.SEVERE, "Received following response: " + response);
   	 			if (responseCode != null) {
   	 				reply.put("responseCode", responseCode);
   	 			} else {
   	 				reply.put("responseCode", 500);
   	 			}
   	 		}
   	 	} catch (Exception e) {
   	 		logger.log(Level.SEVERE, e.getMessage(), e);
   	 		reply.put("responseCode", 500);
   	 	}
    	
    	return reply;
    }
}
