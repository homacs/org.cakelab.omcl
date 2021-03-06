package org.cakelab.omcl.setup;

import java.io.File;

import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.config.GameTypes;


public class SetupParameters {
	// TODO: make everything private
	public GameConfig gameConfig;
	
	public File workdir;
	public File gamedir;
	public GameTypes type;
	public String version;
	public boolean keepVersion;
	public String javaArgs;
	public String[] optionals;

	public String shader;

	public SetupParameters(GameConfig gameConfig, File workdir, File gamedir,
			String version, boolean keepVersion, GameTypes type, String javaArgs, String shader, String[] optionals) {
		super();
		this.gameConfig = gameConfig;
		this.workdir = workdir;
		this.gamedir = gamedir;
		this.version = version;
		this.keepVersion = keepVersion;
		this.type = type;
		this.javaArgs = javaArgs;
		this.shader = shader;
		this.optionals = optionals;
	}

	public boolean containsOptionalAddon(String location) {
		if (optionals != null) {
			for (String l : optionals) {
				if (l.equals(location)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
