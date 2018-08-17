package com.jstakun.lm.server.utils.persistence;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.jstakun.lm.server.persistence.EMF;
import com.jstakun.lm.server.persistence.Notification;

public class NotificationPersistenceUtils {
	
	private static final Logger logger = Logger.getLogger(NotificationPersistenceUtils.class.getName());

	private static Notification persist(String id, Notification.Status status) {
		Notification n = null;
		if (StringUtils.isNotEmpty(id)) {
			EntityManager pm = EMF.get().createEntityManager();
			try {
				n = findById(id, pm);
				if (n == null) {
					n = new Notification(id, status);
					n.setSecret(RandomStringUtils.randomAlphabetic(32));
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
		return n;
    }
	
	public static boolean remove(String id) {
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
	
	private static Notification findBySecret(String secret, EntityManager pm) {
		if (StringUtils.isNotEmpty(secret)) {
			try {
				TypedQuery<Notification> query = pm.createNamedQuery(Notification.NOTIFICATION_FINDBYSECRET, Notification.class);
				query.setParameter("secret", secret);
				return query.getSingleResult();
			} catch (Exception e) {
				return null; 
			}
		}
		return null;
	}
	
	public static List<Notification> findByStatus(Notification.Status status) {
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

	//telegram
	
	public static boolean isWhitelistedTelegramId(String telegramId) {
		 return isVerified(telegramId);
	}

	public static void addToWhitelistTelegramId(String telegramId) {
		 persist(telegramId, Notification.Status.VERIFIED);
	}
	
    //email
	
	public static synchronized boolean isWhitelistedEmail(String email) {
		 return isVerified(email);
	}
	
	public static synchronized Notification addToWhitelistEmail(String email, boolean isRegistered) {
		Notification.Status status =  isRegistered ?  Notification.Status.VERIFIED : Notification.Status.UNVERIFIED;
		return persist(email, status);
	}
	
	public static synchronized Notification verifyWithSecret(String secret) {
		EntityManager pm = EMF.get().createEntityManager();
		Notification n = null;
		try {
			n = findBySecret(secret, pm);
			if (n != null) {
				if (n.getStatus() == Notification.Status.UNVERIFIED) {
					n.setStatus(Notification.Status.VERIFIED);
					pm.getTransaction().begin();
					pm.persist(n);
					pm.flush();
				}
			} 
		} catch (Exception e) {
		    if (pm.getTransaction().isActive()) {
				pm.getTransaction().rollback();
			}
		    n = null;
		} finally {
			if (pm.getTransaction().isActive()) {
				pm.getTransaction().commit();
			}
			pm.close();
		}
		return n;
	}
}
