package com.alecgorge.minecraft.jsonapi.api.v2;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.dynamic.API_Method;
import com.alecgorge.minecraft.jsonapi.dynamic.JSONAPIMethodProvider;

public class StandardAPIMethods implements JSONAPIMethodProvider {
	public StandardAPIMethods(JSONAPI api) {
		api.getCaller().registerMethods(this);
	}
	
	@API_Method(
			name = "players.online.ips",
			description = "Returns a mapping of online player names to their connected IP address"
	)
	public Map<String, String> getPlayerIPAddresses() {
		Map<String, String> m = new HashMap<String, String>();
		
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			m.put(p.getName(), p.getAddress().toString());
		}
		
		return m;
	}
}
