package org.bei.dema.tcp;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.plaf.SliderUI;

import org.bei.dema.tcp.ConnectionUtils;
import org.bei.dema.tcp.IoHandler;
import org.bei.dema.tcp.TcpClients;
import org.bei.dema.tcp.TcpConnection;

/**
 * author：zhaochengbei
 * date：2017/5/25
*/

public class TcpClientsTest {
	static private IoHandler ioHandler = new IoHandler() {
		
		public void onRead(TcpConnection connection){
			try {
				ByteBuffer data = ConnectionUtils.readPacket(connection, 0,4, 4,256);
//				ByteBuffer byteBuffer = getTestPacket();
				System.out.println("c_onRead,"+connection.socket);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void onClose(TcpConnection connection,String reason) {
			System.out.println("c_onClose,"+connection.socket);
			
		}
		
		public void onAccept(TcpConnection connection) {
			System.out.println("c_onAccept,"+connection.socket);
		}
	};
	
	
	static private TcpClients tcpClients;
	/**
	 * 负责发送数据的线程；
	 */
	static private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),new ThreadFactory() {
		public int threadIndex = 0;
		public Thread newThread(Runnable r) {
			return new Thread(r, "sendTestData"+threadIndex);
		}
	} );
	
    public static void main( String[] args )
    {
    	try {
    		int testCount = 10;
    		while(testCount-- >0){
    			testClients();
    			Thread.sleep(20*1000);
        		tcpClients.shutdown();
    		}
    		executorService.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println( "Hello World!" );
    }
    static public void testClients()throws Exception{
    	tcpClients = new TcpClients();
		tcpClients.start("localhost", 9090, 5000, 1*TimeUnit.MILLISECONDS.ordinal(), ioHandler);
		/** 
		 * 给线程分配任务，让线程发送消息给服务器；
		 */
		int writeCount = 20000;
		while(writeCount -- >0){
			Vector<TcpConnection> connections = tcpClients.getConnections();
			System.out.println("last connection "+connections.size());
			long time = System.currentTimeMillis();
			for (int i = 0; i < connections.size(); i++) {
				TcpConnection connection = (TcpConnection) connections.get(i);
				if(time - connection.lastWriteTime> 1000){
						ByteBuffer byteBuffer = getTestPacket();
					WriteTask dataTask = new WriteTask();
					dataTask.connection = connection;
					byteBuffer.flip();
					dataTask.data = byteBuffer;
					executorService.execute(dataTask);
					connection.lastWriteTime = System.currentTimeMillis();
				}
			}
			Thread.sleep(1);
		}
//		tcpClients.shutdown();
    }
    static public ByteBuffer getTestPacket(){
    	ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(1);
		buffer.put(Byte.valueOf("1"));
//		buffer.flip();
		return buffer;
    }
}
