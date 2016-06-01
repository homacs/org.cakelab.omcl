package org.cakelab.omcl.setup;

import java.io.File;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;
import org.cakelab.omcl.setup.tasks.Copy;
import org.cakelab.omcl.setup.tasks.Delete;
import org.cakelab.omcl.taskman.TaskManager;


public abstract class ModSetupServiceBase extends SetupService {

	protected File modfile;
	
	protected ModSetupServiceBase(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository) {
		super(setupParams, pd, repository);
	}

	@Override
	public void init() throws Throwable {
		modfile = new File(setupParams.gamedir,MinecraftClient.SUBDIR_MODS + File.separator + descriptor.filename);
	}

	@Override
	public boolean isDownloaded() {
		return super.isLocalPackageAvailable();
	}

	@Override
	public boolean isBaseInstalled() {
		return modfile.exists();
	}

	@Override
	public boolean hasUpgrade() {
		return false;
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		if (forced || !isDownloaded()) {
			super.schedulePackageDownload(taskman);
		}
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		if (!isBaseInstalled() || force) {
			taskman.addSingleTask(new Copy("installing mod", getPackageRepositoryFile().getPath(), modfile.getPath()));
		}
	}

	@Override
	public void scheduleUpgrades(TaskManager taskman, SetupService formerVersionSetupService) throws Throwable {
		formerVersionSetupService.scheduleRemove(taskman);
		scheduleInstalls(taskman, true);
	}

	@Override
	public void scheduleRemove(TaskManager taskman) throws Throwable {
		if (isBaseInstalled()) {
			taskman.addSingleTask(new Delete("upgrading mod-pack", modfile.getAbsolutePath()));
		}
	}

}
