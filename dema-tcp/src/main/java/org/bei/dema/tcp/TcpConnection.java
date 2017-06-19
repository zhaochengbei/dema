package org.bei.dema.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * author：zhaochengbei
 * date：2017/5/31
*/
public class TcpConnection {

	/**
	 * 
	 */
	public Socket socket;
	
	/**
	 * this lib use
	 */
	public boolean inReading;
	
	/**
	 * 
	 */
	public Object packet; 
	
	/**
	 * use part use
	 */
	public long lastWriteTime;
	/**
	 * use part use
	 */
	public long lastReadTime;
	/**
	 * close Reason
	 */
	public String closeReason;
	/**
	 * 
	 */
	public TcpConnection(Socket socket){
		this.socket = socket;
		
	}
	public TcpConnection(){
		this.socket = new Socket();
	}
	/**
	 * 
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	public void connect(String host,int port) throws IOException{
		socket.connect(new InetSocketAddress(host, port));
	}
	/**
	 * 
	 * @return
	 */
	public boolean isClose(){
		//socket.isConnect stand is not connect ,if close it still true
		return socket.isClosed();
	}
	
	/**
	 * 
	 * @param byteBuffer
	 */
	public void writeAndFlush(ByteBuffer byteBuffer){
//		byteBuffer.flip();
		byte[] bytes = new byte[byteBuffer.remaining()];
		byteBuffer.get(bytes);
		try {
			socket.getOutputStream().write(bytes);
			socket.getOutputStream().flush();
		} catch (IOException e) {
			//after a few millsecond use part use receive a close event;
			close(TcpConnectionCloseReason.WriteError);
		}
	}
	/**
	 * 
	 * @return
	 */
	public int available(){
		int available = 0;
		try {
			available = socket.getInputStream().available();
		} catch (IOException e) {
			close(TcpConnectionCloseReason.ReadError);
		}
		return available;
	}
	/**
	 * 
	 * @param byteBuffer
	 */
	public void read(ByteBuffer byteBuffer){
		try {
			int length = available();
			if(length > byteBuffer.remaining()){
				length = byteBuffer.remaining();
			}
			byte[] bytes = new byte[length];
			socket.getInputStream().read(bytes, 0, length);
			byteBuffer.put(bytes);
			lastReadTime = System.currentTimeMillis();
		} catch (IOException e) {
			//after a few millsecond use part use receive a close event;
			close(TcpConnectionCloseReason.WriteError);
		}
		
	}
	/**
	 * 
	 * @param reason
	 */
	public void close(String reason){
		if(socket.isClosed() == true){
			return;
		}
		this.closeReason = reason;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			//never reach here,because have not block oprate socket
		}
			
		
	}
}
