/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class UpcomingUtils extends LayerHelper {

    //2011-09-01 00:00:00
    private static final SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    public JSONObject processRequest(double latitude, double longitude, String query, int radius,  int version, int limit, int stringLimit, String flex, String flexString2) throws IOException, JSONException {
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, flex, flexString2);

        JSONObject json = null;
        String output = CacheUtil.getString(key);
        if (output == null) {
            String upcomingUrl = "http://upcoming.yahooapis.com/services/rest/?method=event.search&api_key=" + Commons.UPCOMING_API_KEY
                    + "&location=" + latitude + "," + longitude + "&radius=" + radius
                    + "&format=json&sort=popular-score-desc&quick_date=next_30_days&per_page=" + limit;
            //&sort=distance-asc");
            //&quick_date=next_14_days");
            if (StringUtils.isNotEmpty(query)) {
                upcomingUrl += "&search_text=" + URLEncoder.encode(query, "UTF-8");
            }

            String upcomingResponse = HttpUtils.processFileRequest(new URL(upcomingUrl));

            //System.out.println(upcomingResponse);

            json = createCustomJsonUpcomingList(upcomingResponse, version, stringLimit);
            if (json.getJSONArray("ResultSet").length() > 0) {
                CacheUtil.put(key, json.toString());
                logger.log(Level.INFO, "Adding UP landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading UP landmark list from cache with key {0}", key);
            json = new JSONObject(output);
        }
        return json;
    }

    private static JSONObject createCustomJsonUpcomingList(String upcomingJson, int version, int stringLimit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
        if (upcomingJson != null && upcomingJson.startsWith("{")) {
            JSONObject jsonRoot = new JSONObject(upcomingJson);
            JSONObject rsp = jsonRoot.getJSONObject("rsp");
            String stat = rsp.getString("stat");
            if (stat.equals("ok")) {
                JSONArray events = rsp.getJSONArray("event");
                
                for (int i = 0; i < events.length(); i++) {
                    try {
                        JSONObject event = events.getJSONObject(i);
                        String url = event.getString("url");
                        if (StringUtils.isEmpty(url)) {
                            url = event.getString("venue_url");
                            if (StringUtils.isEmpty(url)) {
                                url = event.getString("ticket_url");
                            }
                        }

                        if (StringUtils.isNotEmpty(url)) {
                            Map<String, Object> jsonObject = new HashMap<String, Object>();
                            jsonObject.put("name", StringEscapeUtils.unescapeXml(event.getString("name")));
                            jsonObject.put("lat", event.getDouble("latitude"));
                            jsonObject.put("lng", event.getDouble("longitude"));
                            jsonObject.put("url", url);

                            Map<String, String> desc = new HashMap<String, String>();
                            JSONUtils.putOptValue(desc, "description", event, "description", true, stringLimit, true);
                            JSONUtils.putOptValue(desc, "venue", event, "venue_name", true, stringLimit, false);
                            JSONUtils.putOptValue(desc, "address", event, "venue_address", false, stringLimit, false);
                            JSONUtils.putOptValue(desc, "city", event, "venue_city", false, stringLimit, false);
                            JSONUtils.putOptValue(desc, "country", event, "venue_country_name", false, stringLimit, false);
                            if (event.has("venue_zip") && !event.isNull("venue_zip")) {
                                Object zip = event.get("venue_zip");
                                if (StringUtils.isNotEmpty(zip.toString())) {
                                    desc.put("zip", zip.toString());
                                }
                            }

                            if (version >= 3) {
                                if (event.has("start_date") && !event.isNull("start_date") && event.getString("start_date").length() > 0) {
                                    String start_date = event.getString("start_date");
                                    if (event.has("start_time") && !event.isNull("start_time") && event.getString("start_time").length() > 0) {
                                        start_date += " " + event.getString("start_time");
                                        JSONUtils.putOptDate(desc, "start_date", start_date, f1);
                                    } else {
                                        JSONUtils.putOptDate(desc, "start_date", start_date, f2);
                                    }
                                }
                                if (event.has("end_date") && !event.isNull("end_date") && event.getString("end_date").length() > 0) {
                                    String end_date = event.getString("end_date");
                                    if (event.has("end_time") && !event.isNull("end_time") && event.getString("end_time").length() > 0) {
                                        end_date += " " + event.getString("end_time");
                                        JSONUtils.putOptDate(desc, "end_date", end_date, f1);
                                    } else {
                                        JSONUtils.putOptDate(desc, "end_date", end_date, f2);
                                    }
                                }
                            } else {
                                JSONUtils.putOptValue(desc, "start_date", event, "start_date", false, stringLimit, false);
                            }

                            if (version >= 4) {
                                String photo = event.optString("photo_url");
                                if (StringUtils.isNotEmpty(photo)) {
                                    desc.put("icon", photo);
                                }
                            }

                            jsonObject.put("desc", desc);
                            jsonArray.add(jsonObject);
                        }
                    } catch (JSONException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng,
			String query, int radius, int version, int limit, int stringLimit,
			String flexString, String flexString2, Locale locale)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
