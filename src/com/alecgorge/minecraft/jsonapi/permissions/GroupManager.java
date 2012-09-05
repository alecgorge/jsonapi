package com.alecgorge.minecraft.jsonapi.permissions;

import java.io.File;
import java.io.FileReader;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONAware;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.event.JSONAPIAuthEvent;


public class GroupManager {
	JSONObject permissions;
	JSONObject groups;
	JSONObject users;
	
	Map<AbstractMap.SimpleEntry<String, String>, Boolean> userCache = new ConcurrentHashMap<AbstractMap.SimpleEntry<String,String>, Boolean>();
	
	JSONParser parser = new JSONParser();
	
	File input;
	
	public GroupManager(JSONAPI api, File input) {
		this.input = input;
		
		loadFromConfig();
		api.getServer().getPluginManager().registerEvents(new JSONAPIPermissionsListener(), api);
	}
	
	public void loadFromConfig() {
		JSONObject o;
		try {
			o = (JSONObject) parser.parse(new FileReader(input));
			
			permissions = (JSONObject) o.get("permissions");
			groups 		= (JSONObject) o.get("groups");
			users 		= (JSONObject) o.get("users");
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	JSONObject getMap(Object o, Object key) {
		return (JSONObject)((JSONObject)o).get(key.toString());
	}
	
	boolean getBool(Object o, String key) {
		return Boolean.valueOf(((JSONObject)o).get(key).toString());
	}
	
	JSONArray getList(Object o, String key) {
		return (JSONArray)((JSONObject)o).get(key);
	}
	
	void trace(String s) {
		if(true) {
			System.out.print(s);
		}
	}
	
	void traceLine(Object o) {
		if(true) {
			if(o instanceof JSONAware) System.out.println(((JSONAware)o).toJSONString());
			else System.out.println(o.toString());
		}
	}
	
	boolean effectivePermission(String username, String method, boolean stream) {
		if(!users.containsKey(username)) {
			return true; // no settings for group, assume EVERYTHING
						 // no need to check the cache: it is *always* true
		}
		
		AbstractMap.SimpleEntry<String, String> key = new SimpleEntry<String, String>(username, method);
		if(userCache.containsKey(key)) {
			return userCache.get(key);
		}
		
		trace("Groups: "); traceLine(groups);
		trace("Stream: "); traceLine(stream);
		
		boolean valid = false; // assume no.
		String groupKey = stream ? "streams" : "methods";
		List<Object> groups = getList(users, username);
		for(Object o : groups) {
			String group = o.toString();
			JSONObject groupMap = getMap(groups, group);
			JSONObject groupKeyMap = getMap(groupMap, groupKey);
			
			if(groupKeyMap.containsKey("ALLOW_ALL") && getBool(groupKeyMap, "ALLOW_ALL")) {
				valid = true;
				continue;
			}
			
			if(groupMap.containsKey("permissions")) {
				JSONObject permissionMap = getMap(groupMap, "permissions");
				if(permissionMap.containsKey("ALLOW_ALL") && getBool(groupKeyMap, "ALLOW_ALL")) {
					valid = true;
				}
				for(Object k : permissionMap.keySet()) {
					List<Object> methods = getList(getMap(permissions, k), groupKey);
					if(methods == null) continue;
					
					if(methods.contains(method)) {
						valid = true;
					}
				}
			}
			
			Object ob = groupKeyMap.get(method);
			if(ob != null) {
				valid = Boolean.valueOf(ob.toString());
			}
		}
		
		userCache.put(key, valid);
		
		return valid;
	}
	
	class JSONAPIPermissionsListener implements Listener {
		@EventHandler
		public void onJSONAPIAuthChallenge(JSONAPIAuthEvent e) {
			e.getAuthResponse().setAllowed(effectivePermission(e.getUsername(), e.getMethod(), e.isStream()));
		}
	}
}