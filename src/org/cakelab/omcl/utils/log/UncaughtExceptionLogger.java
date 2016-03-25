package org.cakelab.omcl.utils.log;


class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler {

	public void uncaughtException(Thread t, Throwable e) {
		handle(e);
	}

	public void handle(Throwable throwable) {
		try {
			Log.error("uncaught exception: ", throwable);
		} catch (Throwable t) {
			// can't do anything
		}
	}

	public static void register() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
		// required for Java version < 7
		System.setProperty("sun.awt.exception.handler",
				UncaughtExceptionLogger.class.getName());
	}
}