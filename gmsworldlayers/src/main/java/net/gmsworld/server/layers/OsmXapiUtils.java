package net.gmsworld.server.layers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.xml.ParserManager;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import osm.OSMFile;
import osm.parser.OSMSaxParser;
import osm.primitive.node.Node;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class OsmXapiUtils extends LayerHelper {
	
	private String amenity;

    private static OSMFile getAmenities(String amenity, String bbox) throws IOException {

        String xapiUrl = "http://open.mapquestapi.com/xapi/api/0.6/node" + URLEncoder.encode("[amenity=" + amenity + "][bbox=" + bbox + "]", "UTF-8") + "?key=" + Commons.getProperty(Property.MAPQUEST_APPKEY);
        OSMFile file = null;
        URL fileUrl = new URL(xapiUrl);
        String response = HttpUtils.processFileRequest(fileUrl);
        Integer responseCode = HttpUtils.getResponseCode(xapiUrl);

        if (responseCode != null && responseCode == HttpServletResponse.SC_OK && response != null) {
            OSMSaxParser parser = new OSMSaxParser();
            ParserManager pm = new ParserManager(parser);
            pm.parseInputStream(new ByteArrayInputStream(response.getBytes()));       
            file = parser.getOSMFile();
        } else if (response != null) {
        	logger.log(Level.WARNING, "Received following server response: " + response);
        } else {
        	logger.log(Level.WARNING, "Received empty server response");
        }

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

    	this.amenity = amenity;
        String key = getCacheKey(getClass(), "processRequest", 0, 0, query, radius, version, limit, stringLimit, amenity, bbox);

        String output = cacheProvider.getString(key);

        if (output == null) {
            OSMFile file = getAmenities(amenity.toLowerCase(), bbox);
            if (file != null) {
            	output = buildJsonList(file, limit, StringUtils.capitalize(amenity));
            	if (file.getNodeCount() > 0) {
            		cacheProvider.put(key, output);
            		logger.log(Level.INFO, "Adding OSM landmark list to cache with key {0}", key);
            	}
            } else {
            	logger.log(Level.WARNING, "OSMFile is empty!");
            }
        } else {
            logger.log(Level.INFO, "Reading OSM landmark list from cache with key {0}", key);
        }

        if (output != null) {
        	return new JSONObject(output);
        } else {
        	return new JSONObject().put("ResultSet", new ArrayList<String>());
        }
    }
    
    private static void putOptValue(Node node, String name, Map<String, String> outMap, String outName) {
        String nodeVal = node.getTagValue(name);
        if (nodeVal != null) {
            outMap.put(outName, nodeVal);
        }
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String amenity, String bbox, Locale locale, boolean useCache)	throws Exception {
		this.amenity = amenity;
        List<ExtendedLandmark> output = new ArrayList<ExtendedLandmark>();
        
        if (bbox == null) {
        	double[] dcoords = new double[]{lng, lat, lng, lat};
        
        	if ((dcoords[2] - dcoords[0]) < 0.1) {
        		if (dcoords[2] < 180.0) {
        			dcoords[2] += 0.1;
        		}
        		if (dcoords[0] > -180.0) {
        			dcoords[0] -= 0.1;
        		}
        	}
        
        	if ((dcoords[3] - dcoords[1]) < 0.1) {
        		if (dcoords[3] < 90.0) {
        			dcoords[3] += 0.1;
        		}
        		if (dcoords[1] > -90.0) {
        			dcoords[1] -= 0.1;
        		}
        	}
        
        	bbox = String.format("%.2f", MathUtils.normalizeE2(dcoords[0])) + "," +
        		String.format("%.2f", MathUtils.normalizeE2(dcoords[1])) + "," +
        		String.format("%.2f", MathUtils.normalizeE2(dcoords[2])) + "," +
        		String.format("%.2f", MathUtils.normalizeE2(dcoords[3]));  
        }
        
        if (amenity != null && bbox != null) {
            OSMFile file = getAmenities(amenity.toLowerCase(), bbox);
            if (file != null) {
            	buildLandmarksList(output, file, limit, StringUtils.capitalize(amenity), locale);
            } else {
            	logger.log(Level.WARNING, "OSMFile is empty!");
            }
        } else {
            logger.log(Level.WARNING, "Parameters can't be null! Amenity: " + amenity + " , bbox: " + bbox);
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
	
	public String getLayerName() {
		if (amenity == null) {
			return Commons.OSM_ATM_LAYER;
		} else {
			return amenity;
		}
    }
}
