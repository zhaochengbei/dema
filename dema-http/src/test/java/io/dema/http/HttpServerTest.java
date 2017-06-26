package io.dema.http;

import java.io.IOException;

import io.dema.http.HttpContext;
import io.dema.http.HttpHandler;
import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;
import io.dema.http.HttpResponseStatus;
import io.dema.http.HttpServer;
import io.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/8
*/
public class HttpServerTest {
	/**
	 * 
	 */
	static private HttpServer httpServer = new HttpServer();
	/**
	 * 
	 */
	static private HttpHandler httpHandler = new HttpHandler() {

		public void onAccept(HttpContext context) {
			
		}
		public void onHttpRequest(HttpRequest request, HttpContext context){
			HttpResponse response = new HttpResponse();
			response.status = HttpResponseStatus.OK;
			response.phrase = HttpResponseStatus.phraseMap.get(response.status);
			response.content = "hello world!".getBytes();
			context.write(response);
			context.close(HttpResponseStatus.phraseMap.get(HttpResponseStatus.OK));
		}
		public void onHttpResponse(HttpResponse httpResponse, HttpContext context) {
			
		}
		public void onClose(HttpContext context, String reason) {
			
		}
	};
	static public void main(String[] args){
		try {
			httpServer.config(1000, 10);
			httpServer.start(8080, httpHandler);
			System.out.println("server started");
//			while(true){
//				Thread.sleep(10);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
