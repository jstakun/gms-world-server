/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.NumberUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class QypeUtils extends LayerHelper {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String lang, java.lang.String flexString2) throws Exception {
        int r = NumberUtils.normalizeNumber(radius, 1, 10);
        String key = getCacheKey(QypeUtils.class, "processRequest", lat, lng, query, r, version, limit, stringLimit, lang, null);
        JSONObject json = null;

        String cachedResponse = CacheUtil.getString(key);
        if (cachedResponse == null) {

            List<Object> venueArray = new ArrayList<Object>();
            
            /*Map<Integer, Thread> venueDetailsThreads = new ConcurrentHashMap<Integer, Thread>();
            ThreadFactory qypeThreadFactory = ThreadManager.currentRequestThreadFactory();
            int page = 1;

            while (page * MAX_PAGES <= limit) {
                Thread venueDetailsRetriever = qypeThreadFactory.newThread(new VenueDetailsRetriever(venueDetailsThreads, venueArray,
                        lat, lng, query, r, page, version, stringLimit, lang));
                venueDetailsThreads.put(page, venueDetailsRetriever);
                venueDetailsRetriever.start();

                page++;
            }

            ThreadUtil.waitForLayers(venueDetailsThreads);*/

            String qypeJson = processRequest(lat, lng, radius, query, lang, limit); //limit max = 100
            createCustomJsonQypeList(qypeJson, venueArray, stringLimit, version);

            json = new JSONObject().put("ResultSet", venueArray);

            if (!venueArray.isEmpty()) {
                CacheUtil.put(key, json.toString());
                logger.log(Level.INFO, "Adding Qype landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading Qype landmark list from cache with key {0}", key);
            json = new JSONObject(cachedResponse);
        }

        return json;
    }

    private static String processRequest(double latitude, double longitude, int radius, String query, int page, String lang) throws MalformedURLException, IOException, JSONException {
        String urlString = "http://api.qype.com/v1/positions/" + latitude + "," + longitude + "/places?"
                + "consumer_key=" + Commons.qype_consumer_key + "&radius=" + radius + "&lang=" + lang + "&order=distance"; //rating

        if (StringUtils.isNotEmpty(query)) {
            urlString += "&show=" + URLEncoder.encode(query, "UTF-8");
        }

        if (page > 1) {
            urlString += "&page=" + page;
        }

        //System.out.println(urlString);

        String json = HttpUtils.processFileRequest(new URL(urlString), "GET", "application/json", null);

        return json;
    }

    private static String processRequest(double latitude, double longitude, int radius, String query, String lang, int per_page) throws MalformedURLException, IOException, JSONException {
        String urlString = "http://api.qype.com/v1/positions/" + latitude + "," + longitude + "/places?"
                + "consumer_key=" + Commons.qype_consumer_key + "&radius=" + radius + "&lang=" + lang + "&per_page=" + per_page + "&order=distance"; //rating

        if (StringUtils.isNotEmpty(query)) {
            urlString += "&show=" + URLEncoder.encode(query, "UTF-8");
        }

        //System.out.println(urlString);

        String json = HttpUtils.processFileRequest(new URL(urlString), "GET", "application/json", null);

        return json;
    }

    private static int createCustomJsonQypeList(String qypeJson, List<Object> jsonArray, int stringLimit, int version) throws JSONException, ParseException {
        int total = 0;

        if (StringUtils.startsWith(qypeJson, "{")) {
            JSONObject jsonRoot = new JSONObject(qypeJson);
            if (jsonRoot.has("total_entries")) {
                total = jsonRoot.getInt("total_entries");
                if (total > 0) {
                    JSONArray results = jsonRoot.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject place = results.getJSONObject(i);

                        JSONObject entry = place.getJSONObject("place");

                        String point = entry.optString("point");

                        if (StringUtils.contains(point, ",")) {
                            String[] coords = point.split(",");

                            if (coords.length > 1) {
                                Map<String, Object> jsonObject = new HashMap<String, Object>();

                                String name = entry.optString("title", "");

                                jsonObject.put("name", name);
                                jsonObject.put("lat", coords[0]);
                                jsonObject.put("lng", coords[1]);

                                Map<String, String> desc = new HashMap<String, String>();

                                JSONArray links = entry.getJSONArray("links");
                                //links
                                int numberOfReviews = 0;
                                for (int j = 0; j < links.length(); j++) {
                                    JSONObject link = links.getJSONObject(j);
                                    if (StringUtils.equals(link.getString("rel"), "alternate")) {
                                        jsonObject.put("url", link.getString("href"));
                                    } else if (StringUtils.equals(link.getString("rel"), "http://schemas.qype.com/reviews")) {
                                        numberOfReviews += link.getInt("count");
                                    }
                                }

                                if (numberOfReviews > 0) {
                                    desc.put("numberOfReviews", Integer.toString(numberOfReviews));
                                }

                                JSONUtils.putOptValue(desc, "phone", entry, "phone", false, stringLimit, false);

                                int rating = entry.optInt("average_rating", -1);
                                if (rating > 0) {
                                    desc.put("rating", Integer.toString(rating));
                                }

                                //created : "2009-02-12T18:58:08+01:00"
                                String creationDate = entry.optString("created");
                                if (creationDate != null) {

                                    creationDate = StringUtils.substring(creationDate, 0, 19).replace("T", " ");
                                    Date created = formatter.parse(creationDate);
                                    creationDate = Long.toString(created.getTime());
                                    desc.put("creationDate", creationDate);
                                }

                                try {
                                    String url = entry.optString("url");
                                    if (StringUtils.startsWith(url, "http://www.booking.com")) {
                                        url = StringUtils.split(url, "?")[0].replaceFirst("http://www.", "http://m.") + "?aid=340630";
                                        desc.put("homepage", url);
                                    } else {
                                        JSONUtils.putOptValue(desc, "homepage", entry, "url", false, stringLimit, false);
                                    }
                                } catch (Exception e) {
                                    JSONUtils.putOptValue(desc, "homepage", entry, "url", false, stringLimit, false);
                                }


                                //add categories
                                JSONArray categories = entry.optJSONArray("categories");
                                if (categories != null) {
                                    String catlist = "";
                                    for (int j = 0; j < categories.length(); j++) {
                                        JSONObject cat = categories.getJSONObject(j);
                                        JSONObject title = cat.getJSONObject("title");
                                        if (catlist.length() > 0) {
                                            catlist += ", ";
                                        }
                                        catlist += title.getString("value");
                                    }
                                    if (catlist.length() > 0) {
                                        desc.put("category", catlist);
                                    }
                                }

                                JSONObject address = entry.getJSONObject("address");
                                JSONUtils.putOptValue(desc, "city", address, "city", false, stringLimit, false);
                                JSONUtils.putOptValue(desc, "postcode", address, "zip", false, stringLimit, false);
                                String addressStr = address.optString("street");
                                if (StringUtils.isNotEmpty(addressStr)) {
                                    addressStr += " " + address.optString("housenumber", "");
                                }

                                if (version >= 2) {
                                    JSONObject image = entry.optJSONObject("image");
                                    if (image != null) {
                                        String icon = image.getString("medium"); //small
                                        if (StringUtils.isNotEmpty(icon)) {
                                            desc.put("icon", icon);
                                        }
                                    }
                                }

                                jsonObject.put("desc", desc);
                                jsonArray.add(jsonObject);
                            }
                        }
                    }
                }
            }
        }

        return total;
    }

    private static class VenueDetailsRetriever implements Runnable {

        private Map<Integer, Thread> venueDetailsThreads;
        private List<Object> venueArray;
        private double lat, lng;
        private int radius, page, stringLimit, version;
        private String query, lang;

        public VenueDetailsRetriever(Map<Integer, Thread> venueDetailsThreads, List<Object> venueArray,
                double lat, double lng, String query, int radius,
                int page, int version, int stringLimit, String lang) {
            this.venueDetailsThreads = venueDetailsThreads;
            this.venueArray = venueArray;
            this.lat = lat;
            this.lng = lng;
            this.query = query;
            this.lang = lang;
            this.radius = radius;
            this.page = page;
            this.version = version;
            this.stringLimit = stringLimit;
        }

        @Override
        public void run() {
            try {
                String qypeJson = processRequest(lat, lng, radius, query, page, lang);
                createCustomJsonQypeList(qypeJson, venueArray, stringLimit, version);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "VenueDetailsRetriever.run exception:", e);
            } finally {
                venueDetailsThreads.remove(page);
            }
        }
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
