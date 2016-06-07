package org.cakelab.omcl.update;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Versions;

/**
 * The transaction advisor assist during interpretation of error states in a transaction.
 * 
 * The two validate methods allow to validate the received data before it gets accepted. 
 * For example some web servers respond to requested non-existing URLs with a document of
 * another URL which is most similar. The validate methods allow to check if this has 
 * happened and allow to throw an appropriate exception.
 * 
 * The method checkRetry is called if the client receives an exception and might decide to retry 
 * fetching the resource. In case a given exception is considered as non-recoverable a retry is
 * useless. Therefore the advisor can decide whether he wants to interrupt the transaction.
 * 
 * In both cases the advisor can decide to throw a TransactionFallThrough exception to indicate
 * that the current transaction should be considered as discarded. This ends the current inner 
 * transaction but not its parent transaction. The exception provided as cause to TransactionFallThrough
 * is thrown to the parent transaction context for further error handling.
 * 
 * 
 * @see TransacitonFallThrough
 * 
 * 
 * @author homac
 *
 */
public interface TransactionAdvisor {
	public void checkRetry(URLPath location, int i, Throwable e) throws Throwable;

	public void validate(URLPath location, PackageDescriptor descriptor) throws Throwable;

	public void validate(URLPath location, Versions versions) throws Throwable;

}
