package org.cakelab.omcl.setup.minecraft.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.gui.GUI;
import org.cakelab.omcl.plugins.PluginServices;
import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.minecraft.BootstrapServicesStub;
import org.cakelab.omcl.utils.log.Log;


public class InitMinecraftWorkDir extends RunMinecraftBootstrap {
	private boolean hideLauncher;

	public InitMinecraftWorkDir(File minecraftBootstrapJar, String workingDir, String version, boolean hideLauncher) {
		super("initialisation minecraft working directory at " + workingDir, "installing minecraft", minecraftBootstrapJar, workingDir, version);
		this.hideLauncher = hideLauncher;
	}

	@Override
	public void run() {
		if (hideLauncher) {
			try {
				Log.info("hiding launcher");
				
				File workDir = new File(workingDir);
				
				
				BootstrapServicesStub bootstrap = BootstrapServicesStub.create(new File(this.bootstrapJar), PluginServices.getListener());
				bootstrap.initWorkDir(workDir);
				Log.info("working directory '" + workDir.getAbsolutePath() + "' successfully initialised");
				return; // done
			} catch (StubException | IOException e) {
				Log.warn("running launcher hidden failed. Trying fallback", e);
				hideLauncher = false;
			}
		}
		
		// this is also the fallback in case hiding failed
		if (!hideLauncher) {
			Log.info("showing launcher");
			GUI.getInstance().showInfo(
					  "                  Manual steps required!", 
					  "                      DON'T PANIC!\n"
					+ "\n"
					+ "Mojang updated it's Minecraft launcher and until we have\n"
					+ "updated ours too, we need you to assist us with some clicks.\n"
					+ "\n"
					+ "\n"
					+ "The following will happen now:\n"
					+ "\n"
					+ "1. We will start the Minecraft launcher,\n"
					+ "2. YOU wait until the launcher is fully loaded,\n"
					+ "3. YOU just close it,\n"
					+ "4. and we will proceed with the installation.\n"
					+ "\n"
					+ "                      That's all!\n"
					+ "\n"
					+ "              Press OK when you are ready!");
			super.runMinecraftBootstrap();
		}
	}

}
