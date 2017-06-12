package org.bei.dema.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.bei.dema.tcp.HttpParseException;
import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/9
*/
public class HttpUtils {

	static public HttpResponse request(String host,int port,HttpRequest request,int timeout)throws Exception{
		Socket socket = new Socket();
		try {
			//connect
			socket.setTcpNoDelay(true);
			socket.connect(new InetSocketAddress(host, port), timeout);
			//write packet
			socket.setSoTimeout(timeout);
			ByteBuffer byteBuffer = HttpSerializeUtils.serialize(request);
			socket.getOutputStream().write(byteBuffer.array());
			
			HttpResponse response = new HttpResponse();
			while(true){
				if(socket.getInputStream().available()>0){
					int packetLength = socket.getInputStream().available();
					byte[] bytes = new byte[packetLength];
					socket.getInputStream().read(bytes, 0, packetLength);
					byteBuffer = ByteBuffer.wrap(bytes);
					HttpResponse result = HttpSerializeUtils.deSerialize(byteBuffer, response);
					if(result != null){
						break;
					}
				}
				
				Thread.sleep(1);
			}
			return response;
		}finally {
			socket.close();
		}
	}
}
