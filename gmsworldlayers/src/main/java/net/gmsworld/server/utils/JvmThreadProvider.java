package net.gmsworld.server.utils;

import java.util.concurrent.ThreadFactory;

public class JvmThreadProvider implements ThreadFactory {

	public Thread newThread(Runnable r) {
		return new Thread(r);
	}
}
