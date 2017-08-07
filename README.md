general:<br/>
dema is a set of java network library，strong，high-performance and easy to use. it incloud dema-tcp and dema-http and dema-websocket three nio network server used librarys and a io network client use socketpool library.

nio librarys compare with netty:<br/>
when identical connection and packet send/receive,only pay 1/4 memory and 50% cpu,and not delay not leak.

dema-tcp simple:<br/>

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


dema-http simple:<br/>

	/**
	 * 
	 */
	static private HttpServer httpServer = new HttpServer();
	/**
	 * 
	 */
	static private HttpServerHandler httpServerHandler = new HttpServerHandler() {

		public void onAccept(HttpContext context) {
			
		}
		public void onHttpRequest(HttpRequest request, HttpContext context){
			HttpResponse response = new HttpResponse();
			response.status = HttpResponseStatus.OK;
			response.phrase = HttpResponseStatus.phraseMap.get(response.status);
			response.content = "hello world!".getBytes();
			context.write(response);
			context.close(HttpResponseStatus.phraseMap.get(HttpResponseStatus.OK));
		}
		public void onReadIdle(HttpContext context) {
			context.close(TcpConnectionCloseReason.ReadIdleTimeOut);
		}
		public void onClose(HttpContext context, String reason) {
			
		}
	};
	static public void main(String[] args){
		try {
			httpServer.config(1000, 10);
			httpServer.start(8080, httpServerHandler);
			System.out.println("server started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	    
dema-websocket simple:<br/>

	/**
	 * 
	 */
	static private WebSocketServer webSocketServer = new WebSocketServer();
	/**
	 * 
	 */
	static private WebSocketHandler webSocketHandler = new WebSocketHandler() {

		public void onAccpet(WebSocketConnection webSocketContext) {
			System.out.println("s_onAccpet");
		}

		public void onUpgrade(HttpRequest httpRequest, HttpResponse httpResponse,
				WebSocketConnection webSocketConnection) {
			System.out.println("s_onUpgrade");
			webSocketConnection.write(httpResponse);
			
		}
		public void onFrame(WebSocketConnection webSocketConnection, WebSocketFrame frame) {
			System.out.println("s_onFrame,"+frame);
			if(frame.opcode == WebSocketOpcode.CLOSE){
				webSocketConnection.close();
			}else if(frame.opcode == WebSocketOpcode.TEXT_MSG){
				// down data cannot have mark
				frame.hasMask = false;
				frame.mark = null;
				System.out.println("send frame="+frame);
				webSocketConnection.send(frame);
			}
		}
		
		public void onClose(WebSocketConnection webSocketContext) {
			System.out.println("s_onClose");
		}

		public void onReadIdle(WebSocketConnection webSocketConnection) {
			webSocketConnection.close();
		}
		
	};
	/**
	 * 
	 */
	static public void main(String args[]){
		try {
			webSocketServer.config(1000, 10);
			webSocketServer.start(8090, webSocketHandler);
			System.out.println("server started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

dema-tcp-socketpool simple:<br/>
	
	/**
	 * 
	 */
	static private DemaSocketPool demaSocketPool = new DemaSocketPool();
	static private ExecutorService executorService = Executors.newFixedThreadPool(1,new ThreadFactory() {
		public int threadIndex = 0;
		public Thread newThread(Runnable r) {
			return new Thread(r, "callServerTask"+threadIndex);
		}
	} );
	/**
	 * 
	 */
	static public void main(String[] args){
		demaSocketPool.init("localhost", 9090, 1, 3000);
		//use socketpool and threadpool call server
		for (int i = 0; i < 100; i++) {
			CallServerTask callServerTask = new CallServerTask();
			callServerTask.demaSocketPool = demaSocketPool;
			executorService.execute(callServerTask);
		}
		executorService.shutdown();
	}
