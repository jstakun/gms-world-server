package net.gmsworld.server.layers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.memcache.MockCacheProvider;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

public class LandmarkPersistenceUtilsTest {

	//@Test
	public void test() {
		final double latitude = 52.25;
		final double longitude = 20.95;
		final int radius = 10000;
		Landmark l = new Landmark();
		
		l.setLatitude(latitude);
		l.setLongitude(longitude);
		l.setName(Commons.MY_POSITION_LAYER);
		l.setLayer(Commons.MY_POSITION_LAYER);
		l.setUsername("abcd");
		l.setFlex("{\"cc\":\"BR\",\"appId\":2,\"useCount\":1,\"deviceId\":\"abcd\"}");
		l.setDescription("2a Lô A Cư Xá Phú Thọ Hòa, Phường 5, Quận 11, Thành phố Hồ Chí Minh, Vietnam");
		LandmarkPersistenceUtils.persistLandmark(l, new MockCacheProvider());
		
		System.out.println(LandmarkPersistenceUtils.countLandmarksByCoords(latitude, longitude, 10000));
		System.out.println(LandmarkPersistenceUtils.countLandmarksByCoordsAndLayer(latitude, longitude, "Public", radius));
		//LandmarkPersistenceUtils.countLandmarksByMonth(month);
		//LandmarkPersistenceUtils.countLandmarksByUserAndLayer(user, layer);
		
		//LandmarkPersistenceUtils.getHeatMap(nDays, cacheProvider)
		
		//LandmarkPersistenceUtils.selectLandmarkByHash(hash, cacheProvider)
		//LandmarkPersistenceUtils.selectLandmarkMatchingQuery(query, limit)
		//LandmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(latitude, longitude, layer, radius, limit)
		//LandmarkPersistenceUtils.selectLandmarksByMonth(first, last, month)
		//LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(user, layer, first, last)
		assertEquals(10, LandmarkPersistenceUtils.selectNewestLandmarks().size());
		//LandmarkPersistenceUtils.updateLandmark(id, update);
		if (l.getId() > 0) {
			LandmarkPersistenceUtils.selectLandmarkById(l.getId()+"", new MockCacheProvider());
			LandmarkPersistenceUtils.removeLandmark(l.getId());
		}
	}
	
	@Test
	public void testUpdate() {
		final String user = "abcd";
		int count = LandmarkPersistenceUtils.countLandmarksByUserAndLayer(user, Commons.MY_POS_CODE);
		System.out.println(user + " landmarks count " + count);
		List<Landmark> landmarks = LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(user, Commons.MY_POS_CODE, 0, 1);
		if (!landmarks.isEmpty()) {
			Landmark landmark = landmarks.get(0);
			landmark.setLatitude(landmark.getLatitude() + 0.0001d);
			landmark.setLongitude(landmark.getLongitude() + 0.0001d);
			LandmarkPersistenceUtils.updateLandmark(landmark, new MockCacheProvider());
			System.out.println(landmark.getName() + ": " + landmark.getCreationDate() + " " + landmark.getLatitude() + " " + landmark.getLongitude());
		}
	}
}
