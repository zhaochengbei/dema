package org.bei.dema.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

/**
 * author：zhaochengbei
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
	 * @return
	 */
	public Vector<TcpConnection> getConnections(){
		return connectionManager.connections;
	}
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param count
	 * @param createGap
	 * @param ioHandler
	 * @throws IOException
	 */
	public void start(String ip,int port,int count,int createGap,IoHandler ioHandler)throws IOException{
		connectionManager.start(ioHandler);
		this.lastCreateTime = System.currentTimeMillis();
		//generate connection
		while(count > 0){
			if(System.currentTimeMillis() - lastCreateTime> createGap){
				count--;
				lastCreateTime += createGap;
				/**
				 * general with nuli threads ,will get connect error and efficiency not up
				 */
				Socket socket = new Socket(ip,port);
				TcpConnection connection = new TcpConnection(socket);
				connectionManager.add(connection);
			}
		}
	}
	
	/**
	 * 关闭
	 */
	public void shutdown(){
		Vector<TcpConnection> connections = getConnections();
		for (int i = 0; i < connections.size(); i++) {
			try {
				TcpConnection socket = connections.get(0);
				socket.close(TcpConnectionCloseReason.ShutDownTcpServer);
			} catch (ArrayIndexOutOfBoundsException e) {
				//do nothing
			}	
		}
		
		connectionManager.shutdown();
	}
}
