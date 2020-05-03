package com.alecgorge.minecraft.jsonapi.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandMethods {
	Server server;
	
	public CommandMethods(Server server) {
		this.server = server;
	}
	
	public Map<String, Map<String, Map<String, Object>>> listComplete() {
		Map<String, Map<String, Map<String, Object>>> allCmds = new HashMap<String, Map<String, Map<String, Object>>>();

		for (Plugin p : server.getPluginManager().getPlugins()) {
			final PluginDescriptionFile desc = p.getDescription();
			final Map<String, Map<String, Object>> cmds = desc.getCommands();

			if(desc.getCommands() != null) {
				allCmds.put(p.getDescription().getName(), cmds);
			}
			else {
				allCmds.put(p.getDescription().getName(), new HashMap<String, Map<String,Object>>());
			}
		}
		
		return allCmds;
	}
	
	public Map<String, Map<String, Object>> commandsForPluginName(String name) {
		Map<String, Map<String, Object>> allCmds = new HashMap<String, Map<String, Object>>();

		Plugin p = server.getPluginManager().getPlugin(name);
		if(p != null) {
			final PluginDescriptionFile desc = p.getDescription();
			
			if(desc.getCommands() != null) {
				allCmds = desc.getCommands();
			}
		}
		
		return allCmds;
	}
	
	public List<String> listSimple() {
		List<String> allCmds = new ArrayList<String>();

		for (Plugin p : server.getPluginManager().getPlugins()) {
			final PluginDescriptionFile desc = p.getDescription();
			final Map<String, Map<String, Object>> cmds = desc.getCommands();
			
			allCmds.addAll(cmds.keySet());
		}
		
		return allCmds;
	}
	
	public Map<String, Object> commandForName(String name) {
		Map<String, Object> allCmds = new HashMap<String, Object>();

		for (Plugin p : server.getPluginManager().getPlugins()) {
			final PluginDescriptionFile desc = p.getDescription();
			final Map<String, Map<String, Object>> cmds = desc.getCommands();
			
			if(cmds.get(name) != null) {
				return cmds.get(name);
			}
		}
		
		return allCmds;

	}
}
