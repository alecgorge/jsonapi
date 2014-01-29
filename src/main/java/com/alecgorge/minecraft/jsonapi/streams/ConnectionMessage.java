package com.alecgorge.minecraft.jsonapi.streams;

import java.util.HashMap;
import java.util.Map;

import com.alecgorge.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class ConnectionMessage extends JSONAPIStreamMessage {
	public boolean TrueIsConnectedFalseIsDisconnected;
	public String player;
	
	public ConnectionMessage(String player, boolean TrueIsConnectedFalseIsDisconnected) {
		this.TrueIsConnectedFalseIsDisconnected = TrueIsConnectedFalseIsDisconnected;
		this.player = player;
		setTime();
	}
	
	public String streamName () {
		return "connections";
	}

	public Map<String, Object> toJSONObject() {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("time", getTime());
		o.put("player", player);
		o.put("action", TrueIsConnectedFalseIsDisconnected ? "connected" : "disconnected");
		
		return o;
	}
}
