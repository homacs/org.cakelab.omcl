package org.cakelab.omcl.setup.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.FileSystem;


public class Copy extends RunnableTask {


	private String sourceFile;
	private String targetFile;

	public Copy(String userInfo, String sourceFile, String targetFile) {
		super("copying '" + sourceFile + "' to '" + targetFile, userInfo);
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
	}

	@Override
	public void run() {
		try {
			File target = new File(targetFile);
			
			if (!target.getParentFile().exists()) {
				target.getParentFile().mkdirs();
			} else if (target.exists()) FileSystem.delete(target);
			
			File tmp = new File(targetFile + ".part");
			if (tmp.exists()) FileSystem.delete(tmp);
			Files.copy(new File(sourceFile).toPath(), tmp.toPath());
			
			Files.move(tmp.toPath(), target.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
