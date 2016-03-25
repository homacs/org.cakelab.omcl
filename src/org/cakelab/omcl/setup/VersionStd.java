package org.cakelab.omcl.setup;


/**
 * Utility class for standard format version numbers.
 * <p>
 * Format:
 * <pre>&lt;major&gt;[.&lt;minor&gt;[.&lt;build&gt;[-[r]&lt;revision&gt;]]]</pre>
 * <h4>major</h4>
 * Changes with major releases. 
 * Major release resets minor and build release number.<br/>
 * Can involve:
 * <ul>
 * <li>new features</li>
 * <li>API modifications (breaking APIs)</li>
 * <li>bug fixes</li>
 * <li>minor code changes</li>
 * <li>everything else</li>
 * </ul>
 * 
 * <h4>minor</h4>
 * Changes with minor releases. 
 * Minor release resets build.<br/>
 * Can involve only
 * <ul>
 * <li>new features</li>
 * <li>bug fixes</li>
 * <li>minor code changes</li>
 * </ul>
 * 
 * <h4>build</h4>
 * Changes with bug fixes.
 * Can involve only.<br/>
 * <ul>
 * <li>bug fixes</li>
 * <li>minor code changes</li>
 * <li>other invisible changes (e.g. new repository)
 * </ul>
 * 
 * <h4>revision</h4>
 * Is a number indicating any kind of change. 
 * It is usually incrementing with every committed
 * code change (reflecting the state of the repository
 * in a version control system). 
 * It can be reset with major, minor or build releases 
 * but it cannot be reset or decreased between those 
 * releases.
 * </p>
 * @author homac
 *
 */
public class VersionStd extends Version {

	public static int SIZEOF_MAJOR = 8;
	byte major;
	public static int SIZEOF_MINOR = 8;
	byte minor;
	public static int SIZEOF_BUILD = 2*8;
	short build;
	public static int SIZEOF_REVISION = 4*8;
	int revision;

	private long longVersionNumber;

	
	protected VersionStd(String versionString, byte major, byte minor, short build, int revision) {
		super(versionString);
		this.major = major;
		this.minor = minor;
		this.build = build;
		this.revision = revision;
		longVersionNumber = toLong(this);
	}
	
	public VersionStd(byte major, byte minor, short build) {
		this("" + major + "." + minor + "." + build, major, minor, build, 0);
	}

	public VersionStd(byte major, byte minor, short build, int revision) {
		this("" + major + "." + minor + "." + build + "-r" + revision, major, minor, build, revision);
	}

	
	private long toLong(VersionStd thatVersion) {
		long version = thatVersion.major;
		version = (version<<SIZEOF_MINOR) + thatVersion.minor;
		version = (version<<SIZEOF_BUILD) + thatVersion.build;
		version = (version<<SIZEOF_REVISION) + thatVersion.revision;
		return version;
	}
	
	/**
	 * Decodes version strings in standard format into a 
	 * VersionStd object.
	 * 
	 * @param versionString
	 * @return
	 */
	public static VersionStd decode(String versionString) {
		String[] tokens = versionString.trim().split("\\.");
		if (tokens.length > 3 || tokens.length < 1) throw new VersionFormatException(versionString);
		try {
			
			byte  major = 0;
			byte  minor = 0;
			short build = 0;
			int   revision = 0;
			
			parse: {
				int i = 0;
				major = Byte.decode(tokens[i++]);
				
				if (i == tokens.length) break parse;
				minor = Byte.decode(tokens[i++]);
				
				if (i == tokens.length) break parse;
				// split off the revision number
				tokens = tokens[i].split("-r?");
				i = 0;
				
				build = Short.decode(tokens[i++]);
				
				if (i == tokens.length) break parse;
				revision = Integer.decode(tokens[i++]);
			}

			return new VersionStd(versionString, major, minor, build, revision);
		} catch (NumberFormatException e) {
			 throw new VersionFormatException(versionString);
		}
	}
	
	
	@Override
	public boolean isGreaterThan(Version other) {
		if (other != null && !(other instanceof VersionStd)) throw new IllegalArgumentException("version is not of type VersionStd");
		VersionStd otherVersionStd = (VersionStd) other;
		return longVersionNumber > otherVersionStd.longVersionNumber;
	}

	public byte getMajor() {
		return major;
	}

	public byte getMinor() {
		return minor;
	}

	public short getBuild() {
		return build;
	}

	public int getRevision() {
		return revision;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////
	//
	//   TESTING SECTION BELOW
	//
	////////////////////////////////////////////////////////////////////////
	
	static void testLessThan(Version v1, Version v2, boolean expected) {
		assert(expected == !v1.isGreaterEqual(v2));
		assert(expected == !v1.isGreaterThan(v2));
		assert(expected == v1.isLessEqual(v2));
		assert(expected == v1.isLessThan(v2));
	}
	
	static void testEqual(Version v1, Version v2, boolean expected) {
		assert(expected == v1.isEqual(v2));
		assert(expected == (v1.isGreaterEqual(v2) && !v1.isGreaterThan(v2)));
		assert(expected == !(v1.isGreaterThan(v2) || v1.isLessThan(v2)));
		assert(expected == (v1.isLessEqual(v2) && !v1.isLessThan(v2)));
	}
	
	public static void main (String[] args) {
		// testing
		VersionStd v1 = VersionStd.decode("1.1.1-0");
		VersionStd v2 = VersionStd.decode("1.1.1-1");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
		v2 = VersionStd.decode("1.1.2-0");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
		v2 = VersionStd.decode("1.2.1-0");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
		v2 = VersionStd.decode("2.1.1-0");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
		v2 = VersionStd.decode("1.1.2");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
		v2 = VersionStd.decode("1.2");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
		v2 = VersionStd.decode("2");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
		v2 = VersionStd.decode("1.1.1-r1");
		testLessThan(v1, v2, true);
		testLessThan(v2, v1, false);
		testEqual(v2, v1, false);
	}

}
