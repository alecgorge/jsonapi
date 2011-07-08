package com.ramblingwood.minecraft.jsonapi.api;

public interface JSONAPICallHandler {
	// if you return true here, you WILL handle the API call in the handle method.
	// will be called twice for every API call, once to test if the method exists and once on the actuall call
	public boolean willHandle(APIMethodName methodName);
	
	// the result of this method will be treated as the response to the API request, even if null is returned (some methods do return null)
	public Object handle(APIMethodName methodName, Object[] args);
}

