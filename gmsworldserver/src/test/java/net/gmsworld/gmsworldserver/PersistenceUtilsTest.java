package net.gmsworld.gmsworldserver;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.jstakun.lm.server.persistence.Checkin;
import com.jstakun.lm.server.persistence.Notification;
import com.jstakun.lm.server.utils.persistence.CheckinPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;

public class PersistenceUtilsTest {

	private static final int landmarkId = 77000;
	
	private static final String email = "test@example.com";
	
	private static final String device = "test";
	
	//@Test
	public void checkInPersistenceUtils() {
		CheckinPersistenceUtils.persist("test", null, landmarkId, 0);	
		CheckinPersistenceUtils.persist("test", "test", -1, 2);
		List<Checkin> checkins = CheckinPersistenceUtils.selectCheckinsByLandmark(Integer.toString(landmarkId));
		assertEquals(checkins.size(), 1);
	}
	
	@Test
	public void devicePersistenceUtils() throws Exception {
		 System.out.println(DevicePersistenceUtils.getUserDevices(device));
	}

	//@Test
	public void notificationPersistenceUtils() throws Exception {
		 Notification n = NotificationPersistenceUtils.setVerified(email, false);
		 System.out.println("isVerified1: " + NotificationPersistenceUtils.isVerified(email));
		 System.out.println("Veryfing with secret: " + n.getSecret());
		 NotificationPersistenceUtils.verifyWithSecret(n.getSecret());
		 System.out.println("isVerified2: " + NotificationPersistenceUtils.isVerified(email));
		 System.out.println("Deleted: " + n.getId() + ": " + NotificationPersistenceUtils.remove(n.getId()));
	}
}
