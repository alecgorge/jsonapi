package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.util.HashMap;

import org.json.simpleForBukkit.JSONArray;

public class Argument {
	private Class<?> type;
	private String desc;
	private Object value = null;
	private static HashMap<String, Class<?>> mapping = new HashMap<String, Class<?>>();
	
	static {
		mapping.put("int", Integer.class);
		mapping.put("double", Double.class);
		mapping.put("boolean", Boolean.class);
		mapping.put("float", Float.class);
		mapping.put("String", String.class);
		mapping.put("Integer", Integer.class);
		mapping.put("Player", org.bukkit.entity.Player.class);
		mapping.put("Server", org.bukkit.Server.class);
		mapping.put("World", org.bukkit.World.class);
		mapping.put("World[]", org.bukkit.World[].class);
		mapping.put("Player[]", org.bukkit.entity.Player[].class);
		mapping.put("Plugin", org.bukkit.plugin.Plugin.class);
		mapping.put("Plugin[]", org.bukkit.plugin.Plugin[].class);
	}
	
	public Argument(JSONArray a) {
		type = getClassFromName((String)a.get(0));
		desc = (String)a.get(1); 
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
						e3.printStackTrace();
					}
				}
			}
		}
		return ret;
	}
}
