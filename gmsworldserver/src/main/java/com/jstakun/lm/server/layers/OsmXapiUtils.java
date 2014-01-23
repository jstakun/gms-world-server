/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import osm.OSMFile;
import osm.parser.OSMSaxParser;
import osm.primitive.node.Node;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.xml.ParserManager;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class OsmXapiUtils extends LayerHelper {

    private static OSMFile getAmenities(String amenity, String bbox) throws IOException {

        String xapiUrl = "http://open.mapquestapi.com/xapi/api/0.6/node" + URLEncoder.encode("[amenity=" + amenity + "][bbox=" + bbox + "]", "UTF-8");
        OSMFile file = null;
        //System.out.println(xapiUrl);
        
        URL fileUrl = new URL(xapiUrl);
        HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
        
        conn.connect();
        
        int responseCode = conn.getResponseCode();

        if (responseCode == HttpServletResponse.SC_OK) {
            InputStream is = conn.getInputStream();
            OSMSaxParser parser = new OSMSaxParser();
            ParserManager pm = new ParserManager(parser);
            pm.parseInputStream(is);       
            file = parser.getOSMFile();
        } else {
            logger.log(Level.SEVERE, "Received following response from osm server: " + conn.getResponseCode() + " " + conn.getResponseMessage());
        }

        //OSMSaxParser parser = new OSMSaxParser();
        //ParserManager pm = new ParserManager(parser);
        //pm.parseUri(xapiUrl);
        //file = parser.getOSMFile();

        return file;
    }

    private static String buildJsonList(OSMFile file, int limit, String defaultName) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
        int i = 0;
        for (Iterator<Node> nodeIterator = file.getNodeIterator(); nodeIterator.hasNext();) {
            Map<String, Object> jsonObject = new HashMap<String, Object>();
            Node node = nodeIterator.next();
            i++;
            jsonObject.put("lat", node.getLat());
            jsonObject.put("lng", node.getLon());
            String name = node.getTagValue("name");
            if (name == null) {
                name = node.getTagValue("operator");
            }
            if (name == null) {
                name = defaultName;
            }
            jsonObject.put("name", name);
            jsonObject.put("url", "");

            Map<String, String> desc = new HashMap<String, String>();

            String address = node.getTagValue("addr:street");

            if (address != null) {
                String number = node.getTagValue("addr:housenumber");
                if (number != null) {
                    address += " " + number;
                }
            }

            if (address != null) {
                desc.put("address", address);
            }

            putOptValue(node, "addr:city", desc, "city");
            putOptValue(node, "operator", desc, "operator");
            putOptValue(node, "opening_hours", desc, "opening_hours");

            if (!desc.isEmpty()) {
                jsonObject.put("desc", desc);
            }

            jsonArray.add(jsonObject);

            if (i >= limit) {
                break;
            }
        }

        return JSONUtils.getJsonArrayObject(jsonArray).toString();
    }

    @Override
    public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String amenity, String bbox) throws Exception {
        //String key = "OSM_" + bbox + "_" + limit + "_" + amenity;

        String key = getCacheKey(getClass(), "processRequest", 0, 0, query, radius, version, limit, stringLimit, amenity, bbox);

        String output = CacheUtil.getString(key);

        if (output == null) {
            OSMFile file = getAmenities(amenity.toLowerCase(), bbox);
            if (file != null) {
            	output = buildJsonList(file, limit, StringUtils.capitalize(amenity));
            	if (file.getNodeCount() > 0) {
            		CacheUtil.put(key, output);
            		logger.log(Level.INFO, "Adding OSM landmark list to cache with key {0}", key);
            	}
            } else {
            	logger.log(Level.WARNING, "OSMFile is null!");
            }
        } else {
            logger.log(Level.INFO, "Reading OSM landmark list from cache with key {0}", key);
        }

        return new JSONObject(output);
    }
    
    private static void putOptValue(Node node, String name, Map<String, String> outMap, String outName) {
        String nodeVal = node.getTagValue(name);
        if (nodeVal != null) {
            outMap.put(outName, nodeVal);
        }
    }

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String amenity, String bbox, Locale locale)	throws Exception {
		String key = getCacheKey(getClass(), "processBinaryRequest", 0, 0, query, radius, version, limit, stringLimit, amenity, bbox);

		List<ExtendedLandmark> output = (List<ExtendedLandmark>)CacheUtil.getObject(key);

        if (output == null) {
        	output = new ArrayList<ExtendedLandmark>();
            OSMFile file = getAmenities(amenity.toLowerCase(), bbox);
            if (file != null) {
            	buildLandmarksList(output, file, limit, StringUtils.capitalize(amenity), locale);
            	if (!output.isEmpty()) {
            		CacheUtil.put(key, output);
            		logger.log(Level.INFO, "Adding OSM landmark list to cache with key {0}", key);
            	}
            } else {
            	logger.log(Level.SEVERE, "OSMFile is null!");
            }
        } else {
            logger.log(Level.INFO, "Reading OSM landmark list from cache with key {0}", key);
        }

        return output;
	}

	private static void buildLandmarksList(List<ExtendedLandmark> landmarks, OSMFile file, int limit, String defaultName, Locale locale) throws JSONException {
		String amenity = Commons.OSM_ATM_LAYER;
        if (defaultName.toLowerCase().equals("parking")){
            amenity = Commons.OSM_PARKING_LAYER;
        }
		
        for (Iterator<Node> nodeIterator = file.getNodeIterator(); nodeIterator.hasNext();) {
            Node node = nodeIterator.next();
            double lat = node.getLat();
            double lng = node.getLon();
            String name = node.getTagValue("name");
            if (name == null) {
                name = node.getTagValue("operator");
            }
            if (name == null) {
                name = defaultName;
            }
            
            Map<String, String> tokens = new HashMap<String, String>();

            AddressInfo address = new AddressInfo();
            String val = node.getTagValue("addr:street");
            if (val != null) {
            	String number = node.getTagValue("addr:housenumber");
                if (number != null) {
                    val += " " + number;
                }
            	address.setField(AddressInfo.STREET, val);
            }	
            val = node.getTagValue("addr:city");
            if (val != null) {
            	address.setField(AddressInfo.CITY, val);
            }

            putOptValue(node, "operator", tokens, "operator");
            putOptValue(node, "opening_hours", tokens, "opening_hours");

            QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
            ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, amenity, address, -1, null);
            
            String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
            landmark.setDescription(description);
			
            landmarks.add(landmark);
            
            if (landmarks.size() >= limit) {
                break;
            }
        }
    }
}
