package org.bei.dema.tcp;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * author：zhaochengbei
 * date：2017/5/31
*/
public class TcpConnectionManager {

	/**
	 * read and close use different thread, and read use muli thread we all have teast,the effect is not ideal,so we reduction part of code
	 * use java nio api we has teast ,the api have lots of bug,and when connection count large,the effect not than bio api ideal
	 */
	
	/**
	 * 
	 */

	public int exeIoTaskThreadCount = Runtime.getRuntime().availableProcessors()*5;
	/**
	 * 
	 */
	public int checkStatusGapMillSeconds = 1;
	/**
	 * 
	 */
	public Vector<TcpConnection> connections = new Vector<TcpConnection>();
	/**
	 * 
	 */
	private LinkedBlockingQueue<TcpConnectionManagerTask> tasks = new LinkedBlockingQueue<TcpConnectionManagerTask>();
	/**
	 * 
	 */
	private Runnable checkSocketStatusLogic  = new Runnable() {
		/**
		 * 
		 */
		public void run() {
			while(true){
				try {
					for (int i = 0; i < connections.size(); i++) {
						TcpConnection connection = connections.get(i);
						//check connection is not close
						if(connection.isClose() == true){
							remove(connection);
							i--;
							continue;
						}
						//check connection is not can be read
						/**
						 * in same time,use part only receive one read msg, we use inReading Property control;
						 * 
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
					Thread.sleep(checkStatusGapMillSeconds);
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
	private Runnable distributionTaskLogic = new Runnable() {
		
		public void run() {
			while(true){
				try {
					ArrayList<TcpConnectionManagerTask> processNextLoop = new ArrayList<TcpConnectionManagerTask>();
					while(tasks.size()>0){
						//only here take task ,so never block;
						TcpConnectionManagerTask task = tasks.take();
						if(task.type == TcpConnectionManagerTaskType.CLOSE&&task.connection.inReading == true){
							processNextLoop.add(task);
						}else{
							exeTaskThreads.execute(task);
						}
					}
					while(processNextLoop.size()>0){
						tasks.put(processNextLoop.remove(0));
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
	private Thread checkSocketStatusThread = new Thread(checkSocketStatusLogic,"CheckSocketStatus_0");
	/**
	 * 
	 */
	private Thread distributionTaskThread = new Thread(distributionTaskLogic,"DistributionTask_0");
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
	private IoHandler ioHandler; 
	/**
	 * 
	 */
	public TcpConnectionManager() {
		
	}

	/**
	 * 
	 * @param ioHandler
	 */
	protected void start(IoHandler ioHandler) {
		this.ioHandler = ioHandler;
		//init thread
		exeTaskThreads = Executors.newFixedThreadPool(exeIoTaskThreadCount, exeTaskThreadFactory);
		
		//start thread
		checkSocketStatusThread.start();
		distributionTaskThread.start();
	}
	
	
	/**
	 * 
	 */
	public void shutdown(){
		//stop all thread
		checkSocketStatusThread.interrupt();
		distributionTaskThread.interrupt();
		while(true){
			//keep started task normal complete
			if(((ThreadPoolExecutor)exeTaskThreads).getActiveCount()==0){
				exeTaskThreads.shutdown();
				break;
			}
		}
		//clear connection and task;
		connections.clear();
		tasks.clear();
	}
	/**
	 * 
	 */
	public void add(TcpConnection connection){
		connections.add(connection);
		TcpConnectionManagerTask task = new TcpConnectionManagerTask(TcpConnectionManagerTaskType.ACCEPT, connection, ioHandler);
		tasks.add(task);
	}
	/**
	 * 
	 */
	private void remove(TcpConnection connection){
		connections.remove(connection);
		TcpConnectionManagerTask tcpTask = new TcpConnectionManagerTask(TcpConnectionManagerTaskType.CLOSE, connection, ioHandler);
		tasks.add(tcpTask);
	}
}
