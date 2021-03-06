package org.cakelab.omcl.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.repository.Versions;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.UrlConnectionUtils;
import org.cakelab.omcl.utils.log.Log;

public class UpdateServer {

	public static class DefaultTransactionAdvisor implements TransactionAdvisor {

		public void checkRetry(URLPath path, int i, Throwable e) throws Throwable {
			if (i < MAX_DOWNLOAD_ATTEMPTS) {
				if (e instanceof UnknownHostException) {
					// no need to retry
					throw e;
				} else if (e instanceof IOException 
						|| e instanceof JSONCodecException 
						|| e instanceof NumberFormatException) 
				{
					Log.info("received '" + e.toString() + "' (" + i + "/" + MAX_DOWNLOAD_ATTEMPTS + ")");
					return;
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}

		@Override
		public void validate(URLPath path, PackageDescriptor descriptor) throws Throwable {
			return;
		}

		@Override
		public void validate(URLPath path, Versions versions) throws Throwable {
			return;
		}

	}

	private static final int CONNECT_TIMEOUT_MULTIPLIER  = 2;
	private static final int READ_TIMEOUT                = 10000; // timeout on read
	private static final int CONNECT_TIMEOUT             = READ_TIMEOUT * CONNECT_TIMEOUT_MULTIPLIER; // timeout on connect and read
	private static final int MAX_DOWNLOAD_ATTEMPTS = 10;
	private URLPath root;
	private volatile boolean offline;
	private TransactionContext tx;
	private int firstRevision;
	private TransactionAdvisor txadvisor;

	public UpdateServer(URL root, TransactionAdvisor txadvisor) throws ServerLockedException, TransportException {
		this.txadvisor = txadvisor == null ? new DefaultTransactionAdvisor() : txadvisor;
		this.root = new URLPath(root);
		offline = false;
		tx = new TransactionContext(this);
		firstRevision = fetchRevision();
	}
	
	public UpdateServer() {
		this.offline = true;
	}

	public void init() {
	}
	
	public boolean isOffline() {
		return offline;
	}

	public Versions getVersions(PackageDescriptor descriptor) throws TransportException, ServerLockedException {
		URLPath baseLocation = new URLPath(descriptor.location).getParent();
		return getVersions(baseLocation);
	}

	public Versions getVersions(URLPath baseLocation) throws TransportException, ServerLockedException {
		InputStream in = null;
		try {
			Versions versions = null;
			URLPath versionsPath = getVersionsRemotePath(baseLocation);
			do {
				tx.start();
				int attempt = 0;
				while (true) {
					try {
						in = UrlConnectionUtils.getInputStream(versionsPath.toURL(), CONNECT_TIMEOUT, READ_TIMEOUT);
						versions = Versions.load(in);
						txadvisor.validate(baseLocation,  versions);
						break; // success -> exit inner loop
					} catch (SocketTimeoutException e) {
						txadvisor.checkRetry(versionsPath, attempt+=CONNECT_TIMEOUT_MULTIPLIER, e);
						UrlConnectionUtils.close(in);
					} catch (Throwable e) {
						txadvisor.checkRetry(versionsPath, ++attempt, e);
						UrlConnectionUtils.close(in);
					}
				}
			} while (!tx.commit());
			UrlConnectionUtils.close(in);
			return versions;
		} catch (TransactionFallThrough e) {
			tx.commit();
			throw new TransportException(e.getCause());
		} catch (Throwable e) {
			tx.abortAndThrow(e);
		} finally {
			UrlConnectionUtils.close(in);
		}
		// unreachable
		return null;
	}
	

	public boolean exists(URLPath documentLocation) throws TransportException, ServerLockedException {
		try {
			boolean available = false;
			do {
				tx.start();
				URLPath urlPath = root.append(documentLocation);
				available = UrlConnectionUtils.testAvailabilitySilent(urlPath.toURL(), CONNECT_TIMEOUT);
			} while (!tx.commit());
			return available;
		} catch (Throwable e) {
			tx.abortAndThrow(e);
		}
		return false;
	}

	private URLPath getVersionsRemotePath(URLPath baseLocation) {
		return root.append(baseLocation).append(Versions.FILENAME);
	}

	public PackageDescriptor getPackageDescriptorForLocation(String location) throws TransportException, ServerLockedException {
		URLPath locationPath = new URLPath(location);
		URLPath path = root.append(locationPath).append(PackageDescriptor.FILENAME);
		InputStream in = null;
		try {
			PackageDescriptor descriptor;
			do {
				tx.start();
				int attempt = 0;
				while (true) {
					try {
						in = UrlConnectionUtils.getInputStream(path.toURL(), CONNECT_TIMEOUT, READ_TIMEOUT);
						descriptor = PackageDescriptor.load(in);
						txadvisor.validate(locationPath, descriptor);
						break; // success -> exit inner loop
					} catch (SocketTimeoutException e) {
						txadvisor.checkRetry(path, attempt+=CONNECT_TIMEOUT_MULTIPLIER, e);
						UrlConnectionUtils.close(in);
					} catch (Throwable e) {
						txadvisor.checkRetry(path, ++attempt, e);
						UrlConnectionUtils.close(in);
					}
				}

			} while (!tx.commit());
			UrlConnectionUtils.close(in);
			return descriptor;
		} catch (TransactionFallThrough e) {
			tx.commit();
			throw new TransportException(e.getCause());
		} catch (Throwable e) {
			tx.abortAndThrow(e);
		} finally {
			UrlConnectionUtils.close(in);
		}
		// unreachable
		return null;
	}

	public int fetchRevision() throws ServerLockedException, TransportException {
		int revision = 0;
		InputStream in = null;
		try {
			URLPath path = root.append(Repository.REVISION_FILE);
			int attempt = 0;
			while (true) {
				try {

					in = UrlConnectionUtils.getInputStream(path.toURL(), CONNECT_TIMEOUT, READ_TIMEOUT);
					String s = FileSystem.readText(in, Charset.forName("UTF-8"));
					revision = Integer.parseInt(s.trim());
					break; // success -> exit inner loop
				} catch (SocketTimeoutException e) {
					txadvisor.checkRetry(path, attempt+=CONNECT_TIMEOUT_MULTIPLIER, e);
					UrlConnectionUtils.close(in);
				} catch (Throwable e) {
					txadvisor.checkRetry(path, ++attempt, e);
					UrlConnectionUtils.close(in);
				}
			}
		} catch (TransactionFallThrough e) {
			tx.commit();
			throw new TransportException(e.getCause());
		} catch (Throwable e) {
			this.offline = true;
			throw new TransportException(e);
		} finally {
			UrlConnectionUtils.close(in);
		}
		
		if (revision%2 == 1) {
			// Server content is locked due to an update. Because we don't know
			// how long an update of the update server might take, we will inform
			// the user and ask him to retry later.
			this.offline = true;
			throw new ServerLockedException("Update server is currently getting updated. Please retry later.");
		}
		return revision;
			
	}




	public TransactionContext getTransactionContext() {
		return tx;
	}


	public void setOffline(boolean b) {
		offline = b;
	}


	public URL getLocalUpdateUrl(String localPath) throws MalformedURLException {
		return root.append(localPath).toURL();
	}

	public String getUrl() {
		return root != null ? root.toString() : "";
	}

	public int getMinRevision() {
		return firstRevision;
	}

}
