package net.gmsworld.server.layers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.MathUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class PanoramioUtils extends LayerHelper {
	
	private static final String dateFormat = "dd MMMM yyyy";

    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String bbox, String flexString2) throws Exception {
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, bbox, flexString2);

        JSONObject json = null;
        String output = cacheProvider.getString(key);

        if (output == null) {
        	
        	String size = "thumbnail";
        	if (stringLimit == StringUtil.XLARGE) {
        		size = "small";
        	}

            URL panoramioUrl = new URL("http://www.panoramio.com/map/get_panoramas.php?order=popularity&"
                    + "set=full&from=0&to=" + limit + "&" + bbox + "&size=" + size + "&mapfilter=true");

            //System.out.print("calling url: " + panoramioUrl.toString());

            String panoramioResponse = HttpUtils.processFileRequest(panoramioUrl);

            //System.out.print(panoramioResponse);


            json = createCustomJsonPanoramioList(panoramioResponse, version, stringLimit);
            if (json.getJSONArray("ResultSet").length() > 0) {
                cacheProvider.put(key, json.toString());
                logger.log(Level.INFO, "Adding PN landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading PN landmark list from cache with key {0}", key);
            json = new JSONObject(output);
        }

        return json;
    }

    private static JSONObject createCustomJsonPanoramioList(String panoramioJson, int version, int stringLimit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        if (StringUtils.startsWith(panoramioJson, "{")) {
            try {

                //if (jsonRoot.has("count")) {
                //    int count = jsonRoot.getInt("count");
                //    logger.log(Level.INFO, "Panoramio pictures count: " + count);
                //}

                JSONArray photos = JSONUtils.getJSonArray(panoramioJson, "photos");
                if (photos != null) {
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject photo = photos.getJSONObject(i);

                        Map<String, Object> jsonObject = new HashMap<String, Object>();

                        jsonObject.put("lat", MathUtils.normalizeE6(photo.getDouble("latitude")));
                        jsonObject.put("lng", MathUtils.normalizeE6(photo.getDouble("longitude")));
                        String url = photo.getString("photo_url");

                        if (version >= 2) {
                            jsonObject.put("url", url.replace(".com", ".com/m"));

                            JSONUtils.putOptValue(jsonObject, "name", photo, "photo_title", false, stringLimit, false);

                            Map<String, String> desc = new HashMap<String, String>();

                            String upload_date = photo.optString("upload_date");
                            if (upload_date != null) {
                                desc.put("upload_date", upload_date); //20 January 2007 new SimpleDateFormat("dd MMMM yyyy, Locale.US);
                            }

                            if (version >= 3) {
                                String photo_file_url = photo.optString("photo_file_url");
                                if (photo_file_url != null) {
                                    desc.put("icon", photo_file_url);
                                }
                            }

                            if (!desc.isEmpty()) {
                                jsonObject.put("desc", desc);
                            }

                        } else {
                            if (photo.has("photo_title")) {
                                jsonObject.put("name", photo.getString("photo_title"));
                            }
                            jsonObject.put("desc", url.replace(".com", ".com/m"));
                        }
                        jsonArray.add(jsonObject);
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String bbox, String flexString2, Locale locale, boolean useCache) throws Exception {
		String size = "thumbnail";
    	if (stringLimit == StringUtil.XLARGE) {
    		size = "small";
    	}
		URL panoramioUrl = new URL("http://www.panoramio.com/map/get_panoramas.php?order=popularity&"
                    + "set=full&from=0&to=" + limit + "&" + bbox + "&size=" + size + "&mapfilter=true");
		String panoramioResponse = HttpUtils.processFileRequest(panoramioUrl);
        return createLandmarkPanoramioList(panoramioResponse, stringLimit, locale);    
	}
	
	private static List<ExtendedLandmark> createLandmarkPanoramioList(String panoramioJson, int stringLimit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();

        if (StringUtils.startsWith(panoramioJson, "{")) {
            try {
                JSONArray photos = JSONUtils.getJSonArray(panoramioJson, "photos");
                if (photos != null) {
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject photo = photos.getJSONObject(i);

                        double lat = photo.getDouble("latitude");
                        double lng = photo.getDouble("longitude");
                        String url = photo.getString("photo_url");
                        
                        if (stringLimit != StringUtil.XLARGE) {
                        	url = url.replace(".com", ".com/m");
                        }

                        String name = photo.optString("photo_title", "No name");
                        
                        Map<String, String> tokens = new HashMap<String, String>();

                        long creationDate = -1;
                        String upload_date = photo.optString("upload_date");
                        if (upload_date != null) {
                        	creationDate = DateUtils.parseDate(dateFormat, upload_date).getTime(); //20 January 2007 
                        }
                        
                        QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                        ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.PANORAMIO_LAYER, new AddressInfo(), creationDate, null);
                        landmark.setUrl(url);

                        String photo_file_url = photo.optString("photo_file_url");
                        if (photo_file_url != null) {
                           landmark.setThumbnail(photo_file_url);
                        }
                           
                        String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                        landmark.setDescription(description);
						
                        landmarks.add(landmark);
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return landmarks;
    }
	
	public String getLayerName() {
    	return Commons.PANORAMIO_LAYER;
    }
	
	public String getURI() {
		return "panoramio2Provider";
	}
}
