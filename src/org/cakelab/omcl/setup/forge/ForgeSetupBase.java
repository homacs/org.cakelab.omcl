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

public abstract class ForgeSetupBase extends SetupService {

	protected MinecraftClient minecraftClient;
	protected File forgedJar;
	protected File installerJar;
	protected File successIndicator;

	protected ForgeSetupBase(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository, MinecraftClient minecraftClient) {
		super(setupParams, pd, repository);
		this.minecraftClient = minecraftClient;
	}

	
	protected abstract String getForgeInternalVersion();
	
	protected abstract String createLastVersionId();
	
	

	@Override
	public boolean isBaseInstalled() {
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

	@Override
	public boolean hasModifications() {
		// TODO not defined for forge packages
		return false;
	}

	@Override
	public void scheduleModifications(TaskManager taskman, boolean force) {
		// TODO not defined for forge packages
	}

	
	protected boolean profileIsForged(String profileName) {
		LauncherProfiles profiles = minecraftClient.getLauncherProfiles();
		if (profiles == null) return false;
		if (!profiles.exists(profileName)) return false;
		String lastVersionId = profiles.getProfileAttribute(profileName, LauncherProfiles.ATTR_LAST_VERSION_ID);
		
		return lastVersionId.equals(createLastVersionId());
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		if (!isBaseInstalled() || forced) super.schedulePackageDownload(taskman);
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		if (!successIndicator.exists() || force) {
			String mcVersion = descriptor.getDependencyVersion("thirdparty/minecraft/client");
			if (mcVersion == null) {
				// TODO: remove once workaround has been removed
				throw new RuntimeException("repository inconsistent or launcher needs update.");
			}
			
			taskman.addSingleTask(new InstallForge(installerJar, getForgeInternalVersion(), setupParams.workdir, mcVersion));
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

	@Override
	public void scheduleRemove(TaskManager taskman) {
		throw new RuntimeException("not implemented");
	}


}
