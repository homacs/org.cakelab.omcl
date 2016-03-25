package org.cakelab.omcl.setup.minecraft;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.minecraft.tasks.CreateProfile;
import org.cakelab.omcl.setup.minecraft.tasks.InitMinecraftWorkDir;
import org.cakelab.omcl.setup.minecraft.tasks.InstallMinecraftVersion;
import org.cakelab.omcl.taskman.TaskManager;
import org.cakelab.omcl.utils.OS;

public class MinecraftClient extends SetupService {

	private static final String SUBDIR_VERSIONS = "versions";
	private static final String SUBDIR_LIBRARIES = "libraries";
	public static final String SUBDIR_CONFIG = "config";
	public static final String SUBDIR_MODS = "mods";
	public static final String SUBDIR_SHADERPACKS = "shaderpacks";
	private MinecraftBootstrap bootstrap;
	
	private File versionsDir;
	private File jar;
	private File minecraftWorkdir;
	private File launcherProfilesFile;
	private File librariesDir;
	private File launcher;

	protected MinecraftClient(SetupParameters setupParams, PackageDescriptor pd, Repository repository, MinecraftBootstrap bootstrap) throws IOException {
		super(setupParams, pd, repository);
		this.bootstrap = bootstrap;
		
		versionsDir = new File(setupParams.workdir, SUBDIR_VERSIONS);
		librariesDir = new File(setupParams.workdir, SUBDIR_LIBRARIES);
		
		File specificVersionDir = new File(versionsDir, descriptor.version);
		jar = new File(specificVersionDir, descriptor.version + ".jar");

		minecraftWorkdir = MinecraftClient.getStandardWorkingDirectory();
	}
	
	@Override
	public boolean hasUpgrade() {
		// TODO: implement maybe
		// there are lots of upgrades, we don't care about since the
		// main setup service (LitW/R) chooses versions it needs.
		
		// function will never been called
		return true;
	}

	@Override
	public void init() throws Throwable {
		launcherProfilesFile = new File(setupParams.workdir, LauncherProfiles.PROFILES_FILE);
		launcher = new File(setupParams.workdir, MinecraftBootstrap.LAUNCHER_JAR);
	}

	@Override
	public boolean isDownloaded() {
		return true;
	}

	@Override
	public boolean isInstalled() {
		return profileExists(setupParams.gameConfig.getProfileName()) 
				&& launcher.exists() && jar.exists();
	}

	public boolean profileExists(String profileName) {
		boolean profileExists = false;
		LauncherProfiles launcherProfiles = getLauncherProfiles();
		if (launcherProfiles != null) {
			profileExists = launcherProfiles.exists(setupParams.gameConfig.getProfileName());
		}
		return profileExists;
	}

	public static MinecraftClient getSetupService(SetupParameters setupParams,
			PackageDescriptor mcClientDescriptor, Repository repository, MinecraftBootstrap boostrap) throws IOException {
		return new MinecraftClient(setupParams, mcClientDescriptor, repository, boostrap);
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		// downloads will be triggered through bootstrap in installation
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		if (!isInstalled()) {
			String minecraftWorkingDir = minecraftWorkdir.getAbsolutePath();
			if (!launcher.exists() || !launcherProfilesFile.exists()) {
				taskman.addSingleTask(new InitMinecraftWorkDir(bootstrap.getJar(), setupParams.workdir.getAbsolutePath(), descriptor.version, true));
			}
			if (!profileExists(setupParams.gameConfig.getProfileName())) {
				taskman.addSingleTask(new CreateProfile(setupParams.gameConfig.getProfileName(), setupParams.gamedir, minecraftWorkingDir, setupParams.workdir.getAbsolutePath(), descriptor.version, setupParams.javaArgs));
			}
			if (!jar.exists()) {
				taskman.addSingleTask(new InstallMinecraftVersion(bootstrap.getJar(), setupParams.workdir.getAbsolutePath(), descriptor.version, true));
			}
		}
	}

	@Override
	public void scheduleUpgrades(TaskManager taskman, SetupService formerVersionSetup) throws Throwable {
		// no difference to install
		scheduleInstalls(taskman, false);
	}

	
	@Override
	public void scheduleRemove(TaskManager taskman) {
		// TODO Auto-generated method stub
		
	}

	public static File getStandardWorkingDirectory() {
		String userHome = System.getProperty("user.home", ".");

		File workingDirectory;
		switch (OS.getOSFamily()) {
		case LINUX:
			workingDirectory = new File(userHome, ".minecraft/");
			break;
		case WINDOWS:
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData
					: userHome;

			workingDirectory = new File(folder, ".minecraft/");
			break;
		case MACOS:
			workingDirectory = new File(userHome,
					"Library/Application Support/minecraft");
			break;
		default:
			workingDirectory = new File(userHome, "minecraft/");
		}

		return workingDirectory;
	}
	public File getVersionsDirectory() {
		return versionsDir;
	}

	public LauncherProfiles getLauncherProfiles() {
			try {
				return LauncherProfiles.load(launcherProfilesFile);
			} catch (IOException | JSONException | JSONCodecException e) {
				return null;
			}
		
	}

	public String getVersion() {
		return descriptor.version;
	}

	public File getLibrariesDirectory() {
		return librariesDir;
	}

}
