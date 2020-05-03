package com.alecgorge.minecraft.jsonapi.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class PermissionNodesConfig extends Config {
	public Map<String, Map<String, List<String>>> permissions = new HashMap<String, Map<String, List<String>>>();
	
	public PermissionNodesConfig(Plugin plugin) {
		CONFIG_STREAM = plugin.getResource("permission_nodes.yml");
		CONFIG_HEADER = "JSONAPI v4 permissions file";
	}
	
	public static PermissionNodesConfig inst = null;
	public static PermissionNodesConfig config() {
		if(inst == null) {
			inst = new PermissionNodesConfig(JSONAPI.instance);
			try {
				inst.init();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return inst;
	}
	
	Map<String, JSONAPIPermissionNode> perms = null;
	public Map<String, JSONAPIPermissionNode> getPermissions() {
		if(perms == null) {
			perms = new HashMap<String, JSONAPIPermissionNode>();
			for(String k : permissions.keySet()) {
				JSONAPIPermissionNode node = new JSONAPIPermissionNode(k);
				if(permissions.get(k).get("streams") != null)
					node.streams = permissions.get(k).get("streams");
				if(permissions.get(k).get("methods") != null)
					node.methods = permissions.get(k).get("methods");
				
				perms.put(k, node);
			}
		}
		return perms;
	}

	public static JSONAPIPermissionNode fromName(String name) {
		return config().getPermissionNode(name);
	}
	
	public JSONAPIPermissionNode getPermissionNode(String name) {
		Map<String, JSONAPIPermissionNode> nodes = getPermissions();
		if(!nodes.containsKey(name)) {
			try {
				throw new Exception("The permission node "+ name +" does not exist!");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		return nodes.get(name).setName(name);
	}
}
