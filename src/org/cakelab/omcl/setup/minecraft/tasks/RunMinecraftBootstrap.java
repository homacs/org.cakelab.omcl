package org.cakelab.omcl.setup.minecraft.tasks;

import java.io.File;

import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.OS;

public class RunMinecraftBootstrap extends RunnableTask {

	protected String bootstrapJar;
	protected String workingDir;
	protected String version;

	public RunMinecraftBootstrap(File minecraftBootstrapJar, String workingDir, String version) {
		super("execution of minecraft bootstrap", "installing minecraft");
		this.workingDir = workingDir;
		this.version = version;
		this.bootstrapJar = minecraftBootstrapJar.getAbsolutePath();
	}

	public RunMinecraftBootstrap(String logInfo, String userInfo, File minecraftBootstrapJar,
			String workingDir, String version) {
		super(logInfo, userInfo);
		this.workingDir = workingDir;
		this.version = version;
		this.bootstrapJar = minecraftBootstrapJar.getAbsolutePath();
	}

	@Override
	public void run() {
		runMinecraftBootstrap();
	}

	protected void runMinecraftBootstrap() {
		try {
			
			
			File dir = new File(workingDir);
			dir.mkdirs();
			
			File javaCmd = OS.getJavaExecutable();
			
			
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO();
			pb.command(javaCmd.toString(), "-jar", this.bootstrapJar, "--workDir", this.workingDir);
			pb.directory(new File(this.workingDir));
			
			Process p = pb.start();
		if (0 != p.waitFor()) {
			throw new RuntimeException("Minecraft launcher finished with error.");
		}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
