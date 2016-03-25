package org.cakelab.omcl.setup.minecraft;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.utils.OptionsFile;

public class Options extends OptionsFile {


	public static final String FILENAME = "options.txt";

	private static final String PROPERTY_ADVANCED_OPENGL = "advancedOpengl";
	private static final String PROPERTY_FBO_ENABLE = "fboEnable";

	private static final String PROPERTY_RENDER_DISTANCE = "renderDistance";
	private static final String PROPERTY_CLOUDS = "clouds";

	private static final String PROPERTY_GUI_SCALE = "guiScale";
	public static final int GUI_SCALE_AUTO = 0;
	public static final int GUI_SCALE_SMALL = 1;
	public static final int GUI_SCALE_NORMAL = 2;
	public static final int GUI_SCALE_LARGE = 3;

	private static final String PROPERTY_FULLSCREEN = "fullscreen";

	public static final int KEY_Z = 44;

	private static final String PROPERTY_KEY_ZOOM = "key_Zoom";
	
	
	public static Options loadFromGamedir(File gamedir) throws IOException {
		File f = new File(gamedir, FILENAME);
		Options result = new Options();
		if (f.exists()) result.loadFile(f);
		else result.setFile(f);
		return result;
	}
	
	public void setAdvancedOpenGL(boolean enabled) {
		setProperty(PROPERTY_ADVANCED_OPENGL, enabled);
	}
	
	public void setFboEnable(boolean enabled) {
		setProperty(PROPERTY_FBO_ENABLE, enabled);
	}
	
	public void setRenderDistance(int chunks) {
		setProperty(PROPERTY_RENDER_DISTANCE, chunks);
	}
	
	public int getRenderDistance() {
		return getIntProperty(PROPERTY_RENDER_DISTANCE, 12);
	}

	public void setClouds(boolean enabled) {
		setProperty(PROPERTY_CLOUDS, enabled);
	}
	
	public void setFullscreen(boolean fullscreen) {
		setProperty(PROPERTY_FULLSCREEN, fullscreen);
	}
	
	public void setGuiScale(int scale) {
		setProperty(PROPERTY_GUI_SCALE, scale);
	}
	
	public void setZoomKey(int keyZ) {
		setProperty(PROPERTY_KEY_ZOOM, keyZ);
	}

	
	
	
	public static void main (String[] args) throws IOException {
		Options options = new Options();
		options.loadFile(new File("/home/homac/tmp/configs/running/options.txt"));
		System.out.println("render distance: " + options.getRenderDistance());
		
		options.setFile(new File("/tmp/options.txt"));
		options.save();
	}

}
