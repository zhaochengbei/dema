package org.bei.dema.tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * author：bei
 * date：2017/5/31
*/

public class TcpUtils {
	/**
	 * the head of packet is a int,is the value of packet,the length not incloud itself length
	 */
	static public ByteBuffer request(String host,int port,ByteBuffer requestData,int timeout)throws Exception{
		//connect
		Socket socket = new Socket();
		socket.setTcpNoDelay(true);
		socket.connect(new InetSocketAddress(host, port), timeout);
		//write packet
		socket.setSoTimeout(timeout);
		requestData.flip();
		byte[] bytes = new byte[requestData.remaining()];
		requestData.get(bytes);
		socket.getOutputStream().write(bytes.length);
		socket.getOutputStream().write(bytes);
		//read length of packet
		bytes = new byte[4];
		socket.getInputStream().read(bytes, 0, 4);
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes); 
		byteBuffer.flip();
		int packetLength = byteBuffer.getInt();
		//read packet
		bytes = new byte[packetLength];
		socket.getInputStream().read(bytes, 0, packetLength);
		byteBuffer = ByteBuffer.wrap(bytes);
		//return result
		return byteBuffer;
	}
}
