/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.GeocodeCache;
import com.jstakun.lm.server.persistence.PMF;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class GeocodeCachePersistenceUtils {

    private static final Logger logger = Logger.getLogger(GeocodeCachePersistenceUtils.class.getName());

    public static void persistGeocode(String location, int status, String message, double latitude, double longitude) {
        String loc = StringUtils.replace(location, "\n", " ");
        
        GeocodeCache gc = new GeocodeCache(loc, status, message, latitude, longitude);

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(gc);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static GeocodeCache checkIfGeocodeExists(String address) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        GeocodeCache gc = null;

        try {
            Query cacheQuery = pm.newQuery(GeocodeCache.class);
            cacheQuery.setFilter("location == address && status == 0");
            cacheQuery.declareParameters("String address");
            List<GeocodeCache> gcl = (List<GeocodeCache>) cacheQuery.execute(address);

            if (!gcl.isEmpty()) {
                gc = gcl.get(0);
                gc.setCreationDate(new Date(System.currentTimeMillis()));
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return gc;
    }

    public static List<GeocodeCache> selectNewestGeocodes() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<GeocodeCache> gcl = new ArrayList<GeocodeCache>();

        try {
            String lastStr = ConfigurationManager.getParam(ConfigurationManager.NUM_OF_GEOCODES, "5");
            int last = Integer.parseInt(lastStr);
            if (last > 0) {
                Query cacheQuery = pm.newQuery(GeocodeCache.class);
                cacheQuery.setFilter("status == 0");
                cacheQuery.setRange(0, last);
                cacheQuery.setOrdering("creationDate desc");
                gcl = (List<GeocodeCache>) cacheQuery.execute();
                pm.retrieveAll(gcl);
                gcl = (List<GeocodeCache>) pm.detachCopyAll(gcl);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return gcl;
    }

    public static GeocodeCache selectGeocodeCache(String k) {
        GeocodeCache geocodeCache = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Key key = KeyFactory.stringToKey(k);
            geocodeCache = pm.getObjectById(GeocodeCache.class, key);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return geocodeCache;
    }
}
