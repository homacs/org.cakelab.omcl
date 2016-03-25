package org.cakelab.omcl.setup;

public class VersionFormatException extends NumberFormatException {
	public VersionFormatException(String versionString) {
		super("incompatible version string: " + versionString);
	}

	private static final long serialVersionUID = 1L;

}
