package com.jstakun.lm.server.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
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
    	String recipients = addEmailAddress("to", toA, toP); 
    	if (StringUtils.isNotEmpty(ccA)) {
    		recipients += "|" + addEmailAddress("to", ccA, ccP);
    	}
   	 
    	if  (sendRemoteMail(fromA, fromP, recipients, subject, content, contentType)) {
    		return "ok";
    	} else {	
    		return sendLocalMail(fromA, fromP, toA, toP, subject, content, contentType, ccA, ccP);
   		} 
    }
       
    private static boolean sendRemoteMail(String fromA, String fromP, String recipients, String subject, String content, String contentType)  {
    	 final String MAILER_SERVER_URL = "https://openapi-landmarks.b9ad.pro-us-east-1.openshiftapps.com/actions/emailer"; 
    	 
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
    	 
    	 boolean success = false;
    	 try {
    		 HttpUtils.processFileRequestWithBasicAuthn(new URL(MAILER_SERVER_URL), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
    		 Integer responseCode = HttpUtils.getResponseCode(MAILER_SERVER_URL);
    		 logger.log(Level.INFO, "Received response code: " + responseCode);
    		 if (responseCode != null && responseCode == 200) {
    			 success = true;
    		 } 
    	 } catch (Exception e) {
    		 logger.log(Level.SEVERE, e.getMessage(), e);
    	 }
    	 return success;
    }
    
    public static void sendAdminMail(String message) {
    	sendAdminMail("Admin message", message) ;
    }
    
    public static void sendAdminMail(String title, String message) {
    	sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, "jstakun.appspot@gmail.com", ConfigurationManager.ADMIN_NICK, title, message, "text/plain", null, null);
    }

    public static void sendEmailingMessage(String toA, String nick, String message) {
        sendRemoteMail(ConfigurationManager.LM_MAIL, ConfigurationManager.LM_NICK, toA, nick, "Message from Landmark Manager", message, "text/html", null, null);
    }
    
    public static void sendDeviceLocatorMessage(String toA, String message, String title) {
    	//sendMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, toA, title, message, "text/plain");
    	sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, toA, title, message, "text/plain", null, null);
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

    public static void sendVerificationRequest(String toA, String nick, String secret, ServletContext context) {
        InputStream is = null;
        try {
            String link = ConfigurationManager.SSL_SERVER_URL + "verifyUser/" + secret;
            is = context.getResourceAsStream("/WEB-INF/emails/verification.html");
            String message = String.format(IOUtils.toString(is, "UTF-8"), nick, link);
            String result = sendLocalMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "Welcome to GMS World", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
       		if (!StringUtils.equalsIgnoreCase(result, "ok")) {
       			sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "Welcome to GMS World", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
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
    
    public static String sendDeviceLocatorVerificationRequest(String toA, String nick, String secret, ServletContext context, int version) {
        InputStream is = null;
        String result = null; 
        try {
        	String message = null;
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
            } else {
            	message = link;
            }
            result = sendLocalMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);
       		logger.log(Level.INFO, "Email verification request status: " + result);
            if (!StringUtils.equalsIgnoreCase(result, "ok")) {
       			String recipients = addEmailAddress("bcc", "jstakun.appspot@gmail.com", null) + "|" + addEmailAddress("to", toA, nick);
       			if  (sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, recipients, "Device Locator Registration", message, "text/html")) {
       				result = "ok";
       			} else {	
       				result = "failed";
       			}
       		}	
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
            String result = sendLocalMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Registration", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
       		if (!StringUtils.equalsIgnoreCase(result, "ok")) {
       			sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, toA, nick, "GMS World Registration", message, "text/html",  "landmark-manager@gms-world.net", ConfigurationManager.ADMIN_NICK);
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
            status = sendLocalMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK);
        	if (!StringUtils.equalsIgnoreCase(status, "ok")) {
        		status = sendRemoteMail(ConfigurationManager.DL_MAIL, ConfigurationManager.DL_NICK, toA, nick, "Device Locator Registration", message, "text/html",  "jstakun.appspot@gmail.com", ConfigurationManager.DL_NICK);	
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
        String status = sendLocalMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, title, body, "text/plain", null, null);
        if (!StringUtils.equalsIgnoreCase(status, "ok")) {
        	String recipients = addEmailAddress("to", "jstakun.appspot@gmail.com", null) ;
            sendRemoteMail(ConfigurationManager.SUPPORT_MAIL, ConfigurationManager.ADMIN_NICK, recipients, title, body, "text/plain");
        }
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
    
    private static String addEmailAddress(String type, String email, String nick) {
    	  String emailAddress = "";
    	  if ((StringUtils.equals(type, "to") || StringUtils.equals(type, "cc") || StringUtils.equals(type, "bcc")) && StringUtils.isNotEmpty(email)) {
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
    
    private static int hear( BufferedReader in ) throws IOException {
          String line = null;
          int res = 0;
          while ( (line = in.readLine()) != null ) {
        	  String pfx = line.substring( 0, 3 );
        	  try {
        		  res = Integer.parseInt( pfx );
        	  } 
        	  catch (Exception ex) {
        		  res = -1;
        	  }
        	  if ( line.charAt( 3 ) != '-' ) break;
          }
          return res;
      }
    
      private static void say( BufferedWriter wr, String text )  throws IOException {
    	  wr.write( text + "\r\n" );
    	  wr.flush();
    	  return;
      }
      
      private static ArrayList<String> getMX( String hostName ) throws NamingException {
    	  // Perform a DNS lookup for MX records in the domain
    	  Hashtable<String, String> env = new Hashtable<String, String>();
    	  env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
    	  DirContext ictx = new InitialDirContext( env );
    	  Attributes attrs = ictx.getAttributes( hostName, new String[] { "MX" });
    	  Attribute attr = attrs.get( "MX" );
    	  
    	  // if we don't have an MX record, try the machine itself
    	  if (( attr == null ) || ( attr.size() == 0 )) {
    		  attrs = ictx.getAttributes( hostName, new String[] { "A" });
    		  attr = attrs.get( "A" );
    		  if ( attr == null ) {
    			  throw new NamingException( "No match for name '" + hostName + "'" );
    		  }
    	  }
    	  
    	  ArrayList<String> res = new ArrayList<String>();
    	  NamingEnumeration en = attr.getAll();
    	  while ( en.hasMore() ) {
    		  	String x = (String) en.next();
    		  	String f[] = x.split( " " );
    		  	if ( f[1].endsWith( "." )) { 
    		  		f[1] = f[1].substring( 0, (f[1].length() - 1));
    		  	}
    		  	res.add( f[1] );
    	  }
    	  return res;
      }
      
      public static boolean emailAccountExists( String address ) {
    	  // Find the separator for the domain name
    	  int pos = address.indexOf( '@' );
    	  // If the address does not contain an '@', it's not valid
    	  if ( pos == -1 ) return false;
    	  // Isolate the domain/machine name and get a list of mail exchangers
    	  String domain = address.substring( ++pos );
    	  ArrayList<String> mxList = null;
    	  try {
    		  mxList = getMX( domain );
    	  } 
    	  catch (NamingException ex) {
    		  return false;
    	  }
    	  
    	  if ( mxList.size() == 0 ) return false;
        
    	  for ( int mx = 0 ; mx < mxList.size() ; mx++ ) {
    		  boolean valid = false;
    		  Socket skt = null;
    		  BufferedReader rdr = null;
    		  BufferedWriter wtr = null;
    		  try {
    			  int res;
    			  skt = new Socket( (String) mxList.get( mx ), 25 );
    			  rdr = new BufferedReader( new InputStreamReader( skt.getInputStream() ) );
    			  wtr = new BufferedWriter( new OutputStreamWriter( skt.getOutputStream() ) );
    			  res = hear( rdr );
    			  if ( res != 220 ) throw new Exception( "Invalid header" );
    			  say( wtr, "EHLO gms-world.net");
    			  res = hear( rdr );
    			  if ( res != 250 ) throw new Exception( "Not ESMTP" );
    			  // validate the sender address  
    			  say( wtr, "MAIL FROM: <" + ConfigurationManager.DL_MAIL + ">" );
    			  res = hear( rdr );
    			  if ( res != 250 ) throw new Exception( "Sender rejected" );
    			  say( wtr, "RCPT TO: <" + address + ">" );
    			  res = hear( rdr );
    			  // be polite
    			  say( wtr, "RSET" ); hear( rdr );
    			  say( wtr, "QUIT" ); hear( rdr );
    			  if ( res != 250 && res != 451) {
    				  throw new Exception("Received following SMTP server response: " + res);
    			  };
    			  valid = true;
    		  } catch (Exception ex) {
    			  logger.log(Level.SEVERE, ex.getMessage(), ex);
    		  } 
    		  finally {
    			  if (rdr != null) {
    				  try {
    					  rdr.close();
    				  } catch (Exception e) {}
    			  }
    			  if (wtr != null) {
    				  try { 
    					  wtr.close();
    				  } catch (Exception e) {}
    			  }
    			  if (skt != null)  {
    				  try {
    					  skt.close();
    				  } catch (Exception e) {}
    			  }
    			  if ( valid ) return true;
    			  
    		  }
    	  }
    	  return false;
      }
}
