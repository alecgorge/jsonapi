package com.alecgorge.minecraft.jsonapi.streams;

import java.util.HashMap;
import java.util.Map;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class ChatMessage extends JSONAPIStreamMessage {
	public String player;
	public String message;
	
	public ChatMessage(String player, String message) {
		this.player = player;
		this.message = message;
		setTime();
	}
	
	public String streamName () {
		return "chat";
	}

	public Map<String, Object> toJSONObject() {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("time", getTime());
		o.put("player", player);
		o.put("message", message);
		return o;
	}
}
