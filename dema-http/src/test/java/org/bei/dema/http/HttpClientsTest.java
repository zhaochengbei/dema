package org.bei.dema.http;

import java.nio.ByteBuffer;

import org.bei.dema.tcp.IoHandler;
import org.bei.dema.tcp.TcpClients;
import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/12
*/
public class HttpClientsTest {
	/**
	 * 
	 */
	static private TcpClients tcpClients = new TcpClients();
	
	static private IoHandler ioHandler = new IoHandler() {
		private int count = 0;
		public void onRead(TcpConnection connection){
//			System.out.println("c_read");
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
				connection.close(HttpResponseStatusPhrase.map.get(HttpResponseStatus.BAD_REQUEST));
//				e.printStackTrace();
//				
			}
			if(result != null){
//				System.out.println(result.toString());
				connection.close("");
			}
		}
		
		public void onClose(TcpConnection connection, String reason)  {
//			System.out.println("c_close");
		}
		
		public void onAccept(TcpConnection connection)  {
//			System.out.println("c_accept");
			//发送一个请求;
			HttpRequest request = new HttpRequest();
			request.method = HttpMethodType.GET;
			request.uri = "/";
			request.version = HttpVersion.version1_1;
			request.host = "localhost";
//			System.out.println(request);
			ByteBuffer byteBuffer = HttpSerializeUtils.serialize(request);
			byteBuffer.flip();
			connection.writeAndFlush(byteBuffer);
			System.out.println("totalReqeustCount :"+(++count)+",inRequestingConnection="+tcpClients.getConnections().size());
		}
	};
	
	static public void main(String[] args){
		try {
			tcpClients.start("localhost", 8090, 10, 1000, ioHandler);
//			while(true){
//				System.out.println("connection count:"+tcpClients.getConnections().size());
//				Thread.sleep(1);
//			}
//			httpServer.config(1000, 0);
//			httpServer.start(8090, httpHandler);
//			System.out.println("server started");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
