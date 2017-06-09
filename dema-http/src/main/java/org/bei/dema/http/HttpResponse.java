package org.bei.dema.http;

import java.util.Map;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpResponse {
	
	/**
	 * 
	 */
	public HttpResponseStatus status;
	
	/**
	 * default disable cache;
	 */

	/**
	 * 
	 */
	public String connection = HttpConnectionType.close;
	/**
	 * 
	 */
	public Map<String, Object> otherHead;
	
	/**
	 * 
	 */
	public String contentType = HttpContentType.text_html;
	
	/**
	 * 
	 */
	public byte[] content;
}
