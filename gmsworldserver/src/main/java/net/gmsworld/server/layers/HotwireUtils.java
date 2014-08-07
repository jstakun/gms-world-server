/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gmsworld.server.layers;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.config.Commons.Property;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.memcache.CacheUtil;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class HotwireUtils extends LayerHelper {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    protected JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
        String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, radius, version, limit, stringLimit, flexString, flexString2);

        String output = CacheUtil.getString(key);
        JSONObject json = null;

        if (output == null) {
            URL hotwireUrl = new URL("http://api.hotwire.com/v1/deal/hotel?dest=" + latitude + "," + longitude + "&distance=*~" + radius + "&apikey=" + Commons.getProperty(Property.HOTWIRE_KEY) + "&format=json&sort=price&starrating=3~*&&daystoarrival=0~2&sortorder=asc&limit=" + limit); //&diversity=city
            String hotwireResponse = HttpUtils.processFileRequest(hotwireUrl);

            json = createCustomJsonHotwireList(hotwireResponse, version);
            if (json.getJSONArray("ResultSet").length() > 0) {
                CacheUtil.put(key, json.toString());
                logger.log(Level.INFO, "Adding HW landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading HW landmark list from cache with key {0}", key);
            json = new JSONObject(output);
        }

        return json;
    }

    private static JSONObject createCustomJsonHotwireList(String hotwireJson, int version) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();

        if (StringUtils.startsWith(hotwireJson, "{")) {
            try {
                JSONObject jsonRoot = new JSONObject(hotwireJson);
                JSONArray errors = jsonRoot.optJSONArray("Errors");

                if (errors == null || errors.length() == 0) {
                    JSONArray result = jsonRoot.optJSONArray("Result");

                    if (result != null) {
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject deal = result.getJSONObject(i);
                            jsonArray.add(createHotwireJsonObject(deal, version));
                        }
                    } else {
                        JSONObject deal = jsonRoot.optJSONObject("Result");

                        if (deal != null) {
                            jsonArray.add(createHotwireJsonObject(deal.getJSONObject("HotelDeal"), version));
                        }

                    }
                }

            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

    private static Map<String, Object> createHotwireJsonObject(JSONObject deal, int version) throws JSONException, ParseException {
        Map<String, Object> jsonObject = new HashMap<String, Object>();

        jsonObject.put("name", deal.getString("Headline"));
        jsonObject.put("lat", deal.getString("NeighborhoodLatitude"));
        jsonObject.put("lng", deal.getString("NeighborhoodLongitude"));
        jsonObject.put("url", deal.getString("Url"));

        Map<String, String> desc = new HashMap<String, String>();
        desc.put("price", deal.getString("Price") + " " + deal.getString("CurrencyCode"));
        desc.put("location", deal.getString("Neighborhood"));
        desc.put("star_rating", deal.getString("StarRating"));
        desc.put("city", deal.getString("City"));

        if (version == 3) {
            //08/23/2011
            JSONUtils.putOptDate(desc, "start_date", deal, "StartDate", formatter);
            JSONUtils.putOptDate(desc, "end_date", deal, "EndDate", formatter);
        } else {
            desc.put("start_date", deal.getString("StartDate"));
            desc.put("end_date", deal.getString("EndDate"));
        }

        jsonObject.put("desc", desc);

        return jsonObject;
    }

	@Override
	protected List<ExtendedLandmark> processBinaryRequest(double lat, double lng,
			String query, int radius, int version, int limit, int stringLimit,
			String flexString, String flexString2, Locale locale)
			throws Exception {
		throw new Exception("Not yet implemented");
	}
}
