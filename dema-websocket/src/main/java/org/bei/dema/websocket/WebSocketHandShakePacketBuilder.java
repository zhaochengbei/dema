package org.bei.dema.websocket;

import org.bei.dema.http.HttpMethodType;
import org.bei.dema.http.HttpRequest;
import org.bei.dema.http.HttpResponse;

/**
 * author：zhaochengbei
 * date：2017/6/26
*/
public class WebSocketHandShakePacketBuilder {
	/**
	 * 
	 * @param host
	 * @return
	 */
	static public HttpRequest createHandShakeRequest(String host){
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.method = HttpMethodType.GET;
		httpRequest.connection = "Upgrade";
		httpRequest.host = host;
		httpRequest.otherHead.put("Origin", "null");
		httpRequest.otherHead.put("Sec-WebSocket-Extensions", "x-webkit-deflate-frame");
		httpRequest.otherHead.put("Sec-WebSocket-Key", "puVOuWb7rel6z2AVZBKnfw==");
		httpRequest.otherHead.put("Sec-WebSocket-Version", "13");
		httpRequest.otherHead.put("Upgrade", "websocket");
		return httpRequest;
	}
	/**
	 * 
	 * @param secWebSocketAccept
	 * @return
	 */
	static public HttpResponse createHandShakeResponse(String secWebSocketAccept){
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.status = 101;
		httpResponse.phrase = "Switching Protocols";
		httpResponse.connection = "Upgrade";
		httpResponse.server = "dema websocket server";
		httpResponse.otherHead.put("Upgrade", "WebSocket");
		httpResponse.otherHead.put("Access-Control-Allow-Credentials", "true");
		httpResponse.otherHead.put("Access-Control-Allow-Headers", "content-type");
		httpResponse.otherHead.put("Sec-WebSocket-Accept", secWebSocketAccept);
		return httpResponse;
	}
}
