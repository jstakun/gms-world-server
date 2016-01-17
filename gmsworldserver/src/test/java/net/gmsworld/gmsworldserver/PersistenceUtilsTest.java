package net.gmsworld.gmsworldserver;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.jstakun.lm.server.persistence.Checkin;
import com.jstakun.lm.server.utils.persistence.CheckinPersistenceUtils;

public class PersistenceUtilsTest {

	private static final int landmarkId = 77000;
	
	@Test
	public void checkInPersistenceUtils() {
		CheckinPersistenceUtils.persistCheckin("test", null, landmarkId, 0);
		
		CheckinPersistenceUtils.persistCheckin("test", "test", -1, 2);
		
		List<Checkin> checkins = CheckinPersistenceUtils.selectCheckinsByLandmark(Integer.toString(landmarkId));
		
		assertEquals(checkins.size(), 1);
	}

}
