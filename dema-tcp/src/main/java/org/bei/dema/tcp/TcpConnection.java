package org.bei.dema.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * author：bei
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
	 * use part use 
	 */
	public ByteBuffer byteBuffer;
	
	/**
	 * use part use
	 */
	public int packetLength = -1;
	
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
	 * @throws Exception
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
	 * @throws Exception
	 */
	public void writeAndFlush(ByteBuffer byteBuffer) throws IOException{
		byteBuffer.flip();
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
	 * @throws Exception
	 */
	public int available() throws IOException{
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
	 * @throws Exception
	 */
	public void read(ByteBuffer byteBuffer) throws IOException{
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
	 * @throws Exception
	 */
	public void close(String reason) throws IOException{
		if(socket.isClosed() == true){
			return;
		}
		this.closeReason = reason;
		socket.close();
	}
}
