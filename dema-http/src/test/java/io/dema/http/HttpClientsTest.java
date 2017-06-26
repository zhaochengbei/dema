package io.dema.http;

import java.nio.ByteBuffer;

import io.dema.http.HttpClients;
import io.dema.http.HttpContext;
import io.dema.http.HttpHandler;
import io.dema.http.HttpMethodType;
import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;
import io.dema.http.HttpVersion;
import io.dema.tcp.IoHandler;
import io.dema.tcp.TcpClients;
import io.dema.tcp.TcpConnection;
import io.dema.tcp.TcpConnectionCloseReason;

/**
 * author：zhaochengbei
 * date：2017/6/12
*/
public class HttpClientsTest {
	/**
	 * 
	 */
	static private HttpClients httpClients = new HttpClients();
	/**
	 * 
	 */
	static private int count=0;
	/**
	 * 
	 */
	static private HttpHandler httpHandler = new HttpHandler() {

		public void onAccept(HttpContext context) {
			// TODO Auto-generated method stub
			//发送一个请求;
			HttpRequest request = new HttpRequest();
			request.method = HttpMethodType.GET;
			request.uri = "/";
			request.version = HttpVersion.version1_1;
			request.host = "localhost";
			context.write(request);
			System.out.println("totalReqeustCount :"+(++count)+",inRequestingConnection="+httpClients.getConnections().size());
			
		}
		public void onHttpResponse(HttpResponse httpResponse, HttpContext context) {
			// TODO Auto-generated method stub
			if(httpResponse != null){
				context.close(TcpConnectionCloseReason.NormalActiveClose);
			}
//		}
		}
		
		public void onHttpRequest(HttpRequest request, HttpContext context) {
			// TODO Auto-generated method stub
			
		}

		public void onClose(HttpContext context, String reason) {
			// TODO Auto-generated method stub
			
		}
	};
	
	static public void main(String[] args){
		try {
			httpClients.start("localhost", 8080, 100000, 1, httpHandler);
			Thread.sleep(3000);
			httpClients.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
