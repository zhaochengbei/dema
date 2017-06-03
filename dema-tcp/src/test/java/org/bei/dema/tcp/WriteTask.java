package org.bei.dema.tcp;

import java.nio.ByteBuffer;

/**
 * 作者：赵承北
 * 时间：2017年5月26日
*/
public class WriteTask implements Runnable {
	public TcpConnection connection;
	public ByteBuffer data;
	public void run() {
		try {
			connection.writeAndFlush(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
