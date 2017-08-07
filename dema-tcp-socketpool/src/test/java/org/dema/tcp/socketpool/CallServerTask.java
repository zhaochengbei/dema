package org.dema.tcp.socketpool;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * author：zhaochengbei
 * date：2017年8月4日
*/
public class CallServerTask implements Runnable {
	static public int index =0;
	/**
	 * 
	 */
	public DemaSocketPool demaSocketPool;
	/**
	 * 
	 */
	public void run() {
		// TODO Auto-generated method stub
		DemaSocket demaSocket = null;
		try {
			long time = System.currentTimeMillis();
	    	ByteBuffer buffer = ByteBuffer.allocate(5);
			buffer.putInt(1);
			buffer.put(Byte.valueOf("1"));
			demaSocket = demaSocketPool.getNotInUseSocket();
			if((new Random()).nextInt(2)>1){
				demaSocket.close(DemaSocketCloseReason.WriteError);
			}
			
			buffer.flip();
			demaSocket.writeAndFlush(buffer);
			ByteBuffer buffer2 = ByteBuffer.allocate(5);
			demaSocket.read(buffer2);
			index++;
			System.out.println("call complete,cost time="+(System.currentTimeMillis()-time)+"index="+index);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(demaSocket != null){
				demaSocketPool.laybackSocket(demaSocket);
			}
		}
		
	}

}
