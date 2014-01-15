/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import com.jstakun.lm.server.persistence.Checkin;
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
public class CheckinPersistenceUtils {

    private static final Logger logger = Logger.getLogger(CheckinPersistenceUtils.class.getName());

    public static void persistCheckin(String username, String landmarkKey, Integer type) {
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(new Checkin(username, landmarkKey, type));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static List<Checkin> selectAllLandmarkCheckins(String key) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<Checkin> results = new ArrayList<Checkin>();
        try {
            Query query = pm.newQuery(Checkin.class);
            query.setFilter("landmarkKey == key");
            query.setOrdering("creationDate desc");
            query.setRange(0, 100);
            query.declareParameters("String key");
            results = (List<Checkin>) query.execute(key);
            //pm.retrieveAll(results);
            //results = (List<Checkin>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }
}
