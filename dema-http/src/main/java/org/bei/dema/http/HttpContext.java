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
	 */
	public HttpRequest request;
	/**
	 * 
	 */
	private HttpResponse response;
	/**
	 * 
	 */
	public void write(HttpResponse response){
		//
		this.response = response;
		HttpConnectionUtils.writeHttpResponse(connection, response);
	}
	/**
	 * 
	 */
	public void close() {
		if(response != null){
			connection.close(response.phrase);
		}else{
			connection.close(HttpResponseStatusPhrase.map.get(HttpResponseStatus.INTERNAL_SERVER_ERROR));
		}
		
	}
}
