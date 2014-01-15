/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.Filter;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import java.util.Iterator;
import com.beoui.geocell.model.GeocellQuery;
import java.util.Collection;
import com.google.common.collect.Collections2;
import com.google.common.base.Function;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.google.appengine.api.datastore.PreparedQuery;
import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.model.BoundingBox;
import com.beoui.geocell.model.Point;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.utils.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 *
 * @author jstakun
 */
public class LandmarkPersistenceUtils {

    private static final Logger logger = Logger.getLogger(LandmarkPersistenceUtils.class.getName());
    private static Date defaultValidityDate = null;
    
    static {
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:dd");
       String vs = "2200/01/01 00:00:00";
       try {
         defaultValidityDate = formatter.parse(vs);
       } catch (ParseException pe) {
    	   
       }
    }
    
    public static Landmark persistLandmark(String name, String description, double latitude, double longitude, double altitude, String username, Date validityDate, String layer, String email) {

        String key = null;
        Landmark landmark = null;
        Date vDate = validityDate;

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            if (vDate == null) {
                vDate = defaultValidityDate;
            }
            Point p = new Point(latitude, longitude);
            List<String> cells = GeocellManager.generateGeoCell(p);

            landmark = new Landmark(latitude, longitude, altitude, name, description, username, vDate, layer, cells, email);

            pm.makePersistent(landmark);

            key = landmark.getKeyString();
            
            landmark.setKeyString(key.toLowerCase());

            String hash = UrlUtils.getHash(ConfigurationManager.SERVER_URL + "showLandmark/" + key.toLowerCase());
            if (hash != null) {
                landmark.setHash(hash);
            }

            pm.makePersistent(landmark);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return landmark;
    }

    public static List<Landmark> selectAllLandmarks() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<Landmark> results = new ArrayList<Landmark>();

        try {
            Query query = pm.newQuery(Landmark.class);
            query.setOrdering("creationDate desc");
            query.setRange(0, 100);
            results = (List<Landmark>) query.execute();
            //pm.retrieveAll(results);
            //results = (List<Landmark>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }

    public static long deleteAllLandmarks() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query query = pm.newQuery(Landmark.class);
        long result = -1;

        try {
            result = query.deletePersistentAll();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;
    }

    public static void deleteLandmark(String k) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Landmark landmark = selectLandmark(k);

        try {
        	if (landmark != null) {
        		pm.deletePersistent(landmark);
        	} else {
        		logger.log(Level.SEVERE, "Can't delete landmark with key " + k);
        	}
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

    }

    public static Landmark selectLandmarkByHash(String hashparam) {
        Landmark landmark = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(Landmark.class, "hash == :hashparam");
            query.setUnique(true);
            //query.declareParameters("String hashparam");
            landmark = (Landmark) query.execute(hashparam);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return landmark;
    }

    public static Landmark selectLandmark(String k) {
        Landmark landmark = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
        	if (StringUtil.isAllLowerCaseAndDigit(k)) {
        		Query query = pm.newQuery(Landmark.class, "keyString == :k");
        		query.setUnique(true);
        		//query.declareParameters("String k");
        		landmark = (Landmark) query.execute(k);
        	} else {
                Key key = KeyFactory.stringToKey(k);
                landmark = pm.getObjectById(Landmark.class, key);
                if (landmark != null) {
                    landmark.setKeyString(k.toLowerCase());
                    pm.makePersistent(landmark);
                }
            }
        	if (landmark != null) {
        		landmark = pm.detachCopy(landmark);
        	}
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return landmark;
    }

    public static void updateLandmark(String k, Map<String, Object> update) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Landmark landmark = selectLandmark(k);

        try {
            landmark.setName((String) update.get("name"));
            landmark.setDecription((String) update.get("description"));
            double latitude = (Double) update.get("latitude");
            double longitude = (Double) update.get("longitude");
            landmark.setLatitude(latitude);
            landmark.setLongitude(longitude);
            landmark.setUsername((String) update.get("createdBy"));
            landmark.setValidityDate((Date) update.get("validityDate"));
            landmark.setLayer((String) update.get("layer"));
            //landmark.setCreationDate();
            Point p = new Point(latitude, longitude);
            // Generates the list of GeoCells
            List<String> cells = GeocellManager.generateGeoCell(p);
            landmark.setGeoCells(cells);

            pm.makePersistent(landmark);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
    }

    public static List<Landmark> selectLandmarksByCoordsAndLayer(double latitude, double longitude, String layer, int limit, int radius) {

        List<Landmark> results = new ArrayList<Landmark>();
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Point center = new Point(latitude, longitude);

        try {
            if (layer != null) {
            	List<Object> params = new ArrayList<Object>();
                params.add(layer);
                GeocellQuery baseQuery = new GeocellQuery("layer == layerIn", "String layerIn", params);
                results = GeocellManager.proximitySearch(center, limit, radius, Landmark.class, baseQuery, pm);
            } else {
                GeocellQuery baseQuery = new GeocellQuery();
                results = GeocellManager.proximitySearch(center, limit, radius, Landmark.class, baseQuery, pm);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }

    public static List<Landmark> selectLandmarksByCoordsAndLayer(double latitudeMin, double longitudeMin, double latitudeMax, double longitudeMax, String layer, int limit) {

        List<Landmark> results = new ArrayList<Landmark>();
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Date currentDate = new Date();

            BoundingBox bb = new BoundingBox(latitudeMax, longitudeMax, latitudeMin, longitudeMin);

            List<String> cells = GeocellManager.bestBboxSearchCells(bb, null);

            javax.jdo.Query queryCells = pm.newQuery(Landmark.class);
            queryCells.declareParameters("java.util.Collection geocellsParameter, String layerIn, java.util.Date currentDate");
            queryCells.setFilter("geocellsParameter.contains(geoCells) && layer == layerIn && validityDate > currentDate");
            queryCells.setRange(0, limit);

            results = (List<Landmark>) queryCells.execute(cells, layer, currentDate);

            //results = (List<Landmark>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }

    public static boolean isEmptyLandmarksByPointAndLayer(String layer, double latitude, double longitude, int radius) {

        boolean result = true;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Point center = new Point(latitude, longitude);
        List<Object> params = new ArrayList<Object>();
        params.add(layer);
        params.add(new Date());
        GeocellQuery baseQuery = new GeocellQuery("layer == layerIn && validityDate > currentDate", "String layerIn, java.util.Date currentDate", params);

        try {
        	logger.log(Level.INFO, "Searching landmarks in layer " + layer + " ...");
        	result = GeocellManager.proximityIsEmptySearch(center,radius, Landmark.class, baseQuery, pm);
        	//List<Landmark> landmarks = GeocellManager.proximitySearch(center, 1, radius, Landmark.class, baseQuery, pm);
            //if (result != landmarks.isEmpty()) {
            //	logger.log(Level.SEVERE, "Different results for layer " + layer + "!");
            //}
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;
    }

    public static boolean isEmptyLandmarksByCoordsAndLayer(List<String> cells, String layer) {

        boolean result = true;
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Date currentDate = new Date();
            javax.jdo.Query queryCells = pm.newQuery(Landmark.class);
            queryCells.declareParameters("java.util.Collection geocellsParameter, String layerIn, java.util.Date currentDate");
            queryCells.setFilter("geocellsParameter.contains(geoCells) && layer == layerIn && validityDate > currentDate");
            queryCells.setRange(0, 1);

            List<Landmark> results = (List<Landmark>) queryCells.execute(cells, layer, currentDate);

            result = results.isEmpty();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;
    }

    public static Landmark selectLandmarkMatchingQuery(String queryString) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Landmark landmark = null;

        try {
            Query query = pm.newQuery(Landmark.class);
            query.setOrdering("creationDate desc");
            query.setFilter("layer == 'Public'");
            List<Landmark> results = (List<Landmark>) query.execute();
            if (!results.isEmpty()) {
                for (Landmark l : results) {
                    if (l.getName().toLowerCase().contains(queryString)) {
                        landmark = l;
                        break;
                    } else if (l.getDescription() != null && l.getDescription().toLowerCase().contains(queryString)) {
                        landmark = l;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return landmark;
    }

    public static List<Landmark> selectLandmarksByUserAndLayer(String user, String layer, long first, long last) {

        List<Landmark> results = new ArrayList<Landmark>();
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {

            Query query = pm.newQuery(Landmark.class);
            if (layer != null && user != null) {
                query.setFilter("layer == layerIn && username == userIn");
            } else if (user != null) {
                query.setFilter("username == userIn");
            } else if (layer != null) {
                query.setFilter("layer == layerIn");
            }

            query.setOrdering("creationDate desc");
            query.setRange(first, last);

            if (layer != null && user != null) {
                query.declareParameters("String layerIn, String userIn");
                results = (List<Landmark>) query.execute(layer, user);
            } else if (user != null) {
                query.declareParameters("String userIn");
                results = (List<Landmark>) query.execute(user);
            } else if (layer != null) {
                query.declareParameters("String layerIn");
                results = (List<Landmark>) query.execute(layer);
            }

            //pm.retrieveAll(results);
            //results = (List<Landmark>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }

    public static List<Landmark> selectNewestLandmarks() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<Landmark> results = new ArrayList<Landmark>();

        try {
            String lastStr = ConfigurationManager.getParam(ConfigurationManager.NUM_OF_LANDMARKS, "5");
            int last = Integer.parseInt(lastStr);
            if (last > 0) {
                Query query = pm.newQuery(Landmark.class);
                query.setOrdering("creationDate desc");
                query.setRange(0, last);
                results = (List<Landmark>) query.execute();
                pm.retrieveAll(results);
                results = (List<Landmark>) pm.detachCopyAll(results);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
        return results;
    }

    public static List<Landmark> selectByLandmarksMonth(long first, long last, String month) {

        List<Landmark> results = new ArrayList<Landmark>();
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(Landmark.class);
            query.setOrdering("creationDate desc");
            query.setRange(first, last);

            Date monthFirstDay = DateUtils.getFirstDayOfMonth(month);
            Date nextMonthFirstDay = DateUtils.getFirstDayOfNextMonth(month);
            query.declareImports("import java.util.Date");
            query.setFilter("creationDate >= monthFirstDay && creationDate < nextMonthFirstDay");
            query.declareParameters("Date monthFirstDay, Date nextMonthFirstDay");
            results = (List<Landmark>) query.execute(monthFirstDay, nextMonthFirstDay);
            //results = (List<Landmark>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }

    public static Map<String, Integer> getHeatMap(int nDays) {
        Map<String, Integer> bucket = new HashMap<String, Integer>();

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Date nDaysAgo = DateUtils.getDayInPast(nDays, true);
            Query query = pm.newQuery(Landmark.class);
            query.getFetchPlan().setFetchSize(512);
            query.setOrdering("creationDate desc");
            query.declareImports("import java.util.Date");
            query.setFilter("creationDate >= monthFirstDay");
            query.declareParameters("Date monthFirstDay");
            List<Landmark> results = (List<Landmark>) query.execute(nDaysAgo);
            for (Iterator<Landmark> iter = results.iterator(); iter.hasNext();) {
                Landmark l = iter.next();
                String key = StringUtil.formatCoordE2(l.getLatitude()) + "_" + StringUtil.formatCoordE2(l.getLongitude());
                Integer currentValue = bucket.remove(key);
                if (currentValue != null) {
                    currentValue++;
                } else {
                    currentValue = 1;
                }
                bucket.put(key, currentValue);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        if (!bucket.isEmpty()) {
            try {
                String cacheKey = DateUtils.getDay(new Date()) + "_" + nDays + "_heatMap";
                CacheUtil.put(cacheKey, bucket);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return bucket;
    }

    /*public static int selectByLandmarksMonthCount(String month) {
    int result = 0;
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
    Query query = pm.newQuery(Landmark.class);
    Date monthFirstDay = DateUtils.getFirstDayOfMonth(month);
    Date nextMonthFirstDay = DateUtils.getFirstDayOfNextMonth(month);
    query.declareImports("import java.util.Date");
    query.setFilter("creationDate >= monthFirstDay && creationDate < nextMonthFirstDay");
    query.declareParameters("Date monthFirstDay, Date nextMonthFirstDay");
    query.setResult("count(this)");
    result = ((Integer) query.execute(monthFirstDay, nextMonthFirstDay)).intValue();
    } catch (Exception ex) {
    logger.log(Level.SEVERE, ex.getMessage(), ex);
    } finally {
    pm.close();
    }

    return result;
    }*/
    public static int selectByLandmarksMonthCount(String month) {
        int result = 0;

        try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("Landmark");
            Date monthFirstDay = DateUtils.getFirstDayOfMonth(month);
            Date nextMonthFirstDay = DateUtils.getFirstDayOfNextMonth(month);
            query.setKeysOnly();

            Filter dateMinFilter = new FilterPredicate("creationDate", FilterOperator.GREATER_THAN_OR_EQUAL, monthFirstDay);
            Filter dateMaxFilter = new FilterPredicate("creationDate", FilterOperator.LESS_THAN, nextMonthFirstDay);
            Filter dateRangeFilter = CompositeFilterOperator.and(dateMinFilter, dateMaxFilter);
            query.setFilter(dateRangeFilter);
            //query.addFilter("creationDate", FilterOperator.GREATER_THAN_OR_EQUAL, monthFirstDay);
            //query.addFilter("creationDate", FilterOperator.LESS_THAN, nextMonthFirstDay);

            PreparedQuery pq = ds.prepare(query);
            FetchOptions option = FetchOptions.Builder.withLimit(Integer.MAX_VALUE);
            result = pq.countEntities(option);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return result;
    }

    public static int selectLandmarksByUserAndLayerCount(String user, String layer) {
        int result = 0;

        try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("Landmark");
            query.setKeysOnly();
            Filter usernameFilter = null;
            if (user != null) {
                //query.addFilter("username", FilterOperator.EQUAL, user);
                usernameFilter = new FilterPredicate("username", FilterOperator.EQUAL, user);
            }
            if (layer != null) {
                //query.addFilter("layer", FilterOperator.EQUAL, layer);
                Filter layerFilter = new FilterPredicate("layer", FilterOperator.EQUAL, layer);
                if (usernameFilter != null) {
                    Filter compFilter = CompositeFilterOperator.and(layerFilter, usernameFilter);
                    query.setFilter(compFilter);
                } else {
                    query.setFilter(layerFilter);
                }
            } else if (usernameFilter != null) {
                query.setFilter(usernameFilter);
            }
            PreparedQuery pq = ds.prepare(query);
            FetchOptions option = FetchOptions.Builder.withLimit(Integer.MAX_VALUE);
            result = pq.countEntities(option);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return result;
    }

    public static int updateAllLandmarksWithGeoCells() {
        int result = 0;
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("Landmark");
        query.addSort("creationDate", com.google.appengine.api.datastore.Query.SortDirection.DESCENDING);
        final int chunk = 128;
        int count = 128;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long interval = 0;

        while (count > 0 && interval < Commons.FIVE_MINS) {
            count = 0;
            for (Entity entity : ds.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunk).offset(result))) {

                Object geoCells = entity.getProperty("geoCells");
                if (geoCells == null) {
                    double latitude = (Double) entity.getProperty("latitude");
                    double longitude = (Double) entity.getProperty("longitude");

                    Point p = new Point(latitude, longitude);
                    // Generates the list of GeoCells
                    List<String> cells = GeocellManager.generateGeoCell(p);
                    entity.setProperty("geoCells", cells);

                    ds.put(entity);
                }
                count++;
            }

            result += count;
            currentTime = System.currentTimeMillis();
            interval = (currentTime - startTime);
            logger.log(Level.INFO, "Processed {0} records in {1}ms.", new Object[]{result, interval});
        }

        return result;
    }

    /*public static String getLandmarkHash(String k) {
    Landmark landmark = selectLandmark(k);
    if (landmark != null) {
    return landmark.getHash();
    }
    return null;
    }*/
    public static Map<String, Collection<String>> filterLandmarks(String property, String[] pattern, String resultProperty, boolean unique, long createdBeforeMillis, Map<String, Integer> beforeCreated, Date afterDay) {

        Map<String, List<String>> filteredLandmarks = new HashMap<String, List<String>>();
        for (int i = 0; i < pattern.length; i++) {
            List<String> landmarks = new ArrayList<String>();
            filteredLandmarks.put(pattern[i], landmarks);
        }

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("Landmark");
        query.addSort("creationDate", com.google.appengine.api.datastore.Query.SortDirection.DESCENDING);

        if (afterDay != null) {
            Filter dateFilter = new FilterPredicate("creationDate", FilterOperator.GREATER_THAN_OR_EQUAL, afterDay);
            query.setFilter(dateFilter);
        }

        final int chunk = 128;
        int count = 128;
        int result = 0;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();

        while (count > 0 && (currentTime - startTime) < Commons.FIVE_MINS) {
            count = 0;
            for (Entity entity : ds.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunk).offset(result))) {
                String value = (String) entity.getProperty(property);
                for (int i = 0; i < pattern.length; i++) {
                    if (StringUtils.contains(value, pattern[i])) {
                        String resultValue = (String) entity.getProperty(resultProperty);
                        List<String> landmarks = filteredLandmarks.get(pattern[i]);
                        if (!unique || !landmarks.contains(resultValue)) {
                            landmarks.add(resultValue);
                            Date creationDate = (Date) entity.getProperty("creationDate");
                            if (creationDate.getTime() > createdBeforeMillis) {
                                beforeCreated.put(pattern[i], beforeCreated.get(pattern[i]) + 1);
                            }
                        }
                        break;
                    }
                }
                count++;
            }
            currentTime = System.currentTimeMillis();
            result += count;
        }

        Map<String, Collection<String>> transformedLandmarks = new HashMap<String, Collection<String>>();
        UsernameTransformer transformer = new UsernameTransformer(pattern);

        for (Map.Entry<String, List<String>> entry : filteredLandmarks.entrySet()) {
            transformedLandmarks.put(entry.getKey(), Collections2.transform(entry.getValue(), transformer));
        }

        return transformedLandmarks;
    }

    private static class UsernameTransformer implements Function<String, String> {

        private final String[] patterns;

        public UsernameTransformer(String[] patterns) {
            this.patterns = patterns;
        }

        @Override
        public String apply(String f) {
            if (f == null) {
                return null;
            } else if (f.length() > 3 && StringUtils.endsWithAny(f, patterns)) {
                //return f.substring(0, f.length() - 3);
                //return f.replace("@fb", "");
                //return UrlUtils.createUserProfileUrl(f);
                return ConfigurationManager.SERVER_URL + "socialProfile?uid=" + f;
            } else {
                return f;
            }
        }
    }
}
