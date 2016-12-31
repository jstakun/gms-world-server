package net.gmsworld.server.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jstakun
 */
public class ThreadManager {
	
	private static final long WAIT_LIMIT = 30 * 1000; //30 sec
	private static final long SLEEP_LIMIT = 100;
	private static final int capacity = 50;
    private static final Logger logger = Logger.getLogger(ThreadManager.class.getName());
    
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();
    
    private List<Thread> threads = new CopyOnWriteArrayList<Thread>();
    
    private ThreadFactory threadFactory;
    
    public ThreadManager(ThreadFactory threadFactory) {
    	this.threadFactory = threadFactory;
    }
    
    //Wait until collection containing threads is empty
    public void waitForThreads() {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < WAIT_LIMIT) {
            logger.log(Level.INFO, "Active threads count {0}", threads.size());

            if (threads.isEmpty()) {
                logger.log(Level.INFO, "Finished in {0} ms", (System.currentTimeMillis() - startTime));
                break;
            } else {
            	for (Thread t : threads) {
                	if (!t.isAlive()) {
                		lock.lock();
                		try {
                    		threads.remove(t);
                    		logger.log(Level.INFO, "Finished thread " + t.getId());
                    		notFull.signal();
                		} finally {
                            lock.unlock();
                        }
                	}
                }
            	if (!threads.isEmpty()) {
            		try {
            			Thread.sleep(SLEEP_LIMIT);
            		} catch (InterruptedException ie) {
            		}
            	}
            }
        }
    }
    
    public void put(Runnable r) {
    	Thread t = threadFactory.newThread(r);      
        lock.lock();
        try {
            while(threads.size() == capacity) {
                notFull.await();
            }
            t.start();
    		threads.add(t);
            notEmpty.signal();
        } catch (InterruptedException e) {
        	logger.log(Level.SEVERE, e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}    