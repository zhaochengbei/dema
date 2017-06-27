package io.dema.tcp;
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
	public ArrayList<TcpConnection> connections = new ArrayList<TcpConnection>();
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
					synchronized (connections) {
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
								addTask(TcpConnectionManagerTaskType.READ, connection);
							}
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(checkStatusGapMillSeconds);
				} catch (InterruptedException e) {
					break;
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
						if(task.connection.inOperating == true){
							processNextLoop.add(task);
						}else{
							task.connection.inOperating = true;
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
	private Thread checkSocketStatusThread;
	/**
	 * 
	 */
	private Thread distributionTaskThread;
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
		checkSocketStatusThread = new Thread(checkSocketStatusLogic,"CheckSocketStatus_0");
		distributionTaskThread = new Thread(distributionTaskLogic,"DistributionTask_0");
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
			if(checkSocketStatusThread.isAlive() != false||distributionTaskThread.isAlive() != false){
				break;
			}
		}
		while(true){
			//keep started task normal complete
			if(((ThreadPoolExecutor)exeTaskThreads).getActiveCount()==0){
				exeTaskThreads.shutdown();
				break;
			}
		}
		synchronized (connections) {
			for (int i = 0; i < connections.size(); i++) {
				connections.get(i).close(TcpConnectionCloseReason.ShutDownTcpConnectionManager);
			}
			connections.clear();
		}
		//clear connection and task;
		tasks.clear();
	}
	/**
	 * 
	 */
	public void add(TcpConnection connection){
		addTask(TcpConnectionManagerTaskType.ACCEPT, connection);
		synchronized (connections) {
			connections.add(connection);	
		}
	}
	/**
	 * 
	 */
	private void remove(TcpConnection connection){
		synchronized (connections) {
			connections.remove(connection);	
		}
		addTask(TcpConnectionManagerTaskType.CLOSE, connection);
	}
	/**
	 * 
	 */
	private void addTask(int type,TcpConnection connection){
		TcpConnectionManagerTask tcpTask = new TcpConnectionManagerTask(type, connection, ioHandler);
		tasks.add(tcpTask);
	}
}
