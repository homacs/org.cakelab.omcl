package org.cakelab.omcl.update;

public class ServerLockedException extends Exception {

	private static final long serialVersionUID = 1L;

	public ServerLockedException() {
		super("Transaction failed.");
	}

	public ServerLockedException(String message) {
		super(message);
	}

	public ServerLockedException(Throwable cause) {
		super(cause);
	}

	public ServerLockedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerLockedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
