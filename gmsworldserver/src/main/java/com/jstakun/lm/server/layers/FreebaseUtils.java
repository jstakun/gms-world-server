package com.jstakun.lm.server.layers;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.freebase.Freebase;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.Commons.Property;
import com.jstakun.lm.server.config.ConfigurationManager;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

public class FreebaseUtils extends LayerHelper {
	
	//2006-10-22T09:16:37.0012Z
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS ");
	
	private static final String IMAGE_PREFIX = "https://usercontent.googleapis.com/freebase/v1/image";
	private static final String URL_PREFIX = ConfigurationManager.SERVER_URL + "freebaseView/";

	//https://www.googleapis.com/freebase/v1/search?indent=true&filter=(all (within radius:10km lon:20.95 lat:52.25))&output=(description name geocode url object property category location)
	
	//https://www.googleapis.com/freebase/v1/search?indent=true&filter=(all mid:/m/05qhw)&output=(description name geocode url object property category location)&limit=1
	
	@Override
	protected List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale) throws Exception {
		String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
		List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>) CacheUtil.getObject(key);
		
		if (landmarks == null) {
			List<String> filter = Arrays.asList("(all (within radius:" + radius +"km lon:" + lng + " lat:" + lat + "))");
			landmarks = search(filter, query, limit, stringLimit, locale);	
			if (!landmarks.isEmpty()) {
				CacheUtil.put(key, landmarks);
			}
		} else {
			logger.log(Level.INFO, "Reading FRB landmark list from cache with key {0}", key); 
		}
		
		return landmarks;
	}

	protected List<ExtendedLandmark> search(List<String> filter, String query, int limit, int stringLimit, Locale locale) throws IOException {
		List<ExtendedLandmark> landmarks;
		landmarks = new ArrayList<ExtendedLandmark>();
		Freebase freebase = getFreebase();
		
		Freebase.Search search = freebase.search();
		
		search.setFilter(filter);
		if (StringUtils.isNotEmpty(query)) {
			search.setQuery(query);
		}
		search.setOutput("(name description geocode url object property)");
		search.setLimit(limit);
		
		//TODO "message" : "Invalid language code(s): 'ur'. Languages served are: en,es,fr,de,it,pt,zh,ja,ko,ru,sv,fi,da,nl,el,ro,tr,hu,th,pl,cs,id,bg,uk,ca,eu,no,sl,sk,hr,sr,ar,hi,vi,fa,ga,iw,lv,lt,fil"
		
		search.setLang(Arrays.asList(locale.getLanguage()));
		
		InputStream is = search.executeAsInputStream();
		if (is != null) {
			String freebaseJson = IOUtils.toString(is, "UTF-8");
			//logger.log(Level.INFO, freebaseJson);
			createCustomJsonFreebaseList(landmarks, freebaseJson, stringLimit, locale);		 	
			
		} else {
			logger.log(Level.WARNING, "Received empty response!");
		}
		return landmarks;
	}
	
	private static Freebase getFreebase() {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential requestInitializer = new GoogleCredential.Builder().setClientSecrets(Commons.getProperty(Property.GL_PLUS_KEY), Commons.getProperty(Property.GL_PLUS_SECRET)).setJsonFactory(jsonFactory).setTransport(httpTransport).build();
        requestInitializer.setAccessToken(Commons.getProperty(Property.gl_plus_token)).setRefreshToken(Commons.getProperty(Property.gl_plus_refresh));
        Freebase freebase = new Freebase.Builder(httpTransport, jsonFactory, requestInitializer).setApplicationName("Landmark Manager").build();
        return freebase;
    }
	
	private static void createCustomJsonFreebaseList(List<ExtendedLandmark> landmarks, String freebaseJson, int stringLimit, Locale locale) {	   
		   if (StringUtils.startsWith(freebaseJson, "{")) {
	           try {
	        	   JSONObject response = new JSONObject(freebaseJson);
	        	   if (StringUtils.equals(response.getString("status"), "200 OK")) {
	        		   JSONArray results = response.getJSONArray("result");
	        		   logger.log(Level.INFO, "Processing " + results.length() + " Freebase records...");
	        		   for (int i=0;i<results.length();i++) {
	        			   
	        			   JSONObject result = results.getJSONObject(i);
	        			   JSONObject output = result.getJSONObject("output");
	        			   
	        			   JSONArray geolocation = output.getJSONObject("geocode").optJSONArray("/location/location/geolocation");
	        			   if (geolocation != null) {
	        				   JSONObject geocode = geolocation.getJSONObject(0);
	        				   double lat = geocode.getDouble("latitude");
	        				   double lng = geocode.getDouble("longitude");
	        			   
	        				   String name = result.getString("name");
	        			   
	        				   String mid = StringUtils.substring(result.getString("mid"), 3);
	        			   
	        				   JSONObject desc = output.getJSONObject("description");
	        				   JSONArray desc_arr = desc.optJSONArray("/common/topic/description"); 
	        			   
	        				   String descr = null;
	        				   if (desc_arr != null) {
	        					   if (stringLimit > 0) {
	        						   descr = StringUtils.abbreviate(desc_arr.getString(0), stringLimit);
	        					   } else {
	        						   descr = desc_arr.getString(0);
	        					   }
	        				   }
	        				   
	        				   JSONObject urlo = output.getJSONObject("url");
	        				   JSONArray url_arr = urlo.optJSONArray("/common/topic/official_website");
	        					   
	        				   long creationDate = -1;
	        				   JSONObject objecto = output.optJSONObject("object");
	        				   if (objecto != null) {
	        					   JSONArray object_arr = objecto.optJSONArray("/type/object/timestamp");
	        			   
	        					   if (object_arr != null) {
	        						   try {
	        							   String str = object_arr.getString(0).replace('T', ' ').replace('Z', ' ');
	        							   Date start = formatter.parse(str);
	        					   		   creationDate = start.getTime();
	        				   			} catch (ParseException e) {
	        				   				logger.log(Level.SEVERE, null, e);
	        				   			}     	   
	        					   }
	        				   }  
	        			   
	        				   QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
	        				   ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.FREEBASE_LAYER, new AddressInfo(), creationDate, null);
	        	           
	        	           		JSONObject propertyo = output.optJSONObject("property");
	        	           		if (propertyo != null) {
	        	        	   		JSONArray property_arr = propertyo.optJSONArray("/common/topic/image");
	        			   
	        			   			if (property_arr != null) {
	        			   				JSONObject image = property_arr.getJSONObject(0);
	        			   				landmark.setThumbnail(IMAGE_PREFIX + image.getString("mid"));
	        			   			}       			   
	        	           		}
	        	           
	        	           		Map<String, String> tokens = new HashMap<String, String>();
	        	           		if (descr != null) {
	        	        	   		tokens.put("description", descr);
	        	           		}	        	           
	        	           
	        	           		if (url_arr != null) {
	        	        	   		//landmark.setUrl(url_arr.getString(0));
	        	        	   		tokens.put("homepage", url_arr.getString(0));
	        	           		}
	        	           
	        	           		landmark.setUrl(URL_PREFIX + mid);
	        	           
	        	           		String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
	        	           		landmark.setDescription(description);
	        				
	        	           		landmarks.add(landmark);
	        			   }
	        		   }
	        	   } else {
	        		   logger.log(Level.SEVERE, "Received Freebase response: " + freebaseJson);
	        	   }
	           } catch (JSONException ex) {
	               logger.log(Level.SEVERE, null, ex);
	           }        
		   }
	}		   
}
