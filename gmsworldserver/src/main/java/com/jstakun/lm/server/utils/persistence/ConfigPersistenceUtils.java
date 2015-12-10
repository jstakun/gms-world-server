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

   public static void persistConfig(String key, String value) {
        EntityManager pm = EMF.get().createEntityManager();

        try {
            pm.persist(new Config(key, value));
            pm.flush();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

   public static List<Config> selectAllConfigParams() {
	    EntityManager pm = EMF.get().createEntityManager();    
	    TypedQuery<Config> query = pm.createNamedQuery(Config.CONFIG_FINDALL, Config.class);
        return query.getResultList(); 
    }
}
