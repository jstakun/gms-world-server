package net.gms_world.gmsworldserver;

import java.util.Arrays;
import java.util.StringTokenizer;

import org.junit.Test;

import com.google.gdata.util.common.util.Base64;
import com.jstakun.lm.server.utils.BCTools;
import com.jstakun.lm.server.utils.CryptoTools;

public class CryptoTest {

	@Test
	public void test() {
		byte[] up = null;
        byte[] enc = null;
        
        try {
            enc = org.bouncycastle.util.encoders.Base64.encode(BCTools.encrypt("welcome1".getBytes()));
            //String origpwd = new String(enc);
            //byte[] enc = CryptoTools.encrypt("welcome1".getBytes());
            //System.out.println("Password: " + origpwd + "<br/>");

            //String base64encp = new String(org.bouncycastle.util.encoders.Base64.encode(enc));
            //byte[] userPass = com.google.common.util.Base64.decode(base64encp);

            //System.out.println(userPass.length + " " + (new String(userPass).length()));

            //System.out.println("Password: " + new String(BCTools.decrypt(userPass)));
            up = concat("jstakun:".getBytes(), org.bouncycastle.util.encoders.Base64.decode(enc));
    
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String base64enc = new String(org.bouncycastle.util.encoders.Base64.encode(up));
       
        //String base64enc = new String(com.google.common.util.Base64.encode(enc));
        byte[][] unPw = userPass("Basic " + new String(base64enc));
        if (unPw != null) {
            byte[] password = unPw[1];
            System.out.println(new String(unPw[0]) + " " + new String(unPw[1]));
            
            try {
                System.out.println("Password: " + new String(BCTools.decrypt(password)) + "<br/>");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                System.out.println("Password: " + new String(CryptoTools.decrypt(password)) + "<br/>");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
	}
	
	private static byte[] concat(byte[] b1, byte[] b2)
	  {
	    byte[] b3 = new byte[b1.length+b2.length];
	    System.arraycopy(b1, 0, b3, 0, b1.length);
	    System.arraycopy(b2, 0, b3, b1.length, b2.length);
	    return b3;
	  }
	
	private static byte[][] userPass(String authorization) {
        StringTokenizer st = new StringTokenizer(authorization);
        if (st.hasMoreTokens()) {
            String basic = st.nextToken();
            System.out.println(basic);
            // We only handle HTTP Basic authentication

            if (basic.equalsIgnoreCase("Basic")) {
                String credentials = st.nextToken();

                System.out.println(credentials);
                String userPass = "";
                byte[] authzBytes = null;
                try {
                    authzBytes = Base64.decode(credentials);
                    userPass = new String(authzBytes);
                    //userPass = new String(org.bouncycastle.util.encoders.Base64.decode(credentials));
                    System.out.println(userPass);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // The decoded string is in the form
                // "userID:password".

                int p = userPass.indexOf(":");
                if (p != -1) {
                    byte[] userId = new byte[p];
                    byte[] password = new byte[userPass.length()-p];
                    userId = Arrays.copyOfRange(authzBytes, 0, p);
                    password = Arrays.copyOfRange(authzBytes, p+1, authzBytes.length);
                    //String userId = userPass.substring(0, p);
                    //String password = userPass.substring(p + 1);
                    System.out.println(new String(userId) + " " + new String(password));
                    return new byte[][]{userId, password};
                }
            }
        }
        return null;
    }
}
