package com.jstakun.lm.server.utils;

public interface ThreadProvider {
	public Thread newThread(Runnable r);
}
