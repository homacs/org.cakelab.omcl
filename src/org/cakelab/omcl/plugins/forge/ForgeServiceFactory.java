package org.cakelab.omcl.plugins.forge;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.interfaces.ServicesListener;

public class ForgeServiceFactory {
	public static ForgeServicesStub create(File jar, String forgeVersion, ServicesListener listener) throws StubException, IOException {
		if (forgeVersion.equals("10.13.2.1230")) {
			return org.cakelab.omcl.plugins.forge.v10_13_2_1230.ForgeServicesStub.create(jar, listener);
		} else if (forgeVersion.equals("10.13.4.1614")) {
			return org.cakelab.omcl.plugins.forge.v10_13_4_1614.ForgeServicesStub.create(jar, listener);
		} else if (forgeVersion.equals("12.18.3.2185")) {
			return org.cakelab.omcl.plugins.forge.v12_18_3_2185.ForgeServicesStub.create(jar, listener);
		} else if (forgeVersion.equals("11.15.1.1902")) {
			return org.cakelab.omcl.plugins.forge.v11_15_1_1902.ForgeServicesStub.create(jar, listener);
		} else {
			throw new StubException("No forge services plugin available for version " + forgeVersion);
		}
	}

}
