package net.gmsworld.gmsworldserver;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class DateTest {

	final SimpleDateFormat shortdf = new SimpleDateFormat("MM-dd-yy"); 
	final SimpleDateFormat longdf = new SimpleDateFormat("MM-dd-yyyy"); 
	
	@Test
	public void test() {
		Date now = new Date();
		
		shortdf.setTimeZone(TimeZone.getTimeZone("CET"));
	
		String today = "08-22-14";
		
		try {
			Date d = shortdf.parse(today);
			System.out.println(d);
			
			//String p = shortdf.format(now);
			//d = shortdf.parse(p);
			//System.out.println(p + " " + now.getTime() + " " + d.getTime());
			
			d = longdf.parse(today);
			System.out.println(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
