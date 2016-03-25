package org.cakelab.omcl.setup.tasks;

import java.io.File;

import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.FileSystem;

public class Delete extends RunnableTask {

	private String path;

	public Delete(String userInfo, String path) {
		super("delete " + path, userInfo);
		this.path = path;
	}

	@Override
	public void run() {
		FileSystem.delete(new File(path));
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
