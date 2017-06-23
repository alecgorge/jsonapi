package com.alecgorge.minecraft.jsonapi.permissions;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.alecgorge.minecraft.jsonapi.config.GroupsConfig;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.event.JSONAPIAuthEvent;
import com.alecgorge.minecraft.jsonapi.JSONAPI;

public class GroupManager {
	JSONAPI	plugin;

	public GroupManager(JSONAPI plugin) {
		this.plugin = plugin;
	
		JSONAPI.dbug("registering for jsonapiauthevents");
		plugin.getServer().getPluginManager().registerEvents(new JSONAPIPermissionsListener(), plugin);
	}

	public void loadFromConfig() {
		try {
			UsersConfig.config().init();
			GroupsConfig.config().init();
		}
		catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	boolean effectivePermission(JSONAPIUser username, String method, boolean stream) {
		JSONAPI.dbug("Testing " + method + " (" + stream + ")" + " on " + username);
		if (username == null) {
			return false;
		}
		
		if (stream) {
			return username.canUseStream(method);
		}
		else {
			return username.canUseMethod(method);
		}
	}

	public class JSONAPIPermissionsListener implements Listener {
		@EventHandler
		public void onJSONAPIAuthChallenge(JSONAPIAuthEvent e) {
			JSONAPI.dbug("Recieved authevent " + e);
			e.getAuthResponse().setAllowed(effectivePermission(e.getUser(), e.getMethod(), e.isStream()));
		}
	}
}
