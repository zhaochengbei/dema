package io.dema.http;
/**
 * author：zhaochengbei
 * date：2017年6月26日
*/
public interface HttpClientHandler extends HttpHandler {
	/**
	 * 
	 * @param httpResponse
	 * @param context
	 */
	public void onHttpResponse(HttpResponse httpResponse,HttpContext context);
	
}
