package net.gmsworld.server.layers;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.AuthUtils;
import net.gmsworld.server.utils.CryptoTools;
import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import com.google.gdata.util.common.util.Base64;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.mastercard.api.Errors;
import com.mastercard.api.locations.v1.Address;
import com.mastercard.api.locations.v1.Atm;
import com.mastercard.api.locations.v1.AtmCollection;
import com.mastercard.api.locations.v1.Location;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

/**
 *
 * @author jstakun
 */
public class McOpenApiUtilsV1 extends LayerHelper {

    private static PrivateKey privateKey = null;

    public McOpenApiUtilsV1() {
    	if (privateKey == null) {
    		InputStream stream = getClass().getResourceAsStream(Commons.getProperty(Property.mcopenapi_privKeyFile));
    		setPrivateKey(stream);	
    	}
    }
    
    @Override
	public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
        String output = cacheProvider.getString(key);

        if (output == null) {
            List<Atm> atms = new ArrayList<Atm>();
            loadAtmCollection(lat, lng, radius, limit, atms, 0);        
            JSONObject resp = createCustomJSonAtmList(atms, stringLimit);
            if (atms != null && atms.size() > 0) {
                cacheProvider.put(key, resp.toString());
                logger.log(Level.INFO, "Adding MC landmark list to cache with key {0}", key);
            }
            return resp;
        } else {
            logger.log(Level.INFO, "Reading MC landmark list from cache with key {0}", key);
            return new JSONObject(output);
        }
    }

    private static String getATMbyLocation(double latitude, double longitude, int radius, int limit, int pageOffset) {
        try {
            String endPoint = "https://api.mastercard.com/atms/v1/atm?Format=XML"
                    + "&PageOffset=" + pageOffset
                    + "&PageLength=" + URLEncoder.encode(Integer.toString(limit), "UTF-8")
                    + "&Latitude=" + URLEncoder.encode(Double.toString(latitude), "UTF-8")
                    + "&Longitude=" + URLEncoder.encode(Double.toString(longitude), "UTF-8")
                    + "&Radius=" + URLEncoder.encode(Integer.toString(radius), "UTF-8");
            return getOpenAPIConnection(endPoint);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    private void setPrivateKey(InputStream stream) {       
         try {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                char[] pwd = new String(CryptoTools.decrypt(Base64.decode(Commons.getProperty(Property.mcopenapi_ksPwd).getBytes()))).toCharArray();
                ks.load(stream, pwd);
                Key key = ks.getKey(Commons.getProperty(Property.mcopenapi_keyAlias), pwd);
                privateKey = (PrivateKey) key;
         } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
         }
    }
    

    private static String getOpenAPIConnection(String httpsURL) throws Exception {

        String responseBody = null;
        if (privateKey != null) {
            OAuthRsaSha1Signer rsaSigner = new OAuthRsaSha1Signer();
            OAuthParameters params = new OAuthParameters();
            params.setOAuthConsumerKey(Commons.getProperty(Property.mcopenapi_prodConsumerKey));
            params.setOAuthNonce(OAuthUtil.getNonce());
            params.setOAuthTimestamp(OAuthUtil.getTimestamp());
            params.setOAuthSignatureMethod("RSA-SHA1");
            params.setOAuthType(OAuthParameters.OAuthType.TWO_LEGGED_OAUTH);
            params.addCustomBaseParameter("oauth_version", "1.0");
            rsaSigner.setPrivateKey(privateKey);

            String baseString = OAuthUtil.getSignatureBaseString(httpsURL, "GET", params.getBaseParameters());

            String signature = rsaSigner.getSignature(baseString, params);

            params.addCustomBaseParameter("oauth_signature", signature);

            try {
            	//logger.log(Level.INFO, "Calling: " + httpsURL);
                responseBody = HttpUtils.processFileRequestWithAuthn(new URL(httpsURL), AuthUtils.buildAuthHeaderString(params));
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else {
        	logger.log(Level.SEVERE, "Private key is empty!");
        }

        return responseBody;
    }

    private static JSONObject createCustomJSonAtmList(List<Atm> atms, int stringLimit) throws JSONException {
        List<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
        
        if (atms != null && !atms.isEmpty()) {
            for (Iterator<Atm> atmIter = atms.iterator(); atmIter.hasNext();) {
                Atm atm = atmIter.next();
                Location location = atm.getLocation();
                Map<String, Object> jsonObject = new HashMap<String, Object>();
                jsonObject.put("name", StringUtil.capitalize(location.getName()));
                jsonObject.put("lat", location.getPoint().getLatitude());
                jsonObject.put("lng", location.getPoint().getLongitude());
                jsonObject.put("url", "");

                Map<String, String> desc = new HashMap<String, String>();

                Address address = atm.getLocation().getAddress();

                JSONUtils.putOptValue(desc, "city", StringUtil.capitalize(address.getCity()), stringLimit, false);
                JSONUtils.putOptValue(desc, "country", StringUtil.capitalize(address.getCountry().getName()), stringLimit, false);
                JSONUtils.putOptValue(desc, "state", StringUtil.capitalize(address.getCountrySubdivision().getName()), stringLimit, false);
                String addr = address.getLine1();
                if (!addr.isEmpty() && !address.getLine2().isEmpty()) {
                    addr += "\n" + address.getLine2();
                } else if (addr.isEmpty() && !address.getLine2().isEmpty()) {
                    addr = address.getLine2();
                }
                JSONUtils.putOptValue(desc, "address", StringUtil.capitalize(addr), stringLimit, false);
                JSONUtils.putOptValue(desc, "zip", address.getPostalCode(), stringLimit, false);

                desc.put("owner", StringUtil.capitalize(atm.getOwner()));
                
                setAtmAvailability(desc, atm.getAvailability());

                if (!desc.isEmpty()) {
                    jsonObject.put("desc", desc);
                }

                jsonArray.add(jsonObject);
            }
        }

        JSONObject json = new JSONObject().put("ResultSet", jsonArray);
        return json;
    }

	@Override
	public List<ExtendedLandmark> loadLandmarks(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale, boolean useCache) throws Exception {
		List<Atm> atms = new ArrayList<Atm>();
        loadAtmCollection(lat, lng, radius, limit, atms, 0);
        return createLandmarksAtmList(atms, stringLimit, locale);
    }
	
	private static List<ExtendedLandmark> createLandmarksAtmList(List<Atm> atms, int stringLimit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
        if (atms != null && !atms.isEmpty()) {
            for (Iterator<Atm> atmIter = atms.iterator(); atmIter.hasNext();) {
                Atm atm = atmIter.next();
                Location location = atm.getLocation();
                String name = StringUtil.capitalize(location.getName());
                double lat = location.getPoint().getLatitude();
                double lng = location.getPoint().getLongitude();
                
                AddressInfo addressInfo = new AddressInfo();
                
                Address address = atm.getLocation().getAddress();
                String val = address.getCity();
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.CITY, StringUtil.capitalize(val));	
                }
                val = address.getCountry().getName();
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.COUNTRY, StringUtil.capitalize(val));	
                }
                val = address.getCountrySubdivision().getName();
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.STATE, StringUtil.capitalize(val));	
                }
                
                String addr = address.getLine1();
                if (!addr.isEmpty() && !address.getLine2().isEmpty()) {
                    addr += "\n" + address.getLine2();
                } else if (addr.isEmpty() && !address.getLine2().isEmpty()) {
                    addr = address.getLine2();
                }
                if (StringUtils.isNotEmpty(addr)) {
                	addressInfo.setField(AddressInfo.STREET, StringUtil.capitalize(addr));	
                }
                val = address.getPostalCode();
                if (StringUtils.isNotEmpty(val)) {
                	addressInfo.setField(AddressInfo.POSTAL_CODE, val);	
                }             
                
                Map<String, String> tokens = new HashMap<String, String>();
                tokens.put("owner", StringUtil.capitalize(atm.getOwner()));
                
                setAtmAvailability(tokens, atm.getAvailability());
                
                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.MC_ATM_LAYER, addressInfo, -1, null);
                
                String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                landmark.setDescription(description);
                
                String thumbnail = "https://maps.googleapis.com/maps/api/streetview?size=128x128&location=" + lat + "," + lng;
                
                //TODO landmark.setUrl(url);
                
                landmark.setThumbnail(thumbnail);
				
                landmarks.add(landmark);
            }
        }

        return landmarks;
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
	
	private static void loadAtmCollection(double lat, double lng, int radius, int totalLimit, List<Atm> atms, int pageOffset) throws JAXBException {
		int totalAtmCount = 0;
		
		int limit = totalLimit - atms.size();
		if (25 < limit) {
			limit = 25; 
		}
		
		String xmlResponse = getATMbyLocation(lat, lng, radius, limit, pageOffset);
		
		if (xmlResponse != null) {
            JAXBContext responseContext = JAXBContext.newInstance(new Class[]{AtmCollection.class, Errors.class});
            Unmarshaller unmarshaller = responseContext.createUnmarshaller();
            Object returnedObj = unmarshaller.unmarshal(new StreamSource(new StringReader(xmlResponse)));
            if (returnedObj instanceof AtmCollection) {
                AtmCollection atmCollection = (AtmCollection) returnedObj;
                totalAtmCount = atmCollection.getTotalCount();
                logger.log(Level.INFO, "Found {0} atms", totalAtmCount);
                if (totalAtmCount > 0) {
                	atms.addAll(atmCollection.getAtms());
                }
            } else {
                logger.log(Level.SEVERE, "Received following server response:\n{0}", xmlResponse);               
            }
        }
        
		int size = atms.size();
		logger.log(Level.INFO, "Current atm list size {0}", size);
		
        if (size < totalLimit && size < totalAtmCount && pageOffset < 125) { //5 interations max
        	loadAtmCollection(lat, lng, radius, totalLimit, atms, size);
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
}

