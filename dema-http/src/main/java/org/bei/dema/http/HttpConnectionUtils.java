package org.bei.dema.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpConnectionUtils {
	/**
	 * 
	 * @param connection
	 * @param request
	 */
	static public void writeHttpRequest(TcpConnection connection, HttpRequest request){
		ByteBuffer byteBuffer = HttpSerializeUtils.serialize(request);
		byteBuffer.flip();
		connection.writeAndFlush(byteBuffer);
	}
	/**
	 * 
	 * @param connection
	 * @param response
	 */
	static public void writeHttpResponse(TcpConnection connection, HttpResponse response){
		ByteBuffer byteBuffer = HttpSerializeUtils.serialize(response);
		byteBuffer.flip();
		connection.writeAndFlush(byteBuffer);
	}
}
