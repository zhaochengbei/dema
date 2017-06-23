package org.bei.dema.websocket;

import org.bei.dema.http.HttpRequest;
import org.bei.dema.http.HttpResponse;

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
