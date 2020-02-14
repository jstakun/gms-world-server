package net.gmsworld.server.layers;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import net.gmsworld.server.config.ConfigurationManager;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.UrlUtils;

public class UtilsTest {

	@Test
	public void test() {
		String longUrl = ConfigurationManager.SSL_SERVER_URL;
		String shortUrl1 = UrlUtils.getShortUrl(longUrl);
		System.out.println("Short url #1: " + shortUrl1);
		assertNotEquals("Shortening url 1 failed!", longUrl, shortUrl1);
		//String shortUrl2 = UrlUtils.getGoogleShortUrl(longUrl);
		//System.out.println("Short url #2: " + shortUrl2);
		//assertNotEquals("Shortening url 2 failed!", longUrl, shortUrl2);
	}
	
	public void geocodeTest() {
		System.out.println(GeocodeUtils.getLatitude("52,23455"));
		System.out.println(GeocodeUtils.getLatitude("52.23455"));
	}
	
	public void stringTest() throws Exception {
		//String original = "aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰ";
		/*String original = "ążźćółęńś ĄŻŹĆÓŁĘŃŚ";
		for (int i = 0; i < original.length(); i++) {
		    // we will report on each separate character, to show you how this works
		    String text = original.substring(i, i + 1);
		    // normalizing
		    String decomposed = Normalizer.normalize(text, Form.NFD);
		    // removing diacritics
		    String removed = decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

		    //removed = decomposed.replaceAll("ł", "l");
		    //removed = decomposed.replaceAll("Ł", "L");
		    
		    // checking the inside content
		    System.out.println(text + " (" + asHex(text) + ") -> "
		                + decomposed + " (" + asHex(decomposed) + ") -> "
		                + removed + " (" + asHex(removed) + ")");
		}*/
		
		/*System.out.println("\nLocales:\n");
		
		for (Locale locale : Locale.getAvailableLocales()) {
			System.out.println(locale.getDisplayName() + " -> " + locale.getCountry() + ": " + locale.getDisplayCountry());
		}
		
		Locale en = new Locale("en", "US");
		System.out.println("Currency en-US: " + Currency.getInstance(en).getCurrencyCode());
		
		System.out.println("\nCurrencies:\n");
		
		String validCurrencies = "";
		for (Currency currency : Currency.getAvailableCurrencies()) {
			System.out.println(currency.getCurrencyCode() + ": " + currency.getDisplayName());
		}
		System.out.println(validCurrencies);*/
	}
	
	public void dateTest() {
		long l = 1454963549623L; //21:32 PL
		Date date = new Date(l);
		System.out.println(DateUtils.getFormattedDateTime(new Locale("us"), date));		
		System.out.println(DateUtils.getFormattedDateTime(new Locale("pl"), date));		
	}
	
	public void calendarTest() {
		 Long creationDate = 1473102817567L;
	     String dateString = Long.toString(creationDate);
	     if (dateString.length() > 13) {
	     		dateString = dateString.substring(0, 13);
	     		creationDate = Long.valueOf(dateString);
	      }
	     Calendar c = Calendar.getInstance();
	     c.setTimeInMillis(creationDate);
	     System.out.println(c.getTime());
	}
	
	public void telegramTest() {
		try {
			TelegramUtils.sendTelegram("123456789", "test <b>bold</> <i>italic</i>, <em>italic</em>\n" + 
					"<a href=\"http://www.example.com/\">inline URL</a>\n" + 
					"<a href=\"tg://user?id=123456789\">inline mention of a user</a>\n" + 
					"<code>inline fixed-width code</code>\n" + 
					"<pre>pre-formatted fixed-width code block</pre>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
