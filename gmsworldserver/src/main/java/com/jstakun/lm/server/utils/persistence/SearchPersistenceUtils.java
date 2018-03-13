package com.jstakun.lm.server.utils.persistence;

import javax.persistence.EntityManager;

import com.jstakun.lm.server.persistence.EMF;
import com.jstakun.lm.server.persistence.Search;

/**
 *
 * @author jstakun
 */
public class SearchPersistenceUtils {

    public static void persist(String username, String uri, boolean auth, double latitude, double longitude, int radius, String query, String locale, int count)
    {
    	EntityManager pm = EMF.get().createEntityManager();

        try {
            pm.persist(new Search(username, uri, auth, latitude, longitude, radius, query, locale, count));
            pm.flush();
        } finally {
            pm.close();
        }
    }
}
