package com.jstakun.lm.server.personalization;

import com.jstakun.lm.server.config.Commons;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

public class ReCaptchaUtils {

	
	
	public static String getRecaptchaHtml() {
		ReCaptcha c = ReCaptchaFactory.newReCaptcha(Commons.RECAPTCHA_PUBLIC_KEY, Commons.RECAPTCHA_PRIVATE_KEY, false);
        return c.createRecaptchaHtml(null, null);
	}
	
	public static boolean checkAnswer(String remoteAddr, String challenge, String uresponse) {
		ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey(Commons.RECAPTCHA_PRIVATE_KEY);
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);

        return reCaptchaResponse.isValid();
	}
}
