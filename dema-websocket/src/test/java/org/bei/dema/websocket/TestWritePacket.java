package org.bei.dema.websocket;
/**
 * author：zhaochengbei
 * date：2017年6月22日
*/
public class TestWritePacket {

	/**
	 * 
	 * @param args
	 */
	static public void main(String args[]){
		WebSocketFrame packet = new WebSocketFrame();
		WebSocketSerializeUtils.serialize(packet);
	}
}
