package com.jstakun.lm.server.utils.persistence;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.jstakun.lm.server.persistence.Config;
import com.jstakun.lm.server.persistence.EMF;

/**
 *
 * @author jstakun
 */
public class ConfigPersistenceUtils {

   private static final Logger logger = Logger.getLogger(ConfigPersistenceUtils.class.getName());

   public static void persist(String key, String value) {
        EntityManager pm = EMF.get().createEntityManager();
        try {
        	Config c = findByKey(key, pm);
        	if (c == null) {
            	c = new Config(key, value);
            } else {
            	c.setValue(value);
            }
        	pm.getTransaction().begin();
            pm.persist(c);
            pm.flush();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            pm.getTransaction().rollback();
        } finally {
            pm.getTransaction().commit();
        	pm.close();
        }
    }

   public static List<Config> selectAllConfigParams() {
	    EntityManager pm = EMF.get().createEntityManager();    
	    TypedQuery<Config> query = pm.createNamedQuery(Config.CONFIG_FINDALL, Config.class);
        return query.getResultList(); 
    }
   
   private static Config findByKey(String key, EntityManager pm) {
	   TypedQuery<Config> query = pm.createNamedQuery(Config.CONFIG_FINDBYKEY, Config.class);
	   query.setParameter("key", key);
	   return query.getSingleResult();
   }
}
