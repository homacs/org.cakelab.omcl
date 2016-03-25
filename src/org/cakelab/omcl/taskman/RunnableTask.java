package org.cakelab.omcl.taskman;



public abstract class RunnableTask implements Runnable {

	boolean isFinished = false;
	private String logInfo;
	private String userInfo;
	protected transient Throwable exception;
	protected transient TaskMonitor monitor;
	
	public RunnableTask(String logInfo, String userInfo) {
		this.logInfo = logInfo;
		this.userInfo = userInfo;
		exception = null;
	}
	
	
	protected final void finished() {
		if (!isFinished) {
			isFinished = true;
			TaskManager.INSTANCE.finished(this);
		}
	}
	
	final void start(TaskMonitor monitor) {
		try {
			this.monitor = monitor;
			run();
			finished();
		} catch (Throwable e) {
			isFinished = false;
			if (e.getCause() instanceof TaskCanceledException) {
				TaskManager.INSTANCE.canceled(this);
			} else {
				exception = e;
				TaskManager.INSTANCE.failed(this, e);
				throw e;
			}
		}
	}


	public String getLogInfo() {
		return logInfo;
	}


	public String getUserInfo() {
		return userInfo;
	}

	public String getDefaultErrorMessage() {
		String msg = this.logInfo + " failed.";
		if (exception != null) {
			msg += "\n\nReason: " + exception.getMessage();
			Throwable rootCause = exception;
			while (rootCause.getCause() != null && rootCause != rootCause.getCause()) {
				rootCause = rootCause.getCause();
			}
			if (rootCause != exception) {
				msg += "\nRoot cause: " + rootCause.getMessage();
			}
			
		}
		return msg;
	}
	public abstract String getDetailedErrorMessage();
	
}
