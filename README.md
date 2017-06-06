general:<br/>
java not block io network framework，strong，high-performance and easy to use.

compare with netty:<br/>
when 40 000 tcp connection connect to server deploy in linux,per connection per 3 second send a packet and when server receive will send same packet to client,netty will cost 400m memory and 200%cpu，dema only cost 100m memory and 150%cpu。

simple:<br/>
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
	
tcpServer = new TcpServer();<br/>
tcpServer.config(100000,100*1000);//10 0000 is max connection count ,100*1000 is read idle timeout time unit is millsecond<br/>
tcpServer.start(9090, ioHandler);<br/>
