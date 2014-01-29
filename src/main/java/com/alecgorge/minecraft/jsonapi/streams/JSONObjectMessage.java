package com.alecgorge.minecraft.jsonapi.streams;

import java.util.Map;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class JSONObjectMessage extends JSONAPIStreamMessage {
	Map<String, Object> obj;
	public JSONObjectMessage(Map<String, Object> o) {
		obj = o;
	}
	
	@Override
	public String streamName() {
		return "JSONObjectMessage";
	}

	@Override
	public Map<String, Object> toJSONObject() {
		return obj;
	}

}
