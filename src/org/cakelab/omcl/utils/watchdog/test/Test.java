package org.cakelab.omcl.utils.watchdog.test;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.utils.watchdog.WatchDogClient;
import org.cakelab.omcl.utils.watchdog.WatchDogService;

public class Test {

	public static void main(String[] args) throws IOException {
		File messageBus = new File("/tmp/openmcl.watchdog");
		WatchDogService server = new WatchDogService(messageBus);
		Runnable callback = new Runnable() {

			@Override
			public void run() {
				System.out.println("Server died.");
			}
			
		};
		WatchDogClient client = new WatchDogClient(messageBus, callback);
		server.start();
		client.run();
		
		server.shutdown();
		System.out.println("Main about to exit.");
		try {
			server.join();
		} catch (InterruptedException e) {
		}
		messageBus.delete();
	}

}
