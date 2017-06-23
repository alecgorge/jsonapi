package com.alecgorge.minecraft.jsonapi.api;

/**
 * A simple way to add/overload JSONAPI method calls.
 * 
 * Just make sure you register the class that implements this interface with JSONAPI" {@link https://github.com/alecgorge/jsonapi/wiki/Integration-guide-for-normal-api-methods}
 * 
 * @author alecgorge
 */
public interface JSONAPICallHandler {
	/**
	 * If you return true here, you WILL handle the API call in the handle method.
	 * 
	 * This method will be called twice for every API call, once to test if the method exists and once on the actual call.
	 */
	public boolean willHandle(APIMethodName methodName);
	
	/**
	 * The result of this method will be treated as the response to the API request, even if null is returned (some methods do return null).
	 * 
	 * @param methodName The name of the method (such as "remotetoolkit.startServer" or "getServer").
	 * @param args The arguments passed to the method. The arguments are not validated or necessarily the right length.
	 * @return Whatever you want the response to the API method to be.
	 */
	public Object handle(APIMethodName methodName, Object[] args);
}

