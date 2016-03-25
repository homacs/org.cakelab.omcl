package org.cakelab.omcl.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.log.Log;

public class ExternalBrowser {

	public static void open(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				Log.warn("Failed to open '" + uri.toString() + "' in system web browser", e);
			}
		} else {
			Log.warn("No system web browser available to open '"+ uri.toString() + "'.");
		}
	}

	public static void open(URL url) {
		try {
			open(url.toURI());
		} catch (URISyntaxException e) {
			Log.warn("Failed to open '" + url + "'");
		}
	}

	public static void open(String url) {
		try {
			open(new URI(url));
		} catch (URISyntaxException e) {
			Log.warn("Failed to open '" + url + "'");
		}
	}

	public static File getDefaultDownloadFolder() {
		File folder;
		if (OS.isUnixDerivate()) {
			// Linux and solaris
			// firefox and chromium-browser
			folder = new File(FileSystem.getUserHome(), "Download");
			if (!folder.exists()) {
				folder = new File(FileSystem.getUserHome(), "Downloads");
			}
		} else {
			// Windows
			// firefox and iexplore
			folder = new File(FileSystem.getUserHome(), "Downloads");
			if (!folder.exists()) {
				// chrome
				folder = new File(FileSystem.getUserHome(), "Documents" + File.separator + "Downloads");
			}
		}
		if (!folder.exists()) {
			// at least it should be in the home directory ..
			folder = FileSystem.getUserHome();
		}
		return folder;
	}
}
