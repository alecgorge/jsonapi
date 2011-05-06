package com.ramblingwood.minecraft.jsonapi.streams;

import org.json.simpleForBukkit.JSONObject;

public class ConnectionMessage extends JSONAPIStream {
	private boolean TrueIsConnectedFalseIsDisconnected;
	
	public ConnectionMessage(String player, boolean TrueIsConnectedFalseIsDisconnected) {
		super(player, "");
		this.TrueIsConnectedFalseIsDisconnected = TrueIsConnectedFalseIsDisconnected;
		setTime();
	}

	public JSONObject toJSONObject() {
		JSONObject o = new JSONObject();
		o.put("time", getTime());
		o.put("player", getPlayer());
		o.put("action", TrueIsConnectedFalseIsDisconnected ? "connected" : "disconnected");
		
		return o;
	}
}
