package org.dema.tcp.socketpool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * author：zhaochengbei
 * date：2017/8/4
*/
public class DemaSocketPool {
	/**
	 * 
	 */
	public String host;
	public int port;
	public int connectTimeOut;
	/**
	 * 
	 */
	public DemaSocket[] allSockets;
	public ArrayList<DemaSocket> notInUsedSockets = new ArrayList<DemaSocket>();
	
	/**
	 * 
	 */
	public DemaSocketPool(){
		
	}
	/**
	 * return until all connected
	 */
	public synchronized void init(String host,int port,int socketCount,int connectTimeout){
		this.host = host;
		this.port = port;
		this.connectTimeOut = connectTimeout;
		allSockets = new DemaSocket[socketCount];
		for (int i = 0; i < socketCount; i++) {
			 allSockets[i] = new DemaSocket();
			 notInUsedSockets.add(allSockets[i]);
		}
	}
	/**
	 * 
	 */
	public synchronized void distory(){
		for (int i = 0; i < allSockets.length; i++) {
			try {
				if(allSockets[i].isClosed() == false){
					allSockets[i].close(DemaSocketCloseReason.ClosePool);
				}
				allSockets = null;
				notInUsedSockets = new ArrayList<DemaSocket>();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * outside throw exceptions
	 * @throws IOException 
	 */
	public synchronized DemaSocket getNotInUseSocket() throws IOException{
		if(notInUsedSockets.size()>0){
			DemaSocket socket = notInUsedSockets.get(0);
			if(socket.isConnected() == false){
				socket.connect(host, port, connectTimeOut);
			}
			notInUsedSockets.remove(0);
			return socket;
		}
		return null;
	}
	/**
	 * use finally make true reclaim socket;
	 */
	public synchronized void laybackSocket(DemaSocket socket){
		notInUsedSockets.add(socket);
	}
	
}
