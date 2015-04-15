package net.gmsworld.server.layers;

import static org.junit.Assert.*;
import net.gmsworld.server.utils.UrlUtils;

import org.junit.Test;

public class UrlUtilsTest {

	@Test
	public void test() {
		String longUrl = "http://www.gms-world.net/showLandmark/23159";
		String shortUrl1 = UrlUtils.getShortUrl(longUrl);
		System.out.println("Short url #1: " + shortUrl1);
		String shortUrl2 = UrlUtils.getGoogleShortUrl(longUrl);
		System.out.println("Short url #2: " + shortUrl2);
		assertNotEquals("Shortening url 1 failed!", longUrl, shortUrl1);
		assertNotEquals("Shortening url 2 failed!", longUrl, shortUrl2);
	}

}
