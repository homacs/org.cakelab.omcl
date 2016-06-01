package org.cakelab.omcl.setup;

import java.io.File;
import java.net.URL;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.tasks.DownloadSoftwarePackage;
import org.cakelab.omcl.setup.tasks.DownloadSoftwarePackageExternal;
import org.cakelab.omcl.taskman.TaskManager;

public abstract class SetupService {
	
	protected PackageDescriptor descriptor;
	protected SetupParameters setupParams;
	protected Repository repository;

	protected SetupService(SetupParameters setupParams, PackageDescriptor pd, Repository repository) {
		this.setupParams = setupParams;
		this.descriptor = pd;
		this.repository = repository;
	}

	
	public abstract void init() throws Throwable;

	public abstract boolean isDownloaded();
	
	public abstract boolean isBaseInstalled();
	
	public abstract boolean hasUpgrade();

	public abstract boolean hasModifications();
	
	public abstract void scheduleDownloads(TaskManager taskman, boolean force) throws Throwable;

	public abstract void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable;

	public abstract void scheduleModifications(TaskManager taskman, boolean force) throws Throwable;

	public abstract void scheduleUpgrades(TaskManager taskman, SetupService formerVersionSetupService) throws Throwable;

	public abstract void scheduleRemove(TaskManager taskman) throws Throwable;

	public String getInstalledVersion() throws Throwable {
		throw new UnsupportedOperationException();
	}

	public boolean isLocalPackageAvailable() {
		return repository.isLocalPackageHealthy(descriptor);
	}
	
	public File getPackageRepositoryFile() {
		return repository.getLocalFileLocation(descriptor, descriptor.filename);
	}
	
	protected void schedulePackageDownload(TaskManager taskman) throws Throwable {
		schedulePackageDownload(taskman, "downloading");
	}

	protected void schedulePackageDownload(TaskManager taskman, String userInfo) throws Throwable {
		if (!repository.isLocalPackageHealthy(descriptor)) {
			URL url = repository.getDownloadUrl(descriptor);
			System.out.println("url: " + url.toString());
			if (descriptor.downloadExternal) {
				taskman.addSingleTask(new DownloadSoftwarePackageExternal(url, descriptor.name, repository.getLocalFileLocation(descriptor, descriptor.filename), descriptor.filename, descriptor.checksum, userInfo));
			} else {
				taskman.addSingleTask(new DownloadSoftwarePackage(url, getPackageRepositoryFile(), descriptor.checksum, userInfo));
			}
		}
	}




}
