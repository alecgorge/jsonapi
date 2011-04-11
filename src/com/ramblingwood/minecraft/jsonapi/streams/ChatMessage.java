package com.ramblingwood.minecraft.jsonapi.streams;

public class ChatMessage extends JSONAPIStream {
	public ChatMessage(String player, String message) {
		super(player, message);
		setTime();
	}
}
