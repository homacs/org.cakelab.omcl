package org.cakelab.omcl.gui;



public abstract class GUI {

	private static GUI SINGLETON;

	public static synchronized 
	GUI getInstance() {
		return SINGLETON;
	}

	public static void setInstance(GUI gui) {
		SINGLETON = gui;
	}
	
	
	public abstract void showError(String message, String reason);
	
	public abstract void showWarning(String message, String reason);

	public abstract void showInfo(String message, String reason);

	public abstract IExternalDownloadDialog getExternalDownloadDialog();

	public abstract void toFront();

	
}
