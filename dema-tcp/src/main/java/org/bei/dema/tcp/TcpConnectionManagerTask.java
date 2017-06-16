package org.bei.dema.tcp;


/**
 * author：zhaochengbei
 * date：2017/5/31
*/
public class TcpConnectionManagerTask implements Runnable {
	/**
	 * 
	 */
	public int type;
	/**
	 * 
	 */
	public TcpConnection connection;
	/**
	 * 
	 */
	public IoHandler ioHandler;
	/**
	 * 
	 */
	public TcpConnectionManagerTask(int type,TcpConnection connection,IoHandler ioHandler){
		this.type = type;
		this.connection = connection;
		this.ioHandler = ioHandler;
	}
	/**
	 * 
	 */
	public void run() {
		try {
			switch(type){
			case TcpConnectionManagerTaskType.ACCEPT:
				ioHandler.onAccept(this.connection);
				break;
			case TcpConnectionManagerTaskType.READ:
				ioHandler.onRead(this.connection);
				break;
			case TcpConnectionManagerTaskType.CLOSE:
				ioHandler.onClose(this.connection,this.connection.closeReason);
				break;
			default:
				break;		
			}	
			//mark not in reading status
			if(type == TcpConnectionManagerTaskType.READ){
				connection.inReading = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
}
