package org.cakelab.omcl.setup.minecraft;


import java.io.File;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.taskman.TaskManager;

public class MinecraftBootstrap extends SetupService {

	public static final String LAUNCHER_JAR = "launcher.jar";
	
	private File jar;

	protected MinecraftBootstrap(SetupParameters params, PackageDescriptor pd, Repository repository) {
		super(params, pd, repository);
		jar = repository.getLocalFileLocation(descriptor, descriptor.filename);
	}

	@Override
	public void init() {
	}


	@Override
	public boolean isBaseInstalled() {
		// This is installed if its downloaded
		return isDownloaded();
	}

	
	@Override
	public boolean hasUpgrade() {
		// TODO: implement maybe
		/// another situation here: we are always forced to
		// use the latest bootstrap. So the whole version management
		// for the bootstrap is actually just needed to detect
		// incompatibilities (based on checksum check).
		
		// this method will never been called too.
		return true;
	}

	@Override
	public boolean hasModifications() {
		// not defined for minecraft bootstrap packages
		return false;
	}

	@Override
	public void scheduleModifications(TaskManager taskman, boolean force) {
		// not defined for minecraft bootstrap packages
	}
	
	public static MinecraftBootstrap getSetupService(
			SetupParameters setupParams,
			PackageDescriptor mcBootstrapDescriptor, Repository repository) {
		return new MinecraftBootstrap(setupParams, mcBootstrapDescriptor, repository);
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		//
		// we need the bootstrap to install Minecraft or run it in case the 
		// minecraft launcher was updated and our plugin fails to control the new launcher.
		//
		// Thus, we will always keep a bootstrap available.
		//
		super.schedulePackageDownload(taskman);
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		// no installation required
	}

	@Override
	public void scheduleUpgrades(TaskManager taskman,
			SetupService formerVersionSetupService) throws Throwable {
		// no difference to installation
		scheduleInstalls(taskman, false);
	}

	public File getJar() {
		return jar;
	}

	@Override
	public boolean isDownloaded() {
		return isLocalPackageAvailable();
	}

	@Override
	public void scheduleRemove(TaskManager taskman) {
		// TODO Auto-generated method stub
		
	}

}
