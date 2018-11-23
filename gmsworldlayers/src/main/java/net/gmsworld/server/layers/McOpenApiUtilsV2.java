package net.gmsworld.server.layers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.mastercard.api.core.ApiConfig;
import com.mastercard.api.core.model.RequestMap;
import com.mastercard.api.core.security.oauth.OAuthAuthentication;
import com.mastercard.api.locations.ATMLocations;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.CryptoTools;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.StringUtil;

/**
 *
 * @author jstakun
 */
public class McOpenApiUtilsV2 extends LayerHelper {

    public McOpenApiUtilsV2() {
    	try {
    		String keyAlias = Commons.getProperty(Property.mcopenapi_keyAlias);
    		InputStream is = getClass().getResourceAsStream("/" + keyAlias + ".p12"); //getClass().getResourceAsStream(Commons.getProperty(Property.mcopenapi_privKeyFile));
    		String consumerKey = Commons.getProperty(Property.mcopenapi_prodConsumerKey);
    		String keyPassword = new String(CryptoTools.decrypt(Base64.decode(Commons.getProperty(Property.mcopenapi_ksPwd).getBytes())));
    	
    		ApiConfig.setAuthentication(new OAuthAuthentication(consumerKey, is, keyAlias, keyPassword));
    		
    		ApiConfig.setDebug(true);
    		ApiConfig.setSandbox(false);    

    		logger.log(Level.INFO, "Calling production servers: " + ApiConfig.isProduction());
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    }
    
    @Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {
    	List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
    	int offset  = 0;
    	int totalCount = 0;
    	
    	ATMLocations response = getATMLocations(lat, lng, radius, limit, offset);
    	if (response != null && !response.isEmpty()){
        	totalCount = Integer.parseInt(response.get("Atms.TotalCount").toString());
        	logger.log(Level.INFO, "Found " + totalCount + " ATMs...");
        	createExtendedLandmarkList(response, landmarks, locale);
        	offset += 25;
  		    while (offset < limit && offset < totalCount) {
        		  response = getATMLocations(lat, lng, radius, limit, offset);
        		  if (response != null) {
        			  createExtendedLandmarkList(response, landmarks, locale);
        		  }
        		  offset += 25;
        	 }
        }
      
        if (landmarks.size() > limit) {
        	return new ArrayList<ExtendedLandmark>(landmarks.subList(0, limit));
        } else {
        	return landmarks;
        }
    }
    
    private static void setAtmAvailability(Map<String, String> tokens, String availability) {
		//UNKNOWN, ALWAYS_AVAILABLE, BUSINESS_HOURS, IRREGULAR_HOURS
        if (StringUtils.equalsIgnoreCase(availability, "ALWAYS_AVAILABLE")) {
			tokens.put("availability", "Always (24/7)");
		} else if (StringUtils.equalsIgnoreCase(availability, "BUSINESS_HOURS")) {
			tokens.put("availability", "Business Hours");
		} else if (StringUtils.equalsIgnoreCase(availability, "IRREGULAR_HOURS")) {
			tokens.put("availability", "Irregural Hours");
		} else if (StringUtils.equalsIgnoreCase(availability, "UNKNOWN")) {
			//tokens.put("availability", "Unknown");
		} 
	}
	
    private static  ATMLocations getATMLocations(double latitude, double longitude, int radius, int limit, int pageOffset) {
    	try { 
    		RequestMap map = new RequestMap();
    		map.set("PageLength", Integer.toString(limit));
    		map.set("Latitude", Double.toString(latitude));
    		map.set("Longitude", Double.toString(longitude));
    		map.set("DistanceUnit", "KILOMETER");
    		map.set("Radius", Integer.toString(radius / 1000));
    	    map.set("PageOffset", pageOffset);
         
    		return ATMLocations.query(map);
    	} catch (Throwable e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    	return null;
    }    
    
	private void createExtendedLandmarkList(ATMLocations response, List<ExtendedLandmark> landmarks, Locale locale) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("Atms.Atm");
        if (list != null && !list.isEmpty()) {
        	for (Map<String, Object> atms : list) {
                Map<String, Object> locationMap = (Map<String, Object>) atms.get("Location");
                Map<String, Object> addressMap = (Map<String, Object>) locationMap.get("Address");
                Map<String, Object> subDivisionMap = (Map<String, Object>) addressMap.get("CountrySubdivision");
                Map<String, Object> countryMap = (Map<String, Object>) addressMap.get("Country");
                Map<String, Object> pointMap = (Map<String, Object>) locationMap.get("Point");

                String name = String.valueOf(locationMap.get("Name"));
                     
                double latitude = Double.parseDouble(pointMap.get("Latitude").toString());
                double longitude = Double.parseDouble(pointMap.get("Longitude").toString());
                QualifiedCoordinates qc = new QualifiedCoordinates(latitude, longitude, 0f, 0f, 0f);
                          
                AddressInfo addressInfo = new AddressInfo();
                
                String val = (String) addressMap.get("City");
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.CITY, StringUtil.capitalize(val));	
                }
                val = (String) countryMap.get("Name");
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.COUNTRY, StringUtil.capitalize(val));	
                }
                val = (String)subDivisionMap.get("Name");
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.STATE, StringUtil.capitalize(val));	
                }
                
                String addr = addressMap.get("Line1").toString();
                String line2 =  addressMap.get("Line2").toString();
                if (StringUtils.isNotEmpty(addr) && StringUtils.isNotEmpty(line2)) {
                	addr += "\n" + line2;
                } else if (StringUtils.isEmpty(addr) && StringUtils.isNotEmpty(line2)) {
                    addr = line2;
                }
                
                if (StringUtils.isNotEmpty(addr)) {
                	addressInfo.setField(AddressInfo.STREET, StringUtil.capitalize(addr));	
                }
                val = (String)addressMap.get("PostalCode");
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.POSTAL_CODE, val);	
                }             
                
                Map<String, String> tokens = new HashMap<String, String>();
                tokens.put("owner", StringUtil.capitalize( (String) locationMap.get("Owner")));
                
                setAtmAvailability(tokens, (String) locationMap.get("Availability"));
                
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.MC_ATM_LAYER, addressInfo, -1, null);
                
                String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                landmark.setDescription(description);
                
                String thumbnail = "https://maps.googleapis.com/maps/api/streetview?size=128x128&location=" + latitude + "," + longitude + "&key=" + Commons.getProperty(Property.GOOGLE_API_KEY);
                landmark.setThumbnail(thumbnail);
    			
                String url = "https://maps.google.com/maps?q=" + latitude + "," + longitude;
                landmark.setUrl(url);            
                
                landmarks.add(landmark);        
            }
        }	
	}
		
	public String getLayerName() {
    	return Commons.MC_ATM_LAYER;
    }
	
	public String getIcon() {
		return "mastercard.png";
	}
	
	public String getURI() {
		return "atmProvider";
	}
	
	 public boolean isEnabled() {
	    return true;
	 }
	 
	 private static String convertUtfHex(String arg)  {
			StringBuffer sb = new StringBuffer();
			if (StringUtils.isNotEmpty(arg)) {
				for (int i=0;i<arg.length();i++) {
					if (arg.charAt(i) == '[') {
						StringBuffer hex = new StringBuffer();
						hex.append(arg.charAt(i+3));
						hex.append(arg.charAt(i+4));
						hex.append(Integer.toHexString((int)arg.charAt(i+6)));
						try {
							sb.append(new String(DatatypeConverter.parseHexBinary(hex.toString()), "UTF-8"));
						} catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage());
							return arg;
						}
						i += 6;
					} else {
						sb.append(arg.charAt(i));
					}
				}
			}
			return sb.toString();
	 	}
}

