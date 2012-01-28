package com.alecgorge.minecraft.jsonapi.dynamic;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.alecgorge.minecraft.jsonapi.api.APIMethodName;
import com.alecgorge.minecraft.jsonapi.api.JSONAPICallHandler;

public class Caller {
	public HashMap<String, HashMap<String, Method>> methods = new HashMap<String, HashMap<String, Method>>();
	private JSONParser p = new JSONParser();
	private JSONAPI inst;
	private Logger outLog = Logger.getLogger("JSONAPI");
	public int methodCount = 0;
	
	private List<JSONAPICallHandler> handlers = new ArrayList<JSONAPICallHandler>();
	
	public Caller (JSONAPI plugin) {
		inst = plugin;
	}
	
	public Object call(String methodAndNamespace, final Object[] params) throws Exception {
		String[] methodParts = methodAndNamespace.split("\\.", 2);
		
		APIMethodName n = new APIMethodName(methodAndNamespace);
		for(JSONAPICallHandler c : handlers) {
			if(c.willHandle(n)) {
				return c.handle(n, params);
			}
		}		
		
		final Call c;
		if(methodParts.length == 1) {
			c = methods.get("").get(methodParts[0]).getCall();
		}
		else {
			c = methods.get(methodParts[0]).get(methodParts[1]).getCall();
		}
		
		if(params.length < c.getNumberOfExpectedArgs()) {
			throw new Exception("Incorrect number of args: gave "+params.length+" ("+Arrays.asList(params).toString()+"), expected "+c.getNumberOfExpectedArgs());
		}
		
		return innerCall(c, params);
	}
	
	private Object innerCall(final Call c, final Object[] params) {
		Future<Object> f = inst.getServer().getScheduler().callSyncMethod(inst, new Callable<Object> () {
			public Object call() throws Exception {
				return c.call(params);
			}
		});
		
		try {
			return f.get();
		} catch (InterruptedException e) {
		     System.out.println("Interrupt triggered which waiting on callable to return");
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.getCause().printStackTrace();
		}
		return null;
	}
	
	public boolean methodExists (String name) {
		String[] methodParts = name.split("\\.", 2);
		
		APIMethodName n = new APIMethodName(name);
		for(JSONAPICallHandler c : handlers) {
			if(c.willHandle(n)) {
				return true;
			}
		}
		
		if(methodParts.length == 1) {
			return methods.get("").containsKey(methodParts[0]);
		}
		else {
			return methods.containsKey(methodParts[0]) && methods.get(methodParts[0]).containsKey(methodParts[1]);
		}
	}
	
	public void loadFile (File methodsFile) {
		try {
			magicWithMethods(p.parse(new FileReader(methodsFile)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void registerAPICallHandler(JSONAPICallHandler handler) {
		handlers.add(handler);
	}
	
	public void deregisterAPICallHandler(JSONAPICallHandler handler) {
		handlers.remove(handler);
	}	
	
	private void magicWithMethods (Object raw) throws Exception {
		if(raw instanceof JSONObject) {
			JSONObject methods = (JSONObject)raw;
			String name = "";
			
			if(methods.containsKey("name")) {
				name = methods.get("name").toString();
			}
			else {
				throw new Exception("A JSON file is not well formed: missing the key 'name' for the root object.");
			}
			
			if(methods.containsKey("depends")) {
				Object deps = methods.get("depends");
				List<String> pluginNames = new ArrayList<String>();
				
				if(deps instanceof JSONArray) {
					for(Object o : ((JSONArray)deps)) {
						pluginNames.add(o.toString());
					}
				}
				else {
					pluginNames.add(deps.toString());
				}
				
				for(String plugin : pluginNames) {
					plugin = plugin.trim();
					Plugin p = inst.getServer().getPluginManager().getPlugin(plugin);
					
					if(p == null && !plugin.equals("JSONAPI")) {
						outLog.info("[JSONAPI] "+name+" cannot be loaded because it depends on a plugin that is not installed: '"+plugin+"'");
					}
					else if(plugin.equals("JSONAPI") || p.isEnabled()) {
						if(methods.containsKey("methods")) {
							proccessMethodsWithNamespace((JSONArray)methods.get("methods"), methods.get("namespace").toString());
						}
						else {
							throw new Exception("A JSON file is not well formed: missing the key 'methods' for the root object.");
						}
					}
					else {
						outLog.info("[JSONAPI] "+name+" cannot be loaded because it depends on a plugin that is not enabled: '"+plugin+"'");
					}
				}
			}
		}
		else if(raw instanceof JSONArray) {
			proccessMethodsWithNamespace((JSONArray)raw, "");
		}
		else {
			throw new Exception("JSON file is not a valid methods file.");
		}
	}
	
	public void proccessMethodsWithNamespace(JSONArray methods, String namespace) {	
		for(Object o : methods) {
			if(o instanceof JSONObject) {
				String name = ((JSONObject)o).get("name").toString();
				
				if(this.methods.containsKey(name)) {
					Logger.getLogger("Minecraft").info("[JSONAPI] The method " + name + " already exists! It is being overridden.");
				}
				
				if(!this.methods.containsKey(namespace)) {
					this.methods.put(namespace, new HashMap<String, Method>());
				}
				
				methodCount++;
				this.methods.get(namespace).put(name, new Method((JSONObject)o));
			}
		}	
	}
	
	public void loadString (String methodsString) {
		try {
			magicWithMethods(p.parse(methodsString));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
