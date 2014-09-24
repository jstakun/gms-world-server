package net.gmsworld.server.utils;

public class JvmThreadProvider implements ThreadProvider {

	public Thread newThread(Runnable r) {
		return new Thread(r);
	}
}
