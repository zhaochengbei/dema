package io.dema.websocket;

import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;

/**
 * author：zhaochengbei
 * date：2017/6/20
*/
public interface WebSocketHandler {
	/**
	 * 
	 */
	public void onAccpet(WebSocketConnection webSocketConnection);
	/**
	 * 
	 */
	public void onUpgrade(HttpRequest httpRequest,HttpResponse httpResponse,WebSocketConnection webSocketConnection);
	/**
	 *
	 */
	public void onFrame(WebSocketConnection webSocketConnection,WebSocketFrame packet);
	/**
	 * 
	 */
	public void onClose(WebSocketConnection webSocketConnection);
}
