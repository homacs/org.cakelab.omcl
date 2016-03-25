package org.cakelab.omcl.setup;

public class SetupStatus {
	private SetupParameters setupParams;
	private boolean isInstalled;
	private boolean hasUpgrade;
	
	public SetupStatus(SetupParameters setupParams,boolean isInstalled, boolean hasUpgrade) {
		this.setupParams = setupParams;
		this.isInstalled = isInstalled;
		this.hasUpgrade = hasUpgrade;
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
}
