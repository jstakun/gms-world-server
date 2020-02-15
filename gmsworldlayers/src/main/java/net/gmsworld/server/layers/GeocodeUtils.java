package net.gmsworld.server.layers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.persistence.GeocodeCache;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class GeocodeUtils {

    private static final Logger logger = Logger.getLogger(GeocodeUtils.class.getName());

    private static String processGeocodeCache(GeocodeCache gc) {
        return "{\"status\":\"OK\",\"lat\":\"" + StringUtil.formatCoordE6(gc.getLatitude()) + "\",\"lng\":\"" + StringUtil.formatCoordE6(gc.getLongitude()) + "\",\"type\":\"g\"}";
    }

    private static String processLandmark(Landmark landmark) {
        return "{\"status\":\"OK\",\"lat\":\"" + StringUtil.formatCoordE6(landmark.getLatitude()) + "\",\"lng\":\"" + StringUtil.formatCoordE6(landmark.getLongitude()) + "\",\"type\":\"l\"}";
    }

    public static Double getLatitude(String latitudeString) {
        Double latitude = null;
        if (StringUtils.isNotEmpty(latitudeString)) {
            try {
                double l = Double.parseDouble(latitudeString.replace(',', '.'));
                Validate.isTrue(!(l > 90.0 || l < -90.0), "Latitude must be in [-90, 90]  but was ", l);
                latitude = MathUtils.normalizeE6(l);
            } catch (Exception e) {
            	logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return latitude;
    }

    public static Double getLongitude(String longitudeString) {
        Double longitude = null;
        if (StringUtils.isNotEmpty(longitudeString)) {
            try {
                double l = Double.parseDouble(longitudeString.replace(',', '.'));
                Validate.isTrue(!(l > 180.0 || l < -180.0), "Longitude must be in [-180, 180] but was ", l);
                longitude = MathUtils.normalizeE6(l);
            } catch (Exception e) {
            	logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return longitude;
    }
    
    protected static boolean isValidLatitude(Double latitude) {
    	if (latitude != null) {
    		try {
    			Validate.isTrue(!(latitude > 90.0 || latitude < -90.0), "Latitude must be in [-90, 90]  but was ", latitude);
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, e.getMessage());
    			return false;
    		}
    		return true;
    	} else {
    		return false;
    	}
    }
    
    protected static boolean isValidLongitude(Double longitude) {
    	if (longitude != null) {
    		try {
    			Validate.isTrue(!(longitude > 180.0 || longitude < -180.0), "Longitude must be in [-180, 180] but was ", longitude);
    		} catch (Exception e) {
    			logger.log(Level.SEVERE, e.getMessage());
    			return false;
    		}
    		return true;
    	} else {
    		return false;
    	}
    }

    protected static boolean isNorthAmericaLocation(double lat, double lng) {
    	boolean isNA = false;

        try {
        	
             Validate.isTrue(!(lat > 85.0 || lat < 5.0), "Latitude must be in [5, 85]  but was " + lat);
             Validate.isTrue(!(lng < -170.0 || lng > -50.0), "Longitude must be in [-170, -50] but was " + lng);

             isNA = true;
        } catch (Exception ex) {
        	logger.log(Level.WARNING, ex.getMessage());
        }

        return isNA;
    }

    public static String processRequest(String address, String email, Locale locale, int appId, boolean appendCountry) {
        GeocodeCache gc = null;
        String jsonResp = "{}";
        String addr = "";

        if (appendCountry && locale != null) {
            String country = locale.getDisplayCountry(Locale.US);
            if (StringUtils.isNotEmpty(country)) {
                addr += country.toLowerCase();
            }
        }

        if (StringUtils.isNotEmpty(address)) {
            if (StringUtils.isNotEmpty(addr)) {
                addr += ",";
            }
            addr += address.trim(); //.toLowerCase();
        }

        if (StringUtils.isNotEmpty(addr)) {
            try {
                gc = GeocodeCachePersistenceUtils.checkIfGeocodeExists(addr);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

            if (gc != null) {
                jsonResp = processGeocodeCache(gc);
            } else {
                Landmark landmark = null;
                List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarkMatchingQuery(addr, 1);
            	if (!landmarks.isEmpty()) {
            		landmark = landmarks.get(0);
            	}
                if (landmark != null) {
                    jsonResp = processLandmark(landmark);
                } else {
                    jsonResp = processGeocode(addr, email, appId);
                }
            }
        }
        return jsonResp;
    }

    protected static JSONObject geocodeToJSonObject(String address, String jsonResp) {

        //"{\"status\":\"OK\",\"lat\":\"" + gc.getLatitude() + "\",\"lng\":\"" + gc.getLongitude() + "\",\"type\":\"g\"}"

        List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
        JSONObject reply = null;

        try {

            if (StringUtils.startsWith(jsonResp, "{")) {
                JSONObject json = new JSONObject(jsonResp);
                String status = json.optString("status");

                if (StringUtils.equals(status, "OK")) {
                    Map<String, Object> jsonObject = new HashMap<String, Object>();
                    jsonObject.put("name", StringUtils.capitalize(address));
                    jsonObject.put("lat", json.getDouble("lat"));
                    jsonObject.put("lng", json.getDouble("lng"));
                    jsonObject.put("url", "");

                    Map<String, String> desc = new HashMap<String, String>();
                    //desc.put("type", json.getString("type"));
                    desc.put("creationDate", Long.toString(System.currentTimeMillis()));
                    jsonObject.put("desc", desc);

                    jsonArray.add(jsonObject);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                reply = new JSONObject().put("ResultSet", jsonArray);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return reply;
    }
    
    protected static ExtendedLandmark geocodeToLandmark(String address, String jsonResp, Locale locale) {
    	ExtendedLandmark landmark = null;
    	try {
            if (StringUtils.startsWith(jsonResp, "{")) {
                JSONObject json = new JSONObject(jsonResp);
                String status = json.optString("status");

                if (StringUtils.equals(status, "OK")) {
                    String name = StringUtils.capitalize(address);
                    
                    Map<String, String> tokens = new HashMap<String, String>();
                    
                    QualifiedCoordinates qc = new QualifiedCoordinates(json.getDouble("lat"), json.getDouble("lng"), 0f, 0f, 0f); 
                    landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.LOCAL_LAYER, new AddressInfo(),  System.currentTimeMillis(), null);
                    
                    String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                    landmark.setDescription(desc);                    
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } 

        return landmark;
    }

    protected static boolean geocodeEquals(String geocode1, String geocode2) {
        boolean resp = false;
        if (StringUtils.startsWith(geocode1, "{") && StringUtils.startsWith(geocode2, "{")) {
            try {
                JSONObject json1 = new JSONObject(geocode1);
                JSONObject json2 = new JSONObject(geocode2);
                String status1 = json1.optString("status");
                String status2 = json2.optString("status");

                if (StringUtils.equals(status1, "OK") && StringUtils.equals(status2, "OK")) {
                    //System.out.println(json1.getDouble("lat") + "-" + json2.getDouble("lat") + ", " +
                    //        json1.getDouble("lng") + "-" + json2.getDouble("lng"));
                    resp = (Math.abs(json1.getDouble("lat") - json2.getDouble("lat")) < 0.1
                            && Math.abs(json1.getDouble("lng") - json2.getDouble("lng")) < 0.1);

                    //return (json1.getDouble("lat") == json2.getDouble("lat"))
                    //        && (json1.getDouble("lng") == json2.getDouble("lng"));
                }
            } catch (JSONException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return resp;
    }
    
    private static String processGeocode(String address, String email, int appId) {
    	JSONObject resp = GeocodeHelperFactory.getGoogleGeocodeUtils().processGeocode(address, email, appId, true);
        try {
            if (resp.getString("status").equals("Error")) {
                logger.log(Level.INFO, "Search geocode response {0}", resp.toString());
                resp = GeocodeHelperFactory.getMapQuestUtils().processGeocode(address, email, appId, true);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return resp.toString();
    }
}
