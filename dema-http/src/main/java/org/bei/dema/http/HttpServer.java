package org.bei.dema.http;

import java.io.IOException;
import java.nio.ByteBuffer;

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
		
		public void onRead(TcpConnection connection) throws Exception {
			//创建请求对象；
			if(connection.packet == null){
				connection.packet = new HttpRequest();
			}
			HttpRequest request = (HttpRequest)connection.packet;
			//读出新到的数据 
			ByteBuffer byteBuffer = ByteBuffer.allocate(connection.available());
			connection.read(byteBuffer);
			byteBuffer.flip();
			// parse request line
			if(byteBuffer.hasRemaining() && request.method == null){
				//逐个字节读取，写入缓冲，如果发现结束符，解析请求行；
				while(byteBuffer.hasRemaining()){
	                int c=byteBuffer.get();  
	                if (c=='\t'||c=='\n'||c==-1) {
	                	//去掉window的换行符；
	                	clearCRLF(request.stringBuffer);
	                	//解析
	                	String[] lineElements = request.stringBuffer.toString().split(" ");
	                	if(lineElements.length !=3){
	                		sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
	                		return;
	                	}
	                	request.method = lineElements[0];
	                	if(request.method.equals(HttpMethodType.GET) == false&&request.method.equals(HttpMethodType.HEAD) == false&&request.method.equals(HttpMethodType.POST) == false){
	                		sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
	                		return;
	                	}
	                	//if uri not legal , use part logic will process
	                	request.uri = lineElements[1];
	                	request.version = lineElements[2];
	                	if(request.version.equals("HTTP/1.0")==false&&request.version.equals("HTTP/1.1")==false){
	                		sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
	                		return;
	                	}
	                	//clear buffer
	                	request.stringBuffer.delete(0, request.stringBuffer.length());
	                    break;  
	                }  
	                request.stringBuffer.append((char)c);  
	            }  
			}
			// parse requese head
			if(byteBuffer.hasRemaining() && request.headParseComplete == false){
				while(byteBuffer.hasRemaining()){
	                int c=byteBuffer.get();  
	                if (c=='\t'||c=='\n'||c==-1) {  
	                	//如果 stringbuffer是空的；
	                	clearCRLF(request.stringBuffer);
	                	if(request.stringBuffer.length() == 0){
	                		if(request.host == null){
	                			sendErrorAndCloseConnection(connection, HttpResponseStatus.BAD_REQUEST);
	                			return;
	                		}
	                		request.headParseComplete = true;
	                		break;
	                	}
	                	String head = request.stringBuffer.toString();
	                	request.stringBuffer.delete(0, request.stringBuffer.length());
	                	
	                	String[] keyAndValue = head.split(":");
	                	String headKey = keyAndValue[0];
	                	String headValue = keyAndValue[1];
	                	if(headKey.toLowerCase().equals("host")){
	                		request.host = headValue;
	                	}else if(headKey.toLowerCase().equals("content-length")){
	                		request.contentLength = Integer.valueOf(headValue);
	                	}else if(headKey.toLowerCase().equals("connection")){
	                		request.connection = headValue;
	                	}else {
	                		request.otherHead.put(headKey, headValue);
	                	}  
	                }else{
	                	request.stringBuffer.append((char)c);
	                }
	            } 
			}
			// parse content if exist
			if(byteBuffer.hasRemaining() && request.contentLength != 0){
				if(byteBuffer.remaining() >= request.contentLength){
					byte[] bytes = new byte[request.contentLength];
					byteBuffer.get(bytes);
					request.content = bytes;
				}
			}
			//判断是否解析完成
			if((request.contentLength != 0&&request.content != null)||(request.contentLength ==0 &&request.headParseComplete)){

				HttpContext context = new HttpContext();
				context.connection = connection;
				context.request = request;
				httpHandler.onHttpRequest(request, context);
//				connection.close(conn);
			}
				
			
		}
		
		private void sendErrorAndCloseConnection(TcpConnection connection,HttpResponseStatus responseStatus) throws IOException{
    		HttpResponse response = new HttpResponse();
    		response.status = HttpResponseStatus.BAD_REQUEST;
    		response.content = ("errorcode="+response.status.phrase).getBytes();
    		HttpConnectionUtils.writeHttpResponse(connection, response,null);
    		connection.close(HttpResponseStatus.BAD_REQUEST.phrase);
		}
		
		public void onClose(TcpConnection connection, String reason) throws Exception {
			//do nothing;
			
		}
		
		public void onAccept(TcpConnection connection) throws Exception {
			//do nothing
		}
	};
	/**
	 * 
	 */
	private void clearCRLF(StringBuffer stringBuffer){
		if(stringBuffer.indexOf("\r")==stringBuffer.length()-1){
			stringBuffer.delete(stringBuffer.length()-1, stringBuffer.length());
		}
	}
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
		tcpServer.start(port, ioHandler);
	}
	/**
	 * 
	 */
	public void shutdown() throws IOException{
		tcpServer.shutdown();
	}
}
