package com.ramblingwood.minecraft.jsonapi.streams;

import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStream;

public class ConsoleStream extends JSONAPIStream {
	public void addMessage(ConsoleMessage m) {
		this.stack.add(m);
	}
}
