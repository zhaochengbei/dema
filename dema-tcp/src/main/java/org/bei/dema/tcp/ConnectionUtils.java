package org.bei.dema.tcp;

import java.nio.ByteBuffer;

/**
 * author：zhaochengbei
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
	 * @throws TcpException 
	 */
	static public ByteBuffer readPacket(TcpConnection connection,int lengthFieldOffset,int lengthOfLength,int lengthAdjustment,int maxFrameLength) throws TcpException{
		//if length has not read ,value of packetlength is -1;
		if(connection.packet == null){
			connection.packet = new TcpPacket();
		}
		TcpPacket packet = (TcpPacket)connection.packet;
	
		if(packet.packetLength == -1){
			if(packet.byteBuffer == null){
				packet.byteBuffer = ByteBuffer.allocate(lengthFieldOffset+lengthOfLength);
			} 
			connection.read(packet.byteBuffer);
			if(packet.byteBuffer.remaining() != 0){
				return null;
			}
			//read value of length
			packet.byteBuffer.flip();
			packet.byteBuffer.position(lengthFieldOffset);
			switch(lengthOfLength){
			case 1:
				packet.packetLength = packet.byteBuffer.get();
				break;
			case 2:
				packet.packetLength = packet.byteBuffer.getShort();
				break;
			case 4:
				packet.packetLength = packet.byteBuffer.getInt();
				break;
			default:
				throw new TcpException("lengthOfLength only can be 1,2,4");
			}
			//check value of length
			if(packet.packetLength > maxFrameLength){
				throw new TcpException("packet length out of limit");
			}
			//create a new buffer with lengh of packet 
			ByteBuffer newByteBuffer = ByteBuffer.allocate(packet.packetLength+lengthAdjustment);
			packet.byteBuffer.flip();
			newByteBuffer.put(packet.byteBuffer);
			packet.byteBuffer.clear();
			packet.byteBuffer = newByteBuffer;
		}
		//if length of packet has read
		if(packet.packetLength != -1){
			connection.read(packet.byteBuffer);
			
			if(packet.byteBuffer.remaining() != 0){
				return null;
			}
			
			ByteBuffer result = packet.byteBuffer;
			packet.byteBuffer = null;
			packet.packetLength = -1;
			connection.packet = null;
			return result;
		}
		return null;
	}

}
