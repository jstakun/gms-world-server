package com.jstakun.lm.server.utils;

import org.apache.commons.lang.RandomStringUtils;

import com.jstakun.lm.server.utils.memcache.CacheUtil;
import com.jstakun.lm.server.utils.memcache.CacheUtil.CacheType;

public class OtpUtils {
	private static final String PREFIX = "otp:";
    public static final int DEFAULT_TOKEN_LENGTH = 8;
    
    public static String generateOtpToken(final String deviceId, final Integer length) {
    	int count = DEFAULT_TOKEN_LENGTH;
    	if (length != null && length >= 4 && length <= 32) {
    		count = length;
    	}
    	final String token = RandomStringUtils.random(count, false, true);
		CacheUtil.put(PREFIX + deviceId, token, CacheType.FAST);
		return token;
    }
    
    public static String getToken(final String deviceId) {
    	final Object token = CacheUtil.remove(PREFIX + deviceId);
    	if (token != null) {
    		return token.toString();
    	} else {
    		return null;
    	}
    }
}
