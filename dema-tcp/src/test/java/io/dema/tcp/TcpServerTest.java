package io.dema.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.SocketChannel;

import javax.swing.plaf.SliderUI;

import io.dema.tcp.ConnectionUtils;
import io.dema.tcp.IoHandler;
import io.dema.tcp.TcpConnection;
import io.dema.tcp.TcpException;
import io.dema.tcp.TcpServer;


/**
 * Hello world!
 *
 */
public class TcpServerTest 
{

	/**
	 * 
	 */
	static private TcpServer tcpServer = new TcpServer();
	/**
	 * 
	 */
	static private IoHandler ioHandler = new IoHandler() {
		
		public void onAccept(TcpConnection connection){
			System.out.println("s_onAccept,"+connection.socket);
		}
		public void onRead(TcpConnection connection){

			while(true){
				System.out.println("s_onRead,"+connection.socket);
				ByteBuffer data = null;
				try {
					data = ConnectionUtils.readPacket(connection, 0,4, 4,256);
				} catch (TcpException e) {
					e.printStackTrace();
				}
				if(data == null){
					break;
				}
				data.flip();
				connection.writeAndFlush(data);
			}	
		}
		public void onReadIdle(TcpConnection connection) {
			connection.close(TcpConnectionCloseReason.ReadIdleTimeOut);
		}
		public void onClose(TcpConnection connection,String reason){
			System.out.println("s_onClose,reason="+connection.closeReason+","+connection.socket);
		}
	};
	
	
    public static void main( String[] args )
    {
    	try {
    		tcpServer.config(100000,12);//100 000 is max connection count ,12 is read idle timeout time unit is second
    		tcpServer.start(9090, ioHandler);
            System.out.println( "server started" );        	
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
