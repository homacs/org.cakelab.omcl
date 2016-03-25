package org.cakelab.omcl.setup.minecraft.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.taskman.RunnableTask;

public class UpdateProfile extends RunnableTask {

	private String workingDir;
	private String profileName;
	private String lastVersionId;

	public UpdateProfile(File workingDir, String profileName, String lastVersionId) {
		super("update of profile '" + profileName + "' to run with '" + lastVersionId + "'", "creating profile");
		this.lastVersionId = lastVersionId;
		this.workingDir = workingDir.getAbsolutePath();
		this.profileName = profileName;
	}

	@Override
	public void run() {
		File file = new File(this.workingDir,  LauncherProfiles.PROFILES_FILE);
		try {
			LauncherProfiles profiles = LauncherProfiles.load(file);
			profiles.setProfileAttribute(profileName, LauncherProfiles.ATTR_LAST_VERSION_ID, lastVersionId);
			profiles.setSelectProfile(profileName);
			
			if (profiles.exists("Forge")) {
				profiles.removeProfile("Forge");
			}
			
			profiles.save(file);
		} catch (IOException | JSONException | JSONCodecException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
