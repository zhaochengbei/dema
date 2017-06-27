package io.dema.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.dema.http.HttpParseException;
import io.dema.http.HttpResponse;
import io.dema.http.HttpResponseStatus;
import io.dema.http.HttpSerializeUtils;
import io.dema.tcp.IoHandler;
import io.dema.tcp.TcpClients;
import io.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/26
*/
public class WebSocketClients {

	/**
	 * 
	 */
	private TcpClients tcpClients = new TcpClients();
	/**
	 * 
	 */
	public Map<TcpConnection, WebSocketConnection> webSocketConnections = new ConcurrentHashMap<TcpConnection, WebSocketConnection>();
	/**
	 * 
	 */
	private IoHandler ioHandler = new IoHandler() {

		public void onAccept(TcpConnection connection) {
			//create websocketconnection'
			WebSocketConnection webSocketConnection = new WebSocketConnection();
			webSocketConnection.tcpConnection = connection;
			webSocketConnections.put(connection, webSocketConnection);
			webSocketHandler.onAccpet(webSocketConnection);
		}
		public void onRead(TcpConnection connection) {
			WebSocketConnection webSocketConnection = webSocketConnections.get(connection);
			ByteBuffer byteBuffer = ByteBuffer.allocate(connection.available());
			if(webSocketConnection.hasHandShake==false){
				if(connection.packet == null){
					connection.packet = new HttpResponse();
				}
				HttpResponse response = (HttpResponse)connection.packet;
				connection.read(byteBuffer);
				byteBuffer.flip();
				HttpResponse result = null;
				
				try {
					result = HttpSerializeUtils.deSerialize(byteBuffer, response);
				} catch (HttpParseException e) {
					connection.close(HttpResponseStatus.phraseMap.get(HttpResponseStatus.BAD_REQUEST));	
				}
				//check? connection，upgrade，accept-key;now only check connection
//				if(result.connection.equals("Upgrade") == false){
//					connection.close(TcpConnectionCloseReason.OtherError);
//				}
				connection.packet = null;
				webSocketConnection.hasHandShake = true;
				webSocketHandler.onUpgrade(null, result, webSocketConnection);
			}
			if(webSocketConnection.hasHandShake == true){
				while(connection.available()>0){
					//create frame obj
					if(connection.packet == null){
						connection.packet = new WebSocketFrame();
					}
					//try parse
					WebSocketFrame packet = (WebSocketFrame)connection.packet;
					WebSocketFrame packet2 = WebSocketSerializeUtils.deSerialize(packet, connection);
					if(packet2 == null){
						return;
					}
					connection.packet = null;
					webSocketHandler.onFrame(webSocketConnection,packet2);
				}
			}
			
		}
		public void onReadIdle(TcpConnection connection) {
			WebSocketConnection webSocketConnection = webSocketConnections.get(connection);
			webSocketConnection.close();	
		}
		
		public void onClose(TcpConnection connection, String reason) {
			WebSocketConnection webSocketConnection = webSocketConnections.remove(connection);
			webSocketHandler.onClose(webSocketConnection);
		}
		

	};
	/**
	 * 
	 */
	private WebSocketHandler webSocketHandler;
	/**
	 * 
	 */
	public WebSocketClients(){
		
	}
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param count
	 * @param createGap
	 * @param webSocketHandler
	 * @throws IOException
	 */
	public void start(String ip,int port,int count,int createGap,WebSocketHandler webSocketHandler)throws IOException{
		this.webSocketHandler = webSocketHandler;
		tcpClients.start(ip, port, count, createGap, ioHandler);
	}
	/**
	 * 
	 */
	public void shutdown(){
		tcpClients.shutdown();
		webSocketConnections.clear();
	}
}


