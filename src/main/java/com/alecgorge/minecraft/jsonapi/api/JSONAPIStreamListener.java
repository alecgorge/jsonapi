package com.alecgorge.minecraft.jsonapi.api;

/**
 * A way to programatically "subscribe" to streams.
 * 
 * @author alecgorge
 */
public interface JSONAPIStreamListener {
	/**
	 * Called every time there is a new message from the stream.
	 * 
	 * @param message The message from the stream. You will likely need to cast this to the correct subclass of JSONAPIStreamMessage to use all the features. 
	 * @param sender The JSONAPIStream instance that pushed this message.
	 */
	public void onMessage(JSONAPIStreamMessage message, JSONAPIStream sender);
}
