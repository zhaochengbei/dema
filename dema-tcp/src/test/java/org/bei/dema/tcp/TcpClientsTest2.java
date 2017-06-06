package org.bei.dema.tcp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 作者：赵承北
 * 时间：2017年6月2日
*/
public class TcpClientsTest2 {

    public static void main( String[] args )
    {
    	//不断运行TcpClientTest
//    	while(true){
    		try {
    			Process p = Runtime.getRuntime().exec("java -cp target/test-classes;target/classes org.bei.dema.tcp.TcpClientsTest");
    			//取得命令结果的输出流    
                InputStream fis=p.getErrorStream();    
               //用一个读输出流类去读    
                InputStreamReader isr=new InputStreamReader(fis,"gbk");    
               //用缓冲器读行    
                BufferedReader br=new BufferedReader(isr);    
                String line=null;    
               //直到读完为止    
               while((line=br.readLine())!=null)    
                {    
                    System.out.println(line);    
                }   
    			Thread.sleep(10000);
    			p.destroyForcibly();
    			
			} catch (Exception e) {
				e.printStackTrace();
			}
    		
//    	}
    }
}
