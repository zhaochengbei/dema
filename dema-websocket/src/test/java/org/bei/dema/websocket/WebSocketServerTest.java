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
			System.out.println("s_onAccpet");
		}

		public void onUpgrade(HttpRequest httpRequest, HttpResponse httpResponse,
				WebSocketConnection webSocketConnection) {
			System.out.println("s_onUpgrade");
			webSocketConnection.write(httpResponse);
			
		}
		public void onFrame(WebSocketConnection webSocketConnection, WebSocketFrame packet) {
			System.out.println("s_onFrame,"+packet);
			if(packet.opcode == WebSocketOpcode.CLOSE){
				WebSocketFrame closePacket = new WebSocketFrame();
				packet.opcode = WebSocketOpcode.CLOSE;
				try {
					webSocketConnection.send(closePacket);
				} catch (Exception e) {
					e.printStackTrace();
				}
				webSocketConnection.close();
			}else{
				// down data cannot have mark
				packet.hasMask = false;
				packet.mark = null;
				System.out.println("send frame="+packet	);
				//转发给所有人；
				for (WebSocketConnection webSocketConnection2 : webSocketServer.webSocketConnections.values()) {
					try {
						webSocketConnection2.send(packet);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
						
			}
		}
		
		public void onClose(WebSocketConnection webSocketContext) {
			System.out.println("s_onClose");
		}
		
	};
	/**
	 * 
	 * @param args
	 */
	static public void main(String args[]){
		try {
			webSocketServer.config(1000, 10);
			webSocketServer.start(8090, webSocketHandler);
			System.out.println("server started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
