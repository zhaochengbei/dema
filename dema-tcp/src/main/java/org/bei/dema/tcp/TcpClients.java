package org.bei.dema.tcp;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Vector;

/**
 * author：bei
 * date：2017/5/25
*/
public class TcpClients{
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
	public TcpClients(){
		
	}
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param count
	 * @param createGap
	 * @param ioHandler
	 * @throws Exception
	 */
	public void start(String ip,int port,int count,int createGap,IoHandler ioHandler) throws Exception{
		connectionManager.start(ioHandler);
		this.lastCreateTime = System.currentTimeMillis();
		//generate connection
		while(count > 0){
			if(System.currentTimeMillis() - lastCreateTime> createGap){
				count--;
				lastCreateTime += createGap;
				
				Socket socket = new Socket(ip,port);
				TcpConnection connection = new TcpConnection(socket);
				connectionManager.add(connection);
			}
		}
	}
	public Vector<TcpConnection> getConnections(){
		return connectionManager.connections;
	}
	
	/**
	 * 关闭
	 */
	public void shutdown() throws Exception{
		Vector<TcpConnection> connections = getConnections();
		while(connections.size()>0){
			try {
				TcpConnection socket = connections.get(0);
				socket.close(TcpConnectionCloseReason.ShutDownTcpServer);
			} catch (ArrayIndexOutOfBoundsException e) {
				//donothing
//				e.printStackTrace();
			}	
		}
		//等待连接全部关闭；
		connectionManager.shutdown();
	}
}
