package com.alecgorge.minecraft.jsonapi.dynamic;

import java.util.HashMap;

import org.json.simpleForBukkit.JSONArray;

public class Argument {
	private Class<?> type;
	private String desc;
	private Object value = null;
	private static HashMap<String, Class<?>> mapping = new HashMap<String, Class<?>>();
	
	static {
		mapping.put("int", int.class);
		mapping.put("String[]", String[].class);
		mapping.put("double", double.class);
		mapping.put("boolean", boolean.class);
		mapping.put("float", float.class);
		mapping.put("String", String.class);
		mapping.put("Integer", Integer.class);
		mapping.put("Float", Float.class);
		mapping.put("Double", Double.class);
		mapping.put("Boolean", Boolean.class);
		mapping.put("Player", org.bukkit.entity.Player.class);
		mapping.put("Server", org.bukkit.Server.class);
		mapping.put("World", org.bukkit.World.class);
		mapping.put("World[]", org.bukkit.World[].class);
		mapping.put("Player[]", org.bukkit.entity.Player[].class);
		mapping.put("Plugin", org.bukkit.plugin.Plugin.class);
		mapping.put("Plugin[]", org.bukkit.plugin.Plugin[].class);
		mapping.put("OfflinePlayer", org.bukkit.OfflinePlayer.class);
		mapping.put("OfflinePlayer[]", org.bukkit.OfflinePlayer[].class);
		mapping.put("Object[]", Object[].class);
	}
	
	public Argument(JSONArray a) {
		if(a.size() > 2) {
			type = getClassFromName((String)a.get(1));
			desc = (String)a.get(2);
		}
		else {
			type = getClassFromName((String)a.get(0));
			desc = (String)a.get(1);
		}
		
		if(a.size() == 4) {
			setValue(a.get(3));
		}
	}
	
	public Argument(Class<?> a, String desc) {
		type = a;
		this.desc = desc;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public void setValue (Object o) {
		value = o;
	}
	
	public Object getValue () {
		return value;
	}
	
	public String getDesc () {
		return desc;
	}
	
	public static Class<?> getClassFromName(String name) {
		// auto translate from some of the defaults
	    Class<?> ret = mapping.get(name);
	    if(ret != null) {
	    	return ret;
	    }
	    
		
		ret = void.class;
		try {
			ret = Class.forName(name);
		} catch (ClassNotFoundException e) {
			try {
				ret = Class.forName("java.lang."+name);
			} catch (ClassNotFoundException e1) {
				try {
					ret = Class.forName("org.bukkit."+name);
				} catch (Exception e2) {					
					try {
						ret = Class.forName("org.bukkit.entity."+name);
					} catch (Exception e3) {
						try {
							ret = Class.forName("net.milkbowl.vault.economy."+name);
						} catch (ClassNotFoundException e4) {
						}
					}
				}
			}
		}
		return ret;
	}
}
