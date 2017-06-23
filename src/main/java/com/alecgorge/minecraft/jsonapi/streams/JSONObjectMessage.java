package com.alecgorge.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class JSONObjectMessage extends JSONAPIStreamMessage {
	JSONObject obj;
	public JSONObjectMessage(JSONObject o) {
		obj = o;
	}
	
	@Override
	public String streamName() {
		return "JSONObjectMessage";
	}

	@Override
	public JSONObject toJSONObject() {
		return obj;
	}

}
