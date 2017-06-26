package io.dema.http;

import io.dema.http.HttpMethodType;
import io.dema.http.HttpRequest;
import io.dema.http.HttpResponse;
import io.dema.http.HttpUtils;

/**
 * author：zhaochengbei
 * date：2017/6/9
*/
public class HttpUtilsTest {

	static public void main(String[] args){
		try {
			HttpRequest request = new HttpRequest();
			request.method = HttpMethodType.HEAD;
			request.host = "localhost";
			System.out.println(request);
			HttpResponse response = HttpUtils.request("localhost", 8090, request, 2000);
			System.out.println(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
