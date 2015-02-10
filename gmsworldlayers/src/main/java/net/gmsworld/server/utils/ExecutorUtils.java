package net.gmsworld.server.utils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorUtils<T> {

	private static final Logger logger = Logger.getLogger(ExecutorUtils.class.getName());
	private static final long WAIT_LIMIT = 30 * 1000; //30 sec
	
	private int count = 0;
	private ExecutorService pool;
	private ExecutorCompletionService<T> completionService;
	private List<T> results;
	
	public ExecutorUtils(int count, List<T> results) {
		this.count = count;
		this.pool = Executors.newFixedThreadPool(count);
		this.completionService = new ExecutorCompletionService<T>(pool);
		this.results = results;
	}
	
	public void submit(Callable<T> c) {
		completionService.submit(c);
	}
	
	public void waitForResponses() {
		for(int i = 0; i < count; ++i) {
		    try {
		    	final Future<T> future = completionService.poll(WAIT_LIMIT, TimeUnit.MILLISECONDS); //take();
			    T response = null;
			    if (future != null) {
			    	response = future.get();
			    }
			    if (response != null) {
			    	results.add(response);
			    } else {
			    	logger.log(Level.SEVERE, "Received empty response!");
			    }
		    } catch (ExecutionException ex) {
		        logger.log(Level.SEVERE, "Error while downloading", ex);
		    } catch (InterruptedException e) {
		    	logger.log(Level.SEVERE, "Error while downloading", e);
		    }
		}
		pool.shutdown();
	}
}
