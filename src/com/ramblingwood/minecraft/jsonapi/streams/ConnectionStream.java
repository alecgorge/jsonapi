package com.ramblingwood.minecraft.jsonapi.streams;

import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStream;

public class ConnectionStream extends JSONAPIStream {
	public void addMessage(ConnectionMessage m) {
		this.stack.add(m);
	}
}
