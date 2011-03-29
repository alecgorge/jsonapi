package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Caller {
	public static boolean initted = false;
	public static HashMap<String, Method> methods;
	
	public Caller (File i) {
		initMethods(i);
	}
	
	public Object call(String method, Object[] params) {
		Object r = null;
		try {
			methods.get(method).getCall().call(params);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return r;
	}
	
	public static void initMethods (File methodsFile) {
		try {
			if(!initted) {			
				initted = true;
				
				JSONParser p = new JSONParser();
				JSONArray methods = (JSONArray)p.parse(new FileReader(methodsFile));
				
				for(Object o : methods) {
					if(o instanceof JSONObject) {
						Caller.methods.put(((JSONObject)o).get("name").toString(), new Method((JSONObject)o));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
