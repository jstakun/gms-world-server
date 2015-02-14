package net.gmsworld.server.utils;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jstakun
 */
public class ThreadUtil {

    private static final long WAIT_LIMIT = 30 * 1000; //30 sec
    private static final Logger logger = Logger.getLogger(ThreadUtil.class.getName());
    
    //Wait until layers collection containing threads is empty
    public static void waitForLayers(Map<?, Thread> layers) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < WAIT_LIMIT) {
            logger.log(Level.INFO, "Layers size {0}", layers.size());

            if (layers.isEmpty()) {
                logger.log(Level.INFO, "Finished in {0} ms", (System.currentTimeMillis() - startTime));
                break;
            } else {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ie) {
                }
            }
        }
    }
}
