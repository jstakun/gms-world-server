package com.jstakun.lm.server.utils;

import java.util.concurrent.ThreadFactory;
import com.google.appengine.api.ThreadManager;

public final class GoogleThreadProvider implements ThreadFactory {

	public Thread newThread(Runnable r) {
		return ThreadManager.createThreadForCurrentRequest(r); //.currentRequestThreadFactory().newThread(r);
	}

}
