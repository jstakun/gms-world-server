package net.gmsworld.server.layers;

import static org.junit.Assert.assertNotEquals;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.DateUtils;
import net.gmsworld.server.utils.JSONUtils;
import net.gmsworld.server.utils.StringUtil;
import net.gmsworld.server.utils.UrlUtils;

import org.junit.Test;

import com.jstakun.gms.android.deals.Deal;

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
	
	public void currencyTest() {
		Deal d = new Deal();
		d.setCurrencyCode("PLN");
		d.setPrice(123.99);
		
		JSONUtils.formatCurrency(d, "en", "us", Commons.HOTELS_LAYER);
		System.out.println(d.getPrice() + " " + d.getCurrencyCode());
	}
	
	@Test
	public void stringTest() throws Exception {
		//String original = "aáeéiíoóöőuúüű AÁEÉIÍOÓÖŐUÚÜŰ";
		String original = "ążźćółęńś ĄŻŹĆÓŁĘŃŚ";
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
		}
		
		/*System.out.println("\nLocales:\n");
		
		for (Locale locale : Locale.getAvailableLocales()) {
			System.out.println(locale.getCountry() + ": " + locale.getDisplayCountry());
		}
		
		System.out.println("\nCurrencies:\n");
		
		for (Currency currency : Currency.getAvailableCurrencies()) {
			System.out.println(currency.getCurrencyCode() + ": " + currency.getDisplayName());
		}*/
		for (int i=0;i<50;i++)
		System.out.println(StringUtil.formatCoordE2(13.3001));
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
	

}
