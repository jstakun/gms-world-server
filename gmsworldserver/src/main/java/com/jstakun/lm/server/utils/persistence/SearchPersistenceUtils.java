/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.utils.persistence;

import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.persistence.Search;
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
public class SearchPersistenceUtils {

    private static final Logger logger = Logger.getLogger(SearchPersistenceUtils.class.getName());

    public static void persistSearch(String username, String uri, boolean auth, double latitude, double longitude, int radius, String query, String locale, int count)
    {
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            pm.makePersistent(new Search(username, uri, auth, latitude, longitude, radius, query, locale, count));
        } finally {
            pm.close();
        }
    }

    public static List<Search> selectNewestSearches() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<Search> gcl = new ArrayList<Search>();

        try {
            int last = 10;
            if (last > 0) {
                Query cacheQuery = pm.newQuery(Search.class);
                cacheQuery.setRange(0, last);
                cacheQuery.setOrdering("creationDate desc");
                gcl = (List<Search>) cacheQuery.execute();
                //pm.retrieveAll(gcl);
                //gcl = (List<Search>) pm.detachCopyAll(gcl);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return gcl;
    }
}
