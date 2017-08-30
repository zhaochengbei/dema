package org.dema.tcp.socketpool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import javax.net.ssl.HostnameVerifier;

/**
 * author：zhaochengbei
 * date：2017/8/4
*/
public class DemaSocket {
	/**
	 * 
	 */
	private String host;
	private int port;
	/**
	 * 
	 */
	private int timeout;
	/**
	 * 
	 */
	private Socket socket = new Socket(); 
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
	public DemaSocket(){
		
	}
	/**
	 * first connect remeber host port timeout and connect to server,
	 * second connect close last connection and reconnect
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	public void connect(String host,int port,int timeout) throws IOException{
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		long time = System.currentTimeMillis();
		socket.connect(new InetSocketAddress(host, port),timeout);
//		System.out.println("connect cost time="+(System.currentTimeMillis()-time));
	}

	/**
	 * 
	 * @return
	 */
	public boolean isConnected(){
		//socket.isConnect stand is not connect ,if close it still true
		return socket.isConnected();
	}
	/**
	 * 
	 * @return
	 */
	public boolean isClosed(){
		//socket.isConnect stand is not connect ,if close it still true
		return socket.isClosed();
	}
	
	/**
	 * 
	 * @param byteBuffer
	 */
	public void writeAndFlush(ByteBuffer byteBuffer)throws IOException{
		byte[] bytes = new byte[byteBuffer.remaining()];
		byteBuffer.get(bytes);
		try {
			socket.getOutputStream().write(bytes);
			socket.getOutputStream().flush();
		} catch (IOException e) {
			close(DemaSocketCloseReason.WriteError);
			//重连并在此写入；在出错就通知上面；
			socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(host, port),timeout);
				socket.getOutputStream().write(bytes);
				socket.getOutputStream().flush();
			} catch (IOException e2) {
				close(DemaSocketCloseReason.WriteError);
				throw e2;
			}
		}
	}
	/**
	 * 
	 * @param byteBuffer
	 */
	public void read(ByteBuffer byteBuffer)throws IOException{
		try {
			int length = byteBuffer.remaining();
			byte[] bytes = new byte[length];
			socket.getInputStream().read(bytes, 0, length);
			byteBuffer.put(bytes);
			lastReadTime = System.currentTimeMillis();
		} catch (IOException e) {
			//after a few millsecond use part use receive a close event;
			close(DemaSocketCloseReason.WriteError);
			throw e;
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