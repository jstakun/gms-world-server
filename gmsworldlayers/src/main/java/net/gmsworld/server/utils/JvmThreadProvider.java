package net.gmsworld.server.utils;

public class JvmThreadProvider implements ThreadProvider {

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r);
	}
}
