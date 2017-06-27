package io.dema.websocket;

import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;
import io.dema.websocket.WebSocketConnection;
import io.dema.websocket.WebSocketFrame;
import io.dema.websocket.WebSocketHandler;
import io.dema.websocket.WebSocketOpcode;
import io.dema.websocket.WebSocketServer;

/**
 * author：zhaochengbei
 * date：2017/6/21
*/
public class WebSocketServerTest {

	/**
	 * 
	 */
	static private WebSocketServer webSocketServer = new WebSocketServer();
	/**
	 * 
	 */
	static private WebSocketHandler webSocketHandler = new WebSocketHandler() {

		public void onAccpet(WebSocketConnection webSocketContext) {
			System.out.println("s_onAccpet");
		}

		public void onUpgrade(HttpRequest httpRequest, HttpResponse httpResponse,
				WebSocketConnection webSocketConnection) {
			System.out.println("s_onUpgrade");
			webSocketConnection.write(httpResponse);
			
		}
		public void onFrame(WebSocketConnection webSocketConnection, WebSocketFrame frame) {
			System.out.println("s_onFrame,"+frame);
			if(frame.opcode == WebSocketOpcode.CLOSE){
				webSocketConnection.close();
			}else if(frame.opcode == WebSocketOpcode.TEXT_MSG){
				// down data cannot have mark
				frame.hasMask = false;
				frame.mark = null;
				System.out.println("send frame="+frame);
				webSocketConnection.send(frame);
//				//send to All
//				synchronized (webSocketServer.webSocketConnections) {
//					for (WebSocketConnection webSocketConnection2 : webSocketServer.webSocketConnections.values()) {
//						webSocketConnection2.send(frame);
//					}
//				}
			}
		}
		
		public void onClose(WebSocketConnection webSocketContext) {
			System.out.println("s_onClose");
		}

		public void onReadIdle(WebSocketConnection webSocketConnection) {
			webSocketConnection.close();
		}
		
	};
	/**
	 * 
	 * @param args
	 */
	static public void main(String args[]){
		try {
			webSocketServer.config(10000, 10);
			webSocketServer.start(8090, webSocketHandler);
			System.out.println("server started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
