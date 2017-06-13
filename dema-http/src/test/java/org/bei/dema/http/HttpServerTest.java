package org.bei.dema.http;

import java.io.IOException;

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
		
		public void onHttpRequest(HttpRequest request, HttpContext context){
			// TODO Auto-generated method stub
//			System.out.println("onRequest="+request);
			HttpResponse response = new HttpResponse();
			response.status = HttpResponseStatus.OK;
			response.phrase = HttpResponseStatusPhrase.map.get(response.status);
			response.content = "hello world!".getBytes();
			context.write(response);
			context.close();
		}
	};
	static public void main(String[] args){
		try {
			httpServer.config(1000, 10);
			httpServer.start(8090, httpHandler);
			System.out.println("server started");
//			while(true){
//				Thread.sleep(10);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
