package org.cakelab.omcl.setup;

public class SetupStatus {
	private SetupParameters setupParams;
	private boolean isInstalled;
	private boolean hasUpgrade;
	private boolean hasModifications;
	
	public SetupStatus(SetupParameters setupParams,boolean isInstalled, boolean hasUpgrade, boolean hasModifications) {
		this.setupParams = setupParams;
		this.isInstalled = isInstalled;
		this.hasUpgrade = hasUpgrade;
		this.hasModifications = hasModifications;
	}

	public SetupParameters getSetupParams() {
		return setupParams;
	}

	public boolean isInstalled() {
		return isInstalled;
	}
	
	public boolean hasUpgrade() {
		return hasUpgrade;
	}
	public boolean hasModifications() {
		return hasModifications;
	}
}
