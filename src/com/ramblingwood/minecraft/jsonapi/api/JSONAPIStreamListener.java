package com.ramblingwood.minecraft.jsonapi.api;

public interface JSONAPIStreamListener {
	public void onMessage(JSONAPIStreamMessage message, JSONAPIStream sender);
}
