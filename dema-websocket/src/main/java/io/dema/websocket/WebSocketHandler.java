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
	 * @param webSocketConnection
	 */
	public void onAccpet(WebSocketConnection webSocketConnection);
	/**
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 * @param webSocketConnection
	 */
	public void onUpgrade(HttpRequest httpRequest,HttpResponse httpResponse,WebSocketConnection webSocketConnection);
	/**
	 * 
	 * @param webSocketConnection
	 * @param webSocketFrame
	 */
	public void onFrame(WebSocketConnection webSocketConnection,WebSocketFrame webSocketFrame);
	/**
	 * 
	 * @param webSocketConnection
	 */
	public void onReadIdle(WebSocketConnection webSocketConnection);
	/**
	 * 
	 */
	public void onClose(WebSocketConnection webSocketConnection);
}
