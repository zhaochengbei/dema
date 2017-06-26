package org.bei.dema.http;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpRequest extends HttpPacket{
	
	/**
	 * 
	 */
	public String method;
	
	/**
	 * 
	 */
	public String uri = "/";
	
	/**
	 * 
	 */
	public String host;
	/**
	 * 
	 */
	@Override
	public String toString(){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("[");
		stringBuffer.append("method=");
		stringBuffer.append(method);
		stringBuffer.append(",uri=");
		stringBuffer.append(uri);
		stringBuffer.append(super.toString());
		stringBuffer.append("]");
		return stringBuffer.toString();
	}
}
