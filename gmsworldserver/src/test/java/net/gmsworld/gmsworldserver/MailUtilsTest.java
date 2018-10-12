package net.gmsworld.gmsworldserver;

import org.junit.Test;

import com.jstakun.lm.server.utils.MailUtils;

public class MailUtilsTest {

	@Test
	public void test() {
		MailUtils.sendDeviceLocatorVerificationRequest("jstakun.appspot@gmail.com", null, "abcd", null, 0);
	
		//MailUtils.sendDlRegistrationNotification("jstakun.appspot@gmail.com", null, null); 
	}

}
