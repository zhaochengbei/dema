package org.bei.dema.tcp;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.CancelledKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * author：zhaochengbei
 * date：2017/5/31
*/
public class TcpConnectionManager {

	/**
	 * 
	 */

	public int checkReadThreadCount = 1;
	public int exeIoTaskThreadCount = Runtime.getRuntime().availableProcessors()*5;
	/**
	 * 
	 */
	public int readCheckGapMillSeconds = 1;
	public int closeCheckGapMillSeconds = 100;
	/**
	 * 
	 */
	public Vector<TcpConnection> connections = new Vector<TcpConnection>();
	/**
	 * only for checkReadable thread,use different thread ,thread will parallel
	 */
	public Vector<Vector<TcpConnection>> connectionGroups = new Vector<Vector<TcpConnection>>();
	/**
	 * 
	 */
	private LinkedBlockingQueue<TcpConnectionManagerTask> tasks = new LinkedBlockingQueue<TcpConnectionManagerTask>();
	/**
	 * 
	 */
	private ScheduledExecutorService checkSocketCloseThreads = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		
		public Thread newThread(Runnable r) {
			return new Thread(r,"CheckSocketClose_0");
		}
	});
	/**
	 * 
	 */
	private ExecutorService checkSocketReadbleThreads;
	private ThreadFactory checkSocketReadableThreadFactory = new ThreadFactory() {
		private int index = 0;
		public Thread newThread(Runnable r) {
			return new Thread(r,"CheckSocketReadble_"+(index++));
		}
	};
	
	
	/**
	 * 
	 */
	private ScheduledExecutorService distributionTaskThreads= Executors.newScheduledThreadPool(1,new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return new Thread(r, "DistributionTask_0");
		}
	});
	/**
	 * 
	 */
	private ExecutorService exeTaskThreads;
	private ThreadFactory exeTaskThreadFactory = new ThreadFactory() {
		public int threadIndex = 0;
		public Thread newThread(Runnable r) {
			return new Thread(r, "ExeIoTask_" + threadIndex++);
		}
	};
	/**
	 * 
	 */
	private Runnable checkSocketCloseLogic = new Runnable(){
		
		public void run() {
			while(true){
				try {
					for (int i = 0; i < connections.size(); i++) {
						TcpConnection connection = connections.get(i);
						//check connection is  not close
						if(connection.isClose() == true){
							remove(connection);
							i--;
							TcpConnectionManagerTask tcpTask = new TcpConnectionManagerTask(TcpConnectionManagerTaskType.CLOSE, connection, ioHandler);
							tasks.add(tcpTask);
							continue;
						}
					}
				}catch (ArrayIndexOutOfBoundsException e ) {
					//can not reach here
//					e.printStackTrace();
				}catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(closeCheckGapMillSeconds);
				} catch (InterruptedException e) {
					break;
//					e.printStackTrace();
				}
				
			}
			
		}
	};

	/**
	 * 
	 */
	private class CheckSocketReadbleLogic implements Runnable {
		/**
		 * 
		 */
		private Vector<TcpConnection> connectionGroup;
		/**
		 * 
		 */
		public void run() {
			while(true){
				try {
					for (int i = 0; i < connectionGroup.size(); i++) {
						TcpConnection connection = connectionGroup.get(i);
						//check connection is not can be read
						/**
						 * thread safe explain:
						 * after a thread nodify,another thread don't know in immediately,there has a time gap between happy and to know; 
						 */
						if(connection.inReading == false&&connection.available()>0){
							connection.inReading = true;
							TcpConnectionManagerTask tcpTask = new TcpConnectionManagerTask(TcpConnectionManagerTaskType.READ, connection, ioHandler);
					    	tasks.add(tcpTask);
						}
					}
				
				}catch (ArrayIndexOutOfBoundsException e) {
//					e.printStackTrace();
					//do nothing
				}catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(readCheckGapMillSeconds);
				} catch (InterruptedException e) {
					break;
//					e.printStackTrace();
				}
			}
		}
	};
	private Vector<CheckSocketReadbleLogic> checkSocketReadbleLogics = new Vector<CheckSocketReadbleLogic>();
	/**
	 * 
	 */
	private Runnable distributionTaskLogic = new Runnable() {
		
		public void run() {
			while(true){
				try {
					while(tasks.size()>0){
						//only here take task ,so never block;
						TcpConnectionManagerTask task = tasks.take();
						exeTaskThreads.execute(task);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
//					e.printStackTrace();

				}
			}
			
		}
	};
	/**
	 * 
	 */
	private IoHandler ioHandler; 
	/**
	 * 
	 */
	public TcpConnectionManager() {
		
	}

	/**
	 * 
	 * @param ioHandler
	 * @throws Exception
	 */
	protected void start(IoHandler ioHandler) {
		this.ioHandler = ioHandler;
		// init container
		for (int i = 0; i < checkReadThreadCount; i++) {
			Vector<TcpConnection> connectionGroup = new Vector<TcpConnection>();
			connectionGroups.add(connectionGroup);
			CheckSocketReadbleLogic checkSocketReadbleLogic = new CheckSocketReadbleLogic();
			checkSocketReadbleLogic.connectionGroup = connectionGroup;
			checkSocketReadbleLogics.add(checkSocketReadbleLogic);
		}
		//init thread
		checkSocketReadbleThreads = Executors.newScheduledThreadPool(checkReadThreadCount, checkSocketReadableThreadFactory);
		exeTaskThreads = Executors.newFixedThreadPool(exeIoTaskThreadCount, exeTaskThreadFactory);
		
		//start thread
		checkSocketCloseThreads.execute(checkSocketCloseLogic);
		for (int i = 0; i < checkReadThreadCount; i++) {
			checkSocketReadbleThreads.execute(checkSocketReadbleLogics.get(i));
		}
		distributionTaskThreads.execute(distributionTaskLogic);
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void shutdown(){
		//stop all thread
		checkSocketCloseThreads.shutdown();
		checkSocketReadbleThreads.shutdown();
		distributionTaskThreads.shutdown();
		while(true){
			//keep started task normal complete
			if(((ThreadPoolExecutor)exeTaskThreads).getActiveCount()==0){
				exeTaskThreads.shutdown();
				break;
			}
		}
		//clear connection and task;
		connections.clear();
		connectionGroups.clear();
		checkSocketReadbleLogics.clear();
		tasks.clear();
	}
	/**
	 * 
	 */
	public void add(TcpConnection connection){
		connections.add(connection);
		Vector<TcpConnection> connectionGroup = null;
		for (int i = 0; i < connectionGroups.size(); i++) {
			if(connectionGroup == null||connectionGroups.get(i).size()<connectionGroup.size()){
				connectionGroup = connectionGroups.get(i);
			}
		}
		connectionGroup.add(connection);
		TcpConnectionManagerTask task = new TcpConnectionManagerTask(TcpConnectionManagerTaskType.ACCEPT, connection, ioHandler);
		tasks.add(task);
	}
	/**
	 * 
	 */
	private void remove(TcpConnection connection){
		connections.remove(connection);
//		Vector<TcpConnection> connectionGroup = null;
		for (int i = 0; i < connectionGroups.size(); i++) {
			if(connectionGroups.get(i).contains(connection)){
				connectionGroups.get(i).remove(connection);
				break;
			}
		}
	}
}
