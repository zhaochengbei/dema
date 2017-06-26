package org.bei.dema.http;


/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public class HttpResponse extends HttpPacket {
	/**
	 * 
	 */
	public int status = -1;
	/**
	 *  phrase sever response maybe we do not define;
	 */
	public String phrase;
	/**
	 * 
	 */
	public String server = "dema-http/1.0";
	
	/**
	 * 
	 */
	public String contentType = HttpContentType.text_html;
	
	/**
	 * 
	 */
	public boolean sendContent = true;
	/**
	 * 
	 */
	@Override
	public String toString(){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("[");
		stringBuffer.append("status=");
		stringBuffer.append(status);
		stringBuffer.append(",phrase=");
		stringBuffer.append(phrase);
		stringBuffer.append(",contentType=");
		stringBuffer.append(contentType);
		stringBuffer.append(",sendContent=");
		stringBuffer.append(sendContent);
		stringBuffer.append(super.toString());
		stringBuffer.append("]");
		return stringBuffer.toString();
	}
}
