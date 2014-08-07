package com.jstakun.lm.server.utils;

import com.google.appengine.api.ThreadManager;
import net.gmsworld.server.utils.ThreadProvider;

public final class GoogleThreadProvider implements ThreadProvider {

	@Override
	public Thread newThread(Runnable r) {
		return ThreadManager.createThreadForCurrentRequest(r); //.currentRequestThreadFactory().newThread(r);
	}

}
