package org.bei.dema.http;
/**
 * author：zhaochengbei
 * date：2017/6/8
*/
public enum HttpResponseStatus {
	OK(200,"OK"),
	BAD_REQUEST(400,"Bad Request"),
	INTERNAL_SERVER_ERROR(500,"Internal Server Error")
	;
	public int code;
	public String phrase;
	private HttpResponseStatus(int code,String phrase){
		this.code = code;
		this.phrase = phrase;
	}
}
