package io.dema.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.dema.http.HttpConnectionUtils;
import io.dema.http.HttpParseException;
import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;
import io.dema.http.HttpResponseStatus;
import io.dema.http.HttpSerializeUtils;
import io.dema.tcp.IoHandler;
import io.dema.tcp.TcpConnection;
import io.dema.tcp.TcpServer;

/**
 * author：zhaochengbei
 * date：2017/6/20
*/
public class WebSocketServer {

	/**
	 * 线程安全说明：
	 * 由于在分配任务的时候进行了额外处理，同个连接handler中的操作不会同时进行；
	 * 所以这里的get操作是安全的；
	 * put remove 和其他地方的遍历有冲突，所以在put remove 遍历的时候加了锁；
	 */
	
	/**
	 * 
	 */
	private TcpServer tcpServer = new TcpServer();
	/**
	 * 
	 */
	public Map<TcpConnection, WebSocketConnection> webSocketConnections = new ConcurrentHashMap<TcpConnection, WebSocketConnection>();
	/**
	 * 
	 */
	public WebSocketHandler webSocketHandler;
	/**
	 * 
	 */
	private IoHandler ioHandler = new IoHandler() {

		public void onAccept(TcpConnection connection) {
			WebSocketConnection webSocketConnection = new WebSocketConnection();
			webSocketConnection.tcpConnection = connection;
			/**
			 * websocketConnections is a safe data，when call they method will lock the obj,
			 * there we add a lock will not effect performance
			 */
			synchronized (webSocketConnections) {
				webSocketConnections.put(connection, webSocketConnection);
			}
			webSocketHandler.onAccpet(webSocketConnection);
		}
		
		public void onRead(TcpConnection connection) {
			//if has not hand shake
			WebSocketConnection webSocketConnection = webSocketConnections.get(connection);
			if(webSocketConnection.hasHandShake == false){
				//create request object
				if(connection.packet == null){
					connection.packet = new HttpRequest();
				}
				HttpRequest request = (HttpRequest)connection.packet;
				//read all data
				ByteBuffer byteBuffer = ByteBuffer.allocate(connection.available());
				connection.read(byteBuffer);
				byteBuffer.flip();
				try {
					HttpRequest request2 = HttpSerializeUtils.deSerialize(byteBuffer, request);				//判断是否解析完成
					if(request2 != null){
						//check
						if(request2.connection.equals("Upgrade") == false&&request2.otherHead.get("Upgrade").equals("websocket")==false
								&&request2.otherHead.containsKey("Sec-WebSocket-Key")==false){
							sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
							return;
						}
						
						//calc Accept
						String secWebSocketKey = request2.otherHead.get("Sec-WebSocket-Key").toString();
						String secWebSocketAccept = SHA1Utils.getSha1(secWebSocketKey+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11");
						byte[] bytes = HexUtils.hexStringToBytes(secWebSocketAccept);
						secWebSocketAccept = new String(Base64.getEncoder().encode(bytes));
						//general response
						HttpResponse httpResponse = WebSocketHandShakePacketBuilder.createHandShakeResponse(secWebSocketAccept);
						webSocketConnection.hasHandShake = true;
						connection.packet = null;
						webSocketHandler.onUpgrade(request2, httpResponse, webSocketConnection);
						
					}
				} catch (HttpParseException e) {
					e.printStackTrace();
					sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
				}
			}else{
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
			webSocketHandler.onReadIdle(webSocketConnection);
		}
		public void onClose(TcpConnection connection, String reason) {
			WebSocketConnection webSocketConnection;
			synchronized (webSocketConnections) {
				webSocketConnection = webSocketConnections.remove(connection);
			}
			webSocketHandler.onClose(webSocketConnection);
		}
		

	}; 

	/**
	 * 
	 * @param connection
	 * @param responseStatus
	 */
	private void sendErrorAndCloseConnection(TcpConnection connection,int responseStatus){
		HttpResponse response = new HttpResponse();
		response.status = HttpResponseStatus.BAD_REQUEST;
		response.phrase = HttpResponseStatus.phraseMap.get(response.status);
		response.content = ("errorcode="+response.phrase).getBytes();
		HttpConnectionUtils.writeHttpResponse(connection, response);
		connection.close(response.phrase);
	}
	/**
	 * 
	 */
	public WebSocketServer(){
		
	}
	/**
	 * 
	 * @param maxConnectionCount
	 * @param readIdleTimeoutSeconds
	 * @throws IOException
	 */
	public void config(int maxConnectionCount,int readIdleTimeoutSeconds) throws IOException{
		tcpServer.config(maxConnectionCount, readIdleTimeoutSeconds);
	}
	
	/**
	 * 
	 * @param port
	 * @param webSocketHandler
	 * @throws IOException
	 */
	public void start(int port,WebSocketHandler webSocketHandler) throws IOException{
		this.webSocketHandler = webSocketHandler;
		tcpServer.start(port, ioHandler);
	}
	/**
	 * 
	 */
	public void sendCloseFrameAndcloseAllWebSocketConnection(){
		synchronized (webSocketConnections) {
			for (Iterator<WebSocketConnection> iterator = webSocketConnections.values().iterator(); iterator.hasNext();) {
				WebSocketConnection webSocketConnection = (WebSocketConnection) iterator.next();
				webSocketConnection.sendCloseFrame();
				webSocketConnection.close();
			}
		}
	}
	/**
	 * 
	 */
	public void shutdown() throws IOException{
		tcpServer.shutdown();
		webSocketConnections.clear();
	}
}
