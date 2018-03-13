package com.jstakun.lm.server.utils.persistence;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.persistence.EMF;
import com.jstakun.lm.server.persistence.Notification;
import com.jstakun.lm.server.utils.MailUtils;

public class NotificationPersistenceUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationPersistenceUtils.class.getName());

	private static void persist(String id, Notification.Status status) {
		if (StringUtils.isNotEmpty(id)) {
			EntityManager pm = EMF.get().createEntityManager();
			try {
				Notification n = findById(id, pm);
				if (n == null) {
					n = new Notification(id, status);
				} else {
					n.setStatus(status);
					n.setLastUpdateDate(new Date());
				}
				pm.getTransaction().begin();
				pm.persist(n);
				pm.flush();
			} catch (Exception ex) {
				logger.log(Level.SEVERE, ex.getMessage(), ex);
				if (pm.getTransaction().isActive()) {
					pm.getTransaction().rollback();
				}
			} finally {
				if (pm.getTransaction().isActive()) {
					pm.getTransaction().commit();
				}
				pm.close();
			}
		}
    }
	
	private static boolean remove(String id) {
		boolean removed = false;
		if (StringUtils.isNotEmpty(id)) {
			EntityManager pm = EMF.get().createEntityManager();
			try {
				Notification n = findById(id, pm);
				if (n != null) {
					pm.getTransaction().begin();
					pm.remove(n);
				}
			} catch (Exception ex) {
				logger.log(Level.SEVERE, ex.getMessage(), ex);
			} finally {
				if (pm.getTransaction().isActive()) {
					removed = true;
					pm.getTransaction().commit();
				}
				pm.close();
			}
		}	
        return removed;
    }
	
	private static Notification findById(String id, EntityManager pm) {
		if (StringUtils.isNotEmpty(id)) {
			try {
				TypedQuery<Notification> query = pm.createNamedQuery(Notification.NOTIFICATION_FINDBYID, Notification.class);
				query.setParameter("id", id);
				return query.getSingleResult();
			} catch (Exception e) {
				return null; 
			}
		}
		return null;
	 }
	
	private static List<Notification> findByStatus(Notification.Status status) {
		EntityManager pm = EMF.get().createEntityManager();
		List<Notification> notifications = null;
		try {
        	TypedQuery<Notification> query = pm.createNamedQuery(Notification.NOTIFICATION_FINDALLWITHSTATUS, Notification.class);
        	query.setParameter("status", status);
        	notifications = query.getResultList();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
        	pm.close();
        }
		return notifications;
	}
	
	private static boolean isVerified(String id) {
		boolean verified = false;
		if (StringUtils.isNotEmpty(id)) {
			EntityManager pm = EMF.get().createEntityManager();
			try {
				Notification n = findById(id, pm);
				if (n != null && n .getStatus() == Notification.Status.VERIFIED) {
					verified = true;
				}
			} finally {
				pm.close();
			}
		}
		return verified;
	}
	
	/*public static void migrate() {
		  List<String> whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_TELEGRAM_WHITELIST)));
		  for (String telegramId : whitelistList) {
			    persist(telegramId, Notification.Status.VERIFIED);
		  }
		  whitelistList = new ArrayList<String>(Arrays.asList(ConfigurationManager.getArray(net.gmsworld.server.config.ConfigurationManager.DL_EMAIL_WHITELIST)));
		  for (String emailId : whitelistList) {
			    if  (emailId.contains(":")) {
			    	persist(emailId.split(":")[1], Notification.Status.UNVERIFIED);
			    } else {
			    	persist(emailId, Notification.Status.VERIFIED);
			    }
		  }	
	}*/
	
	//telegram
	
	public static boolean isWhitelistedTelegramId(String telegramId) {
		 return isVerified(telegramId);
	}

	public static void addToWhitelistTelegramId(String telegramId) {
		persist(telegramId, Notification.Status.VERIFIED);
	}
	
	public static boolean removeFromWhitelistTelegramId(String telegramId) {
		return remove(telegramId);
	}
	
	//email
	
	public static synchronized boolean isWhitelistedEmail(String email) {
		 return isVerified(email);
	}
	
	public static synchronized void addToWhitelistEmail(String email, boolean isRegistered) {
		 if (isRegistered) {
			 persist(email, Notification.Status.VERIFIED);
		 } else {
			persist(email, Notification.Status.UNVERIFIED);
		 }
	}
	
	public static void requestForConfirmation(ServletContext sc) {
		List<Notification> unverified = findByStatus(Notification.Status.UNVERIFIED);
		if (unverified != null && !unverified.isEmpty()) {
			  for (Notification n : unverified) {
				  String email = n.getId();
				  String status = MailUtils.sendDlVerificationRequest(email, email, sc, false);
				  logger.log(Level.INFO, "Sent registration request to: " + email + " with status: " + status);
			  }
		}
	}
}
