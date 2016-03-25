package org.cakelab.omcl.setup.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.cakelab.omcl.DownloadManager;
import org.cakelab.omcl.taskman.RunnableTask;

public class DownloadSoftwarePackage extends RunnableTask {

	
	private String targetDirectory;
	private String url;
	private String checksum;

	public DownloadSoftwarePackage(URL url, File targetLocation, String checksum, String userInfo) {
		super("download from '" + url.toString() + "' to '" + targetLocation.getPath() + "'", userInfo);
		this.checksum = checksum;
		this.url = url.toString();
		this.targetDirectory = targetLocation.getAbsolutePath();
	}

	public DownloadSoftwarePackage(URL url, File targetLocation, String checksum) {
		this(url, targetLocation, checksum, "downloading");
	}


	@Override
	public void run() {
		try {
			URL _url = new URL(url);
			File file = new File(targetDirectory);
			if (!file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) throw new IOException("Failed to create directory");
			}
			DownloadManager.download(_url, file, checksum, monitor);
			
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}
}
