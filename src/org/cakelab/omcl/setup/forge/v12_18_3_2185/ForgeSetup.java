package org.cakelab.omcl.setup.forge.v12_18_3_2185;

import java.io.File;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.forge.ForgeSetupBase;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;

public class ForgeSetup extends ForgeSetupBase {

	public ForgeSetup(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository, MinecraftClient minecraftClient) {
		super(setupParams, pd, repository, minecraftClient);
	}

	@Override
	public void init() throws Throwable {

		File versionsDir = minecraftClient.getVersionsDirectory();
		File librariesDir = minecraftClient.getLibrariesDirectory();
		String forgeVersionId = createLastVersionId();
		String forgeVersion = getForgeInternalVersion();
		String mcVer = minecraftClient.getVersion();
		switch (setupParams.type) {
		case CLIENT:
			File forgeVersionDir = new File(versionsDir, forgeVersionId);
			forgedJar = new File(librariesDir, "net/minecraftforge/forge/" + mcVer + "-" + forgeVersion + "/" + "forge-" + mcVer + "-" + forgeVersion + ".jar");
			successIndicator = new File (forgeVersionDir, ".success");
			break;
		case SERVER:
			throw new RuntimeException("not implemented");
		}
		installerJar = repository.getLocalFileLocation(descriptor, descriptor.filename);
	}

	public String getForgeInternalVersion() {
		return descriptor.version.replaceFirst(minecraftClient.getVersion() + "-", "");
	}
	
	public String createLastVersionId() {
		String mcVer = minecraftClient.getVersion();
		return mcVer + "-forge" + mcVer + "-" + getForgeInternalVersion();
	}
	
	

}
