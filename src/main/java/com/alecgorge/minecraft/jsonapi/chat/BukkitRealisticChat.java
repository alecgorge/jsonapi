package com.alecgorge.minecraft.jsonapi.chat;


public class BukkitRealisticChat implements IRealisticChat {

	public boolean chatWithName(String message, String name) {
		return false;
	}

	public void pluginDisable() {
		
	}

	@Override
	public boolean canHandleChats() {
		return false;
	}
}
