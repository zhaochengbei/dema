package org.bei.dema.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpConnectionUtils {
	/**
	 * 
	 */
	static public void writeHttpResponse(TcpConnection connection, HttpResponse response,HttpRequest request) throws IOException{
		// write response line
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("HTTP/1.1 ");
		stringBuffer.append(response.status.code);
		stringBuffer.append(" ");
		stringBuffer.append(response.status.phrase);
		stringBuffer.append("\n");
		//write head
		stringBuffer.append("Server: dema-http/1.0\n");
		stringBuffer.append("Date: ");
		stringBuffer.append((new Date()).toString());
		stringBuffer.append("\n");
		if(response.content!=null){
			if(response.contentType != null){
				stringBuffer.append("Content-Type: ");
				stringBuffer.append(response.contentType);
				stringBuffer.append("\n");
			}
			stringBuffer.append("Content-Length: ");
			stringBuffer.append(response.content.length);
			stringBuffer.append("\n");
		}
		//this will be logic server，so:
		//no cache ,no etag ,no last-modified
		stringBuffer.append("Cache-control: no-cache\n");
		//
		stringBuffer.append("Connection: ");
		stringBuffer.append(response.connection);
		stringBuffer.append("\n\n");
		//general bytes for write
		byte[] bytes = stringBuffer.toString().getBytes();
		ByteBuffer byteBuffer;
		if(request == null||request.method.equals(HttpMethodType.HEAD)==false){
			byteBuffer = ByteBuffer.allocate(bytes.length+response.content.length);
			byteBuffer.put(bytes);
			byteBuffer.put(response.content);
		}else{
			byteBuffer = ByteBuffer.allocate(bytes.length);
			byteBuffer.put(bytes);
		}
		connection.writeAndFlush(byteBuffer);
	}
}
