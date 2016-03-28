package org.cakelab.omcl.setup.forge.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.gui.GUI;
import org.cakelab.omcl.plugins.PluginServices;
import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.forge.ForgeServicesStub;
import org.cakelab.omcl.plugins.minecraft.LauncherServicesStub;
import org.cakelab.omcl.setup.minecraft.MinecraftBootstrap;
import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.log.Log;


public class InstallForge extends RunnableTask {

	private String workingDir;
	private String installer;
	private String mcVersion;
	private String forgeVersion;

	public InstallForge(File installerJar, String forgeVersion, File workingDir, String mcVersion) {
		super("installation of forge", "installing forge");
		this.forgeVersion = forgeVersion;
		this.mcVersion = mcVersion;
		this.workingDir = workingDir.getAbsolutePath();
		this.installer = installerJar.getAbsolutePath();
		
	}

	@Override
	public void run() {
		try {
			runAsPluginService();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			Log.warn("failed to execute forge installation in same process. Trying to run in separate process.", e);
			try {
				runInSeparateProcess();
			} catch (IOException e1) {
				Log.warn("Execution of forge installer in separate process failed either. Giving up.", e);
				throw new RuntimeException(e1);
			}
		}
	}

	
	private void runInSeparateProcess() throws IOException {
		File javaCmd = OS.getJavaExecutable();
		
		
		ProcessBuilder pb = new ProcessBuilder();
		pb.inheritIO();
		pb.command(javaCmd.toString(), "-jar", this.installer);
		pb.directory(new File(this.workingDir));
		try {
			Log.info("executing forge installation program");
			GUI.getInstance().showInfo(
					  "                  Manual steps required!", 
					  "                      DON'T PANIC!\n"
					+ "\n"
					+ "The Forge installer has been updated and until we have updated\n"
					+ "our launcher too, we need you to assist us with some clicks.\n"
					+ "\n"
					+ "\n"
					+ "The following will happen now:\n"
					+ "\n"
					+ "1. We will start the Forge installer,\n"
					+ "2. YOU have to select the directory " + workingDir + ",\n"
					+ "3. YOU press OK,\n"
					+ "4. the installer will do its job,\n"
					+ "5. and we will proceed with the installation.\n"
					+ "\n"
					+ "\n"
					+ "\n"
					+ "              Press OK when you are ready!");
			Process p = pb.start();
			int result = p.waitFor();
			if (0 != result) {
				throw new RuntimeException("Forge installer exited with error code " + result);
			}
		} catch(Exception e) {
			throw new RuntimeException("Couldn't run installer.", e);
		}
	}
	
	private void runAsPluginService() throws StubException, IOException {
		// 
		// In case the user decided to use its own Minecraft installation
		// which has already a 1.7.10 Minecraft client installed
		// we have to make sure that it is healthy before we run the forge 
		// installer. If everything is fine, this call will return quick.
		//
		LauncherServicesStub launcher = LauncherServicesStub.create(new File(this.workingDir, MinecraftBootstrap.LAUNCHER_JAR), PluginServices.getListener()); 
		if (!launcher.installVersion(mcVersion, new File(workingDir))) {
			throw new RuntimeException("Forge installation failed.\nMinecraft installation incomplete.");
		}

		//
		// now we can install forge.
		//
		ForgeServicesStub forge = ForgeServicesStub.create(new File(this.installer), forgeVersion, PluginServices.getListener());
		boolean success = forge.installClient(new File(this.workingDir));
		if (!success) {
			throw new RuntimeException("Forge installation failed.");
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}
	
}
