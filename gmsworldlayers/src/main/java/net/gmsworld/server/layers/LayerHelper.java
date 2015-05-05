package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.ThreadProvider;
import net.gmsworld.server.utils.memcache.CacheProvider;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;

/**
 *
 * @author jstakun
 */
public abstract class LayerHelper {

    protected static final Logger logger = Logger.getLogger(LayerHelper.class.getName());
    protected ThreadProvider threadProvider = null;
    protected CacheProvider cacheProvider = null;
	
	public void setThreadProvider(ThreadProvider threadProvider){
		this.threadProvider = threadProvider;
	}
	
	public void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}
    
    protected JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
    	return null;
    }

    protected abstract List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception;
    
    public void serialize(List<ExtendedLandmark> landmarks, OutputStream out, int version) {
    	ObjectOutputStream outObj = null;
    	DeflaterOutputStream compressor = null;
    	try {
    		if (version >= 12) {
    			compressor = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION, false));
    			outObj = new ObjectOutputStream(compressor);
    		} else {
    			outObj = new ObjectOutputStream(out);
    		}
    		
    		if (version >= 11) {
    			//Externalization
    			outObj.writeInt(landmarks.size());
    			if (!landmarks.isEmpty()) {
    				for (ExtendedLandmark landmark : landmarks) {
    					if (landmark != null) {
    						landmark.writeExternal(outObj);
    					}
    				}
    			}
    			outObj.flush();
    			
    		} else {
    			//Serialize
    			outObj.writeObject(landmarks);
    			//out.flush();
    		}
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	} finally {	
    		if (outObj != null) {
    			try {
    				outObj.close();
    			} catch (IOException e) {
    				
    			}
    			try {
    				if (compressor != null) {
    					compressor.close();
    				}
    			} catch (IOException e) {
    				
    			}
    			try {
    				out.close();
    			} catch (IOException e) {
    				
    			}
    		}
    	}
    }
    
    protected String getCacheKey(Class<?> clazz, String methodName, double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws UnsupportedEncodingException {
        List<String> params = new ArrayList<String>(12);

        params.add(clazz.getName());
        if (StringUtils.isNotEmpty(methodName)) {
            params.add(methodName);
        }

        params.add(StringUtil.formatCoordE2(lat));
        params.add(StringUtil.formatCoordE2(lng));

        if (StringUtils.isNotEmpty(query)) {
            params.add(URLEncoder.encode(query, "UTF-8"));
        }

        params.add(Integer.toString(radius));
        params.add(Integer.toString(version)); 
        params.add(Integer.toString(limit)); 
        params.add(Integer.toString(stringLimit)); 
        
        if (StringUtils.isNotEmpty(flexString)) {
            params.add(flexString);
        }
       
        if (StringUtils.isNotEmpty(flexString2)) {
            params.add(flexString2); 
        }

        return StringUtils.join(params, "_");
    }
    
    public String cacheGeoJson(List<ExtendedLandmark> landmarks, double lat, double lng, String layer) {
    	
    	/*{
  			"type": "Feature",
  			"geometry": {
    			"type": "Point",
    			"coordinates": [125.6, 10.1]
  			},
  			"properties": {
    			"name": "Dinagat Islands"
  			}
		}*/

    	FeatureCollection featureCollection = new FeatureCollection();
		featureCollection.setProperty("layer", layer);
		
		if (!landmarks.isEmpty()) {
    		
			for (ExtendedLandmark landmark : landmarks) {
    			Feature f = new Feature();
    			Point p = new Point();
    			p.setCoordinates(new LngLatAlt(landmark.getQualifiedCoordinates().getLongitude(), landmark.getQualifiedCoordinates().getLatitude()));
    			f.setGeometry(p);
    			f.setProperty("name", StringEscapeUtils.escapeJavaScript(landmark.getName()));
    			if (StringUtils.equals(layer, Commons.FACEBOOK_LAYER)) {
    				f.setProperty("url", StringUtils.replace(landmark.getUrl(), "touch", "www")); 
    			} else if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
    				f.setProperty("url", StringUtils.replace(landmark.getUrl(), "&Mobile=1", "")); 
    				//<img src=\"star_blue\" alt=\"*\"/> <img src=\"star_blue\" alt=\"*\"/> 
    				//<img src=\"star_blue\" alt=\"*\"/> <img src=\"star_blue\" alt=\"*\"/> 
    				//<br/>Price: <font color=\"green\">841.98 NOK</font><br/>Address: Lieng 11, Kolbotn, Oppegard, Norway
    				//<br/>Creation date: 1 year ago,<br/>Users rating: <b>6.87</b>, <img src=\"star_4\" alt=\"****\"/>, 33 reviews
    				f.setProperty("desc", StringUtils.replace(landmark.getDescription(), "star_blue", "/images/star_blue.png"));
    				//TODO replace from star_0 to star_5
        		} 
    			f.setProperty("mobile_url", landmark.getUrl());
    			featureCollection.add(f);
    		}
		}	

    	try {
    			String json = new ObjectMapper().writeValueAsString(featureCollection);
    			if (cacheProvider != null) {
    				String key = "geojson_" + StringUtil.formatCoordE2(lat) + "_" + StringUtil.formatCoordE2(lng) + "_" + layer;
    				logger.log(Level.INFO, "Saved geojson list to cache with key: " + key);
    				cacheProvider.put(key, json, 1);
    			    return key;
    			}
    	} catch (JsonProcessingException e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    	
    	return null;
    }	
    
    public String getGeoJson(double lat, double lng, String layer) {
    	if (cacheProvider != null) {
    		String key = "geojson_" + StringUtil.formatCoordE2(lat) + "_" + StringUtil.formatCoordE2(lng) + "_" + layer;
			return cacheProvider.getString(key);
    	} else {
    		return null;
    	}
    }
    
    public String getLayerName() {
    	return null;
    }
}
