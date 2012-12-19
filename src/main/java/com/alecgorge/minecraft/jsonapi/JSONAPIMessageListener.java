package com.alecgorge.minecraft.jsonapi;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class JSONAPIMessageListener implements PluginMessageListener {
	private JSONAPI api;

	public JSONAPIMessageListener(JSONAPI jsonapi) {
		api = jsonapi;
	}

	@Override
	public void onPluginMessageReceived(String arg0, Player arg1, byte[] arg2) {
		// arg1.sendPla
	}

}
