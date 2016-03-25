package org.cakelab.omcl.setup.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.archive.Zip;


public class PatchArchiveTask extends RunnableTask {

	private String archivePath;
	private String targetArchivePath;
	private String patchPath;

	public PatchArchiveTask(String userInfo, File archive,
			File patch, File targetArchive) {
		super("patching archive " + archive.getPath() + " with patch " + patch.getPath() + " into target archive " + targetArchive.getPath(), userInfo);
		this.archivePath = archive.getAbsolutePath();
		this.patchPath = patch.getAbsolutePath();
		this.targetArchivePath = targetArchive.getAbsolutePath();
	}

	@Override
	public void run() {
		
		File archiveFile = new File(archivePath);
		File patchFile = new File(patchPath);
		File targetArchiveFile = new File(targetArchivePath);
		
		File tmp = new File(System.getProperty("java.io.tmpdir"), targetArchiveFile.getName());
		FileSystem.delete(tmp);
		tmp.mkdirs();
		
		try {
			// extract original archive into tmp
			Zip archive = new Zip(archiveFile);
			archive.extract(tmp, null, false);
			
			// extract patch into tmp (i.e. apply patch)
			Zip patch = new Zip(patchFile);
			patch.extract(tmp, null, true);
			
			// compress tmp into target archive
			Zip target = new Zip(targetArchiveFile);
			target.create(tmp, tmp.listFiles(), null);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return super.getDefaultErrorMessage();
	}


}
