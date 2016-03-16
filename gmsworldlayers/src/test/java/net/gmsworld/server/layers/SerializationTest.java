package net.gmsworld.server.layers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import net.gmsworld.server.utils.HttpUtils;

import org.junit.Test;

import com.jstakun.gms.android.landmarks.ExtendedLandmark;

public class SerializationTest {

	@Test
	public void test() throws ClassNotFoundException, IOException {
		//String buffer = HttpUtils.processFileRequest(new URL("http://www.gms-world.net/facebookProvider?format=bin&lat=52.25&lng=20.95&distance=10"));
		//InputStream is = new ByteArrayInputStream(buffer.getBytes()); 
		
		
		ByteArrayOutputStream bou = new ByteArrayOutputStream(32000);
		HttpUtils.processImageFileRequest(bou, "http://www.gms-world.net/facebookProvider?format=bin&lat=52.25&lng=20.95&distance=10"); //&version=1024");
		InputStream is = new ByteArrayInputStream(bou.toByteArray());
		
		//File file = new File("/home/jstakun/Desktop/facebookProvider.ser");
		//InputStream is = new FileInputStream(file);
		
		ObjectInputStream ois = null;
		try {
				//ois = new ObjectInputStream(new InflaterInputStream(is, new Inflater(false)));
				ois = new ObjectInputStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ois != null && ois.available() > 0) {
			int size = ois.readInt();
			System.out.println("Reading " + size + " landmarks");
			if (size > 0) {
				for(int i = 0;i < size;i++) {
					try {
						System.out.println("Reading landmark " + i); 
						ExtendedLandmark landmark = new ExtendedLandmark(); 
						landmark.readExternal(ois);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Done");
			}
		}
    
		is.close();
		ois.close();
	}

}
