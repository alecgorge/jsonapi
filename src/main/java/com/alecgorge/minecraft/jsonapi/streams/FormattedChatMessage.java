package com.alecgorge.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class FormattedChatMessage extends JSONAPIStreamMessage {
	public String player;
	public String formattedLine;
	
	public FormattedChatMessage(String player, String formatted_line) {
		this.player = player;
		this.formattedLine = formatted_line;
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
		return o;
	}
}
