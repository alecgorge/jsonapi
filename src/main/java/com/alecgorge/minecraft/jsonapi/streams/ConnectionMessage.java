package com.alecgorge.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

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

	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", player);
		o.put("action", TrueIsConnectedFalseIsDisconnected ? "connected" : "disconnected");
		
		return o;
	}
}
