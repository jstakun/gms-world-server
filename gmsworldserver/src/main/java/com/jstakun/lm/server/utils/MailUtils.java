package com.jstakun.lm.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.memcache.CacheUtil;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.HttpUtils;

/**
 *
 * @author jstakun
 */
public class MailUtils {

    private static final Logger logger = Logger.getLogger(MailUtils.class.getName());
    
    public static final String STATUS_OK = "ok";
    public static final String STATUS_FAILED = "failed";
    
    private static final String[] LANGUAGES = new String[]{"es", "pl", "pt", "vi"}; //en is default
	 
	// ---------------------------------------------------------------------------------------------------------------------------
    
    /*private static String sendLocalMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP) {
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
            return STATUS_OK;
        } catch (Exception ex) {
        	logger.log(Level.SEVERE, ex.getMessage(), ex);
            return STATUS_FAILED;
        }
    }*/
    
    // ---------------------------------------------------------------------------------------------------------------------------
    
    private static String sendRemoteMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP)  {
    	if (isValidEmailAddress(toA)) {
    		final long count  = CacheUtil.increment("mailto:" + toA);
        	if (count <= 50 || (count <= 100 && count % 2 == 0) || (count <= 200 && count % 10 == 0) || count % 100 == 0) {
        		//send first 50, every second > 50 && <= 100 every 10th > 100 && <= 200, every 100th > 200
        		final String status = sendSesMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
    			if (StringUtils.equalsIgnoreCase(status, STATUS_OK)) {
    				return STATUS_OK; 
        	    } else {
    				//logger.log(Level.SEVERE, "Failed to send email message with SES! Trying with James...");
    				//return sendJamesMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
        	    	logger.log(Level.WARNING, "Failed to send email message with SES! Trying one more time ...");
    				return sendSesMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
    			}
    		} else {
    			//logger.log(Level.WARNING, "James is sending " + count + " email " + subject + " to " + toA);
    			//return sendJamesMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
    			if (count % 50 == 0) {
    				logger.log(Level.SEVERE, "Skipping to send " + count + " email " + subject + " to " + toA);
    				logger.log(Level.INFO, "Message:\n" + content);
    			} else {
    				logger.log(Level.WARNING, "Skipping to send " + count + " email " + subject + " to " + toA);
    				logger.log(Level.INFO, "Message:\n" + content);
    			}
    			return STATUS_FAILED;
    		}
    	} else {
    		logger.log(Level.SEVERE, "Invalid email address " + toA);
    		return STATUS_FAILED;
    	}
    }
    
    private static String sendSesMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP)  {
    	return sendBackendMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP, "ses");
    }
    
    /*private static String sendJamesMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP)  {
    	return sendBackendMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP, "james");
    }*/
        
    private static String sendBackendMail(String fromA, String fromP, String toA, String toP, String subject, String content, String contentType, String ccA, String ccP, String type)  {
    	String status = STATUS_FAILED;   
    	
   	 	try {
   	 		 String recipients = addEmailAddress("to", toA, toP);
   	 		 if (StringUtils.isEmpty(recipients)) {
   	 			 return status;
   	 		 }
   	 		 if (StringUtils.isNotEmpty(ccA)) {
   	 			 recipients += "|" + addEmailAddress("cc", ccA, ccP);
   	 		 }	 
   	 		 String params = "from=" + URLEncoder.encode(fromA, "UTF-8") +
    	                                "&recipients=" + URLEncoder.encode(recipients, "UTF-8") +
    	                       		    "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
    	     
   	 		 if (StringUtils.isNotEmpty(fromP)) {
  	 			 params +="&fromNick=" + URLEncoder.encode(fromP, "UTF-8");
  	 		 }
   	 		 
   	 		 if (StringUtils.equalsIgnoreCase(type, "ses")) {
   	 			 params += "&type=ses";
   	 		 } else {
   	 			 //james
   	 			 params += "&password=" + Commons.getProperty(Property.RH_MAILER_PWD);
   	 		 }
    	
   	 		 if (subject != null) {
   	 			 params +=  "&subject=" + URLEncoder.encode(subject, "UTF-8");
   	 		 }
   	 		 
   	 		 if (content != null) {
   	 			 if (contentType != null) {
   	 				 params += "&contentType=" + contentType;
   	 			 }	
   	 			 //if (content.length() > 8 * 1024) {
   	 			 //	 params += "&body=" + URLEncoder.encode(content.substring(0, 8 * 1024), "UTF-8");
   	 			 //} else {
   	 		     params += "&body=" + URLEncoder.encode(content, "UTF-8");
   	 			 //}
   	 		 }		                        
   	 		 	
    		 final String sendMailUrl = com.jstakun.lm.server.config.ConfigurationManager.getBackendUrl() + "/emailer"; 
    		 HttpUtils.processFileRequest(new URL(sendMailUrl), "POST", null, params);
    		 Integer responseCode = HttpUtils.getResponseCode(sendMailUrl);
    		 logger.log(Level.INFO, "Received response code: " + responseCode);
    		 if (responseCode != null && responseCode == 200) {
    			 status = STATUS_OK;
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
    	sendSesMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, message, "text/plain", null, null);
    }
    
    public static void sendQuotaResetMail(String fromDevice, String toDevice, String command) {
    	String message = "Quota reset for sending command " + command + " from device " + ConfigurationManager.SSL_SERVER_URL + "showDevice/" + fromDevice 
    			                   + " to device " +   ConfigurationManager.SSL_SERVER_URL + "showDevice/" + toDevice; 
    	sendSesMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, "Quota reset request", message, "text/plain", null, null);
    }

    public static String sendLandmarkCreationNotification(String title, String body) {
        return sendSesMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, body, "text/plain", null, null);
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
            final String link = ConfigurationManager.SSL_SERVER_URL + "verifyUser/" + secret;
            is = context.getResourceAsStream("/WEB-INF/emails/verification.html");
            final String message = String.format(IOUtils.toString(is, "UTF-8"), nick, link);
            sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "Welcome to GMS World", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
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
    
    public static String sendDeviceLocatorVerificationRequest(String toA, String nick, String secret, ServletContext context, int version, String deviceName, String deviceId, String language) {
        InputStream is = null;
        String result = null; 
        try {
        	String message = "";
            String link = null; 
            if (context != null) {
            	if (version == 0) {
            		is = context.getResourceAsStream("/WEB-INF/emails/verification-dl.html");
            		link = ConfigurationManager.SSL_SERVER_URL + "verify/" + secret;
                 	message = String.format(IOUtils.toString(is, "UTF-8"), link, link);
                } else if (version == 1) {
            		is = context.getResourceAsStream("/WEB-INF/emails/verification-next-dl.html");
            		link = ConfigurationManager.SSL_SERVER_URL + "verify/" + secret;
            	 	message = String.format(IOUtils.toString(is, "UTF-8"), link, link);
                } else if (version == 2) {
            		is = context.getResourceAsStream("/WEB-INF/emails/verification-dl-v2.html");
            		String tokens[] = StringUtils.split(secret, ".");
            		if (tokens.length == 2 && tokens[1].length() == 4 && StringUtils.isNumeric(tokens[1])) {
            			link = tokens[1];
            		}
            	 	message = String.format(IOUtils.toString(is, "UTF-8"), link, link);
                } else if (version == 3) {
                	is = context.getResourceAsStream("/WEB-INF/emails/verification-dl-v3.html");
            		link = ConfigurationManager.SSL_SERVER_URL + "verify/" + secret;
            	 	message = String.format(IOUtils.toString(is, "UTF-8"), link, link);
                } else if (version == 4) {
                	if (StringUtils.indexOfAny(language, LANGUAGES)>=0) {
                		is = context.getResourceAsStream("/WEB-INF/emails/verification-dl-v4_" + language + ".html");
                	} else {
                		is = context.getResourceAsStream("/WEB-INF/emails/verification-dl-v4.html");
                	}
            		link = ConfigurationManager.SSL_SERVER_URL + "verify/" + secret + "?dn=" + deviceName;
            		if (StringUtils.isNotEmpty(deviceId)) {
            			 link += "&di=" + deviceId;
            		}
            		message = String.format(IOUtils.toString(is, "UTF-8"), link, deviceName);
                } else {
                	throw new Exception("Invalid version: " + version);
                }
            } 
            return sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick,  "Device Locator Registration", message, "text/html", ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);	
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            result = STATUS_FAILED;
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
            sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Registration", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
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
    
    public static void sendUnregisterNotification(String toA, String nick, ServletContext context) {
        InputStream is = null;
        try {
            is = context.getResourceAsStream("/WEB-INF/emails/unregister.html");
            String message = IOUtils.toString(is, "UTF-8");
            sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick,  "Device Locator Unregistration", message, "text/html", ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);	
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
            result = sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Password Reset", message, "text/html",  null, null);
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
    
    public static String sendDeviceLocatorRegistrationNotification(String toA, String nick, String secret, ServletContext context, String deviceName, String deviceId, String language) {
        InputStream is = null;
        String status = null;
        try {
        	String message = "";
        	final String unregisterLink = ConfigurationManager.SSL_SERVER_URL + "unregister/" + secret;
        	final String userDevicesLink = ConfigurationManager.SSL_SERVER_URL + "showUserDevices/" + secret;
        	if (context != null) {
        		if (StringUtils.isNotEmpty(deviceName)) {
        			//int count = DevicePersistenceUtils.getUserDevicesCount(toA, deviceName);
        			//logger.log(Level.INFO, "Found " + count + " " + toA + " devices");
        			//if (count < 1) {
        			//	count = 1;
        			//}
        			//String countString = count + "";
        			//if (StringUtils.equals("en", language)) {
        			//	countString = ordinalEn(count);
        			//}
        			String userDeviceLink = deviceName;
        			if (StringUtils.isNotEmpty(deviceId)) {
        				final String deviceUrl = ConfigurationManager.SSL_SERVER_URL + "showDevice/" + deviceId;
        				userDeviceLink = "<a href=\"" + deviceUrl + "\">" + deviceName + "</a>";
        			}
        			if (StringUtils.indexOfAny(language, LANGUAGES)>=0) {
                		is = context.getResourceAsStream("/WEB-INF/emails/notification-dl-v3_" + language + ".html");
                	} else {
                		is = context.getResourceAsStream("/WEB-INF/emails/notification-dl-v3.html");
                	}
        			message =String.format(IOUtils.toString(is, "UTF-8"), userDeviceLink, userDevicesLink, unregisterLink);		
        		} else {
        			is = context.getResourceAsStream("/WEB-INF/emails/notification-dl.html");
        			message =String.format(IOUtils.toString(is, "UTF-8"), unregisterLink);
        		}
        	}
            status = sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);	
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

    public static String sendCrashReport(String title, String body) {
        return sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, title, body, "text/plain", null, null) ;
    }

    public static void sendContactMessage(String fromA, String nick, String subject, String body) {
    	String message =  "\nFrom: ";
        if (StringUtils.isNotEmpty(nick)) {
        	message += nick + " "; 
        }
        message += fromA + "\n\n";
    	if (StringUtils.isNotEmpty(subject)) {
    		message += "Subject: " + subject + "\n\n";
    	}
    	message += body;
        sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "Contact form Message", message , "text/plain", null, null);
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
    
    public static int emailAccountExists(String address) {
    	if (StringUtils.isEmpty(address)) {
    		logger.log(Level.SEVERE, "Empty email address");
    		return 400;
    	} else {
    		try {
    			final String url =  com.jstakun.lm.server.config.ConfigurationManager.getBackendUrl() + "/validateEmail?to=" 
    					+ URLEncoder.encode(address, "UTF-8") + "&user_key=" + Commons.getProperty(Property.RH_LANDMARKS_API_KEY);
   	 			String response = HttpUtils.processFileRequest(new URL(url));
   	 			Integer responseCode = HttpUtils.getResponseCode(url);
   	 			if (responseCode != null && responseCode == 200 && StringUtils.startsWith(response, "{")) {
   	 				logger.log(Level.INFO, "Received response code: " + responseCode);
   	 				JSONObject root = new JSONObject(response);
   	 		    	if (StringUtils.equals(root.optString("status"), STATUS_OK)) {
   	 		    		return 200;
   	 		    	} else {
   	 		    		return 500;
   	 		    	}
   	 			} else {
   	 				logger.log(Level.SEVERE, "Received following response " + responseCode + ": " + response);
   	 				if (responseCode != null && responseCode >= 400) {
   	 					return responseCode;
   	 				} else {
   	 					return 500;
   	 				}
   	 			}
   	 		} catch (Exception e) {
   	 			logger.log(Level.SEVERE, e.getMessage(), e);
   	 			return 500;
   	 		}
    	}
    }
    
    /*private static String ordinalEn(int i) {
        final String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
        	case 11:
        	case 12:
        	case 13:
        		return i + "th";
        	default:
        		return i + sufixes[i % 10];
        }
    }*/
}
