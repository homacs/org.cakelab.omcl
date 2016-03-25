package org.cakelab.omcl.setup;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.taskman.TaskManager;

/**
 * This is a generic setup service which implements the download 
 * of the package only.
 * 
 * 
 * 
 * @author homac
 *
 */
public class PackageDownloadSetup  extends SetupService {

	protected PackageDownloadSetup(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository) {
		super(setupParams, pd, repository);
	}

	@Override
	public void init() throws Throwable {
	}

	@Override
	public boolean isDownloaded() {
		return super.isLocalPackageAvailable();
	}

	@Override
	public boolean isInstalled() {
		return super.isLocalPackageAvailable();
	}

	@Override
	public boolean hasUpgrade() {
		return super.isLocalPackageAvailable();
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean force)
			throws Throwable {
		if (!isDownloaded()) {
			super.schedulePackageDownload(taskman);
		}
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force)
			throws Throwable {
	}

	@Override
	public void scheduleUpgrades(TaskManager taskman,
			SetupService formerVersionSetupService) throws Throwable {
	}

	@Override
	public void scheduleRemove(TaskManager taskman) throws Throwable {
	}
	
	public static SetupService getSetupService(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		return new PackageDownloadSetup(setupParams, pd, repository);
	}


}
