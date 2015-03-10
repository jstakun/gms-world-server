package net.gmsworld.server.layers;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import net.gmsworld.server.utils.ImageUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImageUtilsTest {

	@Test
	public void testBlack() throws MalformedURLException, IOException {
		//BufferedImage blackImage = ImageIO.read(new URL("http://lh3.ggpht.com/Ti4s1kmi4eY9kZ-E2LRX1SmHgAAPtr_asUzMMsvshoYUKknScvJfLudbT0HZQvkkvjZnrYzrgwEAGyS2Li5f"));
		BufferedImage blackImage = ImageIO.read(new URL("http://lh6.ggpht.com/i1OHP63MGpmEVSrt2UArxBcCg3aYf8oX4J3ui2UdevIHqcSATS8-h-Px2Xvko4ralasL434Ddt2NgoLJgTt9"));
		assertTrue("Image is black!", ImageUtils.isBlackImage(blackImage));
	
	    DataBufferByte buffer = (DataBufferByte)blackImage.getData().getDataBuffer();
	    byte[] data = buffer.getData();
	    assertTrue("Image is black!", ImageUtils.isBlackImage(data));
	}
	
	@Test
	public void testNotBlack() throws MalformedURLException, IOException {
	    BufferedImage notBlackImage = ImageIO.read(new URL("http://lh3.ggpht.com/Yne3Ma6VvZd5S8svHLlnIxvn5fhb5C9quXgXJYn5vvvqDmiONrhDoN54jnh67Vzc2hprWXzVKBdstmfWirYl"));
	    assertFalse("Image is not black!", ImageUtils.isBlackImage(notBlackImage));
	    
	    DataBufferByte buffer = (DataBufferByte)notBlackImage.getData().getDataBuffer();
	    assertFalse("Image is not black!", ImageUtils.isBlackImage(buffer.getData()));
	}
		
}
