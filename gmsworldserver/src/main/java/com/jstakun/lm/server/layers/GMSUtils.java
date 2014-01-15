/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.persistence.Landmark;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.MathUtils;
import com.jstakun.lm.server.utils.UrlUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.persistence.LandmarkPersistenceUtils;
import com.jstakun.lm.server.utils.xml.XMLUtils;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jstakun
 */
public class GMSUtils extends LayerHelper {

    private static final String landingPage = "showLandmark.do?key=";
    private static final Date migrationDate; //2012-08-25
    
    static {
        Calendar c = Calendar.getInstance();
        c.set(2012, 8, 25, 0, 0, 0);
        migrationDate = c.getTime();
    };

    @Override
    public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String layer, String flexString2) throws JSONException, UnsupportedEncodingException {
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, layer, flexString2);
        JSONObject json = null;
        String output = CacheUtil.getString(key);
        if (output == null) {
            List<Landmark> landmarkList = LandmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(latitude, longitude, layer, limit, radius);
            List<Landmark> results = new ArrayList<Landmark>();
            results.addAll(Collections2.filter(landmarkList, new QueryPredicate(query)));
            json = createCustomJSonLandmarkList(results, version, stringLimit);
            if (!results.isEmpty()) {
                CacheUtil.put(key, json.toString());
                logger.log(Level.INFO, "Adding GMS landmark list to cache with key {0}", key);
            }
        } else {
            json = new JSONObject(output);
            logger.log(Level.INFO, "Reading GMS landmark list from cache with key {0}", key);
        }

        return json;
    }

    public static String processRequest(double latitudeMin, double longitudeMin, double latitudeMax, double longitudeMax, int version, int limit, int stringLimit, String layer, String format) throws JSONException, UnsupportedEncodingException {
        String key = getCacheKey(GMSUtils.class, "processRequest", (latitudeMin + latitudeMax)/2, (longitudeMin + longitudeMax)/2, null, 0, version, limit, stringLimit, layer, format);

        String output = CacheUtil.getString(key);
        if (output == null) {
            List<Landmark> landmarkList = LandmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(latitudeMin, longitudeMin, latitudeMax, longitudeMax, layer, limit);

            if (format.equals("kml")) {
                output = XMLUtils.createKmlLandmarkList(landmarkList, landingPage);
            } else if (format.equals("json")) {
                output = createCustomJSonLandmarkList(landmarkList, version, stringLimit).toString();
            } else {
                output = XMLUtils.createCustomXmlLandmarkList(landmarkList, landingPage);
            }
            if (StringUtils.isNotEmpty(output) && !landmarkList.isEmpty()) {
                CacheUtil.put(key, output);
                logger.log(Level.INFO, "Adding GMS landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading GMS landmark list from cache with key {0}", key);
        }

        return output;
    }

    private static JSONObject createCustomJSonLandmarkList(List<Landmark> landmarkList, int version, int stringLimit) throws JSONException {
        List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        for (Iterator<Landmark> iter = landmarkList.iterator(); iter.hasNext();) {
            Landmark landmark = iter.next();
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            jsonObject.put("name", landmark.getName());
            jsonObject.put("lat", MathUtils.normalizeE6(landmark.getLatitude()));
            jsonObject.put("lng", MathUtils.normalizeE6(landmark.getLongitude()));

            String url = landingPage + landmark.getKeyString();

            if (version >= 2) {
                if (version >= 4) { 
                    String hash = landmark.getHash();
                    if (landmark.getCreationDate().after(migrationDate) && !StringUtils.isEmpty(hash)) { //2012-08-25
                        url = hash;
                        jsonObject.put("urlType", 1);
                    } else {
                        jsonObject.put("urlType", 0);
                    }
                }
                jsonObject.put("url", url);
                Map<String, String> desc = new HashMap<String, String>();
                JSONUtils.putOptValue(desc, "description", landmark.getDescription(), stringLimit, false);
                if (version == 2) {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                    String date = df.format(landmark.getCreationDate());
                    desc.put("creationDate", date);
                } else if (version >= 3) {
                    desc.put("creationDate", Long.toString(landmark.getCreationDate().getTime()));
                }
                jsonObject.put("desc", desc);
            } else {
                jsonObject.put("desc", url);
            }

            jsonArray.add(jsonObject);
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }
    
    @Override
	public List<ExtendedLandmark> processBinaryRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String layer, String flexString2, Locale locale) throws Exception {
		String key = getCacheKey(getClass(), "processBinaryRequest", latitude, longitude, query, radius, version, limit, stringLimit, layer, flexString2);
		List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>)CacheUtil.getObject(key);
        if (landmarks == null) {
            List<Landmark> landmarkList = LandmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(latitude, longitude, layer, limit, radius);
            Collection<Landmark> results = Collections2.filter(landmarkList, new QueryPredicate(query));
            landmarks = new ArrayList<ExtendedLandmark>();            
            landmarks.addAll(Collections2.transform(results, new LandmarkTransformFunction(layer, locale)));

            if (!landmarks.isEmpty()) {
                CacheUtil.put(key, landmarks);
                logger.log(Level.INFO, "Adding GMS landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading GMS landmark list from cache with key {0}", key);
        }
        logger.log(Level.INFO, "Returning " + landmarks.size() + " landmarks for layer " + layer + " ...");

        return landmarks;
	}
	
	private class QueryPredicate implements Predicate<Landmark> {

		String query = null;
		
		public QueryPredicate(String query) {
			this.query = query;
		}
		
		@Override
		public boolean apply(Landmark l) {
			return (query == null || StringUtils.containsIgnoreCase(l.getName(), query) || StringUtils.containsIgnoreCase(l.getDescription(), query)); 
		}
		
	}
	
	private class LandmarkTransformFunction implements Function<Landmark, ExtendedLandmark> {
		private String layer = null;
		private Locale locale = null;
		
		public LandmarkTransformFunction(String layer, Locale locale) {
			this.layer = layer;
			this.locale = locale;
		}
		
		@Override
		public ExtendedLandmark apply(Landmark source) {
			QualifiedCoordinates qc = new QualifiedCoordinates(source.getLatitude(), source.getLongitude(), 0f, 0f, 0f);
	    	String name = source.getName();
	    	long creationDate = source.getCreationDate().getTime();	
	    	String url = UrlUtils.getLandmarkUrl(source);	        
	    	Map<String, String> tokens = new HashMap<String, String>();
	    	tokens.put("description", source.getDescription());	    	
	    	ExtendedLandmark target = LandmarkFactory.getLandmark(name, null, qc, layer, new AddressInfo(), creationDate, null);
	    	target.setUrl(url);
	    	target.setDescription(JSONUtils.buildLandmarkDesc(target, tokens, locale));
	    	return target;
		}
	}
}
