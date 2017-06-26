package org.bei.dema.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.bei.dema.tcp.IoHandler;
import org.bei.dema.tcp.TcpClients;
import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/26
*/
public class HttpClients {

	/**
	 * 
	 */
	private TcpClients tcpClients = new TcpClients();
	/**
	 * 
	 */
	private Map<TcpConnection, HttpContext> httpContexts = new ConcurrentHashMap<TcpConnection, HttpContext>();
	/**
	 * 
	 */
	private IoHandler ioHandler = new IoHandler() {
		public void onRead(TcpConnection connection){
			if(connection.packet == null){
				connection.packet = new HttpResponse();
			}
			HttpResponse response = (HttpResponse)connection.packet;
			ByteBuffer byteBuffer = ByteBuffer.allocate(connection.available());
			connection.read(byteBuffer);
			byteBuffer.flip();
			HttpResponse result = null;
			
			try {
				result = HttpSerializeUtils.deSerialize(byteBuffer, response);
			} catch (HttpParseException e) {
				connection.close(HttpResponseStatus.phraseMap.get(HttpResponseStatus.BAD_REQUEST));//				
			}
			
			HttpContext context = httpContexts.get(connection);
			connection.packet = null;
			httpHandler.onHttpResponse(result, context);
		}
		
		public void onClose(TcpConnection connection, String reason)  {
			HttpContext context = httpContexts.remove(connection);
			httpHandler.onClose(context,reason);
		}
		
		public void onAccept(TcpConnection connection)  {
			HttpContext context = new HttpContext();
			context.connection = connection;
			httpContexts.put(connection, context);
			httpHandler.onAccept(context);
		}
	};

	/**
	 * 
	 */
	private HttpHandler httpHandler;
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param count
	 * @param createGap
	 * @param httpHandler
	 * @throws IOException
	 */
	public void start(String ip,int port,int count,int createGap,HttpHandler httpHandler) throws IOException {
		this.httpHandler = httpHandler;
		tcpClients.start(ip, port, count, createGap, ioHandler);
	}

	/**
	 * 
	 * @return
	 */
	public Vector<TcpConnection> getConnections(){
		return tcpClients.getConnections();
	}
	public void shutdown(){
		tcpClients.shutdown();
	}
}
