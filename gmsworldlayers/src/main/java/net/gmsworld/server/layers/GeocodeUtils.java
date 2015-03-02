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

    /*private static JSONObject processGoogleGeocode(String addressIn, String email) {
        JSONObject jsonResponse = new JSONObject();
        try {
            logger.log(Level.INFO, "Calling Google geocode: {0}", addressIn);
            URL geocodeUrl = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(addressIn, "UTF-8") + "&sensor=false");
            String geocodeResponse = HttpUtils.processFileRequest(geocodeUrl);
            if (geocodeResponse != null) {
                JSONObject json = new JSONObject(geocodeResponse);
                String status = json.getString("status");
                if (status.equals("OK")) {
                    JSONArray results = json.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject item = results.getJSONObject(0);
                        String address = item.getString("formatted_address");
                        JSONObject geometry = item.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        
                        String location_type = geometry.getString("location_type");
                        
                        logger.log(Level.INFO, "Geocode precision is " + location_type);
                        
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");

                        //jsonResponse = "{\"status\":\"OK\",\"lat\":\"" + lat + "\",\"lng\":\"" + lng + "\",\"type\":\"g\"}";
                        jsonResponse.put("status", "OK");
                        jsonResponse.put("lat", lat);
                        jsonResponse.put("lng", lng);
                        jsonResponse.put("type", "g");

                        try {
                           GeocodeCachePersistenceUtils.persistGeocode(addressIn, 0, null, lat, lng);

                           if (ConfigurationManager.getParam(ConfigurationManager.SAVE_GEOCODE_AS_LANDMARK, ConfigurationManager.OFF).equals(ConfigurationManager.ON)) {
                               LandmarkPersistenceUtils.persistLandmark(address, "", lat, lng, 0.0, "geocode", null, Commons.GEOCODES_LAYER, email);
                           }
                        } catch (Exception ex) {
                               logger.log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    } else {
                        jsonResponse.put("status", "Error");
                        jsonResponse.put("message", "No matching place found");
                        logger.log(Level.WARNING, "No matching place found");
                    }
                } else {
                    jsonResponse.put("status", "Error");
                    jsonResponse.put("message", status);
                    logger.log(Level.WARNING, "Error: {0}", status);
                }
            } else {
                jsonResponse.put("status", "Error");
                jsonResponse.put("message", "No response from geocode server");
                logger.log(Level.WARNING, "No response from geocode server");
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            //jsonResponse = "{\"status\":\"Error\",\"message\":\"Internal server error\"}";
            try {
                jsonResponse.put("status", "Error");
                jsonResponse.put("message", "Internal server error");
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        return jsonResponse;
    }*/

    private static String processGeocodeCache(GeocodeCache gc) {
        return "{\"status\":\"OK\",\"lat\":\"" + StringUtil.formatCoordE6(gc.getLatitude()) + "\",\"lng\":\"" + StringUtil.formatCoordE6(gc.getLongitude()) + "\",\"type\":\"g\"}";
    }

    private static String processLandmark(Landmark landmark) {
        return "{\"status\":\"OK\",\"lat\":\"" + StringUtil.formatCoordE6(landmark.getLatitude()) + "\",\"lng\":\"" + StringUtil.formatCoordE6(landmark.getLongitude()) + "\",\"type\":\"l\"}";
    }

    /*public static String processGoogleReverseGeocode(String coords) {
        String address = CacheUtil.getString("GRG_" + coords);

        if (address == null) {
            address = "";
            try {
                URL geocodeUrl = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + coords + "&sensor=false");
                String geocodeResponse = HttpUtils.processFileRequest(geocodeUrl);
                if (geocodeResponse != null) {
                    JSONObject json = new JSONObject(geocodeResponse);
                    String status = json.getString("status");
                    if (status.equals("OK")) {
                        JSONArray results = json.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject item = results.getJSONObject(0);
                            address = item.getString("formatted_address");

                        }
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

            if (StringUtils.isNotEmpty(address)) {
                CacheUtil.put("GRG_" + coords, address);
            }
        } else {
            logger.log(Level.INFO, "Reading GRG geocode from cache with key {0}", address);
        }

        return address;
    }*/

    protected static double getLatitude(String latitudeString) {
        double latitude = 90.0;
        if (StringUtils.isNotEmpty(latitudeString)) {
            try {
                double l = Double.parseDouble(latitudeString);
                Validate.isTrue(!(l > 90.0 || l < -90.0), "Latitude must be in [-90, 90]  but was ", l);
                latitude = MathUtils.normalizeE6(l);
            } finally {
            }
        }
        return latitude;
    }

    protected static double getLongitude(String longitudeString) {
        double longitude = 180.0;
        if (StringUtils.isNotEmpty(longitudeString)) {
            try {
                double l = Double.parseDouble(longitudeString);
                Validate.isTrue(!(l > 180.0 || l < -180.0), "Longitude must be in [-180, 180] but was ", l);
                longitude = MathUtils.normalizeE6(l);
            } finally {
            }
        }
        return longitude;
    }

    protected static boolean isNorthAmericaLocation(String latitude, String longitude) {
        boolean isNA = false;

        try {
            if (StringUtils.isNotEmpty(latitude) && StringUtils.isNotEmpty(longitude)) {
                double lat = Double.parseDouble(latitude);
                double lng = Double.parseDouble(longitude);

                //N 83.162102, E -52.233040
                //S 5.499550, W -167.276413

                Validate.isTrue(!(lat > 85.0 || lat < 5.0), "Latitude must be in [5, 85]  but was ", lat);
                Validate.isTrue(!(lng < -170.0 || lng > -50.0), "Longitude must be in [-170, -50] but was ", lng);

                isNA = true;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }

        return isNA;
    }

    public static String processRequest(String address, String email, Locale locale, boolean appendCountry) {
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
                //search for landmark matching address
                Landmark landmark = null;
                /*String[] token = addr.split(",");
                if (token.length > 1 && token[1].length() > 0) {
                	List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarkMatchingQuery(token[1], 1);
                	if (!landmarks.isEmpty()) {
                		landmark = landmarks.get(0);
                	}
                }*/
                List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarkMatchingQuery(addr, 1);
            	if (!landmarks.isEmpty()) {
            		landmark = landmarks.get(0);
            	}
                if (landmark != null) {
                    jsonResp = processLandmark(landmark);
                } else {
                    JSONObject resp = GeocodeHelperFactory.getGoogleGeocodeUtils().processGeocode(addr, email, true);
                    try {
                        if (resp.getString("status").equals("Error")) {
                            logger.log(Level.INFO, "Search geocode response {0}", resp.toString());
                            resp = GeocodeHelperFactory.getMapQuestUtils().processGeocode(addr, email, true);
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                    jsonResp = resp.toString();
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
    
    /*public static String processYahooReverseGeocode(String coords) {
    String address = CacheUtil.getString("YRG_" + coords);

    if (address == null) {
    address = "";
    try {
    URL geocodeUrl = new URL("http://where.yahooapis.com/geocode?location=" + coords + "&flags=J&gflags=R&appid=" + Commons.yh_apiKey);
    String geocodeResponse = HttpUtils.processFileRequest(geocodeUrl);
    if (geocodeResponse != null && geocodeResponse.startsWith("{")) {
    JSONObject json = new JSONObject(geocodeResponse);
    JSONObject resultSet = json.getJSONObject("ResultSet");
    int quality = resultSet.getInt("Quality");
    int found = resultSet.getInt("Found");
    int errorStatus = resultSet.getInt("Error");
    if (errorStatus == 0) {
    JSONArray results = (JSONArray) resultSet.get("Results");
    if (results.length() > 0) {
    JSONObject result = results.getJSONObject(0);
    if (result.has("country")) {
    String country = result.getString("country");
    address += country + ", ";
    }
    if (result.has("uzip") && !result.isNull("uzip")) {
    String uzip = result.getString("uzip");
    if (uzip != null && uzip.length() > 0) {
    address += uzip + " ";
    }
    }
    if (result.has("city")) {
    String city = result.getString("city");
    if (city != null && city.length() > 0) {
    address += city + ", ";
    }
    }
    if (result.has("street")) {
    String street = result.getString("street");
    if (street != null && street.length() > 0) {
    address += street + " ";
    }
    }
    if (result.has("house")) {
    String house = result.getString("house");
    if (house != null && house.length() > 0) {
    address += house;
    }
    }
    }
    }
    }
    //System.out.println(json);
    } catch (Exception ex) {
    logger.log(Level.SEVERE, ex.getMessage(), ex);
    }

    if (StringUtils.isNotEmpty(address)) {
    CacheUtil.put("YRG_" + coords, address);
    }
    } else {
    logger.log(Level.INFO, "Reading YRG geocode from cache with key {0}", address);
    }

    return address;
    }*/

  //{"ResultSet":{"version":"1.0","Error":0,"ErrorMessage":"No error","Locale":"us_US","Quality":40,"Found":1,"Results":[{"quality":40,"latitude":"52.235352","longitude":"21.009390","offsetlat":"52.235352","offsetlon":"21.009390","radius":24700,"name":"","line1":"","line2":"Warsaw","line3":"","line4":"Poland","house":"","street":"","xstreet":"","unittype":"","unit":"","postal":"","neighborhood":"","city":"Warsaw","county":"Warsaw","state":"Masovian","country":"Poland","countrycode":"PL","statecode":"MZ","countycode":"","uzip":"","hash":"","woeid":523920,"woetype":7}]}}
    /*private static JSONObject processYahooGeocode(String location, String origLocation, Locale locale, String email) {
    JSONObject jsonResponse = new JSONObject();
    try {
    logger.log(Level.INFO, "Calling Yahoo geocode: {0}", location);
    URL geocodeUrl = new URL("http://where.yahooapis.com/geocode?location=" + URLEncoder.encode(location, "UTF-8") + "&flags=J&appid=" + Commons.yh_apiKey + "&locale=" + locale.toString());
    String geocodeResponse = HttpUtils.processFileRequest(geocodeUrl);
    if (StringUtils.startsWith(geocodeResponse, "{")) {
    JSONObject json = new JSONObject(geocodeResponse);
    JSONObject resultSet = json.getJSONObject("ResultSet");
    int errorStatus = resultSet.getInt("Error");

    if (errorStatus == 0) {
    JSONArray results = (JSONArray) resultSet.optJSONArray("Results");
    if (results != null && results.length() > 0) {
    JSONObject result = results.getJSONObject(0);
    double lat = getLatitude(result.getString("latitude"));
    double lng = getLongitude(result.getString("longitude"));
    //jsonResponse = "{\"status\":\"OK\",\"lat\":\"" + lat + "\",\"lng\":\"" + lng + "\",\"type\":\"g\"}";

    jsonResponse.put("status", "OK");
    jsonResponse.put("lat", lat);
    jsonResponse.put("lng", lng);
    jsonResponse.put("type", "y");

    try {
    GeocodeCachePersistenceUtils.persistGeocode(origLocation, errorStatus, "", lat, lng);

    if (ConfigurationManager.getParam(ConfigurationManager.SAVE_GEOCODE_AS_LANDMARK, ConfigurationManager.OFF).equals(ConfigurationManager.ON)) {
    LandmarkPersistenceUtils.persistLandmark(origLocation, "", lat, lng, 0.0, "geocode", null, "Geocodes", email);
    }
    } catch (Exception ex) {
    Logger.getLogger(GeocodeUtils.class.getName()).log(Level.SEVERE, null, ex);
    }
    } else {
    jsonResponse.put("status", "Error");
    jsonResponse.put("message", "No matching place found");
    logger.log(Level.WARNING, "No matching place found");
    }
    } else {
    String errorMessage = resultSet.getString("ErrorMessage");
    jsonResponse.put("status", "Error");
    jsonResponse.put("message", errorStatus + "-" + errorMessage);
    logger.log(Level.WARNING, "Error: {0}-{1}", new Object[]{errorStatus, errorMessage});
    }
    } else {
    jsonResponse.put("status", "Error");
    jsonResponse.put("message", "No response from geocode server");
    logger.log(Level.WARNING, "No response from geocode server");
    }

    } catch (Exception ex) {
    logger.log(Level.SEVERE, ex.getMessage(), ex);
    //jsonResponse = "{\"status\":\"Error\",\"message\":\"Internal server error\"}";
    try {
    jsonResponse.put("status", "Error");
    jsonResponse.put("message", "Internal server error");
    } catch (Exception e) {
    logger.log(Level.SEVERE, e.getMessage(), e);
    }
    }

    return jsonResponse;
    }*/
}
