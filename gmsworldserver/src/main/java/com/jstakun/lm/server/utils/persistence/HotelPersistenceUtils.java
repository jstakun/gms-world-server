/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.utils.persistence;

import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.model.BoundingBox;
import com.beoui.geocell.model.GeocellQuery;
import com.beoui.geocell.model.Point;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.jstakun.lm.server.persistence.Hotel;
import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.utils.xml.HotelParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class HotelPersistenceUtils {

    private static final Logger logger = Logger.getLogger(LandmarkPersistenceUtils.class.getName());

    public static int persistHotel(Map<String, String> hotelMap) {
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Hotel hotel = new Hotel();
            BeanUtils.populate(hotel, hotelMap);
            setGeoCells(hotel);
            hotel.setLastUpdateDate(new Date(System.currentTimeMillis()));
            pm.makePersistent(hotel);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to load hotel {0}", hotelMap.get("hotelId"));
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return HotelParser.CREATE;
    }

    public static int updateHotel(Map<String, String> hotelMap) {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        int response = HotelParser.UPDATE;

        try {
            if (hotelMap.containsKey("hotelId")) {
                String hotelId = hotelMap.get("hotelId");
                if (StringUtils.isNumeric(hotelId)) {
                    Hotel h = selectHotelById(Integer.parseInt(hotelId), pm);
                    if (h != null) {
                        Map<String, String> src = BeanUtils.describe(h);
                        if (!equals(hotelMap, src)) {
                            BeanUtils.populate(h, hotelMap);
                            if (!equalsDecimal(hotelMap.get("latitude"), src.get("latitude")) ||
                                    !equalsDecimal(hotelMap.get("longitude"), src.get("longitude"))) {
                                setGeoCells(h);
                            }
                            //h.setLastUpdateDate(new Date(System.currentTimeMillis()));
                            //pm.makePersistent(h);
                        } //else {
                        //    response = HotelParser.NONE;
                        //}
                        h.setLastUpdateDate(new Date(System.currentTimeMillis()));
                        pm.makePersistent(h);
                    } else {
                        response = persistHotel(hotelMap);
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }
        return response;
    }

    private static Hotel selectHotelById(int hotelId, PersistenceManager pm) {
        Hotel hotel = null;
        try {
            javax.jdo.Query query = pm.newQuery(Hotel.class);
            query.declareParameters("Integer hotelIdParam");
            query.setFilter("hotelId == hotelIdParam");
            query.setUnique(true);
            hotel = (Hotel) query.execute(hotelId);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to update hotel {0}", hotelId);
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return hotel;
    }

    public static int deleteAllHotels() {
        int result = 0;
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Hotel");
        //query.addSort("hotelId", Query.SortDirection.DESCENDING);
        query.setKeysOnly();
        final int chunk = 128;
        int count = 128;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();

        while (count > 0 && ((currentTime - startTime) < (1000 * 60 * 5))) {
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

    public static List<Hotel> selectHotelsByCoordsAndLayer(double latitudeMin, double longitudeMin, double latitudeMax, double longitudeMax, int limit) {

        List<Hotel> results = new ArrayList<Hotel>();
        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {

            BoundingBox bb = new BoundingBox(latitudeMax, longitudeMax, latitudeMin, longitudeMin);

            List<String> cells = GeocellManager.bestBboxSearchCells(bb, null);

            javax.jdo.Query queryCells = pm.newQuery(Hotel.class);
            queryCells.declareParameters("java.util.Collection geocellsParameter");
            queryCells.setFilter("geocellsParameter.contains(GeoCells)");
            queryCells.setOrdering("rating desc");
            queryCells.setRange(0, limit);

            results = (List<Hotel>) queryCells.execute(cells);
            //results = (List<Hotel>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }

    public static List<Hotel> selectHotelsByPointAndRadius(double latitude, double longitude, int radius, int limit) {
        List<Hotel> results = new ArrayList<Hotel>();
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Point center = new Point(latitude, longitude);
        List<Object> params = new ArrayList<Object>();
        GeocellQuery baseQuery = new GeocellQuery("","",params);

        try {
            results = GeocellManager.proximitySearch(center, limit, radius, Hotel.class, baseQuery, pm);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return results;
    }



    public static int updateAllHotelsWithGeoCells(int mins) {
        int result = 0;
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Hotel");
        query.addSort("hotelId", Query.SortDirection.ASCENDING);

        final int chunk = 128;
        int count = 128;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        long interval = 0;
        long deadline = (1000 * 60 * mins);

        while (count > 0 && (interval < deadline)) {
            count = 0;
            for (Entity entity : ds.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunk).offset(result))) {

                Object geoCells = entity.getProperty("GeoCells");
                if (geoCells == null) {

                    double latitude = (Double) entity.getProperty("Latitude");
                    double longitude = (Double) entity.getProperty("Longitude");

                    Point p = new Point(latitude, longitude);
                    // Generates the list of GeoCells
                    List<String> cells = GeocellManager.generateGeoCell(p);
                    entity.setProperty("GeoCells", cells);

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

    public static long deleteHotelsOlderThanDate(Date day) {
        int result = 0;
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Hotel");
        //query.addFilter("lastUpdateDate", FilterOperator.LESS_THAN, day);
        query.setKeysOnly();
        Filter lastUpdateDateFilter =  new FilterPredicate("lastUpdateDate", FilterOperator.LESS_THAN, day);
        query.setFilter(lastUpdateDateFilter);

        final int chunk = 128;
        int count = 128;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        //5 mins limit
        while (count > 0 && ((currentTime - startTime) < (1000 * 60 * 5))) {
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

    private static boolean equals(Map<String, String> dest, Map<String, String> src) {
        boolean equals = true;

        for (Iterator<String> i = src.keySet().iterator(); i.hasNext();) {
            String key = i.next();
            if (dest.containsKey(key)) {
                String srcVal = src.get(key);
                String destVal = dest.get(key);
                if (!StringUtils.equals(srcVal, destVal) && !equalsDecimal(destVal, srcVal)) {
                    //logger.log(Level.INFO, "Property {0}, new: {1}, old: {2}", new Object[]{key, srcVal, destVal});
                    return false;
                }
            }
        }

        return equals;
    }

    private static boolean equalsDecimal(String dest, String src) {
        if (StringUtils.isEmpty(src) || StringUtils.isEmpty(dest)) {
            return false;
        }
        if (src.indexOf(".") > 0) {
            try {
                double srcVal = Double.parseDouble(src);
                double destVal = Double.parseDouble(dest);
                if (srcVal == destVal) {
                    return true;
                }
            } catch (NumberFormatException nme) {
            }
        }
        if (StringUtils.isNumeric(src)) {
            try {
                int srcVal = Integer.parseInt(src);
                int destVal = Integer.parseInt(dest);
                if (srcVal == destVal) {
                    return true;
                }
            } catch (NumberFormatException nme) {
            }
        }

        return false;
    }

    private static void setGeoCells(Hotel hotel) {
        Point p = new Point(hotel.getLatitude(), hotel.getLongitude());
            // Generates the list of GeoCells
        List<String> cells = GeocellManager.generateGeoCell(p);
        hotel.setGeoCells(cells);
    }
}
