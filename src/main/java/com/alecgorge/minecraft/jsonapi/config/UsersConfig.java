package com.alecgorge.minecraft.jsonapi.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.Plugin;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.permissions.JSONAPIUser;

public class UsersConfig extends Config {
	static UsersConfig config = null;
	public UsersConfig(Plugin plugin) {
		if(config == null) {
			config = this;
		}
		CONFIG_FILE = new File(plugin.getDataFolder(), "users.yml");
		CONFIG_HEADER = "JSONAPI v4 users file";
	}
	
	public static UsersConfig config() {
		if(config == null) {
			new UsersConfig(JSONAPI.instance);
		}
		return config; 
	}

	public List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();
	private Map<String, JSONAPIUser> juserCache = new HashMap<String, JSONAPIUser>();
	
	public List<Map<String, Object>> getUsers() {
		return users;
	}
	
	public boolean userExists(String username) {
		return getUser(username) != null;
	}
	
	@SuppressWarnings("unchecked")
	public void generateCache(boolean force) {
		if(force || juserCache.size() != users.size()) {
			juserCache.clear();
			for(Map<String, Object> o : getUsers()) {
				boolean logging = true;
				if(o.containsKey("logging"))
					logging = Boolean.valueOf(o.get("logging").toString());
				
				JSONAPIUser user = new JSONAPIUser(o.get("username").toString(), o.get("password").toString(), (List<String>)o.get("groups"), logging);
				juserCache.put(o.get("username").toString(), user);
			}
		}
	}
	
	public void generateCache() {
		generateCache(false);
	}
	
	public JSONAPIUser getUser(String username) {
		generateCache(false);
		return juserCache.get(username);
	}
	
	public Map<String, JSONAPIUser> getJSONAPIUsers() {
		generateCache();
		return juserCache;
	}
	
	public Map<String, Object> getRawUser(String username) {
		for(Map<String, Object> u : users) {
			if(u.get("username").toString() == username) {
				return u;
			}
		}
		return null;
	}
}
