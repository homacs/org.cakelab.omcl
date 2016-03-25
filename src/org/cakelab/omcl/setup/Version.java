package org.cakelab.omcl.setup;

public abstract class Version {
	protected final String versionString;
	
	protected Version(String versionString) {
		this.versionString = versionString;
	}
	
	public abstract boolean isGreaterThan(Version other);
	
	public boolean isGreaterEqual(Version other) {
		return isGreaterThan(other) || equals(other);
	}
	
	public boolean isLessThan(Version other) {
		return !isGreaterEqual(other);
	}

	public boolean isLessEqual(Version other) {
		return !isGreaterThan(other);
	}
	
	public boolean isEqual(Version other) {
		return equals(other);
	}

	
	public boolean equals(Object other) {
		if (other != null && other instanceof Version) {
			Version otherVersion = (Version) other;
			return versionString.equals(otherVersion.versionString);
		} else {
			return false;
		}
	}
	
	public String toString() {
		return versionString;
	}
	
}
