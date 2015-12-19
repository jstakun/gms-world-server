package com.jstakun.lm.server.utils.persistence;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.ImageUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.social.NotificationUtils;
import com.jstakun.lm.server.utils.FileUtils;
import com.jstakun.lm.server.utils.memcache.CacheAction;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

/**
 *
 * @author jstakun
 */
public class LandmarkPersistenceUtils {

    private static final Logger logger = Logger.getLogger(LandmarkPersistenceUtils.class.getName());
    
    
    /*private static Date defaultValidityDate = null;
    static {
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:dd");
       String vs = "2200/01/01 00:00:00";
       try {
         defaultValidityDate = formatter.parse(vs);
       } catch (ParseException pe) {
    	   
       }
    }*/
    
    private static Map<String, String> persistLandmark(String name, String description, double latitude, double longitude, double altitude, String username, Date validityDate, String layer, String email, String flex) {

    	Map<String, String> response = new HashMap<String, String>();
    	
        try {
        	String landmarksUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "addItem";
        	String params = "type=landmark&latitude=" + latitude + "&longitude=" + longitude + "&name=" + URLEncoder.encode(name, "UTF-8") + 
        			"&altitude=" + altitude + "&username=" + username + "&layer=" + layer;			 
        	if (validityDate != null) {
        		params +=	"&validityDate=" + validityDate.getTime();
        	}	
        	if (StringUtils.isNotEmpty(description)) {
        		params += "&description=" + URLEncoder.encode(description, "UTF-8"); 
        	}
        	if (StringUtils.isNotEmpty(email)) {
        		params += "&email=" + email;
        	}
        	if (flex != null) {
        		params += "&flex=" + URLEncoder.encode(flex, "UTF-8");
        	}
        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
        	String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
        		logger.log(Level.INFO, "Received following response from server: " + landmarksJson);
        		JSONObject resp = new JSONObject(landmarksJson);
        		for (Iterator<String> iter = resp.keys(); iter.hasNext();) {
        			String key = iter.next();
        			String value = resp.get(key).toString();
        			response.put(key, value);
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following response from server: " + landmarksJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
        
        if (!StringUtils.isNumeric(response.get("id"))) {
        	String lat = StringUtil.formatCoordE2(latitude);
            String lng = StringUtil.formatCoordE2(longitude);
        	CacheUtil.remove(name + "_" + lat + "_" + lng);
        }

        return response;
    }

    public static void persistLandmark(Landmark l) {
    	Map<String, String> persistResponse = persistLandmark(l.getName(), l.getDescription(), l.getLatitude(), l.getLongitude(), l.getAltitude(), l.getUsername(), l.getValidityDate(), l.getLayer(), l.getEmail(), l.getFlex());
    	l.setId(NumberUtils.getInt(persistResponse.get("id"),-1));
    	l.setHash(persistResponse.get("hash"));
    }
      
    /*public static List<Landmark> selectAllLandmarks() {
    	List<Landmark> results = new ArrayList<Landmark>();
        PersistenceManager pm = PMF.get().getPersistenceManager();
        
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
    }*/

    /*public static long deleteAllLandmarks() {
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
    }*/

    /*public static void deleteLandmark(String k) {
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
    }*/

    public static Landmark selectLandmarkByHash(String hash) {
        Landmark landmark = null;
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

        try {
            Query query = pm.newQuery(Landmark.class, "hash == :hashparam");
            query.setUnique(true);
            //query.declareParameters("String hashparam");
            landmark = (Landmark) query.execute(hashparam);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
        try {
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
        	String params = "hash=" + hash;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        		JSONObject l = new JSONObject(gJson);
    		    landmark = jsonToLandmark(l);
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return landmark;
    }

    public static Landmark selectLandmarkById(String id) {
        String key = "landmark_" + id;
    	Landmark landmark = (Landmark)CacheUtil.getObject(key);
        
        if (landmark == null) {
        	try {
        		String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
        		String params = "id=" + id;			 
        		//logger.log(Level.INFO, "Calling: " + gUrl);
        		String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        		//logger.log(Level.INFO, "Received response: " + gJson);
        		if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        			JSONObject l = new JSONObject(gJson);
        			landmark = jsonToLandmark(l);
        			CacheUtil.put(key, landmark, CacheType.NORMAL);
        		} else {
        			logger.log(Level.SEVERE, "Received following server response: " + gJson);
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        	}
        }

        return landmark;
    }

    /*public static void updateLandmark(String k, Map<String, Object> update) {
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
    }*/

    public static List<Landmark> selectLandmarksByCoordsAndLayer(double latitude, double longitude, String layer, int limit, int radius) {
    	List<Landmark> results = new ArrayList<Landmark>();
        
        /*PersistenceManager pm = PMF.get().getPersistenceManager();
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
        }*/
        try {
    		String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
        	String params = "limit=" + limit + "&lat=" + latitude + "&lng=" + longitude + "&radius=" + radius + "&layer=" + layer;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray root = new JSONArray(gJson);
        		for (int i=0;i<root.length();i++) {
        			JSONObject landmark = root.getJSONObject(i);
        			try {
        			   Landmark l = jsonToLandmark(landmark);
       				   results.add(l);
        			} catch (Exception e) {
        	        	logger.log(Level.SEVERE, e.getMessage(), e);
        	        }      
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return results;
    }

    /*public static List<Landmark> selectLandmarksByCoordsAndLayer(double latitudeMin, double longitudeMin, double latitudeMax, double longitudeMax, String layer, int limit) {

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
    }*/

    public static int countLandmarksByCoordsAndLayer(String layer, double latitude, double longitude, int radius) {
    	/*boolean result = true;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        Point center = new Point(latitude, longitude);
        List<Object> params = new ArrayList<Object>();
        params.add(layer);
        params.add(new Date());
        GeocellQuery baseQuery = new GeocellQuery("layer == layerIn && validityDate > currentDate", "String layerIn, java.util.Date currentDate", params);

        try {
        	logger.log(Level.INFO, "Searching landmarks in layer " + layer + " ...");
        	result = GeocellManager.proximityIsEmptySearch(center,radius, Landmark.class, baseQuery, pm);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }

        return result;*/
    	int result = 0;
    	
    	try {
   			String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
   			String params = "count=1&lat=" + latitude + "&lng=" + longitude + "&radius=" + radius + "&layer=" + layer;			 
   			//logger.log(Level.INFO, "Calling: " + gUrl);
   			String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
   			//logger.log(Level.INFO, "Received response: " + gJson);
   			if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
   				JSONObject count = new JSONObject(gJson);
   				result = count.getInt("count");     
   			} else {
   				logger.log(Level.SEVERE, "Received following server response: " + gJson);
   			}
        } catch (Exception e) {
       		logger.log(Level.SEVERE, e.getMessage(), e);
        }

    	return result;
    }

    public static Map<String, Integer> countLandmarksByCoords(double latitude, double longitude, int radius) {
    	Map<String, Integer> bucket = new HashMap<String, Integer>();
    	try {
   			String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
   			String params = "count=1&lat=" + latitude + "&lng=" + longitude + "&radius=" + radius;			 
   			logger.log(Level.INFO, "Calling: " + gUrl + "?" + params);
   			String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
   			//logger.log(Level.INFO, "Received response: " + gJson);
   			if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
   				JSONArray count = new JSONArray(gJson);
   				for (int i=0;i<count.length();i++) {
   					JSONArray item = count.getJSONArray(i);
   					bucket.put(item.getString(1), item.getInt(0));
   				}
   			} else {
   				logger.log(Level.SEVERE, "Received following server response: " + gJson);
   			}
        } catch (Exception e) {
       		logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	return bucket;
    }	
    
    /*public static boolean isEmptyLandmarksByCoordsAndLayer(List<String> cells, String layer) {

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
    }*/

    public static List<Landmark> selectLandmarkMatchingQuery(String query, int limit) {
    	List<Landmark> landmarks = new ArrayList<Landmark>();
    	/*PersistenceManager pm = PMF.get().getPersistenceManager();
        

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
        }*/
    	
    	try {
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
        	String params = "query=" + URLEncoder.encode(query, "UTF-8") + "&limit=" + limit;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray root = new JSONArray(gJson);
        		for (int i=0;i<root.length();i++) {
        			JSONObject landmark = root.getJSONObject(i);     			
        			try {
        			   Landmark l = jsonToLandmark(landmark);      				   
       				   landmarks.add(l);
        			} catch (Exception e) {
        	        	logger.log(Level.SEVERE, e.getMessage(), e);
        	        }      
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return landmarks;
    }

    public static List<Landmark> selectLandmarksByUserAndLayer(String user, String layer, int first, int last) {
        List<Landmark> results = new ArrayList<Landmark>();
        
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

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
            results = (List<Landmark>) pm.detachCopyAll(results);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            pm.close();
        }*/
        
        try {
        	int limit = last - first;
    		String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
        	String params = "limit=" + limit + "&first=" + first; 
        	if (user != null) {
   				params += "&username=" + user;			 
   			} 
   			if (layer != null) {
   			    params += "&layer=" + layer;
   			}
        	logger.log(Level.INFO, "Calling: " + gUrl +"?" + params);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray root = new JSONArray(gJson);
        		for (int i=0;i<root.length();i++) {
        			JSONObject landmark = root.getJSONObject(i);     			
        			try {
        			   Landmark l = jsonToLandmark(landmark);      				   
       				   results.add(l);
        			} catch (Exception e) {
        	        	logger.log(Level.SEVERE, e.getMessage(), e);
        	        }      
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return results;
    }

    public static List<Landmark> selectNewestLandmarks() {
    	List<Landmark> results = new ArrayList<Landmark>();
        /*PersistenceManager pm = PMF.get().getPersistenceManager();
        
        try {
            String lastStr = ConfigurationManager.getParam(ConfigurationManager.NUM_OF_LANDMARKS, "10");
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
        }*/
    	
    	try {
    		String limit = com.jstakun.lm.server.config.ConfigurationManager.getParam(ConfigurationManager.NUM_OF_LANDMARKS, "10");
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
        	String params = "limit=" + limit;			 
        	//logger.log(Level.INFO, "Calling: " + gUrl);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray root = new JSONArray(gJson);
        		for (int i=0;i<root.length();i++) {
        			JSONObject landmark = root.getJSONObject(i);
        			
        			try {
        			   Landmark l = jsonToLandmark(landmark);
       				   
       				   results.add(l);
        			} catch (Exception e) {
        	        	logger.log(Level.SEVERE, e.getMessage(), e);
        	        }      
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }
    
        return results;
    }

	private static Landmark jsonToLandmark(JSONObject landmark) throws IllegalAccessException, InvocationTargetException {
		Landmark l = new Landmark();
		   
		Map<String, String> landmarkMap = new HashMap<String, String>();
		for(Iterator<String> iter = landmark.keys();iter.hasNext();) {
				String key = iter.next();
				Object value = landmark.get(key);
				landmarkMap.put(key, value.toString());
		}
		   
		ConvertUtils.register(DateUtils.getRHCloudDateConverter(), Date.class);
		BeanUtils.populate(l, landmarkMap);
		   
		return l;
	}

    public static List<Landmark> selectLandmarksByMonth(int first, int last, String month) {
    	List<Landmark> results = new ArrayList<Landmark>();
        /*PersistenceManager pm = PMF.get().getPersistenceManager();

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
        }*/
    	
    	try {
    		int limit = last - first;
        	String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
        	String params = "limit=" + limit + "&month=" + month + "&first=" + first;			 
        	logger.log(Level.INFO, "Calling: " + gUrl + "?" + params);
        	String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	//logger.log(Level.INFO, "Received response: " + gJson);
        	if (StringUtils.startsWith(StringUtils.trim(gJson), "[")) {
        		JSONArray root = new JSONArray(gJson);
        		for (int i=0;i<root.length();i++) {
        			JSONObject landmark = root.getJSONObject(i);
        			
        			try {
        			   Landmark l = jsonToLandmark(landmark);
       				   
       				   results.add(l);
        			} catch (Exception e) {
        	        	logger.log(Level.SEVERE, e.getMessage(), e);
        	        }      
        		}
        	} else {
        		logger.log(Level.SEVERE, "Received following server response: " + gJson);
        	}
        } catch (Exception e) {
        	logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return results;
    }

    public static Map<String, Integer> getHeatMap(int nDays) {
    	Map<String, Integer> bucket = new HashMap<String, Integer>();

        /*PersistenceManager pm = PMF.get().getPersistenceManager();

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
        }*/
    	
    	
    	try {
   			String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
   			String params = "heatMap=1&days=" + nDays;			 
   			//logger.log(Level.INFO, "Calling: " + gUrl);
   			String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
   			//logger.log(Level.INFO, "Received response: " + gJson);
   			if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
   				JSONObject root = new JSONObject(gJson);
   				for (Iterator<String> keys = root.keys();keys.hasNext();) {
   					String key = keys.next();
   					int value = root.getInt(key);
   					bucket.put(key, value);
   				}
   			} else {
   				logger.log(Level.SEVERE, "Received following server response: " + gJson);
   			}
        } catch (Exception e) {
       		logger.log(Level.SEVERE, e.getMessage(), e);
        }
    	

        if (!bucket.isEmpty()) {
            try {
                String cacheKey = DateUtils.getDay(new Date()) + "_" + nDays + "_heatMap";
                CacheUtil.put(cacheKey, bucket, CacheType.NORMAL);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return bucket;
    }

   public static int countLandmarksByMonth(String month) {
	    int result = 0;

       /*try {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("Landmark");
            Date monthFirstDay = DateUtils.getFirstDayOfMonth(month);
            Date nextMonthFirstDay = DateUtils.getFirstDayOfNextMonth(month);
            query.setKeysOnly();

            Filter dateMinFilter = new FilterPredicate("creationDate", FilterOperator.GREATER_THAN_OR_EQUAL, monthFirstDay);
            Filter dateMaxFilter = new FilterPredicate("creationDate", FilterOperator.LESS_THAN, nextMonthFirstDay);
            Filter dateRangeFilter = CompositeFilterOperator.and(dateMinFilter, dateMaxFilter);
            query.setFilter(dateRangeFilter);
 
            PreparedQuery pq = ds.prepare(query);
            FetchOptions option = FetchOptions.Builder.withLimit(Integer.MAX_VALUE);
            result = pq.countEntities(option);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }*/
	   
	    try {
   			String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
   			String params = "count=1&month=" + month;			 
   			logger.log(Level.INFO, "Calling: " + gUrl);
   			String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
   			logger.log(Level.INFO, "Received response: " + gJson);
   			if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
   				JSONObject count = new JSONObject(gJson);
   				result = count.getInt("count");     
   			} else {
   				logger.log(Level.SEVERE, "Received following server response: " + gJson);
   			}
        } catch (Exception e) {
       		logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return result;
    }

    public static int countLandmarksByUserAndLayer(String user, String layer) {
        int result = 0;

        /*try {
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
        }*/
        
        try {
   			String gUrl = ConfigurationManager.RHCLOUD_SERVER_URL + "landmarksProvider";
   			String params = "count=1";
   			if (user != null) {
   				params += "&username=" + user;			 
   			} 
   			if (layer != null) {
   			    params += "&layer=" + layer;
   			}
   			logger.log(Level.INFO, "Calling: " + gUrl + "?" + params);
   			String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
   			logger.log(Level.INFO, "Received response: " + gJson);
   			if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
   				JSONObject count = new JSONObject(gJson);
   				result = count.getInt("count");     
   			} else {
   				logger.log(Level.SEVERE, "Received following server response: " + gJson);
   			}
        } catch (Exception e) {
       		logger.log(Level.SEVERE, e.getMessage(), e);
        }


        return result;
    }

    /*public static int updateAllLandmarksWithGeoCells() {
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
    }*/

    /*public static Map<String, Collection<String>> filterLandmarks(String property, String[] pattern, String resultProperty, boolean unique, long createdBeforeMillis, Map<String, Integer> beforeCreated, Date afterDay) {

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
    }*/

    /*private static class UsernameTransformer implements Function<String, String> {

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
    }*/
    
    public static boolean isSimilarToNewest(Landmark l) {
    	boolean isSimilarToNewest = false;
    	String name = l.getName();
    	String lat = StringUtil.formatCoordE2(l.getLatitude());
    	String lng = StringUtil.formatCoordE2(l.getLongitude());
        if (CacheUtil.containsKey(name + "_" + lat + "_" + lng)) {
        	isSimilarToNewest = true;
        	logger.log(Level.WARNING, "This landmark is similar to newest: " + name + "_" + lat + "_" + lng);
        } else {
        	CacheAction newestLandmarksAction = new CacheAction(new CacheAction.CacheActionExecutor() {			
        		@Override
        		public Object executeAction() {
        			return LandmarkPersistenceUtils.selectNewestLandmarks();
        		}
        	});
        	
        	List<Landmark> landmarkList = (List<Landmark>)newestLandmarksAction.getObjectFromCache("newestLandmarks", CacheType.FAST);
        	if (!landmarkList.isEmpty()) {
        		Landmark newestLandmark = landmarkList.get(0);
        		logger.log(Level.INFO, "Newest landmark: " + newestLandmark.getName() + ", " + newestLandmark.getLatitude() + ", " + newestLandmark.getLongitude());
        		if (l.compare(newestLandmark)) {
        			logger.log(Level.WARNING, "This landmark is similar to newest: " + name + ", " + lat + ", " + lng);
        			isSimilarToNewest = true;
        		} else {
        			logger.log(Level.INFO, "This landmark is not similar to newest: " + name + ", " + lat + ", " + lng);
        		}
        	}
        }
        if (!isSimilarToNewest) {
        	CacheUtil.put(name + "_" + lat + "_" + lng, "1", CacheType.LANDMARK);
        }
        return isSimilarToNewest;
    }
    
    public static void notifyOnLandmarkCreation(Landmark l, String userAgent, String socialIds) {
    	//
    	try {
	    	//save map image thumbnail
	    	byte[] thumbnail = ImageUtils.loadImage(l.getLatitude(), l.getLongitude(), "128x128", 9, ConfigurationManager.MAP_PROVIDER.OSM_MAPS); 
	    	if (thumbnail != null && thumbnail.length > 0) {
	    		FileUtils.saveFileV2("landmark_" + StringUtil.formatCoordE6(l.getLatitude()) + "_" + StringUtil.formatCoordE6(l.getLongitude()) + ".jpg", thumbnail, l.getLatitude(), l.getLongitude());
	    	}
	    } catch (Exception e) {
	    	logger.log(Level.SEVERE, e.getMessage(), e);
	    }
    	
    	//social notifications
    
    	String landmarkUrl = ConfigurationManager.SERVER_URL + "showLandmark/" + l.getId();
    	if (StringUtils.isNotEmpty(l.getHash())) {
    		landmarkUrl = UrlUtils.BITLY_URL + l.getHash();
    	} 
                        
    	String titleSuffix = "";
    	String[] tokens = StringUtils.split(userAgent, ",");
    	if (tokens != null) {
        	for (int i = 0; i < tokens.length; i++) {
            	String token = StringUtils.trimToEmpty(tokens[i]);
            	if (token.startsWith("Package:") || token.startsWith("Version:") || token.startsWith("Version Code:")) {
                	titleSuffix += " " + token;
            	}
        	}
    	}

    	String messageSuffix = "";
    	if (l.getUseCount() > 0) {
    		messageSuffix = " User has opened LM " + l.getUseCount() + " times.";
    	}
    	
    	String title = "New landmark";
    	if (StringUtils.isNotEmpty(titleSuffix)) {
        	title += titleSuffix;
    	}

    	String body = "Landmark: " + l.getName() + " has been created by user " + 
    			ConfigurationManager.SERVER_URL + "socialProfile?uid=" + l.getUsername() + "." + messageSuffix;
    
    	String userUrl = ConfigurationManager.SERVER_URL;
    	if (l.isSocial()) {
    		userUrl += "blogeo/" + l.getUsername();
    	} else {
    		userUrl += "showUser/" + l.getUsername();
    	}
    	
    	String imageUrl = ConfigurationManager.SERVER_URL + "image?lat=" + l.getLatitude() + "&lng=" + l.getLongitude();
    
    	Map<String, String> params = new ImmutableMap.Builder<String, String>().
            put("key", Integer.toString(l.getId())).
    		put("landmarkUrl", landmarkUrl).
    		put("email", l.getEmail()).
    		put("title", title).
    		put("userUrl", userUrl).
    		put("username", l.getUsername()).
    		put("name", l.getName()).
    		put("body", body).
    		put("latitude", Double.toString(l.getLatitude())).
    		put("longitude", Double.toString(l.getLongitude())).
    		put("layer", l.getLayer()).
    		put("desc", l.getDescription()).
    		put("socialIds", socialIds != null ? socialIds : l.getUsername()).
    		put("imageUrl", imageUrl).build();  
    	  
    	NotificationUtils.createLadmarkCreationNotificationTask(params);
    }
    
    public static void setFlex(Landmark l, HttpServletRequest request) {
    	//AddressInfo addressInfo = new AddressInfo();
    	
    	/*try {
    		//addressInfo = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(l.getLatitude(), l.getLongitude());
			addressInfo = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(l.getLatitude(), l.getLongitude());
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}*/
    	
    	int useCount = NumberUtils.getInt(request.getHeader(Commons.USE_COUNT_HEADER), 1);
		int appId = NumberUtils.getInt(request.getHeader(Commons.APP_HEADER), -1);
		int version = NumberUtils.getInt(request.getHeader(Commons.APP_VERSION_HEADER), -1);
    	
    	JSONObject flex = new JSONObject();
		flex.put("useCount", useCount);
		if (appId > -1) {
			flex.put("appId", appId);
		}
		if (version > 0) {
			flex.put("version", version);
		}
		//flex.putOpt("cc", addressInfo.getField(AddressInfo.COUNTRY_CODE));
		//flex.putOpt("city", addressInfo.getField(AddressInfo.CITY));
		l.setFlex(flex.toString());
		
		//return addressInfo.getField(AddressInfo.EXTENSION); //formatted address
    }
    
    public static void deleteLandmark(String key) {
    	//TODO not yet implemented
    }
    
    public static void updateLandmark(String key, Map<String, Object> update) {
    	//TODO not yet implemented
    }
}
