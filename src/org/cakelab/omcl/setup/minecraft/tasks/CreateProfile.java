package org.cakelab.omcl.setup.minecraft.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.taskman.RunnableTask;

public class CreateProfile extends RunnableTask {

	private String minecraftWorkingDir;
	private String litwWorkingDir;
	private String version;
	private String javaArgs;
	private String profileName;
	private String gamedir;

	public CreateProfile(String profileName, File gamedir, String minecraftWorkingDir, String litwWorkingDir, String version, String javaArgs) {
		super("creation of profile '" + profileName + "' in working directory '" + litwWorkingDir + "'", "installing mods");
		this.profileName = profileName;
		this.gamedir = gamedir.toString();
		this.minecraftWorkingDir = minecraftWorkingDir;
		this.litwWorkingDir = litwWorkingDir;
		this.version = version;
		this.javaArgs = javaArgs;
	}

	@Override
	public void run() {
		File minecraftWD = new File(this.minecraftWorkingDir);
		File litwWD = new File(this.litwWorkingDir);
		File mcLauncherProfilesFile = new File(minecraftWD, LauncherProfiles.PROFILES_FILE);
		File litwProfilesFile = new File(litwWD,  LauncherProfiles.PROFILES_FILE);
		try {
			LauncherProfiles litwProfiles;
			
			if (!litwProfilesFile.exists()) {
				if (mcLauncherProfilesFile.exists()) {
					LauncherProfiles mcProfiles = LauncherProfiles.load(mcLauncherProfilesFile);
					litwProfiles = new LauncherProfiles(mcProfiles);
					litwProfiles.clearProfiles();
				} else {
					litwProfiles = LauncherProfiles.createEmpty();
				}
			} else {
				litwProfiles = LauncherProfiles.load(litwProfilesFile);
			}
			
			litwProfiles.addProfile(profileName, gamedir, version, javaArgs);
			litwProfiles.setLauncherVisibility_CloseOnStart(profileName);
			litwProfiles.setSelectProfile(profileName);
			
			if (!litwWD.exists()) {
				litwWD.mkdirs();
			}
			litwProfiles.save(litwProfilesFile);
		} catch (IOException | JSONException | JSONCodecException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
