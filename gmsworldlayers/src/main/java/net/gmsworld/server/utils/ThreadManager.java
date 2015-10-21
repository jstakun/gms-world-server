package net.gmsworld.server.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jstakun
 */
public class ThreadManager {

    private static final long WAIT_LIMIT = 30 * 1000; //30 sec
    private static final Logger logger = Logger.getLogger(ThreadManager.class.getName());
    
    Map<String, Thread> threads = new ConcurrentHashMap<String, Thread>();
	
    private ThreadFactory threadFactory;
    
    public ThreadManager(ThreadFactory threadFactory) {
    	this.threadFactory = threadFactory;
    }
    
    //Wait until layers collection containing threads is empty
    public void waitForThreads() {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < WAIT_LIMIT) {
            logger.log(Level.INFO, "Layers size {0}", threads.size());

            if (threads.isEmpty()) {
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
    
    public void startThread(String key, Runnable r) {
    	long startTime = System.currentTimeMillis();
        Thread t = threadFactory.newThread(r);
    	
        while (System.currentTimeMillis() - startTime < WAIT_LIMIT) {
        	try {
        		t.start();
        		threads.put(key, t);
        		break;
        	} catch (IllegalStateException e) {
        		logger.log(Level.SEVERE, e.getMessage());
        		try {
                    Thread.sleep(100L);
                } catch (InterruptedException ie) {
                }
        	}
    	}
    }
    
    public Map<String, Thread> getThreads() {
    	return threads;
    }
}
