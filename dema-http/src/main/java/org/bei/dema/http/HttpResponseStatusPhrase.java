package org.bei.dema.http;

import java.util.HashMap;
import java.util.Map;

/**
 * author：zhaochengbei
 * date：2017/6/9
*/
public class HttpResponseStatusPhrase {

	/**
	 * 
	 */
	static public Map<Integer, String> map = new HashMap<Integer, String>(){
		{
			put(200,"OK");
			put(400,"Bad Request");
			put(500,"Internal Server Error");
		}
	};
}
