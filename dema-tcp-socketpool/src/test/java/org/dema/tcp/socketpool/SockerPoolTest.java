package org.dema.tcp.socketpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * author：zhaochengbei
 * date：2017/8/4
*/
public class SockerPoolTest {
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
		demaSocketPool.init("localhost", 9090, 20, 3000);
		//use socketpool and threadpool call server
		for (int i = 0; i < 5000; i++) {
			CallServerTask callServerTask = new CallServerTask();
			callServerTask.demaSocketPool = demaSocketPool;
			executorService.execute(callServerTask);
		}
		executorService.shutdown();
	}
}
