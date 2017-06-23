package org.bei.dema.websocket;

import java.nio.ByteBuffer;

import org.bei.dema.http.HttpConnectionUtils;
import org.bei.dema.http.HttpResponse;
import org.bei.dema.tcp.TcpConnection;
import org.bei.dema.tcp.TcpConnectionCloseReason;

/**
 * author：zhaochengbei
 * date：2017/6/20
*/
public class WebSocketConnection {

	/**
	 * 
	 */
	public TcpConnection tcpConnection;
	/**
	 * 
	 */
	public boolean hasHandShake;
	/**
	 * 
	 */
	public WebSocketConnection(){
		
	}
	/**
	 * 
	 */
	public void write(HttpResponse httpResponse){
		HttpConnectionUtils.writeHttpResponse(tcpConnection, httpResponse);
	}
	/**
	 * 
	 */
	public void send(WebSocketFrame frame){
		ByteBuffer byteBuffer = WebSocketSerializeUtils.serialize(frame);
		byteBuffer.flip();
		tcpConnection.writeAndFlush(byteBuffer);
	}
	/**
	 * 
	 */
	public void close(){
		tcpConnection.close(TcpConnectionCloseReason.NormalActiveClose);
	}
	
}
