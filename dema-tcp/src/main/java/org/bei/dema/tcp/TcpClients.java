package org.bei.dema.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;

/**
 * author：zhaochengbei
 * date：2017/5/25
*/
public class TcpClients{
	/**
	 * 
	 */
	private Selector selector;
	/**
	 * 
	 */
	private long lastCreateTime;
	/**
	 * 
	 */
	private TcpConnectionManager connectionManager = new TcpConnectionManager();
	/**
	 * 
	 */
	/**
	 * 
	 */
	private Runnable readChannelLogic = new Runnable() {
		

		public void run() {
			while(true){
				try {
					if(selector.select(100) != 0){
						Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
						while (keyIter.hasNext()) {
				        	SelectionKey key = keyIter.next();
				        	connectionManager.read(key);
						}
					}
					connectionManager.removeClosedConnections();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * 
	 */
	private Thread readChannelThread = new Thread(readChannelLogic,"ReadChannel_0");

	/**
	 * 
	 */
	public TcpClients(){
		
	}
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param count
	 * @param createGap gap of create connection
	 * @param ioHandler
	 * @throws Exception
	 */
	public void start(String ip,int port,int count,int createGap,IoHandler ioHandler)throws IOException{
		connectionManager.start(ioHandler);

		selector = Selector.open();
		this.lastCreateTime = System.currentTimeMillis();
		//generate connection
		while(count > 0){
			if(System.currentTimeMillis() - lastCreateTime> createGap){
				count--;
				lastCreateTime += createGap;
				/**
				 * general with nuli threads ,will get connect error and efficiency not up
				 */
				SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, port));
				TcpConnection connection = new TcpConnection(channel);
				connectionManager.add(connection,selector);
			}
		}
		//启动读取检测线程；
		readChannelThread.start();
	}
	public Vector<TcpConnection> getConnections(){
		return connectionManager.connections;
	}
	
	/**
	 * 关闭
	 */
	public void shutdown() throws IOException{
		Vector<TcpConnection> connections = getConnections();
//		while(connections.size()>0){
		try {
			for (int i = 0; i < connections.size(); i++) {
				TcpConnection socket = connections.get(i);
				socket.close(TcpConnectionCloseReason.ShutDownTcpServer);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			//do nothing
		}	
//		}
		
		connectionManager.shutdown();
		readChannelThread.interrupt();
	}
}
