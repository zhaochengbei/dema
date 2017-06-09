package org.bei.dema.tcp;

import java.nio.ByteBuffer;

import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/5/26
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
