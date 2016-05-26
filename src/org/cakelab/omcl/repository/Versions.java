package org.cakelab.omcl.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.cakelab.json.codec.JSONCodec;
import org.cakelab.json.codec.JSONCodecConfiguration;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.utils.json.JsonSaveable;

public class Versions implements JsonSaveable {
	public static final String FILENAME = "versions.json";
	private static JSONCodecConfiguration jsonConfig = new JSONCodecConfiguration(Charset.defaultCharset(), true, true);
	
	int latest;
	String[] available;
	private transient String[] versionStringsCache;
	
	
	
	public static Versions load(File file) throws JSONCodecException, IOException {
		FileInputStream in = new FileInputStream(file);
		Versions versions = load(in);
		in.close();
		return versions;
	}

	public static Versions load(InputStream in) throws JSONCodecException, IOException {
		JSONCodec codec = new JSONCodec(jsonConfig);
		Versions versions = (Versions) codec.decodeObject(in, Versions.class);
		return versions;
	}

	public void save(File target) throws IOException, JSONCodecException {
		FileOutputStream out = new FileOutputStream(target);
		JSONCodec codec = new JSONCodec(jsonConfig);
		codec.encodeObject(this,  out);
		
		out.close();
	}

	public String getLatestVersionLocation() {
		return available[latest];
	}

	public String getLatestVersionString() {
		return getAvailableVersionStrings()[latest];
	}

	
	private String getVersionString(String location) {
		return new File(location).getName();
	}
	
	public String[] getAvailableVersionStrings() {
		if (versionStringsCache == null) {
			versionStringsCache = new String[available.length];
			for (int i = 0; i < available.length; i++) {
				versionStringsCache[i] = getVersionString(available[i]);
			}
		}
		return versionStringsCache;
	}

	
	public int addAvailable(String location) {
		versionStringsCache = null;
		
		if (available == null) {
			available = new String[]{location};
			return 0;
		} else {
			available = Arrays.copyOf(available, available.length+1);
			available[available.length-1] = location;
			return available.length -1;
		}
	}

	public void setLatest(int index) {
		latest = index;
	}

}
