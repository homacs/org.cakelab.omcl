package org.cakelab.omcl.config;

import org.cakelab.omcl.setup.minecraft.Options;

public class GameConfig {

	private String name;
	protected String profileName;

	boolean fullscreen = true;
	int guiScale = Options.GUI_SCALE_AUTO;
	
	
	
	public GameConfig(String name, String launcherProfileName) {
		this.setName(name);
		this.profileName = launcherProfileName;
	}

	public String getProfileName() {
		return profileName;
	}

	public boolean getFullscreen() {
		return fullscreen;
	}

	public int getGuiScale() {
		return guiScale;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
