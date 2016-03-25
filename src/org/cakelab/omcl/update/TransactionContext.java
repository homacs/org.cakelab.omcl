package org.cakelab.omcl.update;


public class TransactionContext {

	private int depth;
	private int txid;
	private UpdateServer updateServer;

	public TransactionContext(UpdateServer updateServer) {
		this.updateServer = updateServer;
		abort();
	}

	public void abort() {
		txid = -1;
		depth = 0;
	}


	public int start() throws ServerLockedException, TransportException {
		try {
			
			if (depth == 0) {
				txid = updateServer.fetchRevision();
			}
			depth++;
			return txid;
		} catch (Throwable t) {
			abort();
			throw t;
		}
	}
	
	
	public boolean validate() throws ServerLockedException, TransportException {
		assert(txid > 0);
		if (depth == 0) return true;
		else {
			try {
				int remoteRevision = updateServer.fetchRevision();
				return (remoteRevision == txid);
			} catch (Throwable t)  {
				abort();
				throw t;
			}
		}
	}

	public boolean commit() throws ServerLockedException, TransportException {
		if (txid < 0) {
			throw new TransportException("invalid transaction state. txid = " + txid);
		}
		try {
			depth--;
			if (depth == 0) {
				int expectedRevision = txid;
				txid = 0;
				

				int remoteRevision = updateServer.fetchRevision();
				if (remoteRevision > expectedRevision) {
					return false;
				} else {
					return true;
				}
			} else {
				// we do not support nested transactions.
				// Thus, every time a nested transaction calls commit,
				// it is considered to be valid until the outer-most 
				// transaction commits or aborts.
				return true;
			}
		} catch (Throwable t) {
			abort();
			throw t;
		}
	}

	public void abortAndThrow(Throwable e) throws TransportException, ServerLockedException {
		abort();
		if (e instanceof TransportException) {
			throw (TransportException)e;
		} else if (e instanceof ServerLockedException) {
			throw (ServerLockedException)e;
		} else {
			throw new TransportException(e);
		}
	}

}
