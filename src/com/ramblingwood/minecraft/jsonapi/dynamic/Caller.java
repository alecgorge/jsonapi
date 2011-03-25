package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Caller {
	public static boolean initted = false;
	public static ArrayList<Method> methods;
	
	public Caller (File i) {
		init(i);
	}
	
	public static void init (File methodsFile) {
		try {
			if(!initted) {			
				initted = true;
				
				JSONParser p = new JSONParser();
				JSONArray methods = (JSONArray)p.parse(new FileReader(methodsFile));
				
				for(Object o : methods) {
					if(o instanceof JSONObject) {
						Caller.methods.add(new Method((JSONObject)o));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
