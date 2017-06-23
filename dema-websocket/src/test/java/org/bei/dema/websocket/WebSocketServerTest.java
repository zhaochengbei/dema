package org.bei.dema.websocket;

import org.bei.dema.http.HttpRequest;
import org.bei.dema.http.HttpResponse;

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
			System.out.println("onAccpet");
		}

		public void onUpgrade(HttpRequest httpRequest, HttpResponse httpResponse,
				WebSocketConnection webSocketConnection) {
			System.out.println("onUpgrade");
			webSocketConnection.write(httpResponse);
			
		}
		public void onFrame(WebSocketConnection webSocketConnection, WebSocketFrame packet) {
			System.out.println("onFrame,"+packet);
			if(packet.opcode == WebSocketOpcode.CLOSE){
				WebSocketFrame closePacket = new WebSocketFrame();
				packet.opcode = WebSocketOpcode.CLOSE;
				webSocketConnection.send(closePacket);
				webSocketConnection.close();
			}else{
				// down data cannot have mark
//				WebSocketFrame packet2 = new WebSocketFrame();
				try {
					packet.hasMask = false;
					packet.mark = null;
					System.out.println("send frame="+packet);
					webSocketConnection.send(packet);	
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void onClose(WebSocketConnection webSocketContext) {
			System.out.println("onClose");
		}
		
	};
	/**
	 * 
	 * @param args
	 */
	static public void main(String args[]){
//		webSocketServer.config(1000, readIdleTimeoutSeconds);
		try {
			webSocketServer.start(8090, webSocketHandler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
