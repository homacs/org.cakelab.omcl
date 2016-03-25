package org.cakelab.omcl.setup.minecraft.tasks;

import java.io.File;

import org.cakelab.omcl.gui.GUI;
import org.cakelab.omcl.plugins.PluginServices;
import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.minecraft.LauncherServicesStub;
import org.cakelab.omcl.setup.minecraft.MinecraftBootstrap;
import org.cakelab.omcl.utils.log.Log;


public class InstallMinecraftVersion extends RunMinecraftBootstrap {
	private boolean hideLauncher = true;

	public InstallMinecraftVersion(File minecraftBootstrapJar, String workingDir, String version, boolean hideLauncher) {
		super("installation of minecraft version " + version, "installing minecraft", minecraftBootstrapJar, workingDir, version);
		this.hideLauncher = hideLauncher;
	}

	@Override
	public void run() {
		if (hideLauncher) {
			LauncherServicesStub launcher = null;
			try {
				Log.info("hiding launcher");
				
				File workDir = new File(workingDir);
				
				
				launcher = LauncherServicesStub.create(new File(workDir, MinecraftBootstrap.LAUNCHER_JAR), PluginServices.getListener()); 
				if (!launcher.installVersion(version, workDir)) {
					throw new RuntimeException("Minecraft installation failed.");
				}
				return; // done
			} catch (Throwable e) {
				Throwable cause = e;
				if (cause instanceof StubException) {
					cause = cause.getCause();
				}
				if (launcher == null 
						|| cause instanceof NoSuchMethodError 
						|| cause instanceof IllegalAccessException 
						|| cause instanceof IllegalArgumentException) {
					Log.warn("running launcher hidden failed. Trying fallback", cause);
					hideLauncher = false;
				} else if (cause instanceof RuntimeException) {
					throw (RuntimeException)cause;
				} else {
					throw new RuntimeException(cause);
				}
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
					+ "2. YOU login if you see a login mask,\n"
					+ "3. YOU press 'Play' <-- that's important!\n"
					+ "4. the Minecraft launcher will install Minecraft 1.7.10,\n"
					+ "5. then it will start the Minecraft game client,\n"
					+ "6. YOU just close the game,\n"
					+ "7. and we will proceed with the installation."
					+ "\n"
					+ "\n"
					+ "                Login, press Play, wait, close,\n"
					+ "                         that's all!\n"
					+ "\n"
					+ "              Press OK when you are ready!");

			super.runMinecraftBootstrap();
		}
	}

}
