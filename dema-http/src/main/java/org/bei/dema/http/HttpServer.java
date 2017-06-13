package org.bei.dema.http;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.print.DocFlavor.READER;

import org.bei.dema.http.HttpParseException;
import org.bei.dema.tcp.IoHandler;
import org.bei.dema.tcp.TcpConnection;
import org.bei.dema.tcp.TcpServer;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpServer {
	/**
	 * 
	 */
	private TcpServer tcpServer = new TcpServer();
	/**
	 * 
	 */
	private IoHandler ioHandler = new IoHandler() {
		
		public void onRead(TcpConnection connection){
			//创建请求对象；
			if(connection.packet == null){
				connection.packet = new HttpRequest();
			}
			HttpRequest request = (HttpRequest)connection.packet;
			//读出新到的数据 
			ByteBuffer byteBuffer = ByteBuffer.allocate(connection.available());
			connection.read(byteBuffer);
			byteBuffer.flip();
			try {
				HttpRequest request2 = HttpSerializeUtils.deSerialize(byteBuffer, request);				//判断是否解析完成
				if(request2 != null){
					HttpContext context = new HttpContext();
					context.connection = connection;
					context.request = request;
					httpHandler.onHttpRequest(request, context);
				}
			} catch (HttpParseException e) {
				e.printStackTrace();
				sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
			}
		}
		
		private void sendErrorAndCloseConnection(TcpConnection connection,int responseStatus){
    		HttpResponse response = new HttpResponse();
    		response.status = HttpResponseStatus.BAD_REQUEST;
    		response.phrase = HttpResponseStatusPhrase.map.get(response.status);
    		response.content = ("errorcode="+response.phrase).getBytes();
    		HttpConnectionUtils.writeHttpResponse(connection, response,null);
    		connection.close(response.phrase);
		}
		
		public void onClose(TcpConnection connection, String reason){
			//do nothing;
			
		}
		
		public void onAccept(TcpConnection connection){
			//do nothing
		}
	};
	/**
	 * 
	 */
	private HttpHandler httpHandler;
	/**
	 * 
	 */
	public void config(int maxConnectionCount,int readIdleTimeoutSeconds) throws IOException{
		tcpServer.config(maxConnectionCount, readIdleTimeoutSeconds);
	}
	
	/**
	 * 
	 */
	public void start(int port,HttpHandler httpHandler) throws IOException{
		this.httpHandler = httpHandler;
//		tcpServer.configCheckGap(10, 10, 1);
		tcpServer.start(port, ioHandler);
	}
	/**
	 * 
	 */
	public void shutdown() throws IOException{
		tcpServer.shutdown();
	}
}
