package net.gmsworld.server.layers;

import static org.junit.Assert.*;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;

import org.junit.Test;

public class StringNormalizerTest {

	@Test
	public void test() {
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
		
		for (Locale locale : Locale.getAvailableLocales()) {
			  System.out.println(locale.getCountry() + ": " + locale.getDisplayCountry());
		}
	}
	
	public String asHex(String arg) {
		return Integer.toHexString(arg.charAt(0));
	}

}
