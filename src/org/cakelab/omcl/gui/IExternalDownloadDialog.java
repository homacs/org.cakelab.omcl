package org.cakelab.omcl.gui;

import java.io.File;
import java.net.URL;

public interface IExternalDownloadDialog {
	
	void init(String packageName, String filename, URL _url, String checksum);
	void setVisible(boolean b);
	boolean getResult();
	File getFile();
}
