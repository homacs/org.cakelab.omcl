package org.cakelab.omcl.taskman;

public interface TaskMonitor {

	void startExecution(RunnableTask runnable);

	void finishedExecution(RunnableTask task);

	void failedExecution(RunnableTask runnableTask, Throwable e);

	void cancelledExecution(RunnableTask runnableTask);

	void updateProgress(long total, long current, float f, String string);

	
	
	static final TaskMonitor NULL_MONITOR = new TaskMonitor() {
		@Override
		public void startExecution(RunnableTask runnable) {
		}
		@Override
		public void finishedExecution(RunnableTask task) {
		}
		@Override
		public void failedExecution(RunnableTask runnableTask, Throwable e) {
		}
		@Override
		public void updateProgress(long total, long current, float f,
				String string) {
		}
		@Override
		public void cancelledExecution(RunnableTask runnableTask) {
		}
		
	};



	
}
