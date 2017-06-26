package io.dema.websocket;
/**
 * author：zhaochengbei
 * date：2017/6/22
*/
public class WebSocketOpcode {

	/**
	 * chrome only not support byte_msg
	 */
	static public int CONNECT = 0;
	static public int TEXT_MSG = 1;
	static public int BYTE_MSG = 2;
	static public int CLOSE = 8;
	static public int PING = 9;
	static public int PANG = 10;
}
