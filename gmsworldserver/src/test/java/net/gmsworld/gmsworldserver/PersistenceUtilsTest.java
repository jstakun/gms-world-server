package net.gmsworld.gmsworldserver;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.jstakun.lm.server.persistence.Checkin;
import com.jstakun.lm.server.persistence.Notification;
import com.jstakun.lm.server.persistence.User;
import com.jstakun.lm.server.utils.persistence.CheckinPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.CommentPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.DevicePersistenceUtils;
import com.jstakun.lm.server.utils.persistence.LayerPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.NotificationPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.ScreenshotPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.TokenPersistenceUtils;
import com.jstakun.lm.server.utils.persistence.UserPersistenceUtils;

public class PersistenceUtilsTest {

	private static final int landmarkId = 77000;
	
	private static final String email = "test@example.com";
	
	private static final String device = "test";
	
	//@Test
	public void checkInPersistenceUtils() {
		//done
		CheckinPersistenceUtils.persist("test", null, landmarkId, 0);	
		CheckinPersistenceUtils.persist("test", "test", -1, 2);
		List<Checkin> checkins = CheckinPersistenceUtils.selectCheckinsByLandmark(Integer.toString(landmarkId));
		assertEquals(checkins.size(), 1);
	}
	
	//@Test
	public void commentPersistenceUtils() {
		//done
		CommentPersistenceUtils.persist("test", landmarkId, "test");
		System.out.println(CommentPersistenceUtils.selectCommentsByLandmark(landmarkId).size());
	}
	
	//@Test
	public void devicePersistenceUtils() throws Exception {
		final String imei = "abcd1234";
		int status = DevicePersistenceUtils.setupDevice(imei, "test", null, "test", null);
		if (status == 1) {
			assertEquals(1, DevicePersistenceUtils.isDeviceRegistered(imei));
			//DevicePersistenceUtils.sendCommand(commandString, socialId, socialNetwork)
			//DevicePersistenceUtils.sendCommand(imei, pin, name, username, command, args, correlationId, flex)
			assertEquals(1, DevicePersistenceUtils.deleteDevice(imei));
		}
		System.out.println(DevicePersistenceUtils.getUserDevices(device));
	}

	//@Test
	public void layerPersistenceUtils() {
		System.out.println(LayerPersistenceUtils.selectAllLayers().size());
		//LayerPersistenceUtils.persist(name, desc, enabled, manageable, checkinable, formatted);
	}	
	
	//@Test
	public void notificationPersistenceUtils() throws Exception {
		//done
		Notification n = NotificationPersistenceUtils.setVerified(email, false);
		System.out.println("isVerified1: " + NotificationPersistenceUtils.isVerified(email));
		System.out.println("Veryfing with secret: " + n.getSecret());
		NotificationPersistenceUtils.verifyWithSecret(n.getSecret());
		System.out.println("isVerified2: " + NotificationPersistenceUtils.isVerified(email));
		System.out.println("Deleted: " + n.getId() + ": " + NotificationPersistenceUtils.remove(n.getId()));
	}
	
	//@Test
	public void screenshotPersistenceUtils() {
		//ScreenshotPersistenceUtils.persist(username, latitude, longitude, filename);
		//ScreenshotPersistenceUtils.selectScreenshot(k);
		//ScreenshotPersistenceUtils.deleteScreenshot(filename, id);
		//ScreenshotPersistenceUtils.deleteScreenshotsOlderThanDate(ndays);
	}
		
	//@Test
	public void tokenPersistenceUtils() {
		try {
			String token = TokenPersistenceUtils.generateToken("test", "test");
			assertEquals(1, TokenPersistenceUtils.isTokenValid(token, "test"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void userPersistenceUtils() {
		//done
		String login = "test";
		String password = "test1test2";
		String secret = UserPersistenceUtils.persist(login, password, email, "testing", "testing");
		if (StringUtils.isNotEmpty(secret)) {
			System.out.println("Confirm User Registration: " + UserPersistenceUtils.confirmUserRegistration(login));
			System.out.println("Login: " + UserPersistenceUtils.login(login, password.getBytes()));
			System.out.println("User Exists: " +  UserPersistenceUtils.userExists(login));
			User u = UserPersistenceUtils.selectUserByLogin(login, secret);
			if (u != null) {
				System.out.println("User Name: " + u.getLogin());
			}
			UserPersistenceUtils.removeUser(secret);
			//
		}
	}
}
