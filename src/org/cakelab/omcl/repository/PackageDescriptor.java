package org.cakelab.omcl.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.cakelab.json.codec.JSONCodec;
import org.cakelab.json.codec.JSONCodecConfiguration;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.update.URLPath;
import org.cakelab.omcl.utils.json.JsonSaveable;

public class PackageDescriptor implements JsonSaveable {
	private static JSONCodecConfiguration jsonConfig = new JSONCodecConfiguration(Charset.defaultCharset(), true, true);

	public static final String FILENAME = "package.json";
	
	
	public String name;
	public String filename;
	public String checksum;
	public String location;
	public String version;
	String downloadUrl;
	public boolean downloadExternal;
	public String[] required;
	public String[] optional;
	public String description;

	public boolean hasPatch;
	
	
	public PackageDescriptor(String name, String version, String filename, String location,
			String downloadUrl) {
		super();
		this.name = name;
		this.version = version;
		this.filename = filename;
		this.location = location;
		this.downloadUrl = downloadUrl;
		this.checksum = null;
	}

	public static PackageDescriptor load(File descriptorFile) throws JSONCodecException, IOException {
		FileInputStream in = new FileInputStream(descriptorFile);
		PackageDescriptor descriptor = load(in);
		in.close();
		return descriptor;
	}

	public static PackageDescriptor load(InputStream in) throws JSONCodecException, IOException {
		JSONCodec codec = new JSONCodec(jsonConfig);
		PackageDescriptor descriptor = (PackageDescriptor) codec.decodeObject(in, PackageDescriptor.class);
		return descriptor;
	}

	public void save(File target) throws IOException, JSONCodecException {
		FileOutputStream out = new FileOutputStream(target);
		JSONCodec codec = new JSONCodec(jsonConfig);
		codec.encodeObject(this,  out);
		
		out.close();
	}

	public String getDependencyVersion(String dependencyLocation) {
		for (String dependency : required) {
			if (dependency.startsWith(dependencyLocation)) {
				return dependency.substring(dependencyLocation.length()+1);
			}
		}
		return null;
	}


	public String findRequiredDependency(String path) {
		for (String dependency : required) {
			if (dependency.startsWith(path)) {
				return dependency;
			}
		}
		throw new IllegalArgumentException("dependency '" + path + "' does not exist in requirements of '" + name);
	}

	
	public String findOptionalDependency(String prefix) {
		if (optional != null) for (String option : optional) {
			if (option.startsWith(prefix)) {
				return option;
			}
		}
		throw new IllegalArgumentException("option '" + prefix + "' does not exist in requirements of '" + name);
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getID() {
		return new URLPath(location).getParent().toString();
	}

}
