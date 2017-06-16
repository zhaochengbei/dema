package org.bei.dema.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
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
	private Selector selector;
	/**
	 * 
	 */
	private ServerSocketChannel accepter;

	/**
	 * 
	 */
	public int maxConnectionCount = 0;
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
	private TcpConnectionManager connectionManager = new TcpConnectionManager();
	/**
	 * 
	 */
	private Runnable acceptAndReadChannelLogic = new Runnable() {
		

		public void run() {
			while(true){
				try {
					if(selector.select(100) != 0){
						Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();  
				        while (keyIter.hasNext()) {
				        	SelectionKey key = keyIter.next();
							try {
					        	if(key.isAcceptable()){
				                	SocketChannel channel =  ((ServerSocketChannel) key.channel()).accept();
//						        	keyIter.remove();
				                	TcpConnection connection = new TcpConnection(channel);
									//if exceed maxConnectionCount 
									if(maxConnectionCount!=0 && getConnections().size()>= maxConnectionCount){
										connection.close(TcpConnectionCloseReason.ExceedMaxConnectionCount);
										continue;
									}
			
									connection.inOprating = false;
									connection.lastReadTime = System.currentTimeMillis();
									connectionManager.add(connection,selector);
				                }else{
						        	//尝试读取
						        	connectionManager.read(key);
				                }
							}catch (CancelledKeyException e) {
								e.printStackTrace();
							}
							keyIter.remove();
				        }
			        	//读取处理
//						keyIter = selector.selectedKeys().iterator();
//			        	connectionManager.read(keyIter);
//						continue;
					}
					connectionManager.removeClosedConnections();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
		
			}
			
		}
	};

	/**
	 * 
	 */
	private Runnable checkReadIdleTimeOutLogic = new Runnable() {
		
		public void run() {
			while(true){
				try {
					long time = System.currentTimeMillis();
					for (int i = 0; i < getConnections().size(); i++) {
						TcpConnection connection = getConnections().get(i);
						if(readIdleTimeoutSeconds !=0 &&connection.isClose() == false&&time - connection.lastReadTime> readIdleTimeoutSeconds*1000){
							//use part will receive a close event;
							connection.close(TcpConnectionCloseReason.ReadIdleTimeOut);
						}
					}
				}catch (ArrayIndexOutOfBoundsException e) {
					//thread confict,need do nothing
				}
				
				try {
					Thread.sleep(readIdleCheckGapSeconds);
				} catch (InterruptedException e) {
					break;
//					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
			
			
			
		}
	};

	/**
	 * 
	 */
	private Thread checkReadIdleTimeOutThread = new Thread(checkReadIdleTimeOutLogic,"CheckReadIdleTimeOut_0");

	/**
	 * 
	 */
	private Thread acceptAndReadChannelThread = new Thread(acceptAndReadChannelLogic,"AcceptAndReadChannel_0");


	/**
	 * 
	 * @param maxConnection
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
	public void configCheckGap(int closeCheckGapMillSeconds,int readIdleCheckGapSeconds){
		connectionManager.closeCheckGapMillSeconds = closeCheckGapMillSeconds;
		this.readIdleCheckGapSeconds = readIdleCheckGapSeconds;
	}
	/**
	 * 
	 */
	public void start(int port,IoHandler ioHandler) throws IOException{
		connectionManager.start(ioHandler);
		
		selector = Selector.open();
		accepter = ServerSocketChannel.open();
		accepter.configureBlocking(false);
		accepter.register(selector, SelectionKey.OP_ACCEPT);
		accepter.bind(new InetSocketAddress(port));
		
		acceptAndReadChannelThread.start();
		checkReadIdleTimeOutThread.start();
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
	public void shutdown() throws IOException{
		selector.close();//accept will auto close;
		accepter.close();
		checkReadIdleTimeOutThread.interrupt();
		connectionManager.shutdown();

	}
}
