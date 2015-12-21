package net.gmsworld.server.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final Logger logger = Logger.getLogger(ThreadManager.class.getName());
    
    private static final int capacity = 50;
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();
    
    private Map<String, Thread> threads = new ConcurrentHashMap<String, Thread>(capacity);
	
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
                    Thread.sleep(50L);
                } catch (InterruptedException ie) {
                }
            }
        }
    }
    
    public void put(String key, Runnable r) {
    	Thread t = threadFactory.newThread(r);
        
        lock.lock();
        try {
            while(threads.size() == capacity) {
                notFull.await();
            }
            t.start();
    		threads.put(key, t);
            notEmpty.signal();
        } catch (InterruptedException e) {
        	logger.log(Level.SEVERE, e.getMessage());
        } finally {
            lock.unlock();
        }
    	
        /*long startTime = System.currentTimeMillis();
    	  while (System.currentTimeMillis() - startTime < WAIT_LIMIT) {
        	try {
        		t.start();
        		threads.put(key, t);
        		break;
        	} catch (IllegalStateException e) {
        		logger.log(Level.SEVERE, e.getMessage());
        		try {
                    Thread.sleep(50L);
                } catch (InterruptedException ie) {
                }
        	}
    	}*/
    }
    
    //public Map<String, Thread> getThreads() {
    //	return threads;
    //}
    
    public void take(String key) {
    	lock.lock();
        try {
            while(threads.isEmpty()) {
                notEmpty.await();
            }
            threads.remove(key);
            notFull.signal();
        } catch (InterruptedException e) { 
        	logger.log(Level.SEVERE, e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}    
/*
public class BlockingQueue<T> {

    private Queue<T> queue = new LinkedList<T>();
    private int capacity;
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public void put(T element) throws InterruptedException {
        lock.lock();
        try {
            while(queue.size() == capacity) {
                notFull.await();
            }

            queue.add(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while(queue.isEmpty()) {
                notEmpty.await();
            }

            T item = queue.remove();
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }
}
*/