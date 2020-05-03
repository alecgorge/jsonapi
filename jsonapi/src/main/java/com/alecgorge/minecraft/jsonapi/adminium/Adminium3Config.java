package com.alecgorge.minecraft.jsonapi.adminium;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.config.Config;

public class Adminium3Config extends Config {
	public Adminium3Config(Plugin plugin) {
		CONFIG_FILE = new File(plugin.getDataFolder(), "adminium3.yml");
		CONFIG_HEADER = "Adminium v3 config. Do not edit unless you know what you are doing";
	}
	
	public static Adminium3Config inst = null;
	public static Adminium3Config config() {
		if(inst == null) {
			inst = new Adminium3Config(JSONAPI.instance);
			try {
				inst.init();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		return inst;
	}
	
	@Override
	public Config init() throws InvalidConfigurationException {
		Config c = super.init();
		
		if(notificationDefaults == null || notificationDefaults.keySet().size() < 4) {
			Map<String, Boolean> def = new HashMap<String, Boolean>();
			def.put("calladmin", true);
			def.put("taboo", false);
			def.put("severe", true);
			def.put("player_join", false);
			def.put("player_quit", false);
			
			notificationDefaults = def;
		}
		
		if(taboo == null) {
			taboo = new HashMap<String, List<String>>();
		}
		
		if(devices == null) {
			devices = new HashMap<String, Map<String,Boolean>>();
		}
		
		this.save();
		
		return c;
	}
	
	public Map<String, Map<String, Boolean>> devices;
	public Map<String, Boolean> notificationDefaults;
	public Map<String, List<String>> taboo;
	
	public Map<String, Boolean> getDevicePushNotificationSettings(String device) {
		return devices.get(devices);
	}
}
