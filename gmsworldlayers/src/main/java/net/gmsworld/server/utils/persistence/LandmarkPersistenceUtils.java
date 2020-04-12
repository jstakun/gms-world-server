package net.gmsworld.server.utils.persistence;

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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.NumberUtils;
import net.gmsworld.server.utils.memcache.CacheProvider;

/**
 *
 * @author jstakun
 */
public class LandmarkPersistenceUtils {

    private static final Logger logger = Logger.getLogger(LandmarkPersistenceUtils.class.getName());
    private static final String BACKEND_SERVER_URL = "https://openapi-landmarks.b9ad.pro-us-east-1.openshiftapps.com/actions/"; 
    
    public static Map<String, String> persistLandmark(String name, String description, double latitude, double longitude, double altitude, String username, Date validityDate, String layer, String email, String flex) {

        Map<String, String> response = new HashMap<String, String>();
    	
        try {
        	String landmarksUrl = BACKEND_SERVER_URL + "addItem";
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
        	if (StringUtils.isNotEmpty(flex)) {
        		params += "&flex=" + URLEncoder.encode(flex, "UTF-8");
        	}
        	//logger.log(Level.INFO, "Calling: " + landmarksUrl);
        	String landmarksJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(landmarksUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        	if (StringUtils.startsWith(StringUtils.trim(landmarksJson), "{")) {
        		JSONObject resp = new JSONObject(landmarksJson);
        		logger.log(Level.INFO, "Landmark created: " + landmarksJson);
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

        return response;
    }
    
    public static JSONObject persistLandmark(Landmark landmark, CacheProvider cacheProvider) {
    	Map<String, String> persistResponse = persistLandmark(landmark.getName(), landmark.getDescription(), landmark.getLatitude(), landmark.getLongitude(), landmark.getAltitude(), landmark.getUsername(), landmark.getValidityDate(), landmark.getLayer(), landmark.getEmail(), landmark.getFlex());
    	landmark.setId(NumberUtils.getInt(persistResponse.get("id"), -1));
    	landmark.setHash(persistResponse.get("hash"));
    	JSONObject flexJSon;
    	if (landmark.getFlex() != null) {
    		flexJSon = new JSONObject(landmark.getFlex()); 
    	} else {
    		flexJSon = new JSONObject();
    	}
		if (persistResponse.containsKey("cc")) {
    		flexJSon.put("cc", persistResponse.get("cc"));
    	}
		if (persistResponse.containsKey("city")) {
    		flexJSon.put("city", persistResponse.get("city"));
    	}
		landmark.setFlex(flexJSon.toString());
    	if (landmark.getId() > 0 && cacheProvider != null) {
    		cacheProvider.put(Integer.toString(landmark.getId()), landmark);
    		logger.log(Level.INFO, "Saved landmark to local in-memory cache with key: " + landmark.getId());
    	}
    	return flexJSon;
    }

    public static Landmark selectLandmarkByHash(String hash, CacheProvider cacheProvider) {
    	String key = "landmark_" + hash;
    	Landmark landmark = null;
    	if (cacheProvider != null) {
    		landmark = cacheProvider.getObject(Landmark.class, key);
    	}
    
    	if (landmark == null) {
    		try {
    			String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
    			String params = "hash=" + hash;			 
    			//logger.log(Level.INFO, "Calling: " + gUrl);
    			String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
    			//logger.log(Level.INFO, "Received response: " + gJson);
    			if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
    				JSONObject l = new JSONObject(gJson);
    				if (l.has("error")) {
    					logger.log(Level.SEVERE, "Received following server response: " + l.getString("error"));
    				} else {
    					landmark = jsonToLandmark(l);
    					if (cacheProvider != null) {
    						cacheProvider.put(key, landmark);
    					}
    				}
    			} else {
    				logger.log(Level.SEVERE, "Received following server response: " + gJson);
    			}
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
    		}
    	}

        return landmark;
    }

    public static Landmark selectLandmarkById(String id, CacheProvider cacheProvider) {
        String key = "landmark_" + id;
    	Landmark landmark = null;
    	if (cacheProvider != null) {
    		landmark = cacheProvider.getObject(Landmark.class, key);
    	}
        
        if (landmark == null) {
        	try {
        		String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
        		String params = "id=" + id;			 
        		//logger.log(Level.INFO, "Calling: " + gUrl);
        		String gJson = HttpUtils.processFileRequestWithBasicAuthn(new URL(gUrl), "POST", null, params, Commons.getProperty(Property.RH_GMS_USER));
        		//logger.log(Level.INFO, "Received response: " + gJson);
        		if (StringUtils.startsWith(StringUtils.trim(gJson), "{")) {
        			JSONObject l = new JSONObject(gJson);
        			if (l.has("error")) {
        				logger.log(Level.SEVERE, "Received following server response: " + l.getString("error"));
        			} else {
        				landmark = jsonToLandmark(l);
        				if (cacheProvider != null) {
        					cacheProvider.put(key, landmark);
        				}
        			}
        		} else {
        			logger.log(Level.SEVERE, "Received following server response: " + gJson);
        		}
        	} catch (Exception e) {
        		logger.log(Level.SEVERE, e.getMessage(), e);
        	}
        }

        return landmark;
    }

    public static List<Landmark> selectLandmarksByCoordsAndLayer(double latitude, double longitude, String layer, int limit, int radius) {
    	List<Landmark> results = new ArrayList<Landmark>();
        
        try {
    		String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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

    public static int countLandmarksByCoordsAndLayer(String layer, double latitude, double longitude, int radius) {
    	int result = 0;
    	
    	try {
   			String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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
   			String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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
    
    public static List<Landmark> selectLandmarkMatchingQuery(String query, int limit) {
    	List<Landmark> landmarks = new ArrayList<Landmark>();

    	try {
        	String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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
        
        try {
        	int limit = last - first;
    		String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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
        
    	try {
    		String limit = "10";
        	String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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
		
		try {
			Date d = new Date(Long.parseLong(landmarkMap.get("creationDateLong")));
			l.setCreationDate(d);
			d = new Date(Long.parseLong(landmarkMap.get("validityDateLong")));
			l.setValidityDate(d);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		   
		return l;
	}

    public static List<Landmark> selectLandmarksByMonth(int first, int last, String month) {
    	List<Landmark> results = new ArrayList<Landmark>();
    	
    	try {
    		int limit = last - first;
        	String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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

    public static Map<String, Integer> getHeatMap(int nDays, CacheProvider cacheProvider) {
    	Map<String, Integer> bucket = new HashMap<String, Integer>();

    	try {
   			String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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
    	

        if (!bucket.isEmpty() && cacheProvider != null) {
            try {
                String cacheKey = DateUtils.getDay(new Date()) + "_" + nDays + "_heatMap";
                cacheProvider.put(cacheKey, bucket);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return bucket;
    }

   public static int countLandmarksByMonth(String month) {
	    int result = 0;
	   
	    try {
   			String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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

        try {
   			String gUrl = BACKEND_SERVER_URL + "landmarksProvider";
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
   				result = count.optInt("count", 0);     
   			} else {
   				logger.log(Level.SEVERE, "Received following server response: " + gJson);
   			}
        } catch (Exception e) {
       		logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return result;
    }
    
    public static void deleteLandmark(String key) {
    	//TODO not yet implemented
    }
    
    public static void updateLandmark(String key, Map<String, Object> update) {
    	//TODO not yet implemented
    }
}
