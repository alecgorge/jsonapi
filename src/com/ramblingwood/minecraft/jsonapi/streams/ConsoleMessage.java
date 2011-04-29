package com.ramblingwood.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

public class ConsoleMessage extends JSONAPIStream {

	public ConsoleMessage(String line) {
		super("", line);
		setTime();
	}
	
	public String toJSONString () {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("line", getMessage());
		
		return o.toJSONString();		
	}
}
