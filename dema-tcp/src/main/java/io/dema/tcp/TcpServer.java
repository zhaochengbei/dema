package io.dema.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * author：zhaochengbei
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
					//open too many file or accepter be close
					e.printStackTrace();
					break;
				}
			}
			
		}
	};

	/**
	 * 
	 */
	private Thread acceptSocketThread;
	/**
	 * 
	 */
	public int readIdleTimeoutSeconds = 0;
	/**
	 * 
	 */
	public int readIdleCheckGapSeconds = 1;
	/**
	 * 
	 */
	private Runnable checkReadIdleTimeOutLogic = new Runnable() {
		
		public void run() {
			while(true){
				long time = System.currentTimeMillis();
				ArrayList<TcpConnection> connections = getConnections();
				synchronized (connections) {
					/**
					 * 如果连接列表中存在某个connection，说明他还没有关闭，在这里加超时事件一定会该连接的关闭时间之前；
					 */
					for (int i = 0; i < getConnections().size(); i++) {
						TcpConnection connection = connections.get(i);
						if(readIdleTimeoutSeconds !=0 &&connection.isClose() == false&&time - connection.lastReadTime> readIdleTimeoutSeconds*1000){
							//use part will receive a close event;
							ioHandler.onReadIdle(connection);
						}
					}
				}
				
				try {
					Thread.sleep(readIdleCheckGapSeconds);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	};

	/**
	 * 
	 */
	private Thread checkReadIdleTimeOutThread;

	/**
	 * 
	 */
	private TcpConnectionManager connectionManager = new TcpConnectionManager();

	/**
	 * 
	 */
	private IoHandler ioHandler;
	/**
	 * 
	 * @param maxConnectionCount
	 * @param readIdleTimeoutSeconds ,timeunit is millsecond
	 */
	public void config(int maxConnectionCount,int readIdleTimeoutSeconds){
		this.maxConnectionCount = maxConnectionCount;
		this.readIdleTimeoutSeconds = readIdleTimeoutSeconds;
	}
	/**
	 * 
	 */
	public void configThread(int exeIoTaskThreadCount){
		connectionManager.exeIoTaskThreadCount = exeIoTaskThreadCount;
	}
	/**
	 * 
	 */
	public void configCheckGap(int checkStatusGapMillSeconds,int readIdleCheckGapSeconds){
		connectionManager.checkStatusGapMillSeconds = checkStatusGapMillSeconds;
		this.readIdleCheckGapSeconds = readIdleCheckGapSeconds;
	}
	/**
	 * 
	 */
	public void start(int port,IoHandler ioHandler) throws IOException{
		this.ioHandler = ioHandler;
		connectionManager.start(ioHandler);
		accepter = new ServerSocket(port);
		acceptSocketThread = new Thread(acceptSocketLogic,"AcceptSocket_0");
		checkReadIdleTimeOutThread = new Thread(checkReadIdleTimeOutLogic,"CheckReadIdleTimeOut_0");
		
		acceptSocketThread.start();
		checkReadIdleTimeOutThread.start();
	}
	/**
	 * 
	 */
	public ArrayList<TcpConnection> getConnections(){
		return connectionManager.connections;
	}
	
	
	/**
	 * 
	 */
	public void shutdown() throws IOException{
		accepter.close();
		checkReadIdleTimeOutThread.interrupt();
		connectionManager.shutdown();

	}
}
