package net.gmsworld.server.layers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.memcache.MockCacheProvider;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

class LandmarkPersistenceUtilsTest {

	@Test
	void test() {
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

}
