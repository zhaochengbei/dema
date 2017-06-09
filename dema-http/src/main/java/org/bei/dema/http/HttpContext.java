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
	public void write(HttpResponse response) throws IOException{
		//
		this.response = response;
		
		HttpConnectionUtils.writeHttpResponse(connection, response,request);
	}
	/**
	 * 
	 */
	public void close()throws IOException{
		if(response != null){
			connection.close(response.status.phrase);
		}else{
			connection.close(HttpResponseStatus.INTERNAL_SERVER_ERROR.phrase);
		}
		
	}
}
