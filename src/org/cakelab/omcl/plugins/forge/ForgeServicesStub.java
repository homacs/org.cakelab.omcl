package org.cakelab.omcl.plugins.forge;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.plugins.ServicesStubBase;
import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.interfaces.ServicesListener;

public abstract class ForgeServicesStub extends ServicesStubBase {

	public ForgeServicesStub(ClassLoader classLoader) {
		super(classLoader);
	}


	public static ForgeServicesStub create(File jar, String forgeVersion, ServicesListener listener) throws StubException, IOException {
		if (forgeVersion.equals("10.13.2.1230")) {
			return org.cakelab.omcl.plugins.forge.v10_13_2_1230.ForgeServicesStub.create(jar, listener);
		} else if (forgeVersion.equals("10.13.4.1614")) {
			return org.cakelab.omcl.plugins.forge.v10_13_4_1614.ForgeServicesStub.create(jar, listener);
		} else {
			throw new StubException("No forge services plugin available for version " + forgeVersion);
		}
	}

	
	public abstract boolean installClient(File workDir) throws StubException;
	
}
