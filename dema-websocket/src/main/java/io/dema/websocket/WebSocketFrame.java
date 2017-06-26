package io.dema.websocket;

import java.nio.ByteBuffer;

/**
 * author：zhaochengbei
 * date：2017/6/20
*/
public class WebSocketFrame {
	/**
	 * 
	 */
	public boolean isEof = true;
	/**
	 * 
	 */
	public int opcode = WebSocketOpcode.TEXT_MSG;
	/**
	 * 
	 */
	public boolean hasMask = false;
	/**
	 * 
	 */
	public long packageLength = -1;
	/**
	 * 
	 */
	public byte[] mark;
	/**
	 * 
	 */
	public byte[] data;
	/**
	 * 
	 */
	public int parseProcess = 0;
	/**
	 * 
	 */
	public  ByteBuffer byteBuffer;
	/**
	 * 
	 */
	@Override
	public String toString(){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("[");
		stringBuffer.append("isEof=");
		stringBuffer.append(isEof);
		stringBuffer.append(",type=");
		stringBuffer.append(opcode);
		stringBuffer.append(",hasMark=");
		stringBuffer.append(hasMask);
		stringBuffer.append(",packageLength=");
		stringBuffer.append(packageLength);
		stringBuffer.append(",mark=[");
		stringBuffer.append(HexUtils.bytesToHexString(mark));
		stringBuffer.append("],data=[");
		if(opcode == WebSocketOpcode.TEXT_MSG){
			stringBuffer.append(new String(data));
		}else{
			stringBuffer.append(HexUtils.bytesToHexString(data));
		}
		stringBuffer.append("]]");
		return stringBuffer.toString();
		
	}
}
