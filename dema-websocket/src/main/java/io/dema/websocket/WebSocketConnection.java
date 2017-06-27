package io.dema.websocket;

import java.nio.ByteBuffer;

import io.dema.http.HttpConnectionUtils;
import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;
import io.dema.tcp.TcpConnection;
import io.dema.tcp.TcpConnectionCloseReason;

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
	 * only can write httprequest http response and frame；
	 * @param request
	 */
	public void write(HttpRequest request){
		
		HttpConnectionUtils.writeHttpRequest(tcpConnection, request);
	}
	/**
	 * 
	 * @param httpResponse
	 */
	public void write(HttpResponse httpResponse){
		HttpConnectionUtils.writeHttpResponse(tcpConnection, httpResponse);
	}
	/**
	 * if not handshake ,will not send data to connection
	 * @param frame
	 */
	public void send(WebSocketFrame frame){
		if(hasHandShake == false){
			//as not connected
//			tcpConnection.close("has not handshake");//process as tcp,because it not upgrade
			return;
		}
		ByteBuffer byteBuffer = WebSocketSerializeUtils.serialize(frame);
		byteBuffer.flip();
		tcpConnection.writeAndFlush(byteBuffer);
	}
	/**
	 * 
	 */
	public void close(){
		//if tcp connection is not close
		if(tcpConnection.isClose() == false){
			WebSocketFrame closePacket = new WebSocketFrame();
			closePacket.opcode = WebSocketOpcode.CLOSE;
			send(closePacket);
		}
		tcpConnection.close(TcpConnectionCloseReason.NormalActiveClose);
	}
	
}
