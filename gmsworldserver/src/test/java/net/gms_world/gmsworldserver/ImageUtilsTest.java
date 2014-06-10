package net.gms_world.gmsworldserver;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.junit.Test;

import static org.junit.Assert.*;

import com.jstakun.lm.server.utils.ImageUtils;

public class ImageUtilsTest {

	@Test
	public void testBlack() throws MalformedURLException, IOException {
		BufferedImage blackImage = ImageIO.read(new URL("http://lh6.ggpht.com/hwuIHGrdglH8qhjX2sivpUy83jimoVA1kqDI6pm06kWG0jAq-yHE9CxMiFXckoiZki5VWk9e08s-CMqVrVc"));
	    assertTrue("Image is black!", ImageUtils.isBlackImage(blackImage));
	
	    DataBufferByte buffer = (DataBufferByte)blackImage.getData().getDataBuffer();
	    assertTrue("Image is black!", ImageUtils.isBlackImage(buffer.getData()));
	}
	
	@Test
	public void testNotBlack() throws MalformedURLException, IOException {
	    BufferedImage notBlackImage = ImageIO.read(new URL("http://lh3.ggpht.com/Yne3Ma6VvZd5S8svHLlnIxvn5fhb5C9quXgXJYn5vvvqDmiONrhDoN54jnh67Vzc2hprWXzVKBdstmfWirYl"));
	    assertFalse("Image is not black!", ImageUtils.isBlackImage(notBlackImage));
	    
	    DataBufferByte buffer = (DataBufferByte)notBlackImage.getData().getDataBuffer();
	    assertFalse("Image is not black!", ImageUtils.isBlackImage(buffer.getData()));
	}
		
}
