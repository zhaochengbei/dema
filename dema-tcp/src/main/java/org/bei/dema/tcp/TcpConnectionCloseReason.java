package org.bei.dema.tcp;
/**
 * author：zhaochengbei
 * date：2017/6/2
*/
public class TcpConnectionCloseReason {

	static public final String ExceedMaxConnectionCount ="ExceedMaxConnectionCount";
	static public final String ReadError ="ReadError";
	static public final String WriteError ="WriteError";
	static public final String ReadIdleTimeOut ="ReadIdleTimeOut";
	static public final String ShutDownTcpServer ="ShutDownTcpServer";
	static public final String OtherError ="OtherError";
	static public final String NormalActiveClose ="NormalActive";
	
}
