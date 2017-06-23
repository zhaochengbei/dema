package org.bei.dema.websocket;

import java.nio.ByteBuffer;

import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/22
*/
public class WebSocketSerializeUtils {

	/**
	 * 
	 */
	static public ByteBuffer serialize(WebSocketFrame frame){
		//general head
		byte[] head = new byte[2];
		head[0] = frame.isEof ==true?(byte)(1<<7):(byte)(0<<7);
		head[0] = (byte)(head[0]|frame.opcode);
		head[1] = frame.hasMask ==true?(byte)(1<<7):(byte)(0<<7);
		//if length not exceed 125
		byte[] lengthBytes = new byte[0];
		frame.packageLength = frame.data==null?0:frame.data.length;
		if(frame.packageLength<=125){
			head[1] = (byte)(head[1]|frame.packageLength);
		}else if(frame.packageLength <= (0xffff)){
			head[1] = (byte)(head[1]|126);
			lengthBytes = new byte[2];
			lengthBytes[0] = (byte)(frame.packageLength|0xff00);
			lengthBytes[1] = (byte)(frame.packageLength|0xff);
		}else if(frame.packageLength <= 0xffffffff){
			head[1] = (byte)(head[1]|127);
			lengthBytes = new byte[4];
			lengthBytes[0] = (byte)(frame.packageLength|0xff000000);
			lengthBytes[1] = (byte)(frame.packageLength|0xff0000);
			lengthBytes[2] = (byte)(frame.packageLength|0xff00);
			lengthBytes[3] = (byte)(frame.packageLength|0xff);
		}
		//process mark
		byte[] mark = frame.mark;
		if(mark == null){
			mark = new byte[0];
		}
		/**
		 * a^b=c <=> c^b=a；
		 */
		byte[] data = frame.data;
		if(data == null){
			data = new byte[0];
		}
		if(frame.hasMask){
			//use mark xor
	        for (int i = 0; i < frame.data.length; i++){
	            data[i] = (byte)(frame.data[i] ^ frame.mark[i % 4]);
	        }
		}
		//build bytebuffer
		ByteBuffer byteBuffer = ByteBuffer.allocate(head.length+lengthBytes.length+mark.length+data.length);
		byteBuffer.put(head);
		byteBuffer.put(lengthBytes);
		byteBuffer.put(mark);
		byteBuffer.put(data);
		return byteBuffer;
	}
	/**
	 * 
	 */
	static public WebSocketFrame deSerialize(WebSocketFrame frame,TcpConnection connection){

		if(frame.parseProcess == WebSocketParseProcess.HEAD){
			if(frame.byteBuffer == null){
				frame.byteBuffer = ByteBuffer.allocate(2);
			} 
			connection.read(frame.byteBuffer);
			if(frame.byteBuffer.remaining() != 0){
				return null;
			}
			//parse start；
			frame.byteBuffer.flip();
			frame.isEof = ((frame.byteBuffer.get(0)&0xFF)>>>7)>0;
			frame.opcode = frame.byteBuffer.get(0)&0xF;//仅保留低四位置
			frame.hasMask = ((frame.byteBuffer.get(1)&0xFF)>>>7)>0;
			frame.packageLength = frame.byteBuffer.get(1)&0x7F;
			frame.byteBuffer = null;
			if(frame.packageLength < 126){
				frame.parseProcess = WebSocketParseProcess.MARK;
			}else{
				frame.parseProcess = WebSocketParseProcess.LENGTH;
			}
		}
		if(frame.parseProcess == WebSocketParseProcess.LENGTH&&frame.packageLength == 126){
			if(frame.byteBuffer == null){
				frame.byteBuffer = ByteBuffer.allocate(2);
			}
			connection.read(frame.byteBuffer);
			if(frame.byteBuffer.remaining() != 0){
				return null;
			}
			frame.packageLength = frame.byteBuffer.getShort()&0xFFFF;
			frame.parseProcess = WebSocketParseProcess.MARK;
			frame.byteBuffer = null;
		}
		if(frame.parseProcess == WebSocketParseProcess.LENGTH&&frame.packageLength == 127){
			if(frame.byteBuffer == null){
				frame.byteBuffer = ByteBuffer.allocate(8);
			}
			connection.read(frame.byteBuffer);
			if(frame.byteBuffer.remaining() != 0){
				return null;
			}
			frame.packageLength = frame.byteBuffer.getLong()&0xffffffff;//取出8字节，然后求或运算；
			frame.byteBuffer = null;
			frame.parseProcess = WebSocketParseProcess.MARK;
		}
		if(frame.parseProcess == WebSocketParseProcess.MARK){
			//if has not mark skip
			if(frame.hasMask == false){
				frame.parseProcess = WebSocketParseProcess.DATA;
			}
		}
		if(frame.parseProcess == WebSocketParseProcess.MARK){
			if(frame.byteBuffer == null){
				frame.byteBuffer = ByteBuffer.allocate(4);
			}
			connection.read(frame.byteBuffer);
			if(frame.byteBuffer.remaining() != 0){
				return null;
			}
			frame.mark = frame.byteBuffer.array();
			frame.byteBuffer = null;
			frame.parseProcess = WebSocketParseProcess.DATA;
		}
		if(frame.parseProcess == WebSocketParseProcess.DATA){
			if(frame.byteBuffer == null){
				/**
				 * rank of value of int is -2.G~+2.1G
				 */
				frame.byteBuffer = ByteBuffer.allocate((int)frame.packageLength);
			}
			connection.read(frame.byteBuffer);
			if(frame.byteBuffer.remaining() != 0){
				return null;
			}
			frame.data = frame.byteBuffer.array();
			frame.byteBuffer = null;
			if(frame.hasMask){
				
		        for (int i = 0; i < frame.data.length; i++){
		            frame.data[i] = (byte)(frame.data[i] ^ frame.mark[i % 4]);
		        }
			}
			return frame;
			
		}
		return null;
	}
}
