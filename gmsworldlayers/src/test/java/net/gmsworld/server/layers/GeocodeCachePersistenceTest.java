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

public class GeocodeCachePersistenceTest {

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
        System.out.println("Searching for landmark: " + address);
    	List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarkMatchingQuery(address, 1);
    	if (!landmarks.isEmpty()) {
    		landmark = landmarks.get(0);
    	}
    	
    	GeocodeHelperFactory.getInstance().setCacheProvider(new MockCacheProvider());
    	if (landmark != null) {
        	System.out.println("Landmark: " + landmark.getLatitude() + "," + landmark.getLongitude());
        	String geocode = GeocodeHelperFactory.getInstance().processReverseGeocode(landmark.getLatitude(),landmark.getLongitude());
        	System.out.println("Landmark geocode: " + geocode);
        } else {
			System.out.println("Landmark == null");
		}
        
        System.out.println("GeocodeUtils.isValidLatitude(10.763859) " + GeocodeUtils.isValidLatitude(10.763859));
        
        System.out.println("GeocodeUtils.isValidLongitude(199d) " + GeocodeUtils.isValidLongitude(199d));
        
        System.out.println("GeocodeUtils.isValidLatitude(89d) " + GeocodeUtils.isValidLatitude(89d));
        
        System.out.println("GeocodeUtils.isValidLongitude(106.614632)" + GeocodeUtils.isValidLongitude(106.614632));
        
        String addressString = GeocodeHelperFactory.getInstance().processReverseGeocode(10.76819, 106.644512);//54.352025, 18.646638);
        System.out.println("processReverseGeocode 10: " + addressString);
        
        //GeocodeHelperFactory.getInstance().getRoute(lat_start, lng_start, lat_end, lng_end, type, username)
	}

}
