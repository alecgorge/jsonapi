package com.alecgorge.minecraft.jsonapi.streams;

import java.util.HashMap;
import java.util.Map;

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
	
	public Map<String, Object> toJSONObject () {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("time", getTime());
		o.put("line", line);
		
		return o;
	}
}
