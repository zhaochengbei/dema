package io.dema.http;
/**
 * author：zhaochengbei
 * date：2017年6月26日
*/
public interface HttpServerHandler extends HttpHandler {
	/**
	 * 
	 * @param request
	 * @param context
	 */
	public void onHttpRequest(HttpRequest request,HttpContext context);
	
}
