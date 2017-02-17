package org.cakelab.omcl.update;

import java.net.URL;
import java.util.ArrayList;

import org.cakelab.omcl.utils.log.Log;

@SuppressWarnings("serial")
public class UpdateServerPool extends ArrayList<UpdateServer>{

	public static UpdateServer OFFLINE = new UpdateServer();
	
	
	public class ConnectThread extends Thread {

		private UpdateServer updateServer;
		private int serverId;
		private TransactionAdvisor txadvisor;
		public ConnectThread(int serverId, TransactionAdvisor txadvisor) {
			super("connect (" + serverId + ")");
			this.serverId = serverId;
			this.txadvisor = txadvisor;
		}
		public void run() {
			try {
				updateServer = new UpdateServer(new URL(UpdateServerPool.this.pool[serverId]), txadvisor);
			} catch (Throwable t) {
				Log.warn("can't connect to update server.", t);
				updateServer = OFFLINE;
			}
		}
		
		public UpdateServer getResult() throws InterruptedException {
			join();
			return updateServer;
		}

	}


	private String[] pool;
	
	public UpdateServerPool(String primary, String[] serverPool) {
		super(serverPool != null ? serverPool.length+1 : 1);
		this.pool = new String[serverPool.length+1];
		
		int j = ((int)System.currentTimeMillis()) & 0xfffffff;
		for (int i = 0; i < this.pool.length; i++) {
			j = j % this.pool.length;
			if (j == 0) {
				pool[i] = primary;
			} else {
				pool[i] = serverPool[j-1];
			}
			j++;
		}
	}


	public UpdateServer connect(int minRevision, TransactionAdvisor txadvisor) {
		
		ConnectThread[] connectThreads = new ConnectThread[pool.length];
		
		for (int i = 0; i < pool.length; i++) {
			connectThreads[i] = new ConnectThread(i, txadvisor);
			connectThreads[i].start();
		}
		
		for (ConnectThread t : connectThreads) {
			UpdateServer connection = null;
			try {
				connection = t.getResult();
				if (!connection.isOffline() && connection.getMinRevision() >= minRevision) {
					add(connection);
				}
			} catch (InterruptedException e) {
				// ignore, probably shutting down
			};
		}
		
		return this.size() > 0 ? this.get(0) : null;
	}

}
