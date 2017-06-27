package io.dema.websocket;
/**
 * author：zhaochengbei
 * date：2017年6月26日
*/
public class WebSocketSendMessageTask implements Runnable {

	/**
	 * 
	 */
	public WebSocketConnection webSocketConnection;
	/**
	 * 
	 */
	public WebSocketFrame webSocketFrame;
	/**
	 * 
	 */
	public void run() {
		webSocketConnection.send(webSocketFrame);
	}

}
