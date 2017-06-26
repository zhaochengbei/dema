package io.dema.http;

import java.util.HashMap;
import java.util.Map;

/**
 * author：zhaochengbei
 * date：2017/6/9
*/
public class HttpPacket {

	/**
	 * 
	 */
	public StringBuffer stringBuffer = new StringBuffer();
	/**
	 * 
	 */
	public int parseStage = ParseStage.line;
	
	/**
	 * 
	 */
	public String version = "HTTP/1.1";
	
	/**
	 * 
	 */
	public String connection = HttpConnectionType.close;
	/**
	 * 
	 */
	public Map<String, String> otherHead = new HashMap<String, String>();
	
	/**
	 * 
	 */
	public int contentLength;
	
	/**
	 * 
	 */
	public byte[] content;
	
	/**
	 * 
	 */
	@Override
	public String toString(){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(",parseStage=");
		stringBuffer.append(parseStage);
		stringBuffer.append(",version=");
		stringBuffer.append(version);
		stringBuffer.append(",connection=");
		stringBuffer.append(connection);
		for (String key : otherHead.keySet()) {
			stringBuffer.append(",");
			stringBuffer.append(key);
			stringBuffer.append("=");
			stringBuffer.append(otherHead.get(key));
		}
		stringBuffer.append(",contentLength=");
		stringBuffer.append(contentLength);
		
		if(content != null){
			stringBuffer.append(",content=");
			try {
				stringBuffer.append(new String(content,"utf-8"));	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return stringBuffer.toString();
	}
}
