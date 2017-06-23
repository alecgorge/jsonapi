package com.alecgorge.minecraft.jsonapi.streams;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class ChatMessage extends JSONAPIStreamMessage {
	public String player;
	public String message;
	public boolean isCancelled;

	public ChatMessage(AsyncPlayerChatEvent e) {
		this.player = e.getPlayer().getName();
		this.message = e.getMessage();
		this.isCancelled = e.isCancelled();
		setTime();
	}
	
	public ChatMessage(String player, String message) {
		this.player = player;
		this.message = message;
		this.isCancelled = false;
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
		o.put("isCancelled", isCancelled);
		return o;
	}
}
