package net.gmsworld.server.layers;

import java.io.IOException;
import java.net.URL;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.config.Commons.Property;
import net.gmsworld.server.utils.HttpUtils;

import org.junit.Test;

public class SerializationTest {

	@Test
	public void test() throws ClassNotFoundException, IOException {
        URL url = new URL("https://landmarks-gmsworld.rhcloud.com/s/facebookProvider?format=bin&latitude=52.25&longitude=20.95&distance=10");
		HttpUtils.loadLandmarksList(url.toExternalForm(), Commons.getProperty(Property.RH_TEST_TOKEN), Commons.getProperty(Property.RH_TEST_SCOPE));
		
		System.out.println("\n\n------------------------------------------------------------------------------\n");
		
		url = new URL("http://www.gms-world.net/facebookProvider?format=bin&lat=52.25&lng=20.95&distance=10&version=12"); //version=11,12
		HttpUtils.loadLandmarksList(url.toExternalForm(), null, null);
	}

}
