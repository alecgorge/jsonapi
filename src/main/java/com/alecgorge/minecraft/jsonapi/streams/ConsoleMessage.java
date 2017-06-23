package com.alecgorge.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class ConsoleMessage extends JSONAPIStreamMessage {
	public String line;
	
	public ConsoleMessage(String line) {
		this.line = line;
		setTime();
	}
	
	public String streamName () {
		return "console";
	}
	
	public JSONObject toJSONObject () {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("line", line);
		
		return o;
	}
}
