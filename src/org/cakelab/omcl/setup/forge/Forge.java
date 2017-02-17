package org.cakelab.omcl.setup.forge;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;

public class Forge {

	public static SetupService getSetupService(SetupParameters setupParams,
			PackageDescriptor forgeDescriptor, Repository repository,
			MinecraftClient minecraftClient) {
		String version = forgeDescriptor.version;
		if (version.equals("1.7.10-10.13.2.1230")) {
			return new org.cakelab.omcl.setup.forge.v10_13_2_1230.ForgeSetup(setupParams, forgeDescriptor, repository, minecraftClient);
		} else if (version.equals("1.7.10-10.13.4.1614")) {
			return new org.cakelab.omcl.setup.forge.v10_13_4_1614.ForgeSetup(setupParams, forgeDescriptor, repository, minecraftClient);
		} else if (version.equals("1.10.2-12.18.3.2185")) {
			return new org.cakelab.omcl.setup.forge.v12_18_3_2185.ForgeSetup(setupParams, forgeDescriptor, repository, minecraftClient);
		} else {
			throw new RuntimeException("no setup service for forge version " + version + " found");
		}
	}
}
