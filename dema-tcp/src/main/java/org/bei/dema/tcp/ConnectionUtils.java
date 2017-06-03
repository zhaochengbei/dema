package org.bei.dema.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author：bei
 * date：2017/5/25
*/
public class ConnectionUtils {
	
	/**
	 * the length is length of packet,if not you can use lengthAdjustment adjust;
	 * 
	 * @param connection
	 * @param lengthFieldOffset
	 * @param lengthOfLength ,can be 1,2,4; 
	 * @param lengthAdjustment
	 * @param maxFrameLength
	 * @return next packet from connection
	 * @throws Exception
	 */
	static public ByteBuffer readPacket(TcpConnection connection,int lengthFieldOffset,int lengthOfLength,int lengthAdjustment,int maxFrameLength) throws IOException,TcpException{
		//if length has not read ,value of packetlength is -1;
		if(connection.packetLength == -1){
			if(connection.byteBuffer == null){
				connection.byteBuffer = ByteBuffer.allocate(lengthFieldOffset+lengthOfLength);
			} 
			connection.read(connection.byteBuffer);
			if(connection.byteBuffer.remaining() != 0){
				return null;
			}
			//read value of length
			connection.byteBuffer.flip();
			connection.byteBuffer.position(lengthFieldOffset);
			switch(lengthOfLength){
			case 1:
				connection.packetLength = connection.byteBuffer.get();
				break;
			case 2:
				connection.packetLength = connection.byteBuffer.getShort();
				break;
			case 4:
				connection.packetLength = connection.byteBuffer.getInt();
				break;
			default:
				throw new TcpException("lengthOfLength only can be 1,2,4");
			}
			//check value of length
			if(connection.packetLength > maxFrameLength){
				throw new TcpException("packet length out of limit");
			}
			//create a new buffer with lengh of packet 
			ByteBuffer newByteBuffer = ByteBuffer.allocate(connection.packetLength+lengthAdjustment);
			connection.byteBuffer.flip();
			newByteBuffer.put(connection.byteBuffer);
			connection.byteBuffer.clear();
			connection.byteBuffer = newByteBuffer;
		}
		//if length of packet has read
		if(connection.packetLength != -1){
			connection.read(connection.byteBuffer);
			
			if(connection.byteBuffer.remaining() != 0){
				return null;
			}
			
			ByteBuffer result = connection.byteBuffer;
			connection.byteBuffer = null;
			connection.packetLength = -1;
			return result;
		}
		return null;
	}

}
