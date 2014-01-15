package net.gms_world.gmsworldserver;

//import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.w3c.tidy.Tidy;

public class TidyTest {

	@Test
	public void test() {
		try {
            Tidy tidy = new Tidy();
            tidy.setPrintBodyOnly(true);
            //String val = "7:30 pm <br> <br> <img src=\"http://www.dancenyc.org/images/featuredStar.png\"> 20th Anniversary NYC Season (Buglisi Dance Theatre) <br> <br> <b>20th Anniversary NYC Season</b><br> Buglisi Dance Theatre<br> The Joyce Theater<br> 175 Eighth Avenue New York, NY...";
            String val = "<p><a href=\"http://www.nyc2013.eventbrite.com/\" rel=\"nofollow\"></a><p> <p> <p> <p><strong>Divas in New York City</strong><p><strong></strong><strong>::::Our Most Popular Tour Package::::</strong><strong></strong><p><strong>April 6-10, 2013</strong><p><strong></strong> <p><strong>Package...";
            //String val = "<a href=\"http://www.nyc-arts.org/venues/639/city-hall-park\" rel=\"nofollow\">City Hall Park</a> <br>between Broadway, Park Row and Chambers Streets <br>New York, NY 10007 <br><a href=\"http://www.nyc-arts.org/map/index?lat=40.712264&long=-74.007103&venue_type=Venues&venue_id=639\"...";
            tidy.parse(new ByteArrayInputStream(val.getBytes("utf-8")), System.out);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TidyTest.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

}
