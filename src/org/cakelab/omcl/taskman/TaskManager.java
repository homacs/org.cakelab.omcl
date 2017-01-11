package org.cakelab.omcl.taskman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import org.cakelab.omcl.utils.log.Log;
import org.cakelab.json.JSONArray;
import org.cakelab.json.JSONException;
import org.cakelab.json.JSONObject;
import org.cakelab.json.codec.JSONCodec;
import org.cakelab.json.codec.JSONCodecConfiguration;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.json.codec.Parser;


public class TaskManager {
	private static JSONCodecConfiguration jsonConfig = new JSONCodecConfiguration(Charset.defaultCharset(), true, true);

	/* TODO: turn this into a generic persistent task manager 
	 * - guarantee atomicity of scheduling and finishing of tasks
	 * - move to a separate project 
	 * - support parallelism
	 * - support nested tasks
	 */

	
	public static TaskManager INSTANCE;
	transient ArrayList<Task> db = new ArrayList<Task>();
	
	// TODO: tasks.db: use a single file per task instead of all tasks in one.
	private transient File dbfile;
	private transient boolean errors;
	private transient TaskMonitor monitor;
	
	class TaskDB {
		Object[] log;
	}

	
	
	public TaskManager(File dbfile) {
		this.dbfile = dbfile;
		TaskManager.INSTANCE = this;
		errors = false;
	}

	public void loadDB() throws IOException, JSONException, JSONCodecException, ClassNotFoundException {
		try {
			InputStream in = new FileInputStream(dbfile);
			Parser jsonParser = new Parser(in);
			JSONObject logdb = jsonParser.parse();
			in.close();
	
			JSONArray entries = (JSONArray) logdb.get("log");
	
			JSONCodec codec = new JSONCodec(jsonConfig);
	
			
			ClassLoader cl = TaskManager.class.getClassLoader();
			
			for (Object entry : entries) {
				// since our json codec does not support inheritance we have to do it manually
				JSONObject jsonTask = (JSONObject) entry;
				String className = jsonTask.getString("fqnClassName");
				JSONObject jsonRunnable = (JSONObject) jsonTask.get("runnable");
				@SuppressWarnings("unchecked")
				Class<? extends RunnableTask> clazz = (Class<? extends RunnableTask>) cl.loadClass(className);
				RunnableTask runnable = (RunnableTask) codec.decodeObject(jsonRunnable, clazz);
				
				Task task = new Task(runnable);
				db.add(task);
			}
		} catch (FileNotFoundException e) {
			updateDB();
		}
	}

	public void updateDB() throws JSONCodecException, IOException {
		JSONCodec codec = new JSONCodec(jsonConfig);

		FileOutputStream out = new FileOutputStream(dbfile);
		
		TaskDB jsonConform = new TaskDB();
		jsonConform.log = db.toArray();
		
		codec.encodeObject(jsonConform, out);
		out.close();
	}

	public void resetDB() throws IOException, JSONCodecException {
		Files.deleteIfExists(dbfile.toPath());
		db.clear();
		updateDB();
	}

	public boolean runToCompletion(boolean stopOnError, TaskMonitor _monitor) throws JSONCodecException, IOException {
		setupMonitor(_monitor);
		errors = false;
		while (hasPendingTasks()) {
			Task task = db.get(0);
			String info = task.getUserInfo();
			try {
				this.monitor.startExecution(task.runnable);
			} catch (Throwable e) {
				Log.error("reporting start to monitor failed. Muted.", e);
				this.monitor = TaskMonitor.NULL_MONITOR;
			}
			Log.info("start " + info);
			task.run(this.monitor);
			if (errors && stopOnError) break;
		}
		return !errors;
	}

	private void setupMonitor(TaskMonitor monitor) {
		if (monitor != null) this.monitor = monitor;
		else this.monitor = TaskMonitor.NULL_MONITOR;
	}

	public void add(Task task) {
		db.add(task);
		try {
			updateDB();
		} catch (JSONCodecException | IOException e) {
			Log.warn("failed to save task list", e);
		}
	}

	public void addSingleTask(RunnableTask t) {
		add(new Task(t));
	}
	
	
	public void finished(RunnableTask task) {
		try {
			monitor.finishedExecution(task);
		} catch (Throwable e) {
			Log.error("reporting progress to monitor failed. Muted.", e);
			this.monitor = TaskMonitor.NULL_MONITOR;
		}
		Log.info("finished " + task.getLogInfo());
		assert(db.get(0).runnable.equals(task));
		db.remove(0);
		try {
			updateDB();
		} catch (JSONCodecException | IOException e) {
			errors = true;
			throw new RuntimeException("Updating task log failed", e);
		}
	}

	public void failed(RunnableTask runnableTask, Throwable e) {
		try {
			try {
				while (e.getCause() != null && e.getCause() != e) e = e.getCause();
				monitor.failedExecution(runnableTask, e);
			} catch (Throwable t) {
				Log.error("reporting error to monitor failed. Monitor muted.", t);
				this.monitor = TaskMonitor.NULL_MONITOR;
			}
			// resume failed, so reset the log and start over.
			Log.warn(runnableTask.getClass().getCanonicalName() + " failed.", e);
			errors = true;
			resetDB();
		} catch (IOException | JSONCodecException e1) {
			Log.error("Task manager failed to recover from error in task " + runnableTask.getClass().getCanonicalName(), e1);
		}
	}

	public void canceled(RunnableTask runnableTask) {
		try {
			try {
				monitor.cancelledExecution(runnableTask);
			} catch (Throwable t) {
				Log.error("reporting canceled task failed. Monitor muted.", t);
				this.monitor = TaskMonitor.NULL_MONITOR;
			}
			// user cancelled our task .. so delete remaining tasks and hope we are in a consistent state
			Log.info(runnableTask.getClass().getCanonicalName() + " canceled.");
			errors = true;
			resetDB();
		} catch (IOException | JSONCodecException e1) {
			Log.error("Task manager failed to cancel task " + runnableTask.getClass().getCanonicalName(), e1);
		}
	}
	public boolean hasPendingTasks() {
		return !db.isEmpty();
	}


	
	
}
