package net.gmsworld.server.layers;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

import net.gmsworld.server.config.Commons;
import net.gmsworld.server.utils.ImageUtils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImageUtilsTest {

	@Test
	public void testBlack() throws MalformedURLException, IOException {
		String[] blackImages = {
				//"http://lh5.ggpht.com/Z-IuuMwGbGAJyOobO2o1OyatirvnMqcZypn2r8PcSNIjnvDRYLofDxZZxp5WPpWZ0411ncy1xK1_vNLlYtpElA",
				//"http://lh3.ggpht.com/Ti4s1kmi4eY9kZ-E2LRX1SmHgAAPtr_asUzMMsvshoYUKknScvJfLudbT0HZQvkkvjZnrYzrgwEAGyS2Li5f",
				//"http://lh5.ggpht.com/Z-IuuMwGbGAJyOobO2o1OyatirvnMqcZypn2r8PcSNIjnvDRYLofDxZZxp5WPpWZ0411ncy1xK1_vNLlYtpElA",
			    "http://lh6.ggpht.com/i1OHP63MGpmEVSrt2UArxBcCg3aYf8oX4J3ui2UdevIHqcSATS8-h-Px2Xvko4ralasL434Ddt2NgoLJgTt9",
				"http://www.gms-world.net/image/20565"};
		
		for (int i=0;i<blackImages.length;i++) {
			BufferedImage blackImage = ImageIO.read(new URL(blackImages[i]));
			assertTrue("Image is black!", ImageUtils.isBlackImage(blackImage));
	
			DataBufferByte buffer = (DataBufferByte)blackImage.getData().getDataBuffer();
			byte[] data = buffer.getData();
			assertTrue("Image is black!", ImageUtils.isBlackImage(data));
		}	
	}
	
	@Test
	public void testNotBlack() throws MalformedURLException, IOException {
	    BufferedImage notBlackImage = ImageIO.read(new URL("http://lh3.ggpht.com/Yne3Ma6VvZd5S8svHLlnIxvn5fhb5C9quXgXJYn5vvvqDmiONrhDoN54jnh67Vzc2hprWXzVKBdstmfWirYl"));
	    assertFalse("Image is not black!", ImageUtils.isBlackImage(notBlackImage));
	    
	    DataBufferByte buffer = (DataBufferByte)notBlackImage.getData().getDataBuffer();
	    assertFalse("Image is not black!", ImageUtils.isBlackImage(buffer.getData()));
	}
	
	@Test
	public void testUrl() throws URISyntaxException, IOException {
	    String url = "http://maps.google.com/maps/api/staticmap?size=640x256&key=" + Commons.getProperty(Commons.Property.GOOGLE_API_KEY) + "&path=color:0xff0000ff" + URLEncoder.encode("|weight:5|52.230037,21.020021|52.230319,21.019512|52.230319,21.019512|52.229789,21.017101|52.229751,21.016897|52.229751,21.016897|52.229846,21.016849|52.23056,21.016487|52.230735,21.016395|52.230796,21.016365|52.230827,21.016349|52.230903,21.016309|52.230903,21.016309|52.230846,21.016067|52.230697,21.015441|52.230335,21.013757|52.230152,21.012514|52.230136,21.012205|52.23011,21.012016|52.23011,21.012016|52.230224,21.011848|52.230548,21.011495|52.230716,21.011333|52.231376,21.010957|52.232402,21.010343|52.232875,21.010051|52.233516,21.009677|52.233657,21.009595|52.233798,21.009515|52.234203,21.009269|52.235038,21.00876|52.235084,21.008735|52.23524,21.008647|52.235439,21.008518|52.23632,21.00798|52.238029,21.006948|52.23822,21.006818|52.238285,21.006742|52.238327,21.006668|52.238372,21.006591|52.238498,21.006414|52.238864,21.005802|52.23904,21.005634|52.239406,21.005273|52.239723,21.005027|52.239978,21.004894|52.240245,21.004816|52.240486,21.004779|52.240947,21.004779|52.241104,21.004732|52.241268,21.004623")
	    		     + "&markers=color:green%7Clabel:S%7C52.230037,21.020021" + "&markers=color:red%7Clabel:E%7C52.241268,21.004623";    				
        URI uri = new URI(url);	
        System.out.println(uri.toString());
        byte[] image = ImageUtils.loadImage(url);
	}
		
}
