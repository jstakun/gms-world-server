package net.gmsworld.server.layers;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.memcache.CacheProvider;

import org.apache.commons.lang.StringUtils;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.openlapi.AddressInfo;

/**
 *
 * @author jstakun
 */
public abstract class LayerHelper {

    protected static final Logger logger = Logger.getLogger(LayerHelper.class.getName());
    protected ThreadFactory threadProvider = null;
    protected CacheProvider cacheProvider = null;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected void setThreadProvider(ThreadFactory threadProvider){
		this.threadProvider = threadProvider;
	}
	
	protected void setCacheProvider(CacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}
	
	public CacheProvider getCacheProvider() {
		return cacheProvider;
	}
    
    protected JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
    	List<ExtendedLandmark> landmarks = processBinaryRequest(lat, lng, query, radius, version, limit, stringLimit, flexString, flexString, Locale.US, true);
    	return new JSONObject().put("ResultSet", landmarks);
    }

    protected List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {
    	String key = null;
		List<ExtendedLandmark> landmarks = null;
		if (useCache) {
			key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
			if (cacheProvider != null) {
				landmarks = cacheProvider.getList(ExtendedLandmark.class, key);
			}
		}
        if (landmarks == null) {
        	landmarks = loadLandmarks(lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2, locale, useCache);
        	if (useCache && !landmarks.isEmpty()) { // && landmarks.size() <= 300) { //don't cache too large objects
        		logger.log(Level.INFO, "Adding {0} landmark list to cache with key {1}", new Object[]{getLayerName(), key});
                cacheProvider.put(key, landmarks);
            }
        } else {
        	logger.log(Level.INFO, "Reading {0} landmark list from cache with key {1}", new Object[]{getLayerName(), key});
        }
           
        logger.log(Level.INFO, "Found {0} landmarks", landmarks.size()); 
    		
    	return landmarks;
    }
    
    protected abstract List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception;
    
    protected void serialize(List<ExtendedLandmark> landmarks, OutputStream out, int version) {
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
    
    protected String cacheGeoJson(List<ExtendedLandmark> landmarks, double lat, double lng, final String layer, Locale locale, String flex) {
    	
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
		featureCollection.setProperty("creationDate", new Date());
		featureCollection.setProperty("language", locale.getLanguage());
		if (!landmarks.isEmpty()) {    		
			for (ExtendedLandmark landmark : landmarks) {
    			Feature f = new Feature();
    			Point p = new Point();
    			p.setCoordinates(new LngLatAlt(landmark.getQualifiedCoordinates().getLongitude(), landmark.getQualifiedCoordinates().getLatitude()));
    			f.setGeometry(p);
    			f.setProperty("name", landmark.getName());
    			if (StringUtils.equals(layer, Commons.FACEBOOK_LAYER)) {
    				f.setProperty("url", StringUtils.replace(landmark.getUrl(), "touch", "www")); 
    			} else if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
    				f.setProperty("url", StringUtils.replace(landmark.getUrl(), "&Mobile=1", "")); 
    				String desc = StringUtils.replace(landmark.getDescription(), "star_blue", "/images/star_blue.png");
    				if (desc.contains("star_0")) {
    					desc = StringUtils.replace(desc, "star_0", "/images/star_0.png");
    				} else if (desc.contains("star_1")) {
    					desc = StringUtils.replace(desc, "star_1", "/images/star_1.png");
    				} else if (desc.contains("star_2")) {
    					desc = StringUtils.replace(desc, "star_2", "/images/star_2.png");
    				} else if (desc.contains("star_3")) {
    					desc = StringUtils.replace(desc, "star_3", "/images/star_3.png");
    				} else if (desc.contains("star_4")) {
    					desc = StringUtils.replace(desc, "star_4", "/images/star_4.png");
    				} else if (desc.contains("star_5")) {
    					desc = StringUtils.replace(desc, "star_5", "/images/star_5.png");
    				}	
    				f.setProperty("desc", desc); //desc
    				int stars = StringUtils.countMatches(desc, "/images/star_blue.png");
    				String icon = "star_" + stars + ".png";
    				if (landmark.getAddressInfo().getField(AddressInfo.EXTENSION) != null)	{
    					//single room venue
        				try {
    						if (landmark.getAddressInfo().getField(AddressInfo.EXTENSION).equals("1")) {
    							icon = stars + "stars_blue.png";
    						}
    					} catch (Exception e) {
    						
    					}
    				} 
    				f.setProperty("icon", icon); //icon
    				if (landmark.containsDeal()) {
    					f.setProperty("price", StringUtil.formatCoordE0(landmark.getDeal().getPrice())); //price
    					f.setProperty("cc", landmark.getDeal().getCurrencyCode());
    				}
    				String thumbnail = landmark.getThumbnail(); 
    				if (thumbnail != null) {
    					f.setProperty("thumbnail", thumbnail); //thumbnail
    				}
        		} else if (StringUtils.equals(layer, Commons.PANORAMIO_LAYER)) {
        			f.setProperty("url", StringUtils.replace(landmark.getUrl(), "/m/photo", "/photo")); 
        		} else if (StringUtils.equals(layer, Commons.FREEBASE_LAYER)) {
        			String thumbnail = landmark.getThumbnail(); 
    				if (thumbnail != null) {
    					f.setProperty("thumbnail", thumbnail);
    				}
    				f.setProperty("desc", StringUtils.replace(landmark.getDescription(), "<a href=", "<a target=\"_blank\" href=")); //desc
        		}
    			f.setProperty("mobile_url", landmark.getUrl());
    			featureCollection.add(f);
    		}
						
			//build stats and exchange rate for hotels
			if (StringUtils.equals(layer, Commons.HOTELS_LAYER)) {
				Map<String, Double> exchangeRates = new HashMap<String, Double>();
				Map<Integer, Integer> stars = new HashMap<Integer, Integer>();
				Map<Integer, Integer> prices = new HashMap<Integer, Integer>();
				
				for (ExtendedLandmark landmark : landmarks) {
					String desc = landmark.getDescription();
					
					int s = StringUtils.countMatches(desc, "star_blue");
					if (stars.containsKey(s)) {
						stars.put(s, stars.get(s)+1);
					} else {
						stars.put(s, 1);
					}
					
					s = 0;
					if (landmark.containsDeal()) {
						Double exchangeRate = 1d;
						String cc = landmark.getDeal().getCurrencyCode();
						if (!StringUtils.equals(cc, "EUR")) {
							exchangeRate = JSONUtils.getExchangeRate("EUR", landmark.getDeal().getCurrencyCode());
							if (exchangeRate != null) {
								exchangeRates.put(landmark.getDeal().getCurrencyCode(), exchangeRate);
							}
						}
						if (exchangeRate != null) {							
							double eurvalue = landmark.getDeal().getPrice() / exchangeRate;
							if (eurvalue < 50d) {
								s = 1;
							} else if (eurvalue >= 50d && eurvalue < 100d) {
								s = 2;
							} else if (eurvalue >= 100d && eurvalue < 150d) {
								s = 3;
							} else if (eurvalue >= 150d && eurvalue < 200d) {
								s = 4;
							} else if (eurvalue >= 200d) {
								s = 5;
							}
						}
					}
					if (prices.containsKey(s)) {
						prices.put(s, prices.get(s)+1);
					} else {
						prices.put(s, 1);
					}

				}
				String language = locale.getLanguage();
				String country = locale.getCountry();
				try {
					if (StringUtils.isEmpty(country)) {
						country = language;
					}
					Locale l = new Locale(language, country);
					String cc = Currency.getInstance(l).getCurrencyCode();
					featureCollection.setProperty("currencycode", cc);
					if (!StringUtils.equals(cc, "EUR") && !exchangeRates.containsKey(cc)) {
						Double exchangeRate = JSONUtils.getExchangeRate("EUR", cc);
						if (exchangeRate != null) {
							exchangeRates.put(cc, exchangeRate);
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error for: " + country + "," + language, e);
					featureCollection.setProperty("currencycode", "EUR");
				} finally {
					exchangeRates.put("EUR", 1d);
				}
				featureCollection.setProperty("stats_price", prices);
				featureCollection.setProperty("stats_stars", stars);
				featureCollection.setProperty("eurexchangerates", exchangeRates);
				if (StringUtils.isNotEmpty(flex)) {
					featureCollection.setProperty("sortType", flex);
				}
			}	

			try {
    			final String json = objectMapper.writeValueAsString(featureCollection);
    			final String latStr = StringUtil.formatCoordE2(lat);
    			final String lngStr = StringUtil.formatCoordE2(lng);
    			
    			if (!landmarks.isEmpty() && StringUtils.isNotEmpty(json)) {
    				/*threadProvider.newThread(new Runnable() {
						@Override
						public void run() {
							logger.log(Level.INFO, "Saving geojson list to second level cache");
		    				String key = "geojson/" + latStr + "/" + lngStr + "/" + layer;
		    				cacheProvider.putToSecondLevelCache(key, json);							
						}}).start();		*/
    				logger.log(Level.INFO, "Saving geojson list to second level cache");
    				String key = "geojson/" + latStr + "/" + lngStr + "/" + layer;
    				cacheProvider.putToSecondLevelCache(key, json);					
    			}
    			
    			if (cacheProvider != null) {
    				String key = "geojson_" + latStr + "_" + lngStr + "_" + layer + "_" + locale.getLanguage();
    				if (StringUtils.isNotEmpty(flex)) {
    					key += "_" + flex;
    				}
    				logger.log(Level.INFO, "Saved geojson list to local in-memory cache with key: " + key);
    				cacheProvider.put(key, json, 1);
    			    return key;
    			}
			} catch (JsonProcessingException e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}	
    	return null;
    }	
    
    public String getGeoJson(double lat, double lng, String layer, String language, String flex) {
    	if (cacheProvider != null) {
    		String key = "geojson_" + StringUtil.formatCoordE2(lat) + "_" + StringUtil.formatCoordE2(lng) + "_" + layer + "_" + language;
    		if (StringUtils.isNotEmpty(flex)) {
				key += "_" + flex;
			}
    		return cacheProvider.getString(key);
    	} else {
    		return null;
    	}
    }
    
    protected abstract String getLayerName();
    
    public boolean isEnabled() {
    	return true;
    }
    
    public String getIcon() {
    	return getLayerName().toLowerCase() + ".png";
    }
    
    public String getURI() {
    	return getLayerName().toLowerCase() + "Provider";
    }
}
