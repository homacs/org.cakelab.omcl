package org.cakelab.omcl.setup.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.cakelab.json.JSONException;
import org.cakelab.json.JSONObject;
import org.cakelab.json.JSONPrettyprint;
import org.cakelab.json.Parser;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.utils.OS;

public class LauncherProfiles {


	
	public static final String PROFILES_FILE = "launcher_profiles.json";


	private static final String ATTR_GAMEDIR = "gameDir";


	public static final String ATTR_LAST_VERSION_ID = "lastVersionId";

	public static final String ATTR_LAUNCHER_VISIBILITY_ON_GAME_CLOSE = "launcherVisibilityOnGameClose";
	public static final String ATTR_LAUNCHER_VISIBILITY_RULE_CLOSE_ON_START = "close launcher when game starts";
	public static final String ATTR_LAUNCHER_VISIBILITY_RULE_KEEP_OPEN = "keep the launcher open";
	private static final String ATTR_JAVA_ARGS = "javaArgs";


	public static final Charset PROFILE_CHARSET = Charset.defaultCharset();

	private JSONObject content;
	private transient File profilesFile;
	private transient boolean modified;




	public LauncherProfiles(LauncherProfiles that) {
		this.content = that.content;
		modified = false;
	}




	private LauncherProfiles(JSONObject content) {
		this.content = content;
		modified = true;
	}




	public LauncherProfiles() {
		this.content = new JSONObject();
		modified = true;
	}




	public static LauncherProfiles load(File file) throws IOException, JSONException, JSONCodecException {
		InputStream in = new FileInputStream(file);
		Parser jsonParser = new Parser(in, PROFILE_CHARSET);
		JSONObject json = jsonParser.parse();
		in.close();
		LauncherProfiles p = new LauncherProfiles(json);
		
		p.profilesFile = file;
		p.modified = false;
		return p;
	}
	


	public void save(File file) throws IOException, JSONCodecException, JSONException {
		FileOutputStream out = new FileOutputStream(file);
		
		JSONPrettyprint jpp = new JSONPrettyprint(true, JSONPrettyprint.NON_UNICODE_VALUES);
		String string = content.toString(jpp);
		out.write(string.getBytes(PROFILE_CHARSET));
		out.close();
		profilesFile = file;
		modified = false;
	}
	
	public void addProfile(String name, String gamedir, String lastVersionId, String javaArgs) {
		JSONObject profile = new JSONObject();
		profile.put("name", name);
		if (gamedir != null) profile.put("gameDir", gamedir);
		profile.put("lastVersionId", lastVersionId);
		if (javaArgs != null) profile.put("javaArgs", javaArgs);
		
		JSONObject profiles = getProfiles();
		profiles.put(name, profile);
		modified = true;
	}

	private JSONObject getProfiles() {
		JSONObject profiles = (JSONObject) content.get("profiles");
		if (profiles == null) {
			profiles = new JSONObject();
			content.put("profiles", profiles);
			modified = true;
		}
		return profiles;
	}




	public void setSelectProfile(String name) {
		JSONObject profiles = getProfiles();

		if (!profiles.containsKey(name)) throw new IllegalArgumentException("Profile with name " + name + " does not exist");
		String selectedProfile = getSelectedProfile();
		if (selectedProfile == null || !selectedProfile.equals(name)) {
			content.put("selectedProfile", name);
			modified = true;
		}
	}

	private String getSelectedProfile() {
		return content.getString("selectedProfile");
	}

	public String getProfileAttribute(String profileName, String attrName) {
		JSONObject profiles = getProfiles();
		JSONObject json = (JSONObject) profiles.get(profileName);
		if (json == null) throw new IllegalArgumentException("profile '" + profileName + "' does not exist.");
		return json.getString(attrName);
	}

	public void setProfileAttribute(String profileName, String attrName,
			String value) {
		JSONObject profiles = getProfiles();
		JSONObject json = (JSONObject) profiles.get(profileName);
		if (json == null) throw new IllegalArgumentException("profile '" + profileName + "' does not exist.");
		String previousValue = getProfileAttribute(profileName, attrName);
		if (!value.equals(previousValue)) {
			json.put(attrName, value);
			modified = true;
		}
	}

	public void setLauncherVisibility_CloseOnStart(String profileName) {
		setProfileAttribute(profileName, ATTR_LAUNCHER_VISIBILITY_ON_GAME_CLOSE, ATTR_LAUNCHER_VISIBILITY_RULE_KEEP_OPEN);
	}

	public void removeProfile(String profileName) {
		JSONObject profiles = getProfiles();
		profiles.remove(profileName);
		modified = true;
	}

	public void setGameDir(String profileName, File selectedFile) {
		setProfileAttribute(profileName, ATTR_GAMEDIR, selectedFile.toString());
	}

	public String getGameDir(String profileName) {
		return getProfileAttribute(profileName, ATTR_GAMEDIR);
	}

	public boolean exists(String profileName) {
		JSONObject profiles = getProfiles();
		return profiles.containsKey(profileName);
	}

	public String getJavaArgs(String profileName) {
		if (exists(profileName)) {
			return getProfileAttribute(profileName, ATTR_JAVA_ARGS);
		}
		return null;
	}

	public void setJavaArgs(String profileName, String javaArgs) {
		if (javaArgs.length() > 0) setProfileAttribute(profileName, ATTR_JAVA_ARGS, javaArgs);
		else removeProfileAttribut(profileName, ATTR_JAVA_ARGS);
	}

	private void removeProfileAttribut(String profileName, String attr) {
		JSONObject profiles = getProfiles();
		JSONObject json = (JSONObject) profiles.get(profileName);
		if (json == null) throw new IllegalArgumentException("profile '" + profileName + "' does not exist.");
		json.remove(attr);
		modified = true;
	}

	public static LauncherProfiles createEmpty() {
		return new LauncherProfiles();
	}

	public boolean isModified() {
		return modified;
	}

	public void save() throws IOException, JSONCodecException, JSONException {
		save(profilesFile);
	}


	
	public static void main(String[] args) throws IOException, JSONException, JSONCodecException {
		// testing
//		File parent = new File(Shell.getEnvironment().get("APPDATA"));
		File parent = new File("C:\\Users\\Teströpôliß\\AppData\\Roaming");
		if (!OS.isWindows()) {
			parent = new File(System.getProperty("user.home"));
		}
		File f = new File(parent, ".minecraft" + File.separator + PROFILES_FILE);
		LauncherProfiles p = load(f);
		String dir = p.getGameDir("pr0t0z0rq");
		System.out.println("dir: " + dir);
		
		System.out.println(p);
		
	}




	public void clearProfiles() {
		content.put("profiles", new JSONObject());
		modified = true;
	}

}
