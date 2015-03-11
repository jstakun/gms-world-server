package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.NumberUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

public class InstagramUtils extends LayerHelper {

	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws MalformedURLException, IOException {
		int normalizedDistance = NumberUtils.normalizeNumber(radius, 1000, 5000);
		int normalizedLimit = NumberUtils.normalizeNumber(limit, 30, 100);
		String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, normalizedDistance, version, normalizedLimit, stringLimit, flexString, flexString2);
		List<ExtendedLandmark> landmarks = (List<ExtendedLandmark>) cacheProvider.getObject(key);
		
		if (landmarks == null) {
			landmarks = new ArrayList<ExtendedLandmark>();
			if (lat != 0.0d && lng != 0.0d) {
				String instagramUrl = "https://api.instagram.com/v1/media/search?lat=" + lat + "&lng=" + lng + "&distance=" + normalizedDistance + "&count=" + normalizedLimit + "&client_id=" + Commons.getProperty(Property.INSTAGRAM_CLIENT_ID);			
				String instagramJson = HttpUtils.processFileRequest(new URL(instagramUrl));		
				createCustomJsonInstagramList(landmarks, instagramJson, stringLimit, locale);		 	
				if (!landmarks.isEmpty()) {
					cacheProvider.put(key, landmarks);
				}
			}
		} else {
			logger.log(Level.INFO, "Reading INS landmark list from cache with key {0}", key); 
		}
		logger.log(Level.INFO, "Found {0} landmarks", landmarks.size()); 
		
		return landmarks;
   }
	
   private static void createCustomJsonInstagramList(List<ExtendedLandmark> landmarks, String instagramJson, int stringLimit, Locale locale) {	   
	   if (StringUtils.startsWith(instagramJson, "{")) {
           try {
               JSONObject jsonRoot = new JSONObject(instagramJson);
               JSONObject meta = jsonRoot.getJSONObject("meta");
               int code = meta.getInt("code");
               if (code == 200) {
            	   JSONArray data = jsonRoot.getJSONArray("data");
            	   for (int i=0;i<data.length();i++) {
            		   JSONObject media = data.getJSONObject(i); 
            		   String type = media.getString("type");
            		   String name = type;
            		   JSONObject location = media.getJSONObject("location");
            		   QualifiedCoordinates qc = new QualifiedCoordinates(location.getDouble("latitude"), location.getDouble("longitude"), 0f, 0f, 0f);
            		   if (location.has("name")) {
            			   name = location.getString("name");
            		   }
            		   
            		   //System.out.println(new JSONObject(qc).toString());
            		   long creationDate = media.getLong("created_time") * 1000;
            		   JSONObject images = media.getJSONObject("images");
            		   JSONObject thumbnail = images.getJSONObject("thumbnail");
            		   String url = media.getString("link");
            		   String icon = thumbnail.getString("url");
            		    
            		   if (!media.isNull("caption")) {
            			   JSONObject caption = media.getJSONObject("caption");
            			   name = caption.getString("text");
            		   }         		   
            		   
            		   JSONObject comments = media.getJSONObject("comments");
            		   int reviews = comments.getInt("count");
            		   
            		   //desc
            		   Map <String, String> tokens = new HashMap<String, String>();
            		   JSONObject user = media.getJSONObject("user");
            		   String username = user.getString("username");
            		   if (user.has("full_name")) {
            			   username = user.getString("full_name");
            			   if (StringUtils.isEmpty("username")) {
            				   username = "anonymous";
            			   }
            			   tokens.put("artist", username);
            		   }
            		   
            		   JSONObject likes = media.getJSONObject("likes");
            		   int count = likes.getInt("count");
            		   
            		   if (count > 0) {
            			   tokens.put("likes", Integer.toString(count));
            		   }
            		   
            		   ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.INSTAGRAM_LAYER, new AddressInfo(), creationDate, null);
            		   landmark.setUrl(url);
            		   landmark.setThumbnail(icon);
            		   landmark.setNumberOfReviews(reviews);
            		   String desc = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
            		   landmark.setDescription(desc);		   
                    		   
            		   landmarks.add(landmark);
            	   }
               } else {
            	   logger.log(Level.SEVERE, "Received Instagram response: " + instagramJson);
               }
           
           } catch (JSONException ex) {
               logger.log(Level.SEVERE, null, ex);
           }           
	   }
   }
   
   public String getLayerName() {
   		return Commons.INSTAGRAM_LAYER;
   }

}
