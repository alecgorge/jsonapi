package com.alecgorge.minecraft.jsonapi.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIGroup;

public class GroupsConfig extends Config {
	public GroupsConfig(Plugin plugin) {
		CONFIG_FILE = new File(plugin.getDataFolder(), "groups.yml");
		CONFIG_HEADER = "JSONAPI v4 users file\nThis file works under the assumption" +
				"\nthat the user has access to nothing initially and this file allows" +
				"\nyou to give them specific permissions";
	}
	
	public static GroupsConfig inst = null;
	public static GroupsConfig config() {
		if(inst == null) {
			inst = new GroupsConfig(JSONAPI.instance);
			try {
				inst.init();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return inst;
	}

	public List<Map<String, Object>> groups = new ArrayList<Map<String, Object>>();
	List<JSONAPIGroupConfigObject> grr = null;
	
	@SuppressWarnings("unchecked")
	public List<JSONAPIGroupConfigObject> getGroups() {
		if(grr == null) {
			grr = new ArrayList<JSONAPIGroupConfigObject>();
			for(Map<String, Object> o : groups) {
				JSONAPIGroupConfigObject j = new JSONAPIGroupConfigObject();
				j.methods = (List<String>)o.get("methods");
				j.permissions = (List<String>)o.get("permissions");
				j.streams = (List<String>)o.get("streams");
				j.name = o.get("name").toString();
				grr.add(j);
			}
		}
		return grr;
	}
	
	public List<JSONAPIGroup> getJSONAPIGroups() {
		List<JSONAPIGroup> arr = new ArrayList<JSONAPIGroup>();
		for(JSONAPIGroupConfigObject o : getGroups()) {
			arr.add(new JSONAPIGroup(o.name));
		}
		return arr;
	}
	
	public void generateCache() {
		grr = null;
	}
	
	public JSONAPIGroupConfigObject getGroup(String name) {
		for(JSONAPIGroupConfigObject g : getGroups()) {
			if(g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}
	
	public Map<String, Object> getRawGroup(String name) {
		for(Map<String, Object> g : groups) {
			if(g.get("name").equals(name)) {
				return g;
			}
		}
		return null;		
	}
}
