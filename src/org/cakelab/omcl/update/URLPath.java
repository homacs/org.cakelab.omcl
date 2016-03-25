package org.cakelab.omcl.update;

import java.net.MalformedURLException;
import java.net.URL;

public class URLPath {

	private String[] components;
	private int length;
	private String protocol;
	
	public static final char separatorChar = '/';
	public static final String separator = "/";

	public URLPath(String path) {
		
		path = path.replace('\\', separatorChar);
		
		if (path.matches("^[^\\/]*:\\/\\/[^//]+.*")) {
			// has protocol (that's what the expression above tested)
			int colon = path.indexOf(':');
			protocol = path.substring(0, colon);
			path = path.substring(colon+2);
		}
		components = path.split(separator);
		
		length = components.length;
	}

	public URLPath(String protocol, String[] components, int length) {
		this.protocol = protocol;
		this.length = length;
		this.components = new String[length];
		System.arraycopy(components, 0, this.components, 0, length);
	}

	public URLPath(URLPath copy) {
		this(copy.protocol, copy.components, copy.length);
	}
	

	public URLPath(URL url) {
		this(url.toString());
	}

	public URLPath(URL url, String subpath) {
		this(url.toString() + separatorChar + subpath);
	}

	public URLPath getParent() {
		return new URLPath(protocol, components, length-1);
	}

	public boolean isAbsolutePath() {
		return components[0].length() == 0;
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (protocol != null && protocol.length() > 0) {
			sb.append(protocol).append(':').append("/");
		}
		if (length > 0) {
			int i = 0;
			sb.append(components[i]);
			for (i = 1; i < length; i++) {
				sb.append(separatorChar);
				sb.append(components[i]);
			}
		}
		return sb.toString();
	}
	
	public URL toURL() throws MalformedURLException {
		return new URL(toString());
	}

	public URLPath getCleanPath() {
		URLPath clean = new URLPath(this);
		if (length > 0) {
			// first component is allowed to be empty to indicate an absolute path
			clean.components[0] = components[0];
			
			int j = 1;
			for (int i = 1; i < length; i++) {
				if (components[i].length() > 0) {
					clean.components[j] = components[i];
					j++;
				}
			}
			clean.length = j;
		}
		return clean;
	}
	
	public URLPath append(URLPath path) {
		int newLength = length + path.length;
		String[] tmp = new String[newLength];
		System.arraycopy(this.components, 0, tmp, 0, this.length);
		System.arraycopy(path.components, 0, tmp, this.length, path.length);
		return new URLPath(protocol, tmp, newLength);
	}
	
	
	public static void main(String[] args) {
		URLPath simple = new URLPath("hello\\my/lovely//path/");
		System.out.println(simple);
		URLPath protocolPath = new URLPath("http://hello\\my/lovely//path/");
		System.out.println(protocolPath);
		
		URLPath extended = protocolPath.append(simple);
		System.out.println(extended);

		URLPath parent = simple.getParent();
		System.out.println(parent);
		
		URLPath cleanPath = extended.getCleanPath();
		System.out.println(cleanPath);
		
	}

	public URLPath append(String component) {
		int newLength = length + 1;
		String[] tmp = new String[newLength];
		System.arraycopy(this.components, 0, tmp, 0, this.length);
		tmp[newLength-1] = component;
		return new URLPath(protocol, tmp, newLength);
	}
	
	
}
