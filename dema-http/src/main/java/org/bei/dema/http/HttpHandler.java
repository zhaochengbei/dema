package org.bei.dema.http;

import java.io.IOException;

/**
 * author：zhaochengbei
 * date：2017/6/7
*/
public interface HttpHandler {

	public void onHttpRequest(HttpRequest request,HttpContext context);
}
