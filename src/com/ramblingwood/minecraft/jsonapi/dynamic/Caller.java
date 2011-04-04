package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;
import org.json.simpleForBukkit.parser.ParseException;

public class Caller {
	public HashMap<String, Method> methods = new HashMap<String, Method>();
	
	public Caller () {
	}
	
	public Object call(String method, Object[] params) {
		Object r = null;
		try {
			// System.out.println("Called:"+method);
			// System.out.println("with args:"+Arrays.asList(params).toString());
			r = methods.get(method).getCall().call(params);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return r;
	}
	
	public boolean methodExists (String name) {
		return methods.containsKey(name);
	}
	
	public void loadFile (File methodsFile) {
		JSONParser p = new JSONParser();
		JSONArray methods;
		try {
			methods = (JSONArray)p.parse(new FileReader(methodsFile));
		
			for(Object o : methods) {
				if(o instanceof JSONObject) {
					String name = ((JSONObject)o).get("name").toString();
					
					if(this.methods.containsKey(name)) {
						Logger.getLogger("Minecraft").warning("[JSONAPI] The method " + name + " already exists! It is being overridden.");
					}
					
					this.methods.put(name, new Method((JSONObject)o));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
