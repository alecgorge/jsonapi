package com.ramblingwood.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class ConsoleMessage extends JSONAPIStreamMessage {
	private String line;
	
	public ConsoleMessage(String line) {
		this.line = line;
		setTime();
	}
	
	public JSONObject toJSONObject () {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("line", line);
		
		return o;
	}
}
