package net.gms_world.gmsworldserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.utils.BCTools;
import com.jstakun.lm.server.utils.CryptoTools;

public class BCTest {

	@Test
	public void test() {
		try {
            String encoded = Base64.encode(BCTools.encrypt("password".getBytes()));            
            String pwd = new String(CryptoTools.decrypt(Base64.decode(encoded.getBytes())));
            Assert.assertEquals("PWD not equal", "password", pwd);        
        } catch (Exception ex) {
            Logger.getLogger(BCTools.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

}
