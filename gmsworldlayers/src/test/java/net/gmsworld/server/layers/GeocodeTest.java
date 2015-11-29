package net.gmsworld.server.layers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import net.gmsworld.server.utils.memcache.MockCacheProvider;
import net.gmsworld.server.utils.persistence.GeocodeCache;
import net.gmsworld.server.utils.persistence.GeocodeCachePersistenceUtils;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.junit.Test;

import com.openlapi.AddressInfo;

public class GeocodeTest {

	@Test
	public void test() throws IOException {
		String address = "Nigeria, Onishtatioa"; //"UK, London, 64 Baker Street";
		Locale locale = Locale.UK;
		String email = null;
		boolean appendCountry = false;
		String response = GeocodeUtils.processRequest(address, email, locale, 10, appendCountry);
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
        	GeocodeHelperFactory.setCacheProvider(new MockCacheProvider());
        	String geocode = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(landmark.getLatitude(),landmark.getLongitude()).getField(AddressInfo.EXTENSION);
        	System.out.println("Landmark geocode: " + geocode);
        } else {
			System.out.println("Landmark == null");
		}
        
        AddressInfo ai = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(39.457651,-0.400439);
        System.out.println("google 1: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));
        
        ai = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(51.107885, 17.038537);
        System.out.println("google 2: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));
        
        ai = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(54.352025, 18.646638);
        System.out.println("google 3: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));
        
        ai = GeocodeHelperFactory.getGoogleGeocodeUtils().processReverseGeocode(41.54, 12.27);
        System.out.println("google 4: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));
        
        ai = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(39.457651,-0.400439);
        System.out.println("mapquest 1: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));
        
        ai = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(51.107885, 17.038537);
        System.out.println("mapquest 2: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));
        
        ai = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(54.352025, 18.646638);
        System.out.println("mapquest 3: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));   
        
        ai = GeocodeHelperFactory.getMapQuestUtils().processReverseGeocode(41.54, 12.27);
        System.out.println("mapquest 4: " + ai.getField(AddressInfo.CITY) + " " + ai.getField(AddressInfo.COUNTRY) + " " + ai.getField(AddressInfo.EXTENSION));   
	}

}
