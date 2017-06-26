package io.dema.http;

import java.util.HashMap;
import java.util.Map;

/**
 * author：zhaochengbei
 * date：2017/6/8
*/
public class HttpResponseStatus {
	/**
	 * 
	 */
	static public int OK = 200;
	/**
	 * 
	 */
	static public int BAD_REQUEST = 400;
	/**
	 * 
	 */
	static public int INTERNAL_SERVER_ERROR = 500;
	/**
	 * 
	 */	static public Map<Integer, String> phraseMap = new HashMap<Integer, String>(){
		{
			put(200,"OK");
			put(400,"Bad Request");
			put(500,"Internal Server Error");
		}
	};
}
