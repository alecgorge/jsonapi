package com.ramblingwood.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

import com.ramblingwood.minecraft.jsonapi.api.JSONAPIStreamMessage;

public class ConnectionMessage extends JSONAPIStreamMessage {
	private boolean TrueIsConnectedFalseIsDisconnected;
	private String player;
	
	public ConnectionMessage(String player, boolean TrueIsConnectedFalseIsDisconnected) {
		this.TrueIsConnectedFalseIsDisconnected = TrueIsConnectedFalseIsDisconnected;
		this.player = player;
		setTime();
	}

	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", player);
		o.put("action", TrueIsConnectedFalseIsDisconnected ? "connected" : "disconnected");
		
		return o;
	}
}
