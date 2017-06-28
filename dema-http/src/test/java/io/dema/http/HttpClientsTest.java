package io.dema.http;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

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
	static private  AtomicInteger count = new AtomicInteger();
	/**
	 * 
	 */
	static private HttpClientHandler httpClientHandler = new HttpClientHandler() {
		
		public void onAccept(HttpContext context) {
			HttpRequest request = new HttpRequest();
			request.method = HttpMethodType.GET;
			request.uri = "/";
			request.version = HttpVersion.version1_1;
			request.host = "localhost";
			context.write(request);
			count.getAndAdd(1);
			System.out.println("totalReqeustCount :"+(count)+",inRequestingConnection="+httpClients.getConnections().size());
			
		}
		
		public void onHttpResponse(HttpResponse httpResponse, HttpContext context) {
			if(httpResponse != null){
				context.close(TcpConnectionCloseReason.NormalActiveClose);
			}
		}
		public void onReadIdle(HttpContext context) {
			context.close(TcpConnectionCloseReason.ReadIdleTimeOut);
			
		}
		public void onClose(HttpContext context, String reason) {
			
		}

		
	};
	
	static public void main(String[] args){
		try {
			httpClients.start("localhost", 8080, 100000, 1, httpClientHandler);
			Thread.sleep(100000);
			httpClients.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
