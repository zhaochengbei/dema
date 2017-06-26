package org.bei.dema.http;

import java.io.IOException;

import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpContext {
	/**
	 * 
	 */
	public TcpConnection connection;
	/**
	 * 
	 * @param request
	 */
	public void write(HttpRequest request){
		HttpConnectionUtils.writeHttpRequest(connection, request);
	}
	/**
	 * 
	 * @param response
	 */
	public void write(HttpResponse response){
		HttpConnectionUtils.writeHttpResponse(connection, response);
	}
	/**
	 * 
	 * @param closeReason
	 */
	public void close(String closeReason) {
		connection.close(closeReason);
	}
}
