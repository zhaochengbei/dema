package org.bei.dema.tcp;

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
 * author：bei
 * date：2017/5/31
*/
public class TcpConnectionManager {

	/**
	 * 
	 */
	public Vector<TcpConnection> connections = new Vector<TcpConnection>();
	/**
	 * only for checkReadable thread,use different thread will 
	 */
//	private Vector<TcpConnection> connectionsForRead = new Vector<TcpConnection>();
	public Vector<Vector<TcpConnection>> connectionGroups = new Vector<Vector<TcpConnection>>();
	/**
	 * 
	 */
	private LinkedBlockingQueue<TcpConncetionManagerTask> tasks = new LinkedBlockingQueue<TcpConncetionManagerTask>();
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
	private ScheduledExecutorService checkSocketReadbleThreads = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
		private int index = 0;
		public Thread newThread(Runnable r) {
			return new Thread(r,"CheckSocketReadble_"+(index++));
		}
	});
	
	
	/**
	 * 
	 */
	private ScheduledExecutorService distributionTaskThreads = Executors.newScheduledThreadPool(1,new ThreadFactory() {
		
		public Thread newThread(Runnable r) {
			return new Thread(r, "DistributionTask_0");
		}
	});
	/**
	 * 
	 */
	private ExecutorService exeTaskThreads = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*5,new ThreadFactory() {
		public int threadIndex = 0;
		public Thread newThread(Runnable r) {
			return new Thread(r, "ExeIoTask_" + threadIndex++);
		}
	});
	/**
	 * 
	 */
	private TimerTask checkSocketCloseLogic = new TimerTask() {
		
		public void run() {
			try {
				for (int i = 0; i < connections.size(); i++) {
					TcpConnection connection = connections.get(i);
					//check connection is  not close
					if(connection.isClose() == true){
//						connections.remove(i);
						remove(connection);
						i--;
						TcpConncetionManagerTask tcpTask = new TcpConncetionManagerTask(TcpConnectionManagerTaskType.CLOSE, connection, ioHandler);
						tasks.add(tcpTask);
						continue;
					}
				}
			}catch (Exception e) {
				//can not reach here
				e.printStackTrace();
			}
		}
	};

	/**
	 * 
	 */
	private class CheckSocketReadbleLogic extends TimerTask {
		/**
		 * 
		 */
		private Vector<TcpConnection> connectionGroup = new Vector<TcpConnection>();
		/**
		 * 
		 */
		public void run() {
			try {
				for (int i = 0; i < connectionGroup.size(); i++) {
					TcpConnection connection = connectionGroup.get(i);
					//check connection is not can be read
					/**
					 * 线程安全说明：
					 * 线程修改了堆之后，另外一个线程并不会立即知道，中间有个时间差，通过标志位控制访问的两个线程是不会同时操作connection的；
					 */
					if(connection.inReading == false&&connection.available()>0){
						connection.inReading = true;
						TcpConncetionManagerTask tcpTask = new TcpConncetionManagerTask(TcpConnectionManagerTaskType.READ, connection, ioHandler);
				    	tasks.add(tcpTask);
					}
				}
			}catch (Exception e) {
				//do nothing
//				e.printStackTrace();
			}
		}
	};
	private Vector<CheckSocketReadbleLogic> checkSocketReadbleLogics = new Vector<CheckSocketReadbleLogic>(); 
//	/**
//	 * max heard gap
//	 */
//	public int maxReadIdleTime = 0;
//	/**
//	 * 
//	 */
//	private TimerTask checkReadIdleTimeOutLogic = new TimerTask() {
//		
//		public void run() {
//			try {
//				long time = System.currentTimeMillis();
//				for (int i = 0; i < connections.size(); i++) {
//					TcpConnection connection = connections.get(i);
//					if(maxReadIdleTime !=0 &&connection.isClose() == false&&time - connection.lastReadTime> maxReadIdleTime){
//						//use part will receive a close event;
//						connection.close(TcpConnectionCloseReason.ReadIdleTimeOut);
//					}
//				}
//			}catch (Exception e) {
//				//can not reach here
//				e.printStackTrace();
//			}
//			
//		}
//	};
	/**
	 * 
	 */
	private TimerTask distributionTaskLogic = new TimerTask() {
		
		public void run() {
			try {
				while(tasks.size()>0){
					//only here take task ,so never block;
					TcpConncetionManagerTask task = tasks.take();
					exeTaskThreads.execute(task);
				}
			} catch (Exception e) {
				e.printStackTrace();
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
	protected void start(IoHandler ioHandler) throws Exception{
		this.ioHandler = ioHandler;
		// init container
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			Vector<TcpConnection> connectionGroup = new Vector<TcpConnection>();
			connectionGroups.add(connectionGroup);
			CheckSocketReadbleLogic checkSocketReadbleLogic = new CheckSocketReadbleLogic();
			checkSocketReadbleLogic.connectionGroup = connectionGroup;
			checkSocketReadbleLogics.add(checkSocketReadbleLogic);
		}
		//start thread
		checkSocketCloseThreads.scheduleAtFixedRate(checkSocketCloseLogic,1, 1, TimeUnit.MILLISECONDS);
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			checkSocketReadbleThreads.scheduleAtFixedRate(checkSocketReadbleLogics.get(i),1, 1, TimeUnit.MILLISECONDS);
		}
		distributionTaskThreads.scheduleAtFixedRate(distributionTaskLogic, 1, 1, TimeUnit.MILLISECONDS);
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception{
//		for (Iterator<TcpConnection> iterator = connections.iterator(); iterator.hasNext();){ 
//			TcpConnection socket = iterator.next();
//			socket.close(TcpConnectionCloseReason.ShutDownTcpServer);
//		}
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
		TcpConncetionManagerTask task = new TcpConncetionManagerTask(TcpConnectionManagerTaskType.ACCEPT, connection, ioHandler);
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
