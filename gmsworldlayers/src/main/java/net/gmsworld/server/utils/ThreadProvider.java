package net.gmsworld.server.utils;

public interface ThreadProvider {
	public Thread newThread(Runnable r);
}
