package net.gmsworld.gmsworldserver;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.jstakun.lm.server.utils.MailUtils;

public class MailUtilsTest {

	private static final String MAIL = "jstakun.appspot@gamil.com";
	
	
	@Test
	public void test() {
		//MailUtils.sendDeviceLocatorVerificationRequest(MAIL, "Admin", "abcd", null, 0);
		File file = new File("/home/user/crash-report.txt");

		try (FileInputStream inputStream = new FileInputStream(file)) {
			String fileContents = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
			int pos = fileContents.indexOf("SHARED_PREFERENCES");
			if (pos  < 0) {
				pos = 0;
			}
			MailUtils.sendCrashReport("New crash report", fileContents.substring(pos));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(MAIL + " exists: " + MailUtils.emailAccountExists(MAIL));
	}
}
