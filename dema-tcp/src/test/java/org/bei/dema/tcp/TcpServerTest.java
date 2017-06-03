package org.bei.dema.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.SocketChannel;

import javax.swing.plaf.SliderUI;


/**
 * Hello world!
 *
 */
public class TcpServerTest 
{

	static private IoHandler ioHandler = new IoHandler() {
		
		public void onRead(TcpConnection connection) throws Exception{

			System.out.println("s_onRead,"+connection.socket);
			ByteBuffer data = ConnectionUtils.readPacket(connection, 0,4, 4,256);
			ByteBuffer byteBuffer = getTestPacket();
			connection.writeAndFlush(byteBuffer);
		}
		
		public void onClose(TcpConnection connection,String reason) throws Exception{
			System.out.println("s_onClose,reason="+connection.closeReason+","+connection.socket);
		}
		
		public void onAccept(TcpConnection connection) throws Exception{
			System.out.println("s_onAccept,"+connection.socket);
		}
	};
	
	
	static private TcpServer tcpServer;
    public static void main( String[] args )
    {
    	try {
    		tcpServer = new TcpServer();
    		tcpServer.config(10000,5000);
    		tcpServer.start(9090, ioHandler);

            System.out.println( "Hello World!" );
//        	Thread.sleep(6000);
//        	tcpServer.shutdown();
        	
		} catch (Exception e) {
			e.printStackTrace();
		}
    	//10秒后关闭；
    }
    static public ByteBuffer getTestPacket(){
    	ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.putInt(1);
		buffer.put(Byte.valueOf("1"));
		return buffer;
    }
}
