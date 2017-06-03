package org.bei.dema.tcp;


/**
 * author：bei
 * date：2017/5/22
*/
public interface IoHandler {

	/**
	 * 
	 */
	public void onAccept(TcpConnection connection) throws Exception;
	
	/**
	 * 
	 */
	public void onRead(TcpConnection connection) throws Exception;
	
	/**
	 * first remove connection from collection,second call the method;
	 */
	public void onClose(TcpConnection connection,String reason) throws Exception;
}
