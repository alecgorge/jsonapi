package com.alecgorge.minecraft.jsonapi.permissions;

import java.io.File;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.json.simpleForBukkit.parser.JSONParser;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.event.JSONAPIAuthEvent;


public class GroupManager {
	Map<String, Object> permissions;
	Map<String, Object> groups;
	Map<String, Object> users;
	
	Map<AbstractMap.SimpleEntry<String, String>, Boolean> userCache = new ConcurrentHashMap<AbstractMap.SimpleEntry<String,String>, Boolean>();
	
	JSONParser parser = new JSONParser();
	
	public GroupManager(JSONAPI api, File input) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(input);
		
		Map<String, Object> o = config.getValues(true);
		permissions = getMap(o, "permissions");
		groups 		= getMap(o, "groups");
		users 		= getMap(o, "users");
		
		api.getServer().getPluginManager().registerEvents(new JSONAPIPermissionsListener(), api);
	}
	
	@SuppressWarnings("unchecked")
	Map<String, Object> getMap(Object o, String key) {
		return ((Map<String,Object>)((Map<String, Object>)o).get(key));
	}
	
	@SuppressWarnings("unchecked")
	boolean getBool(Object o, String key) {
		return ((Boolean)((Map<String, Object>)o).get(key)).booleanValue();
	}
	
	@SuppressWarnings("unchecked")
	List<Object> getList(Object o, String key) {
		return ((List<Object>)((Map<String, Object>)o).get(key));
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
		
		boolean valid = false; // assume no.
		String groupKey = stream ? "streams" : "methods";
		List<Object> groups = getList(users, username);
		for(Object o : groups) {
			String group = o.toString();
			Map<String, Object> groupMap = getMap(groups, group);
			Map<String, Object> groupKeyMap = getMap(groupMap, groupKey);
			
			if(groupKeyMap.containsKey("ALLOW_ALL") && Boolean.valueOf(groupKeyMap.get("ALLOW_ALL").toString())) {
				valid = true;
			}
			
			if(groupMap.containsKey("permissions")) {
				Map<String, Object> permissionMap = getMap(groupMap, "permissions");
				if(permissionMap.containsKey("ALLOW_ALL") && Boolean.valueOf(permissionMap.get("ALLOW_ALL").toString())) {
					valid = true;
				}
				for(String k : permissionMap.keySet()) {
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
			// only change if it is false. if already false, leave it.
			if (e.getAuthResponse().isAuthenticated() && e.getAuthResponse().isAllowed()) {
				if (!effectivePermission(e.getUsername(), e.getMethod(), e.isStream())) {
					e.getAuthResponse().setAllowed(false);
				}
			}
		}
	}
}