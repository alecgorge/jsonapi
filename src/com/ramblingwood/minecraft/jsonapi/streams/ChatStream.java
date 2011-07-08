package com.ramblingwood.minecraft.jsonapi.streams;

import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStream;

public class ChatStream extends JSONAPIStream {
	public void addMessage(ChatMessage m) {
		this.stack.add(m);
	}
}
