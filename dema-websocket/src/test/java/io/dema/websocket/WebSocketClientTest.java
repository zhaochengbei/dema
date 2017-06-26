package io.dema.websocket;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Vector;

import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;
import io.dema.http.HttpSerializeUtils;
import io.dema.tcp.IoHandler;
import io.dema.tcp.TcpClients;
import io.dema.tcp.TcpConnection;
import io.dema.tcp.WriteTask;
import io.dema.websocket.WebSocketClients;
import io.dema.websocket.WebSocketConnection;
import io.dema.websocket.WebSocketFrame;
import io.dema.websocket.WebSocketHandShakePacketBuilder;
import io.dema.websocket.WebSocketHandler;

/**
 * author：zhaochengbei
 * date：2017/6/26
*/
public class WebSocketClientTest {
	/**
	 * 
	 */
	static private WebSocketClients webSocketClients = new WebSocketClients();
	/**
	 * 
	 */
	static private WebSocketHandler webSocketHandler = new WebSocketHandler() {
		
		public void onUpgrade(HttpRequest httpRequest, HttpResponse httpResponse, WebSocketConnection webSocketConnection) {
			System.out.println("c_onUpgrade");
		}
		
		public void onFrame(WebSocketConnection webSocketConnection, WebSocketFrame packet) {
			System.out.println("c_onFrame");
		}
		
		public void onClose(WebSocketConnection webSocketConnection) {
			System.out.println("c_onClose");
		}
		
		public void onAccpet(WebSocketConnection webSocketConnection) {
			System.out.println("c_onAccept");
			HttpRequest httpRequest = WebSocketHandShakePacketBuilder.createHandShakeRequest(webSocketConnection.tcpConnection.socket.getInetAddress().getHostAddress());
			webSocketConnection.write(httpRequest);
		}
	};

	/**
	 * 
	 * @param args
	 */
    public static void main( String[] args )
    {
    	try {
    		int testCount = 10;
    		while(testCount-- >0){
    			testClients();
    			Thread.sleep(20*1000);
        		webSocketClients.shutdown();
    			Thread.sleep(2*1000);
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println( "Hello World!" );
    }
    static public void testClients()throws Exception{
		try {
			webSocketClients.start("localhost", 8090, 500, 1, webSocketHandler);
			//time send packet
			int writeCount = 20000;
			while(writeCount -- >0){
				Collection<WebSocketConnection> connections = webSocketClients.webSocketConnections.values();
				System.out.println("last connection "+connections.size());
				long time = System.currentTimeMillis();
				for (WebSocketConnection webSocketConnection : connections) {
					if(webSocketConnection.hasHandShake == false){
						continue;
					}
					if(time - webSocketConnection.tcpConnection.lastWriteTime> 1000){
						//send frame
						WebSocketFrame webSocketFrame = new WebSocketFrame();
						webSocketFrame.data = "packet".getBytes();
						webSocketConnection.send(webSocketFrame);
						webSocketConnection.tcpConnection.lastWriteTime = time;
					}
				}
				Thread.sleep(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
