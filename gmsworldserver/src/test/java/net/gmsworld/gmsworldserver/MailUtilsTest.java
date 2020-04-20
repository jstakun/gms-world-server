package net.gmsworld.gmsworldserver;

import org.junit.Test;

import com.jstakun.lm.server.utils.MailUtils;

public class MailUtilsTest {

	private static final String MAIL = "jstakun.appspot@gamil.com";
	
	@Test
	public void test() {
		//MailUtils.sendDeviceLocatorVerificationRequest(MAIL, "Admin", "abcd", null, 0);
		MailUtils.sendCrashReport("New crash report", "test");
		System.out.println(MAIL + " exists: " + MailUtils.emailAccountExists(MAIL));
	}
}
