package org.cakelab.omcl.setup.forge;

import java.io.File;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.forge.tasks.InstallForge;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;
import org.cakelab.omcl.setup.minecraft.tasks.UpdateProfile;
import org.cakelab.omcl.taskman.TaskManager;

public class Forge extends SetupService {

	private MinecraftClient minecraftClient;
	private File forgedJar;
	private File installerJar;
	private File successIndicator;

	public Forge(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository, MinecraftClient minecraftClient) {
		super(setupParams, pd, repository);
		this.minecraftClient = minecraftClient;
	}

	
	public String getForgeInternalVersion() {
		return descriptor.version.replaceFirst(minecraftClient.getVersion() + "-", "");
	}
	
	public String createLastVersionId() {
		String mcVer = minecraftClient.getVersion();
		return mcVer + "-Forge" + getForgeInternalVersion();
	}
	
	
	@Override
	public void init() throws Throwable {

		File versionsDir = minecraftClient.getVersionsDirectory();
		String lastVersionId = createLastVersionId();
		switch (setupParams.type) {
		case CLIENT:
			File forgeVersionDir = new File(versionsDir, lastVersionId);
			forgedJar = new File(forgeVersionDir, lastVersionId + ".jar");
			successIndicator = new File (forgeVersionDir, ".success");
			break;
		case SERVER:
			throw new RuntimeException("not implemented");
		}
		
		
		installerJar = repository.getLocalFileLocation(descriptor, descriptor.filename);
		
	}

	@Override
	public boolean isInstalled() {
		return successIndicator.exists() && forgedJar.exists() && profileIsForged(setupParams.gameConfig.getProfileName());
	}

	@Override
	public boolean isDownloaded() {
		return isLocalPackageAvailable();
	}

	@Override
	public boolean hasUpgrade() {
		// TODO: implement maybe
		// there are lots of upgrades, we don't care about since the
		// main setup service (LitW/R) chooses versions it needs.
		// so, this function will never be called.
		return false;
	}


	
	private boolean profileIsForged(String profileName) {
		LauncherProfiles profiles = minecraftClient.getLauncherProfiles();
		if (profiles == null) return false;
		if (!profiles.exists(profileName)) return false;
		String lastVersionId = profiles.getProfileAttribute(profileName, LauncherProfiles.ATTR_LAST_VERSION_ID);
		
		return lastVersionId.equals(createLastVersionId());
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		if (!isInstalled() || forced) super.schedulePackageDownload(taskman);
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		if (!successIndicator.exists() || force) {
			String mcVersion = descriptor.getDependencyVersion("thirdparty/minecraft/client");
			if (mcVersion == null) {
				// TODO: remove once workaround has been removed
				throw new RuntimeException("repository inconsistent or launcher needs update.");
			}
			
			taskman.addSingleTask(new InstallForge(installerJar, setupParams.workdir, mcVersion));
		}
		if (!profileIsForged(setupParams.gameConfig.getProfileName())) {
			taskman.addSingleTask(new UpdateProfile(setupParams.workdir, setupParams.gameConfig.getProfileName(), createLastVersionId()));
		}
	}
	
	@Override
	public void scheduleUpgrades(TaskManager taskman, SetupService formerSetupService) throws Throwable {
		// forge itself is not affected by upgrades
		scheduleInstalls(taskman, false);
	}	

	public static Forge getSetupService(SetupParameters setupParams,
			PackageDescriptor forgeDescriptor, Repository repository,
			MinecraftClient minecraftClient) {
		return new Forge(setupParams, forgeDescriptor, repository, minecraftClient);
	}


	@Override
	public void scheduleRemove(TaskManager taskman) {
		throw new RuntimeException("not implemented");
	}


}
