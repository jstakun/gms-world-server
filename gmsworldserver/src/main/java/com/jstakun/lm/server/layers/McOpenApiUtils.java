/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jstakun.lm.server.layers;

import com.google.gdata.client.authn.oauth.OAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import com.google.gdata.util.common.util.Base64;
import com.jstakun.gms.android.landmarks.ExtendedLandmark;
import com.jstakun.gms.android.landmarks.LandmarkFactory;
import com.jstakun.lm.server.config.Commons;
import com.jstakun.lm.server.oauth.CommonUtils;
import com.jstakun.lm.server.utils.CryptoTools;
import com.jstakun.lm.server.utils.HttpUtils;
import com.jstakun.lm.server.utils.JSONUtils;
import com.jstakun.lm.server.utils.StringUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.mastercard.api.Errors;
import com.mastercard.api.locations.v1.Address;
import com.mastercard.api.locations.v1.Atm;
import com.mastercard.api.locations.v1.AtmCollection;
import com.mastercard.api.locations.v1.Location;
import com.openlapi.AddressInfo;
import com.openlapi.QualifiedCoordinates;

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
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jstakun
 */
public class McOpenApiUtils extends LayerHelper {

    private static PrivateKey privateKey = null;

    @Override
    public JSONObject processRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2) throws Exception {
        String key = getCacheKey(getClass(), "processRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
        String output = CacheUtil.getString(key);

        if (output == null) {
            String xmlResponse = getATMbyLocation(lat, lng, radius, limit);
            //System.out.println(xmlResponse);
            AtmCollection atmCollection = null;
            if (xmlResponse != null) {
                JAXBContext responseContext = JAXBContext.newInstance(new Class[]{AtmCollection.class, Errors.class});
                Unmarshaller unmarshaller = responseContext.createUnmarshaller();
                Object returnedObj = unmarshaller.unmarshal(new StreamSource(new StringReader(xmlResponse)));
                if (returnedObj instanceof AtmCollection) {
                    //System.out.println("We've got atm collection");
                    atmCollection = (AtmCollection) returnedObj;
                }
            }
            JSONObject resp = createCustomJSonAtmList(atmCollection, stringLimit);
            if (atmCollection != null && atmCollection.getTotalCount() > 0) {
                CacheUtil.put(key, resp.toString());
                logger.log(Level.INFO, "Adding MC landmark list to cache with key {0}", key);
            }
            return resp;
        } else {
            logger.log(Level.INFO, "Reading MC landmark list from cache with key {0}", key);
            return new JSONObject(output);
        }
    }

    private static String getATMbyLocation(double latitude, double longitude, int radius, int limit) {
        try {
            String endPoint = "https://api.mastercard.com/atms/v1/atm?Format=XML"
                    + "&PageOffset=0"
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

    public void setPrivateKey(InputStream stream) {
        if (privateKey == null) {
            try {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                char[] pwd = new String(CryptoTools.decrypt(Base64.decode(Commons.mcopenapi_ksPwd.getBytes()))).toCharArray();
                ks.load(stream, pwd);
                Key key = ks.getKey(Commons.mcopenapi_keyAlias, pwd);
                privateKey = (PrivateKey) key;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private static String getOpenAPIConnection(String httpsURL) throws Exception {

        String responseBody = null;
        if (privateKey != null) {
            OAuthRsaSha1Signer rsaSigner = new OAuthRsaSha1Signer();
            OAuthParameters params = new OAuthParameters();
            params.setOAuthConsumerKey(Commons.mcopenapi_prodConsumerKey);
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
                responseBody = HttpUtils.processFileRequestWithAuthn(new URL(httpsURL), CommonUtils.buildAuthHeaderString(params));
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }

        return responseBody;
    }

    private static JSONObject createCustomJSonAtmList(AtmCollection atmCollection, int stringLimit) throws JSONException {
        ArrayList<Map<String, Object>> jsonArray = new ArrayList<Map<String, Object>>();
        ArrayList<Atm> atms = null;
        if (atmCollection != null) {
            atms = atmCollection.getAtms();
        }

        if (atms != null && !atms.isEmpty()) {
            for (Iterator<Atm> atmIter = atms.iterator(); atmIter.hasNext();) {
                Atm atm = atmIter.next();
                Location location = atm.getLocation();
                Map<String, Object> jsonObject = new HashMap<String, Object>();
                jsonObject.put("name", StringUtil.capitalize(location.getName()));
                jsonObject.put("lat", location.getPoint().getLatitude());
                jsonObject.put("lng", location.getPoint().getLongitude());
                jsonObject.put("url", "");

                Map<String, Object> desc = new HashMap<String, Object>();

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
                desc.put("availability", StringUtil.capitalize(atm.getAvailability()));

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
	public List<ExtendedLandmark> processBinaryRequest(double lat, double lng, String query, int radius, int version, int limit, int stringLimit, String flexString, String flexString2, Locale locale) throws Exception {
		String key = getCacheKey(getClass(), "processBinaryRequest", lat, lng, query, radius, version, limit, stringLimit, flexString, flexString2);
		List<ExtendedLandmark> output = (List<ExtendedLandmark>)CacheUtil.getObject(key);

        if (output == null) {
            String xmlResponse = getATMbyLocation(lat, lng, radius, limit);
            //System.out.println(xmlResponse);
            AtmCollection atmCollection = null;
            if (xmlResponse != null) {
                JAXBContext responseContext = JAXBContext.newInstance(new Class[]{AtmCollection.class, Errors.class});
                Unmarshaller unmarshaller = responseContext.createUnmarshaller();
                Object returnedObj = unmarshaller.unmarshal(new StreamSource(new StringReader(xmlResponse)));
                if (returnedObj instanceof AtmCollection) {
                    //System.out.println("We've got atm collection");
                    atmCollection = (AtmCollection) returnedObj;
                }
            }
            output = createLandmarksAtmList(atmCollection, stringLimit, locale);
            if (!output.isEmpty()) {
                CacheUtil.put(key, output);
                logger.log(Level.INFO, "Adding MC landmark list to cache with key {0}", key);
            }
        } else {
            logger.log(Level.INFO, "Reading MC landmark list from cache with key {0}", key);
        }
        
        return output;
	}
	
	private static List<ExtendedLandmark> createLandmarksAtmList(AtmCollection atmCollection, int stringLimit, Locale locale) throws JSONException {
		List<ExtendedLandmark> landmarks = new ArrayList<ExtendedLandmark>();
        ArrayList<Atm> atms = null;
        if (atmCollection != null) {
            atms = atmCollection.getAtms();
        }

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
                if (val != null) {
                	addressInfo.setField(AddressInfo.CITY, StringUtil.capitalize(val));	
                }
                val = address.getCountry().getName();
                if (val != null) {
                	addressInfo.setField(AddressInfo.COUNTRY, StringUtil.capitalize(val));	
                }
                val = address.getCountrySubdivision().getName();
                if (val != null) {
                	addressInfo.setField(AddressInfo.STATE, StringUtil.capitalize(val));	
                }
                
                String addr = address.getLine1();
                if (!addr.isEmpty() && !address.getLine2().isEmpty()) {
                    addr += "\n" + address.getLine2();
                } else if (addr.isEmpty() && !address.getLine2().isEmpty()) {
                    addr = address.getLine2();
                }
                if (addr != null) {
                	addressInfo.setField(AddressInfo.STREET, StringUtil.capitalize(addr));	
                }
                val = address.getPostalCode();
                if (val != null) {
                	addressInfo.setField(AddressInfo.POSTAL_CODE, val);	
                }             
                
                Map<String, String> tokens = new HashMap<String, String>();
                tokens.put("owner", StringUtil.capitalize(atm.getOwner()));
                tokens.put("availability", StringUtil.capitalize(atm.getAvailability()));

                QualifiedCoordinates qc = new QualifiedCoordinates(lat, lng, 0f, 0f, 0f);
                ExtendedLandmark landmark = LandmarkFactory.getLandmark(name, null, qc, Commons.MC_ATM_LAYER, addressInfo, -1, null);
                
                String description = JSONUtils.buildLandmarkDesc(landmark, tokens, locale);
                landmark.setDescription(description);
				
                landmarks.add(landmark);
            }
        }

        return landmarks;
    }
}
