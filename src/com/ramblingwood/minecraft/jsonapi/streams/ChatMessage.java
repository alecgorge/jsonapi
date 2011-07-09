package com.ramblingwood.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class ChatMessage extends JSONAPIStreamMessage {
	private String player;
	private String message;
	
	public ChatMessage(String player, String message) {
		this.player = player;
		this.message = message;
		setTime();
	}
	
	public String streamName () {
		return "chat";
	}

	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", player);
		o.put("message", message);
		return o;
	}
}
