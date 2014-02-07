/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jstakun.lm.server.utils.persistence;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.persistence.PMF;
import com.jstakun.lm.server.persistence.Screenshot;
import com.jstakun.lm.server.utils.StringUtil;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 *
 * @author jstakun
 */
public class ScreenshotPersistenceUtils {

    private static final Logger logger = Logger.getLogger(ScreenshotPersistenceUtils.class.getName());

    public static String persistScreenshot(String username, boolean auth, double latitude, double longitude, BlobKey blobKey, Date creationDate)
    {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        String key = null;

        try {
            Screenshot s = new Screenshot(username, auth, latitude, longitude, blobKey, creationDate);
            pm.makePersistent(s);
            key = KeyFactory.keyToString(s.getKey());
            s.setKeyString(key.toLowerCase());
            pm.makePersistent(s);
        } finally {
            pm.close();
        }

        return key;
    }

    public static long deleteScreenshotsOlderThanDate(Date day) {
        int result = 0;
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("Screenshot");
        //query.setKeysOnly();
        Filter loginFilter =  new FilterPredicate("creationDate", FilterOperator.LESS_THAN, day);
        query.setFilter(loginFilter);
        final int chunk = 128;
        int count = 128;
        long startTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        //5 mins limit
        while (count > 0 && (currentTime - startTime) < Commons.FIVE_MINS) {
            count = 0;
            for (Entity entity : ds.prepare(query).asIterable(FetchOptions.Builder.withLimit(chunk))) {

                BlobKey blobKey = (BlobKey)entity.getProperty("blobKey");

                //delete blob with blobkey
                blobstoreService.delete(blobKey);

                ds.delete(entity.getKey());
                count++;
            }
            result += count;
            currentTime = System.currentTimeMillis();
        }

        return result;
    }

    public static Screenshot selectScreenshot(String k) {
        Screenshot s = null;

        PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
        	if (StringUtil.isAllLowerCaseAndDigit(k)) {
        		Query query = pm.newQuery(Screenshot.class, "keyString == :k");
        		query.setUnique(true);
        		//query.declareParameters("String k");
        		s = (Screenshot) query.execute(k);
        	} else {
        		Key key = KeyFactory.stringToKey(k);
        		s = pm.getObjectById(Screenshot.class, key);
        		if (s != null) {
                    s.setKeyString(k.toLowerCase());
                    pm.makePersistent(s);
                }
        	}
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return s;
    }
}
