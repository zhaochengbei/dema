package io.dema.http;

import java.io.IOException;

import io.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public interface HttpHandler {

	/**
	 * 
	 * @param context
	 */
	public void onAccept(HttpContext context);

	/**
	 * 
	 * @param context
	 * @param reason
	 */
	public void onReadIdle(HttpContext context);
	/**
	 * 
	 * @param context
	 * @param reason
	 */
	public void onClose(HttpContext context,String reason);
	
}
