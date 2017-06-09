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

import org.bei.dema.tcp.ConnectionUtils;
import org.bei.dema.tcp.IoHandler;
import org.bei.dema.tcp.TcpConnection;
import org.bei.dema.tcp.TcpServer;


/**
 * Hello world!
 *
 */
public class TcpServerTest 
{

	static private IoHandler ioHandler = new IoHandler() {
		
		public void onAccept(TcpConnection connection) throws Exception{
			System.out.println("s_onAccept,"+connection.socket);
		}
		
		public void onRead(TcpConnection connection) throws Exception{

			while(true){
				System.out.println("s_onRead,"+connection.socket);
				ByteBuffer data = ConnectionUtils.readPacket(connection, 0,4, 4,256);
				if(data == null){
					break;
				}
				connection.writeAndFlush(data);
			}
			
		}
		
		public void onClose(TcpConnection connection,String reason) throws Exception{
			System.out.println("s_onClose,reason="+connection.closeReason+","+connection.socket);
		}
		
	};
	
	
	static private TcpServer tcpServer;
    public static void main( String[] args )
    {
    	try {
    		tcpServer = new TcpServer();
    		tcpServer.config(100000,1);
    		tcpServer.configCheckGap(1, 100, 100);
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
