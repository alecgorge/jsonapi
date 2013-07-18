package com.alecgorge.minecraft.jsonapi.permissions;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.alecgorge.minecraft.jsonapi.config.GroupsConfig;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.event.JSONAPIAuthEvent;


public class GroupManager {
	public void loadFromConfig() {
		try {
			UsersConfig.config().init();
			GroupsConfig.config().init();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	boolean effectivePermission(JSONAPIUser username, String method, boolean stream) {
		if(stream) {
			return username.canUseStream(method);
		}
		else {
			return username.canUseMethod(method);
		}
	}
	
	class JSONAPIPermissionsListener implements Listener {
		@EventHandler
		public void onJSONAPIAuthChallenge(JSONAPIAuthEvent e) {
			e.getAuthResponse().setAllowed(effectivePermission(e.getUser(), e.getMethod(), e.isStream()));
		}
	}
}