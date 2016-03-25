package org.cakelab.omcl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import org.cakelab.omcl.taskman.TaskMonitor;
import org.cakelab.omcl.utils.Md5Sum;
import org.cakelab.omcl.utils.UrlConnectionUtils;
import org.cakelab.omcl.utils.log.Log;

public class DownloadManager {
	
	private static final int CONNECT_TIMEOUT = 15000;
	private static final int READ_TIMEOUT = 10000;

	public static void download(URL url, File targetLocation, String checksum, TaskMonitor monitor) throws IOException {
		IOException lastException = null;
		for (int i = 0; i < 10; i++) {
			if (i > 0) Log.info("retrying download of " + url.toString());
			
			if (targetLocation.exists()) {
				// TODO: implement download resume
				targetLocation.delete();
			}
			
			URLConnection connection = UrlConnectionUtils.openConnection(url, CONNECT_TIMEOUT, READ_TIMEOUT);
			try {
				connection.connect();
			} catch (SocketTimeoutException e) {
				Log.warn("socket connect timeout");
				continue;
			}
			
			InputStream in = null;
			try {
				in = connection.getInputStream();
				
				monitoredDownload(in, targetLocation, connection.getContentLength(), monitor);
			} catch (SocketTimeoutException e) {
				Log.warn("socket read timeout");
				lastException = e;
				continue;
			} catch (IOException e) {
				throw e;
			} finally {
				if (in != null) in.close();
			}
			
			if (checksum != null && checksum.trim().length()>0 && !Md5Sum.check(targetLocation, checksum)) {
				targetLocation.delete();
				Log.warn("checksum check failed");
				lastException = new IOException("checksum check failed");
				continue;
			} else {
				// we have the data and it is valid.
				lastException = null;
				break;
			}
		}
		
		if (lastException != null) {
			// looks like we finally failed, so propagate the exception
			throw lastException;
		}
	}

	private static void monitoredDownload(InputStream in, File targetLocation,
			int total, TaskMonitor monitor) throws IOException {
		
		FileOutputStream out = new FileOutputStream(targetLocation);
		try {
			byte [] buffer = new byte [1024];
			int len;
			int current = 0;
			while (0 < (len = in.read(buffer))) {
				out.write(buffer, 0, len);
				current += len;
				try {
					monitor.updateProgress(total, current, ((float)current)/total, "downloading");
				} catch (Throwable e) {
					Log.error("reporting progress failed. Monitor muted.");
					monitor = TaskMonitor.NULL_MONITOR;
				}
			}
		} finally {
			out.close();
		}
	}

}
