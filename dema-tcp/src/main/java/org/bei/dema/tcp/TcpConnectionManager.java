package org.bei.dema.tcp;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
	public int exeIoTaskThreadCount = Runtime.getRuntime().availableProcessors()*5;
	/**
	 * 
	 */
	public int closeCheckGapMillSeconds = 100;
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
	private IoHandler ioHandler; 
	
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
	public void removeClosedConnections(){

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
//			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
//		try {
//			Thread.sleep(closeCheckGapMillSeconds);
//		} catch (InterruptedException e) {
////			break;
////			e.printStackTrace();
//		}
	}

	/**
	 * 
	 */
	private Runnable distributionTaskLogic = new Runnable() {
		
		public void run() {
			while(true){
				try {
					ArrayList<TcpConnectionManagerTask> needWaitTasks = new ArrayList<TcpConnectionManagerTask>();
					while(tasks.size()>0){
						//only here take task ,so never block;
						TcpConnectionManagerTask task = tasks.take();
						if(task.connection.inOprating == true){
							needWaitTasks.add(task);
						}else{
							task.connection.inOprating = true;
							exeTaskThreads.execute(task);
						}
					}
					tasks.addAll(needWaitTasks);
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
	private Thread checkSocketCloseThread = new Thread(checkSocketCloseLogic,"CheckChannelClose_0");
	
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
	public TcpConnectionManager() {
		
	}

	/**
	 * 
	 * @param ioHandler
	 * @throws Exception
	 */
	protected void start(IoHandler ioHandler) throws IOException{
		this.ioHandler = ioHandler;
		
		//init thread pool;
		exeTaskThreads = Executors.newFixedThreadPool(exeIoTaskThreadCount, exeTaskThreadFactory);
		
		//start thread
//		checkSocketCloseThread.start();
		
		distributionTaskThread.start();
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws IOException{
		//stop all thread
//		checkSocketCloseThread.interrupt();
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
	public void add(TcpConnection connection,Selector selector) throws IOException{
		connection.channel.configureBlocking(false);
		connection.selectionKey = connection.channel.register(selector, SelectionKey.OP_READ);
		connection.selectionKey.attach(connection);
		
		connections.add(connection);
		TcpConnectionManagerTask task = new TcpConnectionManagerTask(TcpConnectionManagerTaskType.ACCEPT, connection, ioHandler);
		tasks.add(task);
	}
	/**
	 * 
	 */
	private void remove(TcpConnection connection){
		connection.selectionKey.cancel();
		connections.remove(connection);
	}
	public void read(SelectionKey key){  

//        while (keyIter.hasNext()) { 
//        	SelectionKey key = keyIter.next();
    		TcpConnection connection = (TcpConnection)key.attachment();
//        	try {
        		if(key.isReadable()){
//                    if (connection.inOprating == false) {// 判断是否有数据发送过来
//    					connection.inOprating = true;
//                    	connection.byteBuffer.clear();
//                    	long byteRead = connection.channel.read(connection.byteBuffer);
//                    	connection.byteBuffer.flip();
    				long byteRead = connection.readToBuffer();
//    				System.out.println("receive a data");
                	if(byteRead == -1){
                		connection.close(TcpConnectionCloseReason.ReadError);
                	}else if(byteRead == 0){
//                		connection.close(TcpConnectionCloseReason.ReadError);
//                		continue;
                	}else{
						TcpConnectionManagerTask tcpTask = new TcpConnectionManagerTask(TcpConnectionManagerTaskType.READ, connection, ioHandler);
				    	tasks.add(tcpTask);
                	}
//                    }
                 
    			}	
//			} catch (Exception e) {
//				connection.close(TcpConnectionCloseReason.ReadError);
////				e.printStackTrace();
//			}
//        	keyIter.remove();
//        }
	}
}
