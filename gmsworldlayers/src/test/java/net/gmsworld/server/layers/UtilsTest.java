package net.gmsworld.server.layers;

import static org.junit.Assert.assertNotEquals;

import java.util.Date;
import java.util.Locale;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.UrlUtils;

public class UtilsTest {

	public void test() {
		String longUrl = "http://www.gms-world.net/showLandmark/23159";
		String shortUrl1 = UrlUtils.getShortUrl(longUrl);
		System.out.println("Short url #1: " + shortUrl1);
		String shortUrl2 = UrlUtils.getGoogleShortUrl(longUrl);
		System.out.println("Short url #2: " + shortUrl2);
		assertNotEquals("Shortening url 1 failed!", longUrl, shortUrl1);
		assertNotEquals("Shortening url 2 failed!", longUrl, shortUrl2);
	}
	
	public void geocodeTest() {
		System.out.println(GeocodeUtils.getLatitude("52,23455"));
		System.out.println(GeocodeUtils.getLatitude("52.23455"));
	}
	
	@Test
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
		
		String utf8 = "POW[0xc4]\u0084ZKOWSKA G[0xc3]\u0093RCZEWSKA";
		String text = convertUtfHex(utf8);
		System.out.println(text);
			
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
	
	private static String asHex(String arg) {
		return Integer.toHexString(arg.charAt(0));
	}
	
	private static String convertUtfHex(String arg)  {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotEmpty(arg)) {
			for (int i=0;i<arg.length();i++) {
				if (arg.charAt(i) == '[') {
					StringBuffer hex = new StringBuffer();
					hex.append(arg.charAt(i+3));
					hex.append(arg.charAt(i+4));
					hex.append(Integer.toHexString((int)arg.charAt(i+6)));
					try {
						sb.append(new String(DatatypeConverter.parseHexBinary(hex.toString()), "UTF-8"));
					} catch (Exception e) {
						return arg;
					}
					i += 6;
				} else {
					sb.append(arg.charAt(i));
				}
			}
		}
		return sb.toString();
 	}
}
