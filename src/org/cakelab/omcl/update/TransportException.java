package org.cakelab.omcl.update;

/**
 * A transport exception indicates an error during network communication.
 * 
 * @author homac
 *
 */
public class TransportException extends Exception {
	private static final long serialVersionUID = 1L;

	public TransportException() {
	}

	public TransportException(String message) {
		super(message);
	}

	public TransportException(Throwable cause) {
		super(cause);
	}

	public TransportException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransportException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
