package net.gmsworld.server.layers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

class LandmarkPersistenceUtilsTest {

	@Test
	void test() {
		//LandmarkPersistenceUtils.persistLandmark(landmark, cacheProvider)
		
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
