package com.alecgorge.minecraft.jsonapi.streams;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class FormattedChatMessage extends JSONAPIStreamMessage {
	public String player;
	public String formattedLine;
	public boolean isCancelled;
	
	public FormattedChatMessage(AsyncPlayerChatEvent e) {
		this.player = e.getPlayer().getName();
		this.formattedLine = String.format(e.getFormat(), e.getPlayer().getDisplayName(), e.getMessage());
		this.isCancelled = e.isCancelled();
		setTime();
	}
	
	public String streamName () {
		return "formatted_chat";
	}

	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", player);
		o.put("line", formattedLine);
		o.put("isCancelled", isCancelled);
		return o;
	}
}
