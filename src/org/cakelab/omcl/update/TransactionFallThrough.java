package org.cakelab.omcl.update;


/**
 * This exception is thrown to discard a currently running (child) transaction. 
 * The transaction is considered as it has never occurred. The constructors all
 * require a cause exception to be provided, which indicates the actual reason 
 * for the transaction discard. This cause exception is thrown to the parent 
 * transaction context (the calling method) for further error handling.
 * 
 * @author homac
 *
 */
@SuppressWarnings("serial")
public class TransactionFallThrough extends Exception {

	public TransactionFallThrough(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public TransactionFallThrough(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public TransactionFallThrough(Throwable arg0) {
		super(arg0);
	}

}
