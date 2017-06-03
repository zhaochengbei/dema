package org.bei.dema.tcp;


/**
 * author：bei
 * date：2017/5/31
*/
public class TcpConncetionManagerTask implements Runnable {
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
	public TcpConncetionManagerTask(int type,TcpConnection connection,IoHandler ioHandler){
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
		} catch (Exception e) {
			e.printStackTrace();
			try {
				this.connection.close(TcpConnectionCloseReason.OtherError);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		//mark not in reading status
		if(type == TcpConnectionManagerTaskType.READ){
			connection.inReading = false;
		}
		
	}
	
}
