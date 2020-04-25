package net.gmsworld.server.layers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.persistence.Landmark;
import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

class LandmarkPersistenceUtilsTest {

	@Test
	void test() {
		Landmark l = new Landmark();
		l.setLatitude(52.25);
		l.setLongitude(20.95);
		l.setName(Commons.MY_POSITION_LAYER);
		l.setLayer(Commons.MY_POSITION_LAYER);
		l.setUsername("abcd");
		l.setFlex("{\"cc\":\"BR\",\"appId\":2,\"useCount\":1,\"deviceId\":\"abcd\"}");
		//LandmarkPersistenceUtils.persistLandmark(l, new MockCacheProvider());
		
		//LandmarkPersistenceUtils.countLandmarksByCoords(latitude, longitude, radius)
		//LandmarkPersistenceUtils.countLandmarksByCoordsAndLayer(latitude, longitude, layer, radius)
		//LandmarkPersistenceUtils.countLandmarksByMonth(month)
		//LandmarkPersistenceUtils.countLandmarksByUserAndLayer(user, layer)
		
		//LandmarkPersistenceUtils.getHeatMap(nDays, cacheProvider)
		
		//LandmarkPersistenceUtils.selectLandmarkByHash(hash, cacheProvider)
		//LandmarkPersistenceUtils.selectLandmarkById(id, cacheProvider)
		//LandmarkPersistenceUtils.selectLandmarkMatchingQuery(query, limit)
		//LandmarkPersistenceUtils.selectLandmarksByCoordsAndLayer(latitude, longitude, layer, radius, limit)
		//LandmarkPersistenceUtils.selectLandmarksByMonth(first, last, month)
		//LandmarkPersistenceUtils.selectLandmarksByUserAndLayer(user, layer, first, last)
		assertEquals(10, LandmarkPersistenceUtils.selectNewestLandmarks().size());
		//LandmarkPersistenceUtils.updateLandmark(key, update);
		//LandmarkPersistenceUtils.deleteLandmark(key);
	}

}
