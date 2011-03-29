package com.ramblingwood.minecraft.jsonapi.dynamic;

import org.json.simple.JSONArray;

public class Argument {
	private Class<?> type;
	private String desc;
	private Object value = null;
	
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
		if(name.equals("int")) {
			name = "java.lang.Integer";
		}
		Class<?> ret = void.class;
		try {
			ret = Class.forName(name);
		} catch (ClassNotFoundException e) {
			try {
				ret = Class.forName("java.lang."+name);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		return ret;
	}
}
