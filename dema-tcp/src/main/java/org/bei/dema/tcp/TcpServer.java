package org.bei.dema.tcp;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * author：bei
 * date：2017/5/31
*/
public class TcpServer {

	/**
	 * 
	 */
	private ServerSocket accepter;

	/**
	 * 
	 */
	public int maxConnectionCount = 0;
	/**
	 * 
	 */
	private Runnable acceptSocketLogic = new Runnable() {
		
		public void run() {
			while(true){
				try {
					Socket socket = accepter.accept();
					
					if(socket != null){
						TcpConnection connection = new TcpConnection(socket);
						//if exceed maxConnectionCount 
						if(maxConnectionCount!=0 && getConnections().size()>= maxConnectionCount){
							connection.close(TcpConnectionCloseReason.ExceedMaxConnectionCount);
							continue;
						}
						connection.inReading = false;
						connection.lastReadTime = System.currentTimeMillis();
						connectionManager.add(connection);
						
					}
				}catch (Exception e) {
					//accepter be close，end this thread
					break;
					//can not reach here
//					e.printStackTrace();
				}
			}
			
		}
	};

	/**
	 * 
	 */
	private Thread acceptSocketThread = new Thread(acceptSocketLogic,"AcceptSocket_0");
//	/**
//	 * max heard gap
//	 */
	/**
	 * 
	 */
	public int maxReadIdleTime = 0;
	/**
	 * 
	 */
	private TimerTask checkReadIdleTimeOutLogic = new TimerTask() {
		
		public void run() {
			try {
				long time = System.currentTimeMillis();
				for (int i = 0; i < getConnections().size(); i++) {
					TcpConnection connection = getConnections().get(i);
					if(maxConnectionCount !=0 &&connection.isClose() == false&&time - connection.lastReadTime> maxReadIdleTime){
						//use part will receive a close event;
						connection.close(TcpConnectionCloseReason.ReadIdleTimeOut);
					}
				}
			}catch (Exception e) {
				//do nothing
//				e.printStackTrace();
			}
			
		}
	};

	/**
	 * 
	 */
	private ScheduledExecutorService checkReadIdleTimeOutThreads = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		
		public Thread newThread(Runnable r) {
			return new Thread(r,"CheckReadIdleTimeOut_0");
		}
	});

	/**
	 * 
	 */
	/**
	 * 
	 */
	private TcpConnectionManager connectionManager = new TcpConnectionManager();

	/**
	 * 
	 * @param maxConnection
	 * @param maxReadIdleTime ,timeunit is millsecond
	 */
	public void config(int maxConnection,int maxReadIdleTime){
		this.maxConnectionCount = maxConnection;
		this.maxReadIdleTime = maxReadIdleTime;
	}
	/**
	 * 
	 */
	public void start(int port,IoHandler ioHandler) throws Exception{
		connectionManager.start(ioHandler);
		accepter = new ServerSocket(port);
		acceptSocketThread.start();
		checkReadIdleTimeOutThreads.scheduleAtFixedRate(checkReadIdleTimeOutLogic, 10,10, TimeUnit.MICROSECONDS);
	}
	/**
	 * 
	 */
	public Vector<TcpConnection> getConnections(){
		return connectionManager.connections;
	}
	
	
	/**
	 * 
	 */
	public void shutdown() throws Exception{
//		acceptSocketThread.interrupt();
		accepter.close();
		checkReadIdleTimeOutThreads.shutdown();
		connectionManager.shutdown();

	}
}
