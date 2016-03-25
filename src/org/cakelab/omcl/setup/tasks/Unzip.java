package org.cakelab.omcl.setup.tasks;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.archive.Zip;


public class Unzip extends RunnableTask {

	private String zipfile;
	private String targetFolder;

	public Unzip(String userInfo, String zipfile, String targetFolder) {
		super("unzipping '" + zipfile + "' to '" + targetFolder, userInfo);
		this.zipfile = zipfile;
		this.targetFolder = targetFolder;
	}

	@Override
	public void run() {
		Zip archive = new Zip(new File(zipfile));
		try {
			archive.extract(new File(targetFolder), new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return true;
				}
				
			}, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
