package org.cakelab.omcl.taskman;



public class Task {

	String fqnClassName;
	RunnableTask runnable;
	
	public Task (RunnableTask runnable) {
		this.fqnClassName = runnable.getClass().getCanonicalName();
		this.runnable = runnable;
	}

	public void run (TaskMonitor monitor) {
		runnable.start(monitor);
	}

	public String getUserInfo() {
		return runnable.getLogInfo();
	}
}
