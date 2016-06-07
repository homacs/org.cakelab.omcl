package org.cakelab.omcl.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.update.ServerLockedException;
import org.cakelab.omcl.update.TransactionContext;
import org.cakelab.omcl.update.TransportException;
import org.cakelab.omcl.update.URLPath;
import org.cakelab.omcl.update.UpdateServer;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.Md5Sum;
import org.cakelab.omcl.utils.json.JsonSaveTask;
import org.cakelab.omcl.utils.log.Log;





/**
 * All methods with the prefix "fetch" force to contact the
 * update server and fetch a fresh file.
 * 
 * All methods with prefix "getLocal" refer to local data only.
 * 
 * TODO: needs refactoring
 * 
 * @author homac
 *
 */
public class Repository {

	public static final String REVISION_FILE = "revision";
	protected File root;
	protected UpdateServer updateServer;

	protected int revision;
	protected TransactionContext tx;
	private boolean treatCorruptedFilesAsNoLongerExisting;
	
	
	public Repository(UpdateServer updateServer, File root) {
		this.updateServer = updateServer;
		this.root = root;
		this.revision = 0;
		tx = updateServer.getTransactionContext();
	}

	/**
	 * 
	 * @param treatCorruptedFilesAsNoLongerExisting This parameter has been introduced to support web servers which send a valid response on non existing document paths. If your web server does this, than use true.
	 * @throws IOException
	 * @throws JSONCodecException
	 * @throws ServerLockedException
	 * @throws TransportException
	 */
	public void init(boolean treatCorruptedFilesAsNoLongerExisting) throws IOException, JSONCodecException, ServerLockedException, TransportException {
		//
		// Read local repository revision
		//
		
		this.treatCorruptedFilesAsNoLongerExisting = treatCorruptedFilesAsNoLongerExisting;
		readLocalRevision();

		//
		// update local meta-infos if possible and necessary.
		//
		
		if (!updateServer.isOffline()) {
			int remoteRevision = updateServer.fetchRevision();
			if (remoteRevision > revision) {
				Log.info("updating local repository to " + remoteRevision);
				updateRepository();
				
				Log.info("local repository updated to revision " + revision);
			} else if (remoteRevision < revision) {
				Log.error("negative revision diff: remote: " + remoteRevision + " <--> local: " + revision);
				updateServer.setOffline(true);
				throw new IOException("update server inconsistent");
			} else {
				Log.info("local repository at revision " + revision);
			}
		}
	}

	/**
	 * This method updates all meta-information in the local repository
	 * and fetches, regardless whether it is up-to-date or not.
	 * 
	 * @return
	 * @throws ServerLockedException
	 * @throws TransportException
	 * @throws IOException
	 * @throws JSONCodecException
	 */
	protected final void updateRepository() throws ServerLockedException, TransportException, IOException, JSONCodecException {
		int remoteRevision = 0;
		ArrayList<JsonSaveTask> saveables = new ArrayList<JsonSaveTask>();
		try {
			do {
				saveables.clear();
				
				//
				// Traverse through the versions and descriptor files of the
				// local repository and fetch its current state from the server.
				//
				remoteRevision = tx.start();
				
				prepareUpdate(saveables);
				
				for (File file : FileSystem.listRecursive(root)) {
					if (file.getName().equals(Versions.FILENAME)) {
						String baseLocation = getRepositoryPath(file);
						try {
							Versions versions = updateServer.getVersions(new URLPath(baseLocation));
							saveables.add(new JsonSaveTask(versions, file));
						} catch (TransportException e) {
							if (e.getCause() != null && e.getCause() instanceof FileNotFoundException) {
								Log.warn("versions file at " + baseLocation + " does no longer exist on server side - ignored");
								// TODO: determine whether this package is still in use (dependencies)
							} else if (treatCorruptedFilesAsNoLongerExisting && e.getCause() != null && e.getCause() instanceof JSONCodecException) {
								Log.warn("received corrupted versions file at " + baseLocation + " from server side - ignored");
								// TODO: kick Tim in his butt to fix the web server!
							} else {
								throw e;
							}
						}
					}
					else if (file.getName().equals(PackageDescriptor.FILENAME)) 
					{
						String location = getRepositoryPath(file);
						try {
							PackageDescriptor newDescriptor = updateServer.getPackageDescriptorForLocation(location);
							if (checkDescriptorIntegrity(location, newDescriptor)) {
								saveables.add(new JsonSaveTask(newDescriptor, file));
							}
						} catch (TransportException e) {
							if (e.getCause() != null && e.getCause() instanceof FileNotFoundException) {
								Log.warn("package " + location + " does no longer exist on server side - ignored");
								// TODO: determine whether this package is still in use (dependencies)
							} else if (treatCorruptedFilesAsNoLongerExisting && e.getCause() != null && e.getCause() instanceof JSONCodecException) {
								Log.warn("received corrupted package " + location + " from server side - ignored");
								// TODO: kick Tim in his butt to fix the web server!
							} else {
								throw e;
							}
						}
					}
				}

				finishUpdate(saveables);
			} while (!tx.commit());
			
			for (JsonSaveTask s : saveables) {
				s.save();
			}
			
			revision = remoteRevision;
			saveLocalRevision(remoteRevision);
			
		} catch (Throwable e) {
			tx.abortAndThrow(e);
		}
		
	}

	private String getRepositoryPath(File file) {
		return FileSystem.removeParentPath(root, file).getParent().replace("\\", "/");
	}

	private boolean checkDescriptorIntegrity(String location,
			PackageDescriptor descriptor) throws TransportException {
		// We need this method to verify data received from a web server which sends
		// valid HTTP replies even if the requested document does not exist!
		// TODO: kick Tim in his butt to fix his web server!
		
		if (descriptor.location != null && !descriptor.location.equals(location)) {
			String message = "received invalid descriptor:"
					+ "\n\trequested: " + location
					+ "\n\treceived:  " + descriptor.location;
			if (treatCorruptedFilesAsNoLongerExisting) {
				Log.warn(message);
				return false;
			}
			else throw new TransportException(message);
		} else {
			String version = new URLPath(location).getLast();
			if (version == null || !version.equals(descriptor.version)) {
				String message = "received descriptor with invalid version field"
						+ "\n\trequested: " + version
						+ "\n\treceived:  " + descriptor.version;
				if (treatCorruptedFilesAsNoLongerExisting) {
					Log.warn(message);
					return false;
				}
				else throw new TransportException(message);
			}
		}
		return true;
	}


	protected void prepareUpdate(ArrayList<JsonSaveTask> saveables) throws TransportException, ServerLockedException {}
	protected void finishUpdate(ArrayList<JsonSaveTask> saveables) throws TransportException, ServerLockedException {}




	private void readLocalRevision() throws IOException {
		File revisionFile = new File(root, REVISION_FILE);
		if (revisionFile.exists()) {
			try {
				FileInputStream in = new FileInputStream(revisionFile);
				String s = FileSystem.readText(in, Charset.forName("UTF-8"));
				revision = Integer.parseInt(s.trim());
			} catch (FileNotFoundException | NumberFormatException e) {
				Log.error("error reading repository revision file. Forcing revision update.", e);
				revision = 0;
			}
		} else {
			revision = 0;
			saveLocalRevision(revision);
		}
	}

	public static int getRevision(File root) throws IOException {
		File revisionFile = new File(root, REVISION_FILE);
		int revision = 0;
		if (revisionFile.exists()) {
			try {
				FileInputStream in = new FileInputStream(revisionFile);
				String s = FileSystem.readText(in, Charset.forName("UTF-8"));
				revision = Integer.parseInt(s.trim());
			} catch (FileNotFoundException | NumberFormatException e) {
				Log.error("error reading repository revision file. Forcing revision update.", e);
				revision = 0;
			}
		}
		return revision;
	}
			


	private void saveLocalRevision(int thatRevision) throws IOException {
		File revisionFile = new File(root, REVISION_FILE);
		if (!revisionFile.exists()) {
			revisionFile.getParentFile().mkdirs();
		}
		FileSystem.writeText(Integer.toString(thatRevision), revisionFile.getAbsolutePath());
	}

			
	public boolean checkDependencies(PackageDescriptor descriptor, Set<String> missingDependencies) throws IOException, JSONCodecException {
		// if it is locally available, then its dependencies have already been resolved.
		if (descriptor.required != null) {
			for (String dependencyLocation : descriptor.required) {
				PackageDescriptor dependency = getLocalPackageDescriptorFromLocation(dependencyLocation);
				if (dependency == null) {
					missingDependencies.add(dependencyLocation);
				} else {
					checkDependencies(dependency, missingDependencies);
				}
			}
		}
		if (descriptor.optional != null) {
			for (String dependencyLocation : descriptor.optional) {
				PackageDescriptor dependency = getLocalPackageDescriptorFromLocation(dependencyLocation);
				if (dependency == null) {
					missingDependencies.add(dependencyLocation);
				} else {
					checkDependencies(dependency, missingDependencies);
				}
			}
		}
		return !missingDependencies.isEmpty();
	}


	public void resolveDependencies(String location, ArrayList<JsonSaveTask> saveables) throws Exception {
		PackageDescriptor descriptor;
		File f = getLocalPackageDescriptorFileFromLocation(location);
		if (f.exists()) {
			descriptor = getLocalPackageDescriptorFromLocation(location);
		} else {
			descriptor = updateServer.getPackageDescriptorForLocation(location);
			// we can't proceed unless we know that the received descriptor is valid
			if (tx.validate()) {
				saveables.add(new JsonSaveTask(descriptor, f));
			}
		}
		
		if (descriptor.required != null) {
			for (String dependencyLocation : descriptor.required) {
				resolveDependencies(dependencyLocation, saveables);
			}
		}
		
		if (descriptor.optional != null) {
			for (String dependencyLocation : descriptor.optional) {
				resolveDependencies(dependencyLocation, saveables);
			}
		}
	}


	public File getLocalPackageDescriptorFileFromLocation(String location) {
		File file = new File(root.toString(), location);
		file = new File(file, PackageDescriptor.FILENAME);
		return file;
	}


	/** 
	 * This method checks whether the file associated with a package descriptor
	 * is locally available and healthy according to a checksum check.
	 * @param pd
	 * @return
	 */
	public boolean isLocalPackageHealthy(PackageDescriptor pd) {
		File packageFile = new File(getLocalLocation(pd), pd.filename);
		if (packageFile.exists() && packageFile.isFile()) {
			return pd.checksum == null || Md5Sum.check(packageFile, pd.checksum);
		} else {
			return false;
		}
	}
	
	public PackageDescriptor getLocalPackageDescriptorFromLocation(String packageLocation) throws IOException, JSONCodecException {
		File file = getLocalPackageDescriptorFileFromLocation(packageLocation);
		try {
			PackageDescriptor pd = PackageDescriptor.load(file);
			return pd;
		} catch (FileNotFoundException e) {
			Log.error("package not available locally", e);
			throw e;
		} catch (IOException | JSONCodecException e) {
			Log.error("package descriptor corrupted", e);
			file.delete();
			throw e;
		}
	}

	public File getLocalLocation(PackageDescriptor pd) {
		return getLocalLocation(pd.location);
	}


	private File getLocalLocation(String location) {
		return new File(root.getAbsolutePath(), location);
	}

	public File getLocalFileLocation(PackageDescriptor pd, String filename) {
		return new File(getLocalLocation(pd), filename);
	}

	public File getLocalVersionsFile(String baseLocation) {
		File local = getLocalLocation(baseLocation);
		return new File(local, Versions.FILENAME);
	}


	public File getLocalVersionsFile(PackageDescriptor descriptor) {
		File local = getLocalLocation(descriptor);
		local = local.getParentFile();
		
		return new File(local, Versions.FILENAME);
	}


	public URL getDownloadUrl(PackageDescriptor descriptor) throws MalformedURLException {
		String url = descriptor.downloadUrl;
		if (url != null && url.matches("^[^:]*://.*")) {
			return new URL(url);
		} else {
			if (url == null) {
				url = descriptor.location + "/" + descriptor.filename;
			}
			return updateServer.getLocalUpdateUrl(url);
		}
	}




}
