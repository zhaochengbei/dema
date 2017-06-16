package org.bei.dema.http;

import java.nio.ByteBuffer;
import java.util.Date;

import org.bei.dema.http.HttpParseException;

/**
 * author：zhaochengbei
 * date：2017/6/9
*/
public class HttpSerializeUtils {

	/**
	 * byte要进行整合计算就会设计ByteBuffer；
	 * nio使用byteBuffer，我们沿用了；
	 */
	static public ByteBuffer serialize(HttpRequest request){
		// write request line
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(request.method);
		stringBuffer.append(" ");
		stringBuffer.append(request.uri);
		stringBuffer.append(" ");
		stringBuffer.append("HTTP/1.1\n");
		//write head
		stringBuffer.append("Host: ");
		stringBuffer.append(request.host);
		stringBuffer.append("\n");
		stringBuffer.append("Connection: ");
		stringBuffer.append(request.connection);
		stringBuffer.append("\n");
		stringBuffer.append("User-Agent: ");
		stringBuffer.append("dema-http/1.0\n");
		if(request.content != null){
			stringBuffer.append("Content-Length: ");
			stringBuffer.append(request.content.length);
			stringBuffer.append("\n");
		}
		stringBuffer.append("\n");
		byte[] bytes = stringBuffer.toString().getBytes();
		ByteBuffer byteBuffer;
		if(request.content != null){
			byteBuffer = ByteBuffer.allocate(bytes.length+request.content.length);
			byteBuffer.put(bytes);
			byteBuffer.put(request.content);
		}else{
			byteBuffer = ByteBuffer.allocate(bytes.length);
			byteBuffer.put(bytes);
		}
		return byteBuffer;
	}
	/**
	 * 
	 */
	static public ByteBuffer serialize(HttpResponse response){
		// write response line
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("HTTP/1.1 ");
		stringBuffer.append(response.status);
		stringBuffer.append(" ");
		if(response.phrase == null){
			response.phrase = HttpResponseStatusPhrase.map.get(response.status);
		}
		stringBuffer.append(response.phrase);
		stringBuffer.append("\n");
		//write head
		stringBuffer.append("Server: dema-http/1.0\n");
		stringBuffer.append("Date: ");
		stringBuffer.append((new Date()).toString());
		stringBuffer.append("\n");
		if(response.content!=null){
			stringBuffer.append("Content-Type: ");
			stringBuffer.append(response.contentType);
			stringBuffer.append("\n");
			stringBuffer.append("Content-Length: ");
			stringBuffer.append(response.content.length);
			stringBuffer.append("\n");
		}
		//this will be logic server，so:
		//no cache ,no etag ,no last-modified
		stringBuffer.append("Cache-control: no-cache\n");
		stringBuffer.append("Connection: ");
		stringBuffer.append(response.connection);
		stringBuffer.append("\n\n");
		//general bytes for write
		byte[] bytes = stringBuffer.toString().getBytes();
		ByteBuffer byteBuffer;
		if(response.sendContent){
			byteBuffer = ByteBuffer.allocate(bytes.length+response.content.length);
			byteBuffer.put(bytes);
			byteBuffer.put(response.content);
		}else{
			byteBuffer = ByteBuffer.allocate(bytes.length);
			byteBuffer.put(bytes);
		}
		return byteBuffer;
	}
	/**
	 * 
	 */
	static public HttpRequest deSerialize(ByteBuffer byteBuffer,HttpRequest request)throws HttpParseException{
		//读出新到的数据 
//		byteBuffer.flip();
		// parse request line
		if(byteBuffer.hasRemaining() && request.parseStage == ParseStage.line){
			//逐个字节读取，写入缓冲，如果发现结束符，解析请求行；
			while(byteBuffer.hasRemaining()){
                int c=byteBuffer.get();  
                if (c=='\t'||c=='\n'||c==-1) {
                	//去掉window的换行符；
                	clearCRLF(request.stringBuffer);
                	//解析
                	String[] lineElements = request.stringBuffer.toString().split(" ");
                	if(lineElements.length !=3){
                		throw new HttpParseException("");
                	}
                	request.method = lineElements[0];
                	if(request.method.equals(HttpMethodType.GET) == false&&request.method.equals(HttpMethodType.HEAD) == false&&request.method.equals(HttpMethodType.POST) == false){
                		throw new HttpParseException("");
                	}
                	//if uri not legal , use part logic will process
                	request.uri = lineElements[1];
                	request.version = lineElements[2];
                	if(request.version.equals("HTTP/1.0")==false&&request.version.equals("HTTP/1.1")==false){
                		throw new HttpParseException("");
                	}
                	//clear buffer
                	request.stringBuffer.delete(0, request.stringBuffer.length());
                	request.parseStage = ParseStage.head;
                    break;  
                }  
                request.stringBuffer.append((char)c);  
            }  
		}
		return (HttpRequest) deSerialize(byteBuffer, (HttpPacket)request);
	}

	/**
	 * 
	 */
	static private void clearCRLF(StringBuffer stringBuffer){
		int index = stringBuffer.indexOf("\r"); 
		if(index != -1&&index ==stringBuffer.length()-1){
			stringBuffer.delete(stringBuffer.length()-1, stringBuffer.length());
		}
	}
	/**
	 * 解析发生错误，抛出异常;
	 * 解析未完成，返回null;
	 * 解析完成，返回HttpResponse;
	 * 
	 */
	static public HttpResponse deSerialize(ByteBuffer byteBuffer,HttpResponse response)throws HttpParseException{
		// parse request line
		boolean b = byteBuffer.hasRemaining();
		if(byteBuffer.hasRemaining() && response.parseStage == ParseStage.line){
			//逐个字节读取，写入缓冲，如果发现结束符，解析请求行；
			while(byteBuffer.hasRemaining()){
		    	int c=byteBuffer.get();  
		        if (c=='\t'||c=='\n'||c==-1) {
			        //去掉window的换行符；
			        clearCRLF(response.stringBuffer);
			        //解析
			        String responseLine = response.stringBuffer.toString();
			        String[] lineElements = responseLine.split(" ");
			        response.version = lineElements[0];
			        if(response.version.equals("HTTP/1.0")==false&&response.version.equals("HTTP/1.1")==false){
	            		throw new HttpParseException("");
	            	}
			        response.status = Integer.parseInt(lineElements[1]);
			        String[] phraseParts = new String[lineElements.length-2];
			        for (int i = 2; i < lineElements.length; i++) {
						phraseParts[i-2] = lineElements[i];
					}
	            	response.phrase = String.join(" ", phraseParts);
	            	
	            	//clear buffer
	            	response.stringBuffer.delete(0, response.stringBuffer.length());
	            	response.parseStage = ParseStage.head;
	                break;  
	            }  
	            response.stringBuffer.append((char)c);  
	        }  
		}
		return (HttpResponse) deSerialize(byteBuffer, (HttpPacket)response);
	}
	static public HttpPacket deSerialize(ByteBuffer byteBuffer,HttpPacket packet) throws HttpParseException{

		// parse requese head
		if(byteBuffer.hasRemaining() && packet.parseStage == ParseStage.head){
			while(byteBuffer.hasRemaining()){
                int c=byteBuffer.get();  
                if (c=='\t'||c=='\n'||c==-1) {  
                	//如果 stringbuffer是空的；
                	clearCRLF(packet.stringBuffer);
                	if(packet.stringBuffer.length() == 0){
                		if(packet instanceof HttpRequest &&((HttpRequest)packet).host == null){
                			throw new HttpParseException("");
                		}
                		if(packet.contentLength != 0){
                			packet.parseStage = ParseStage.content;
                		}else{
                			packet.parseStage = ParseStage.complete;
                		}
    	            	
                		break;
                	}
                	String head = packet.stringBuffer.toString();
                	packet.stringBuffer.delete(0, packet.stringBuffer.length());
                	
                	String[] keyAndValue = head.split(":");
                	String headKey = keyAndValue[0];
                	String headValue = keyAndValue[1].trim();
                	if(headKey.toLowerCase().equals("content-length")){
                		packet.contentLength = Integer.valueOf(headValue);
                	}else if(headKey.toLowerCase().equals("connection")){
                		packet.connection = headValue;
                	}else if(packet instanceof HttpRequest && headKey.toLowerCase().equals("host")){
                		((HttpRequest)packet).host = headValue;
                	}else {
                		packet.otherHead.put(headKey, headValue);
                	}  
                }else{
                	packet.stringBuffer.append((char)c);
                }
            } 
		}
//		if(packet.contentLength == 0){
//			
//		}
		// parse content if exist
		if(byteBuffer.hasRemaining()&&packet.parseStage == ParseStage.content && packet.contentLength != 0){
			if(byteBuffer.remaining() >= packet.contentLength){
				byte[] bytes = new byte[packet.contentLength];
				byteBuffer.get(bytes);
				packet.content = bytes;
				packet.parseStage = ParseStage.complete;
			}
		}
		
		
		//判断是否解析完成
		if(packet.parseStage == ParseStage.complete){
			return packet;
		}
		return null;
	}
}
