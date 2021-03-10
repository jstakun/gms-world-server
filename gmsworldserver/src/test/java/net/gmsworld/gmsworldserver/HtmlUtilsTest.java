package net.gmsworld.gmsworldserver;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.jstakun.lm.server.utils.HtmlUtils;

import net.gmsworld.server.utils.persistence.GeocodeCache;

public class HtmlUtilsTest {

	@Test
	public void test() {
		List<GeocodeCache> geocodeCacheList = HtmlUtils.getNewestGeocodes();
		
	    if (geocodeCacheList != null) {
	    	for (GeocodeCache geocodeCache : geocodeCacheList)
	    	{
	            System.out.println(HtmlUtils.getGeocodeDesc(geocodeCache, Locale.ENGLISH));
	    	}
	    }
	}
}

