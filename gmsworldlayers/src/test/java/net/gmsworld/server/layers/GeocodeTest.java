package net.gmsworld.server.layers;

import java.util.List;
import java.util.Locale;

import net.gmsworld.server.utils.persistence.GeocodeCache;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.junit.Test;

public class GeocodeTest {

	@Test
	public void test() {
		String address = "UK, London, 64 Baker Street";
		Locale locale = Locale.UK;
		String email = null;
		boolean appendCountry = false;
		String response = GeocodeUtils.processRequest(address, email, locale, appendCountry);
		System.out.println(response);
		GeocodeCache gc = GeocodeCachePersistenceUtils.checkIfGeocodeExists(address);
		if (gc != null) {
			System.out.println("GeocodeCache: " + gc.getLatitude() + "," + gc.getLongitude());
		} else {
			System.out.println("GeocodeCache == null");
		}
		
		Landmark landmark = null;
        /*String[] token = address.split(",");
        if (token.length > 1 && token[1].length() > 0) {
        	System.out.println("Searching for landmark: " + token[1]);
        	List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarkMatchingQuery(token[1],1);
        	if (!landmarks.isEmpty()) {
        		landmark = landmarks.get(0);
        	}
        }*/
		System.out.println("Searching for landmark: " + address);
    	List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarkMatchingQuery(address,1);
    	if (!landmarks.isEmpty()) {
    		landmark = landmarks.get(0);
    	}
        if (landmark != null) {
        	System.out.println("Landmark: " + landmark.getLatitude() + "," + landmark.getLongitude());
		} else {
			System.out.println("Landmark == null");
		}
        
        //JSONObject resp = GeocodeHelperFactory.getGoogleGeocodeUtils().processGeocode(address, email, false);
        //System.out.println("Google: " + resp);
        
        //resp = GeocodeHelperFactory.getMapQuestUtils().processGeocode(address, email, false);
        //System.out.println("MapQuest: " + resp);
	}

}