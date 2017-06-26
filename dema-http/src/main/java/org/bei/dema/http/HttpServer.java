package org.bei.dema.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private Map<TcpConnection, HttpContext> httpContexts = new ConcurrentHashMap<TcpConnection, HttpContext>();
	/**
	 * 
	 */
	private IoHandler ioHandler = new IoHandler() {
		
		public void onRead(TcpConnection connection){
			//create packet if not create before
			if(connection.packet == null){
				connection.packet = new HttpRequest();
			}
			HttpRequest request = (HttpRequest)connection.packet;
			//read all data from connection 
			ByteBuffer byteBuffer = ByteBuffer.allocate(connection.available());
			connection.read(byteBuffer);
			byteBuffer.flip();
			try {
				HttpRequest request2 = HttpSerializeUtils.deSerialize(byteBuffer, request);				//判断是否解析完成
				if(request2 != null){
					HttpContext context = httpContexts.get(connection);
					httpHandler.onHttpRequest(request, context);
					connection.packet = null;
				}
			} catch (HttpParseException e) {
				e.printStackTrace();
				sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
			}
		}
		
		private void sendErrorAndCloseConnection(TcpConnection connection,int responseStatus){
    		HttpResponse response = new HttpResponse();
    		response.status = HttpResponseStatus.BAD_REQUEST;
    		response.phrase = HttpResponseStatus.phraseMap.get(response.status);
    		response.content = ("errorcode="+response.phrase).getBytes();
    		HttpConnectionUtils.writeHttpResponse(connection, response);
    		connection.close(response.phrase);
		}

		public void onAccept(TcpConnection connection){
			HttpContext context = new HttpContext();
			context.connection = connection;
			httpContexts.put(connection, context);
			httpHandler.onAccept(context);
		}
		public void onClose(TcpConnection connection, String reason){
			HttpContext context = httpContexts.remove(connection);
			httpHandler.onClose(context,reason);
		}
		
	};
	/**
	 * 
	 */
	private HttpHandler httpHandler;
	/**
	 * 
	 * @param maxConnectionCount
	 * @param readIdleTimeoutSeconds
	 * @throws IOException
	 */
	public void config(int maxConnectionCount,int readIdleTimeoutSeconds) throws IOException{
		tcpServer.config(maxConnectionCount, readIdleTimeoutSeconds);
	}
	
	/**
	 * 
	 * @param port
	 * @param httpHandler
	 * @throws IOException
	 */
	public void start(int port,HttpHandler httpHandler) throws IOException{
		this.httpHandler = httpHandler;
		tcpServer.start(port, ioHandler);
	}
	/**
	 * 
	 */
	public void shutdown() throws IOException{
		tcpServer.shutdown();
	}
}
