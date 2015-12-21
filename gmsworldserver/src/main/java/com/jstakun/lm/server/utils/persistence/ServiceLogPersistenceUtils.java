package com.jstakun.lm.server.utils.persistence;

import java.util.Date;

import javax.persistence.EntityManager;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.jstakun.lm.server.persistence.EMF;
import com.jstakun.lm.server.persistence.ServiceLog;

/**
 *
 * @author jstakun
 */
public class ServiceLogPersistenceUtils {

    private static final long FIVE_MINS = 1000 * 60 * 5;

    public static void persistServiceLog(String username, String serviceUri, boolean auth, int appId)
    {
    	EntityManager pm = EMF.get().createEntityManager();

        try {
            pm.persist(new ServiceLog(username, serviceUri, auth, appId));
            pm.flush();
        } finally {
            pm.close();
        }
    }

    public static long countServiceLogByDay(Date day) {
        //TODO must be implemented
    	//day in format dd-MM-yyyy
        long result = 0;
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(ServiceLog.class);
            Date nextDay = DateUtils.getNextDay(day);
            query.declareImports("import java.util.Date");
            query.setFilter("creationDate >= day && creationDate < nextDay");
            query.declareParameters("Date day, Date nextDay");
            query.setResult("count(this)");
            //Object o = query.execute(day, nextDay);
            result = ((Long) query.execute(day, nextDay)).longValue();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/

        return result;
    }

    public static long deleteLogsOlderThanDate(Date day) {
        int result = 0;
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("ServiceLog");
        //query.addFilter("creationDate", FilterOperator.LESS_THAN, day);
        query.setKeysOnly();
        Filter loginFilter =  new FilterPredicate("creationDate", FilterOperator.LESS_THAN, day);
        query.setFilter(loginFilter);
        final int chunk = 128;
        int count = 128;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        //5 mins limit
        while (count > 0 && (currentTime - startTime) < FIVE_MINS) {
            count = 0;
            for (Entity entity : ds.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunk))) {
                ds.delete(entity.getKey());
                count++;
            }
            result += count;
            currentTime = System.currentTimeMillis();
        }

        return result;
    }
}
