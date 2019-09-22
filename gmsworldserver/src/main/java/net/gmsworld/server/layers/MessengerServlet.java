package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;

/**
 * Servlet implementation class MessengerServlet
 */
public final class MessengerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static final String PSID_PREFIX = "fb:"; 
	
	private static final Logger logger = Logger.getLogger(MessengerServlet.class.getName());
   
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		validateToken(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String content = IOUtils.toString(request.getReader());
		//logger.info("Received following data:\n" + content);		
		JSONObject root = new JSONObject(content);
		String object = root.getString("object");
		if (StringUtils.equals(object, "page")) {
			String psid = null, rcpt = null, text = null; 
			try {
				JSONObject entry = root.getJSONArray("entry").getJSONObject(0);
				JSONObject message = entry.getJSONArray("messaging").getJSONObject(0);
				psid = message.getJSONObject("sender").getString("id");
				rcpt = message.getJSONObject("recipient").getString("id");
				text = message.getJSONObject("message").getString("text");
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			if (StringUtils.equals(rcpt, Commons.getProperty(Property.DL_PAGE_ID)) && StringUtils.isNotEmpty(psid)) {
				MessengerUtils.sendMessage(psid, MessengerUtils.ACTION_MARK_SEEN, null);
				MessengerUtils.sendMessage(psid, MessengerUtils.ACTION_TYPING_ON, null);
				if (StringUtils.equalsIgnoreCase(text, "register") || StringUtils.equalsIgnoreCase(text, "/register")) {
					//register
					if (!NotificationPersistenceUtils.isVerified(PSID_PREFIX + psid)) {
						NotificationPersistenceUtils.setVerified(PSID_PREFIX + psid, true);
					} else {
						logger.log(Level.WARNING, "Messenger " + psid + " is already verified!");
					}		
					MessengerUtils.sendMessage(psid, null, "You've been registered to Device Locator notifications.\n"
							+ "You can unregister at any time by sending unregister message.");
				} else if (StringUtils.equalsIgnoreCase(text, "getmyid") || StringUtils.equalsIgnoreCase(text, "myid") ||  StringUtils.equalsIgnoreCase(text, "id") ||
						StringUtils.equalsIgnoreCase(text, "/getmyid") || StringUtils.equalsIgnoreCase(text, "/myid") ||  StringUtils.equalsIgnoreCase(text, "/id")) {
					//return psid
					MessengerUtils.sendMessage(psid, null, psid);
					MessengerUtils.sendMessage(psid, MessengerUtils.ACTION_TYPING_ON, null);
					MessengerUtils.sendMessage(psid, null, "Please long click on message above containing your psid, select Copy and come back to Device Locator.\n" 
																+ "Your psid should be pasted automatically otherwise please paste it to \"Facebook Messenger psid\" form field.");
				} else if (StringUtils.equalsIgnoreCase(text, "hello") || StringUtils.equalsIgnoreCase(text, "/hello")) {
					//hello
					MessengerUtils.sendMessage(psid, null, "Hello there!");
				} else if (StringUtils.equalsIgnoreCase(text, "unregister") || StringUtils.equalsIgnoreCase(text, "/unregister")) {
					//unregister
					if (NotificationPersistenceUtils.isVerified(PSID_PREFIX + psid)) {
						if (!NotificationPersistenceUtils.remove(PSID_PREFIX + psid)) {
							logger.log(Level.SEVERE, "Unable to remove Messenger psid " + psid + " from the whitelist!");
						}
					} else {
						logger.log(Level.WARNING, "Messenger psid " + psid + " doesn't exists in the whitelist!");
					}
					MessengerUtils.sendMessage(psid, null,  "You've been unregistered from Device Locator notifications.");
				} else if (StringUtils.equalsIgnoreCase(text, "help") || StringUtils.equalsIgnoreCase(text, "/help")) {
					InputStream is = null;
					try {
						is= getServletContext().getResourceAsStream("/WEB-INF/emails/bot-dl.txt");
						String helpMessage = String.format(IOUtils.toString(is, "UTF-8"));
						MessengerUtils.sendMessage(psid, null, helpMessage);
					} catch (IOException ex) {
			            logger.log(Level.SEVERE, ex.getMessage(), ex);
			        } finally {
			            if (is != null) {
			                try {
			                    is.close();
			                } catch (IOException ex) {
			                    logger.log(Level.SEVERE, null, ex);
			                }
			            }
			        }
			    } else if (DevicePersistenceUtils.isValidCommand(text)) {
					String reply = DevicePersistenceUtils.sendCommand(text, psid, "messenger");
					MessengerUtils.sendMessage(psid, null, reply);
				} else {
					MessengerUtils.sendMessage(psid, null, "Oops! I didn't understand your message.");
				}
			}
		}
	}
	
	private void validateToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String mode = request.getParameter("hub.mode");
		final String token = request.getParameter("hub.verify_token");
		final String challenge = request.getParameter("hub.challenge");
				    
		// Checks if a token and mode is in the query string of the request
		if (StringUtils.isNotEmpty(mode) && StringUtils.isNotEmpty(token)) {		  
			 // Checks the mode and token sent is correct
			 if (mode.equals("subscribe") && token.equals(Commons.getProperty(Property.DL_VERIFY_TOKEN))) {	      
				      // Responds with the challenge token from the request
				      response.getWriter().append(challenge);
				      response.setStatus(200);
			 } else {
				      // Responds with '403 Forbidden' if verify tokens do not match
				      response.setStatus(403);      
			 }
		}
	}
}
