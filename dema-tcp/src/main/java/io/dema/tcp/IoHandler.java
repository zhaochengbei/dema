package io.dema.tcp;


/**
 * author：zhaochengbei
 * date：2017/5/22
*/
public interface IoHandler {

	/**
	 * 
	 */
	public void onAccept(TcpConnection connection);
	
	/**
	 * 
	 */
	public void onRead(TcpConnection connection);
	
	/**
	 * first remove connection from collections,second call the method;
	 */
	public void onClose(TcpConnection connection,String reason);
}
