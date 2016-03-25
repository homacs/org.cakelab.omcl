package org.cakelab.omcl.setup.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import org.cakelab.omcl.gui.GUI;
import org.cakelab.omcl.gui.IExternalDownloadDialog;
import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.taskman.TaskCanceledException;


public class DownloadSoftwarePackageExternal extends RunnableTask {

	private String target;
	private String url;
	private String checksum;
	private String packageName;
	private String filename;
	
	
	

	public DownloadSoftwarePackageExternal(URL url, String packageName, File targetLocation, String filename, String checksum, String userInfo) {
		super("download from '" + url.toString() + "' to '" + targetLocation.getPath() + "'", userInfo);
		this.packageName = packageName;
		this.filename = filename;
		this.checksum = checksum;
		this.url = url.toString();
		this.target = targetLocation.getAbsolutePath();
	}

	public DownloadSoftwarePackageExternal(URL url, String packageName, File targetLocation, String filename, String checksum) {
		this(url, packageName, targetLocation, filename, checksum, "downloading");
	}


	@Override
	public void run() {
		try {
			URL _url = new URL(url);
			IExternalDownloadDialog dialog = GUI.getInstance().getExternalDownloadDialog();

			dialog.init(packageName, filename, _url, checksum);
			
			dialog.setVisible(true);
			GUI.getInstance().toFront();
			if (dialog.getResult()) {
				File file = new File(target);
				if (!file.getParentFile().exists()) {
					if (!file.getParentFile().mkdirs()) throw new IOException("Failed to create directory");
				}
				if (file.exists()) file.delete();
				Files.copy(dialog.getFile().toPath(), file.toPath());
			} else {
				throw new TaskCanceledException();
			}
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
