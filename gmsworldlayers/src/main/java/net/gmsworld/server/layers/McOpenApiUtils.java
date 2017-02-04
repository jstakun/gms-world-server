package net.gmsworld.server.layers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

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
public class McOpenApiUtils extends LayerHelper {

    public McOpenApiUtils() {
    	try {
    		InputStream is = getClass().getResourceAsStream(Commons.getProperty(Property.mcopenapi_privKeyFile));
    		String consumerKey = Commons.getProperty(Property.mcopenapi_prodConsumerKey);
    		String keyAlias = Commons.getProperty(Property.mcopenapi_keyAlias);
    		String keyPassword = new String(CryptoTools.decrypt(Base64.decode(Commons.getProperty(Property.mcopenapi_ksPwd).getBytes())));
    	
    		ApiConfig.setAuthentication(new OAuthAuthentication(consumerKey, is, keyAlias, keyPassword));
    	
    		ApiConfig.setDebug(false);
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
         
        if (response != null ){
        	totalCount = Integer.parseInt(response.get("Atms.TotalCount").toString());
        	System.out.println("Found " + totalCount + " atms");
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
        	return landmarks.subList(0, limit);
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
    		map.set("DistanceUnit", "m");
    		map.set("Radius", Integer.toString(radius));
    		map.set("PageOffset", "0");
         
    		return ATMLocations.query(map);
    	} catch (Exception e) {
    		logger.log(Level.SEVERE, e.getMessage(), e);
    	}
    	return null;
    }    
    
	private void createExtendedLandmarkList(ATMLocations response, List<ExtendedLandmark> landmarks, Locale locale) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("Atms.Atm");
        if (list != null && !list.isEmpty()) {
        	for (Map<String, Object> i : list) {
                Map<String, Object> locationMap = (Map<String, Object>) i.get("Location");
                Map<String, Object> addressMap = (Map<String, Object>) locationMap.get("Address");
                Map<String, Object> subDivisionMap = (Map<String, Object>) addressMap.get("CountrySubdivision");
                Map<String, Object> countryMap = (Map<String, Object>) addressMap.get("Country");
                Map<String, Object> pointMap = (Map<String, Object>) locationMap.get("Point");

                String name = StringUtil.capitalize((String) locationMap.get("Name"));
                
                double latitude = Double.parseDouble(pointMap.get("Latitude").toString());
                double longitude = Double.parseDouble(pointMap.get("Latitude").toString());
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
                
                String addr = (String)addressMap.get("Line1");
                String line2 =  (String)addressMap.get("Line2");
                if (StringUtils.isNotEmpty(addr) && line2 != null) {
                    addr += "\n" + line2;
                } else if (StringUtils.isEmpty(addr) && line2 != null) {
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
                tokens.put("owner", StringUtil.capitalize( (String) i.get("Owner")));
                
                setAtmAvailability(tokens, (String) i.get("Availability"));
                
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.MC_ATM_LAYER, addressInfo, -1, null);
                
                String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                landmark.setDescription(description);
                
                String thumbnail = "https://maps.googleapis.com/maps/api/streetview?size=128x128&location=" + latitude + "," + longitude;
                
                //TODO landmark.setUrl(url);
                
                landmark.setThumbnail(thumbnail);
				
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
}
