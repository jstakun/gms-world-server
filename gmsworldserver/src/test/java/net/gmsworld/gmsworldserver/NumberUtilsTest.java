package net.gmsworld.gmsworldserver;

import org.junit.Test;

import com.jstakun.lm.server.utils.HtmlUtils;

public class NumberUtilsTest {

	@Test
	public void test() {
		//int limit = (int)1E16;
        
		/*long start = System.currentTimeMillis();
        for (int i=0;i<limit;i++) {
            String number = i + 0.25 + "";
            Double.parseDouble(number);
        }
        long end = System.currentTimeMillis();
        System.out.println("Double.parseDouble(): " + (end - start) + " ms");

        start = end;
        for (int i=0;i<limit;i++) {
            String number = i + 0.25 + "";
            org.apache.commons.lang.math.NumberUtils.createDouble(number);
        }
        end = System.currentTimeMillis();
        System.out.println("NumberUtils.createDouble(): " + (end - start) + " ms");

        start = end;
        for (int i=0;i<limit;i++) {
            String number = i + 0.25 + "";
            Double.valueOf(number);
        }
        end = System.currentTimeMillis();
        System.out.println("Double.valueOf(): " + (end - start) + " ms");*/

        /*long start = System.currentTimeMillis();
        for (int i=0;i<limit;i++) {
            double number = 12345678 / 1E6;
        }
        long end = System.currentTimeMillis();
        System.out.println("Div: " + (end - start) + " ms");

        start = end;
        for (int i=0;i<limit;i++) {
            double number = 12345678 * 0.000001;
        }
        end = System.currentTimeMillis();
        System.out.println("Mult: " + (end - start) + " ms");*/
		
		double[] coords = {52.25, 20.95, 30.16,-97.79, -74.01, 40.71};
		
		for (int i=0; i < coords.length; i++) {
			String enc = HtmlUtils.encodeDouble(coords[i]);
			System.out.println(coords[i] + " -> " + enc);
			double dec = HtmlUtils.decodeDouble(enc);
			System.out.println(coords[i] + " -> " + enc + " -> " + dec);
		}
	}
}
