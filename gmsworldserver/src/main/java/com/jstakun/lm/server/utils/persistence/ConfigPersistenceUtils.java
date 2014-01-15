/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.utils.persistence;

import com.jstakun.lm.server.persistence.Config;
import com.jstakun.lm.server.persistence.PMF;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 *
 * @author jstakun
 */
public class ConfigPersistenceUtils {

   private static final Logger logger = Logger.getLogger(ConfigPersistenceUtils.class.getName());

   public static void persistConfig(String key, String value) {
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(new Config(key, value));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

   public static List<Config> selectAllConfigParams() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<Config> results = new ArrayList<Config>();

        try {
            Query query = pm.newQuery(Config.class);
            query.setRange(0, 100);
            results = (List<Config>) query.execute();
            //pm.retrieveAll(results);
            //results = (List<Config>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }
}
