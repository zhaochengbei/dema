package org.bei.dema.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Vector;

/**
 * author：zhaochengbei
 * date：2017/5/31
*/
public class TcpConnection {

	/**
	 * 
	 */
	public SocketChannel channel;
	public SelectionKey selectionKey;
	/**
	 *  50k connection will consume 12m memrey
	 */
	public Vector<byte[]> bytesVector = new Vector<byte[]>();
	/**
	 * 
	 */
	public ByteBuffer byteBuffer = ByteBuffer.allocate(256);
	public ByteBuffer byteBuffer2 = ByteBuffer.allocate(256);
	/**
	 * this lib use
	 */
	public boolean inOprating;
	
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
	public TcpConnection(SocketChannel channel){
		this.channel = channel;
		
	}
	/**
	 * 
	 * @return
	 */
	public boolean isClose(){
		if(closeReason != null){
			return true;
		}
		//socket.isConnect stand is not connect ,if close it still true
		return channel.isOpen() == false;
	}
	
	
	/**
	 * 
	 * @param byteBuffer
	 * @throws Exception
	 */
	public void writeAndFlush(ByteBuffer byteBuffer){
		while(true){
			try {
				channel.write(byteBuffer);
			} catch (IOException e) {
				//after a few millsecond use part use receive a close event;
				close(TcpConnectionCloseReason.WriteError);
			}
			if(byteBuffer.remaining() > 0){
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				break;
			}
			
		}
			
	}
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public int available(){
		int length = 0;
		for (int i = 0; i < bytesVector.size(); i++) {
			length += bytesVector.get(i).length;
		}
		return length;
	}
	/**
	 * 
	 * @param byteBuffer
	 * @throws Exception
	 */
	public void read(ByteBuffer byteBuffer){
		/**
		 * 这里没有考虑 byteBuffer剩余空间与bytes不等的情况；
		 */
//		int length = available();
//		if(length > byteBuffer.remaining()){
//			length = byteBuffer.remaining();
//		}
//		byte[] bytes = new byte[length];
		for (int i = 0; i < bytesVector.size(); i++) {
			byte[] bytes = bytesVector.remove(0);
			i--;
			if(byteBuffer.remaining() >0){
//				int length  = byteBuffer.remaining();
				if(bytes.length>byteBuffer.remaining()){
					 byteBuffer2.clear();
					 byteBuffer2.put(bytes);
					 byteBuffer2.flip();
					 byte[] bytes2 = new byte[byteBuffer.remaining()];
					 byteBuffer2.get(bytes2);
					 byteBuffer.put(bytes2);
					 byte[] bytes3 = new byte[byteBuffer2.remaining()];
					 byteBuffer2.get(bytes3);
					 bytesVector.add(0, bytes3);
//					length = bytes.length;
				}else{
					byteBuffer.put(bytes);
				}
				
			}
		
		}
//		this.byteBuffer.get(bytes);
//		byteBuffer.put(bytes);
		lastReadTime = System.currentTimeMillis();
	}
	/**
	 * 
	 * @throws Exception
	 */
	public void close(String reason){
		this.closeReason = reason;
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
			//never reach here,because have not block oprate socket
		}
	}
	/**
	 * 
	 */
	public long readToBuffer(){
		long byteRead = 0;
		while(true){
			try {
				byteBuffer.clear();
				long byteReadPart = channel.read(byteBuffer);
				if(byteReadPart > 0){
					byteRead += byteReadPart;
					byte[] bytes = new byte[(int)byteRead];
					byteBuffer.flip();
					byteBuffer.get(bytes);
					bytesVector.add(bytes);
					if(byteReadPart == byteBuffer.capacity()){
						break;
					}
				}else{
					break;
				}
				 
			} catch (Exception ioException) {
				close(TcpConnectionCloseReason.ReadError);
				break;
			}
		}
		return byteRead;
	}
}
