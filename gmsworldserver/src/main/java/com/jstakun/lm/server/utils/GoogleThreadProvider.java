package com.jstakun.lm.server.utils;

import com.google.appengine.api.ThreadManager;

public final class GoogleThreadProvider implements ThreadProvider {

	@Override
	public Thread newThread(Runnable r) {
		return ThreadManager.createThreadForCurrentRequest(r); //.currentRequestThreadFactory().newThread(r);
	}

}
