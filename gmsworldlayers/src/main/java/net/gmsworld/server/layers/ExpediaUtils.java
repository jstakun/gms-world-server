package net.gmsworld.server.layers;

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
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstakun.gms.android.deals.Deal;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 * 
 * @author jstakun
 */
public class ExpediaUtils extends LayerHelper {

	@Override
	public JSONObject processRequest(double latitude, double longitude, String query, int radius, int version, int limit, int stringLimit, String lang, String flexString2) throws Exception {
		int r = NumberUtils.normalizeNumber(radius, 2, 80);
		String key = getCacheKey(getClass(), "processRequest", latitude, longitude, query, r, version, limit, stringLimit, lang, flexString2);
		String output = cacheProvider.getString(key);
		JSONObject json = null;
		if (output == null) {

			//MessageDigest md = MessageDigest.getInstance("MD5");
		    //long timeInSeconds = (System.currentTimeMillis() / 1000);
		    //String input = Commons.getProperty(Property.EXPEDIA_KEY) + Commons.getProperty(Property.EXPEDIA_SECRET) + timeInSeconds;
		    //md.update(input.getBytes());
		    //String sig = String.format("%032x", new BigInteger(1, md.digest()));
			
			URL expediaUrl = new URL(
					"http://api.ean.com/ean-services/rs/hotel/v3/list?"
							+ "&apiKey=" + Commons.getProperty(Property.EXPEDIA_KEY)
							//+ "&sig=" + sig
							+ "&cid=55505" //+ "&cid=00001"
							+ "&latitude=" + Double.toString(latitude) 
							+ "&longitude=" + Double.toString(longitude) 
							+ "&searchRadius=" + r
							+ "&sort=OVERALL_VALUE" // QUALITY_REVERSE,PRICE,PRICE_AVERAGE,PRICE_REVERSE
							+ "&searchRadiusUnit=KM" 
							+ "&locale=" + lang
							+ "&_type=json");

			//System.out.println(expediaUrl.toString());

			String expediaResponse = HttpUtils.processFileRequest(expediaUrl);

			// System.out.println(expediaResponse);

			json = createCustomJsonExpediaList(expediaResponse, stringLimit, lang, limit);

			if (json.getJSONArray("ResultSet").length() > 0) {
				cacheProvider.put(key, json.toString());
				logger.log(Level.INFO, "Adding EXP landmark list to cache with key {0}", key);
			}

		} else {
			logger.log(Level.INFO, "Reading EXP landmark list from cache with key {0}", key);
			json = new JSONObject(output);
		}

		return json;
	}

	private static JSONObject createCustomJsonExpediaList(String expediaJson, int stringLimit, String language, int limit) throws JSONException {
		ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
		
		if (StringUtils.startsWith(expediaJson, "{")) {
			try {
				JSONObject jsonRoot = new JSONObject(expediaJson);
				JSONObject hotelListResponse = jsonRoot
						.getJSONObject("HotelListResponse");
				JSONObject hotelList = hotelListResponse
						.optJSONObject("HotelList");
				if (hotelList != null) {
					int size = hotelList.getInt("@size");
					if (size > 0) {
						JSONArray hotelSummary = hotelList
								.optJSONArray("HotelSummary");
						if (hotelSummary != null) {
							for (int i = 0; i < size; i++) {
								JSONObject hotel = hotelSummary
										.getJSONObject(i);

								Map<String, Object> jsonObject = new HashMap<String, Object>();

								String name = hotel.optString("name");
								if (StringUtils.isNotEmpty(name)) {

									jsonObject.put("name", StringEscapeUtils.unescapeHtml(name));
									jsonObject.put("lat", hotel.getDouble("latitude"));
									jsonObject.put("lng", hotel.getDouble("longitude"));

									String url = StringEscapeUtils.unescapeHtml(hotel.getString("deepLink"));

									jsonObject.put("url", url);

									Map<String, String> desc = new HashMap<String, String>();

									JSONUtils.putOptValue(desc, "description", hotel, "shortDescription", true, stringLimit, true);
									String description = desc
											.remove("description");
									if (StringUtils.isNotEmpty(description)) {
										description = StringUtils.replace(description, "<p>", "<br/>");
										description = StringUtils.replace(description, "</p>", "");
										desc.put("description", description);
									}

									JSONUtils.putOptValue(desc, "address",hotel, "address1", false,stringLimit, false);
									JSONUtils.putOptValue(desc, "city", hotel,"city", false, stringLimit, false);
									JSONUtils.putOptValue(desc, "country",hotel, "countryCode", false,stringLimit, false);
									JSONUtils.putOptValue(desc, "zip", hotel,"postalCode", false, stringLimit,false);

									double rating = hotel.optDouble("hotelRating", -1);
									if (rating > 0) {
										desc.put("star_rating",Double.toString(rating));
									}

									rating = hotel.optDouble("tripAdvisorRating", -1);
									if (rating > 0) {
										desc.put("rating",Double.toString(rating));
									}

									String rate = StringUtil.formatCoordE2((hotel.getDouble("highRate") + hotel.getDouble("lowRate")) / 2);
									String price = rate + " " + hotel.getString("rateCurrencyCode");
									desc.put("average_price", price);

									if (hotel.has("thumbNailUrl") && !hotel.isNull("thumbNailUrl")) {
										String icon = hotel.getString("thumbNailUrl");
										if (StringUtils.isNotEmpty(icon)) {
											desc.put("icon", "http://images.travelnow.com" + icon);
										}
									}

									jsonObject.put("desc", desc);

									jsonArray.add(jsonObject);

									if (limit == jsonArray.size()) {
										break;
									}
								}
							}
						}
					}
				} else {
					logger.log(Level.WARNING, hotelListResponse.toString());
				}
			} catch (JSONException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}

		return new JSONObject().put("ResultSet", jsonArray);
	}

	@Override
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String lang, String flexString2, Locale locale, boolean useCache) throws Exception {
		if (lang == null) {
			lang = locale.toString();
		}
		int r = NumberUtils.normalizeNumber(radius, 2, 80);
		String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, r, version, limit, stringLimit, lang, flexString2);
		List<ExtendedLandmark> output = (List<ExtendedLandmark>)cacheProvider.getObject(key);
		if (output == null) {
			//MessageDigest md = MessageDigest.getInstance("MD5");
		    //long timeInSeconds = (System.currentTimeMillis() / 1000);
		    //String input = Commons.getProperty(Property.EXPEDIA_KEY) + Commons.getProperty(Property.EXPEDIA_SECRET) + timeInSeconds;
		    //md.update(input.getBytes());
		    //String sig = String.format("%032x", new BigInteger(1, md.digest()));
			
			URL expediaUrl = new URL(
					"http://api.ean.com/ean-services/rs/hotel/v3/list?json"
							+ "&apiKey=" + Commons.getProperty(Property.EXPEDIA_KEY) 
							//+ "&sig=" + sig
							+ "&cid=55505" //+ "&cid=00001"
							+ "&latitude=" + Double.toString(lat) 
							+ "&longitude=" + Double.toString(lng) 
							+ "&searchRadius=" + r
							+ "&sort=OVERALL_VALUE" // QUALITY_REVERSE,PRICE,PRICE_AVERAGE,PRICE_REVERSE
							+ "&searchRadiusUnit=KM" 
							+ "&locale=" + lang
							+ "&_type=json");

			//System.out.println(expediaUrl.toString());

			String expediaResponse = HttpUtils.processFileRequest(expediaUrl);

			// System.out.println(expediaResponse);

			output = createCustomLandmarkExpediaList(expediaResponse, stringLimit, lang, limit, locale);

			if (!output.isEmpty()) {
				cacheProvider.put(key, output);
				logger.log(Level.INFO, "Adding EXP landmark list to cache with key {0}", key);
			}

		} else {
			logger.log(Level.INFO, "Reading EXP landmark list from cache with key {0}", key);
		}
		logger.log(Level.INFO, "Found {0} landmarks", output.size()); 
		
		return output;
	}
	
	private static List<ExtendedLandmark> createCustomLandmarkExpediaList(String expediaJson, int stringLimit, String language, int limit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
		
		if (StringUtils.startsWith(expediaJson, "{")) {
			try {
				JSONObject jsonRoot = new JSONObject(expediaJson);
				JSONObject hotelListResponse = jsonRoot.getJSONObject("HotelListResponse");
				JSONObject hotelList = hotelListResponse.optJSONObject("HotelList");
				if (hotelList != null) {
					int size = hotelList.getInt("@size");
					if (size > 0) {
						JSONArray hotelSummary = hotelList
								.optJSONArray("HotelSummary");
						if (hotelSummary != null) {
							for (int i = 0; i < size; i++) {
								JSONObject hotel = hotelSummary.getJSONObject(i);

								String name = hotel.optString("name");
								if (StringUtils.isNotEmpty(name)) {
									name = StringEscapeUtils.unescapeHtml(name);
									name = StringEscapeUtils.unescapeXml(name);
									double lat = hotel.getDouble("latitude");
									double lng = hotel.getDouble("longitude");
									String url = StringEscapeUtils.unescapeHtml(hotel.getString("deepLink"));

									Map<String, String> tokens = new HashMap<String, String>();

									JSONUtils.putOptValue(tokens, "description",hotel, "shortDescription", true, stringLimit, true);
									String description = tokens.remove("description");
									if (StringUtils.isNotEmpty(description)) {
										description = StringUtils.replace(description, "<p>", "<br/>");
										description = StringUtils.replace(description, "</p>", "");
										tokens.put("description", description);
									}

									AddressInfo address = new AddressInfo();
			                        
			                        String val = hotel.optString("address1");
			                        if (val != null) {
			                        	address.setField(AddressInfo.STREET, val);
			                        }                    
			                        val = hotel.optString("city");
			                        if (val != null) {
			                        	address.setField(AddressInfo.CITY, val);
			                        }
			                        val = hotel.optString("countryCode");
			                        if (val != null) {
			                        	address.setField(AddressInfo.COUNTRY_CODE, val);
			                        }
			                        val = hotel.optString("zip");
			                        if (val != null) {
			                        	address.setField(AddressInfo.POSTAL_CODE, val);
			                        }
			                        
			                        QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
			                        ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.EXPEDIA_LAYER, address, -1, null);
			                        landmark.setUrl(url);
			                        
			                        landmark.setCategoryId(7);
			                        landmark.setSubCategoryId(129);
			                        
									double rating = hotel.optDouble("hotelRating", -1);
									if (rating > 0) {
										tokens.put("star_rating",Double.toString(rating));
									}
								    rating = hotel.optDouble("tripAdvisorRating", -1);
									if (rating > 0) {
										landmark.setRating(rating);
									}

									double rate = (hotel.getDouble("highRate") + hotel.getDouble("lowRate")) / 2;
									Deal deal = new Deal(rate, -1, -1, null, hotel.getString("rateCurrencyCode"));
						            landmark.setDeal(deal);

									if (hotel.has("thumbNailUrl") && !hotel.isNull("thumbNailUrl")) {
										String icon = hotel.getString("thumbNailUrl");
										if (StringUtils.isNotEmpty(icon)) {
											landmark.setThumbnail("http://images.travelnow.com"	+ icon);
										}
									}

									description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
			                        landmark.setDescription(description);
									
			                        landmarks.add(landmark);
			                        
									if (limit == landmarks.size()) {
										break;
									}
								}
							}
						}
					}
				} else {
					logger.log(Level.WARNING, hotelListResponse.toString());
				}
			} catch (JSONException ex) {
				logger.log(Level.SEVERE, null, ex);
			}
		}

		return landmarks;
	}
	
	public String getLayerName() {
    	return Commons.EXPEDIA_LAYER;
    }
}
