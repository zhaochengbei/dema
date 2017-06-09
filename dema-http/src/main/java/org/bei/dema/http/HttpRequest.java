package org.bei.dema.http;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpRequest {
	/**
	 * 
	 */
	public StringBuffer stringBuffer = new StringBuffer();
	
	/**
	 * 
	 */
	public String method;
	
	/**
	 * 
	 */
	public String uri;
	
	/**
	 * 
	 */
	public String version;
	
	/**
	 * 
	 */
	public String host;
	/**
	 * 
	 */
	public String connection;
	/**
	 * 
	 */
	public Map<String, Object> otherHead = new HashMap<String, Object>();
	
	/**
	 * 
	 */
	public boolean headParseComplete;
	
	/**
	 * 
	 */
	public int contentLength;
	
	/**
	 * 
	 */
	public byte[] content;
}
