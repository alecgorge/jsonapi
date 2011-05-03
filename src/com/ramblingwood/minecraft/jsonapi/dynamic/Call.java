package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;

public class Call {
	private static Server Server = JSONAPI.instance.getServer();
	private static APIWrapperMethods APIInstance = APIWrapperMethods.getInstance();
	
	private Class<?>[] signature = new Class<?>[] {};
	private ArrayList<Object> stack = new ArrayList<Object>();
	private HashMap<Integer, String> defaults = new HashMap<Integer, String>();
	public static boolean debug = false;
	private int expectedArgs = 0;

	public Call (String input, ArgumentList args) {
		signature = args.getTypes();
		parseString(input);
	}
	
	private void debug(String in) {
		if(debug) {
			System.out.println(in);
		}
	}
	
	public int getNumberOfExpectedArgs () {
		return expectedArgs;
	}
	
	public Object call(Object[] params) throws Exception {
		int size = stack.size();
		
		ArrayList<Object> oParams = new ArrayList<Object>(Arrays.asList(params));
		
		oParams.ensureCapacity(oParams.size()+defaults.size());
		for(Integer i : defaults.keySet()) {
			oParams.add(i, defaults.get(i));
		}
		
		
		debug("oParams:"+oParams.toString());
		debug("Stack:"+stack);
		
		Object lastResult = null;
		for(int i = 0; i < size; i++) {
			Object v = stack.get(i);
			debug("v:"+v.getClass().getCanonicalName());
			if(v instanceof Server || v instanceof APIWrapperMethods || (i == 0 && v instanceof Plugin)) {
				lastResult = v;
			}
			else if(v instanceof SubField) {
				SubField obj = (SubField)v;
				
				if(obj.getName().equals("length") && lastResult.getClass().isArray()) {
					lastResult = Array.getLength(lastResult);
				}
				else {
					lastResult = lastResult.getClass().getField(obj.getName()).get(lastResult);
				}
			}
			else if(v instanceof SubCall) {
				SubCall obj = (SubCall)v;
				
				debug("Calling method: '"+obj.getName()+"' with signature: '"+obj.requiresArgs()+"' '"+Arrays.asList(sigForIndices(obj.requiresArgs()))+"'.");
				debug("Last result:"+lastResult.toString());
				debug("Invoking method: '"+obj.getName()+"' with args: '"+Arrays.asList(indicies(oParams, obj.requiresArgs()))+"'.");
				
				Object[] args = indicies(oParams, obj.requiresArgs());
				Class<?>[] sig = sigForIndices(obj.requiresArgs());
				for(int x = 0; x < args.length; x++) {
					Object val = args[x];
					if((val.getClass().equals(Long.class) || val.getClass().equals(Double.class) || val.getClass().equals(String.class) || val.getClass().equals(long.class) || val.getClass().equals(double.class)) && (sig[x].equals(Integer.class) || sig[x].equals(int.class))) {
						args[x] = Integer.valueOf(val.toString());
						val = args[x];
					}
					if((val.getClass().equals(Integer.class) || val.getClass().equals(Long.class) || val.getClass().equals(String.class) || val.getClass().equals(long.class) || val.getClass().equals(int.class)) && (sig[x].equals(Double.class) || sig[x].equals(double.class))) {
						args[x] = Double.valueOf(val.toString());
						val = args[x];
					}
					if((val.getClass().equals(Integer.class) || val.getClass().equals(Double.class) || val.getClass().equals(String.class) || val.getClass().equals(int.class) || val.getClass().equals(double.class)) && (sig[x].equals(Long.class) || sig[x].equals(long.class))) {
						args[x] = Long.valueOf(val.toString());
						val = args[x];
					}
					debug("Arg "+x+": '"+val+"', type: "+val.getClass().getName());
					debug("Sig type: "+sig[x].getName());
				}
				
				lastResult = lastResult.getClass().getMethod(obj.getName(), sig).invoke(lastResult, args);
				
				debug("New value:"+lastResult);
			}
		}
		
		return lastResult;
	}
	
	public Object[] indicies (ArrayList<Object> o, ArrayList<Integer>i) {
		if(i == null) { return new Object[] {}; }
		
		Object[] ret = new Object[i.size()];
		for(int y = 0; y < ret.length; y++) {
			ret[y] = o.get(i.get(y));
		}
		
		return ret;
	}
	
	public Class<?>[] sigForIndices (ArrayList<Integer> i) {
		if(i == null) { return new Class<?>[]{}; }
		
		Class<?>[] ret = new Class<?>[i.size()];
		for(int y = 0; y < ret.length; y++) {
			ret[y] = signature[i.get(y)];
		}
		
		return ret;
	}
	
	public void parseString (String input) {
		String[] parts = input.split("\\.(?=[a-zA-Z])");

		for(int i = 0; i < parts.length; i++) {
			String v = parts[i];
			//if(i == 0) {
				if(v.equals("Server")) {
					stack.add(Server);
				}
				else if(v.equals("this")) {
					stack.add(APIInstance);
				}
				else if(v.equals("Plugins")) { // handles Plugins.PLUGINNAME.pluginMethod(0,1,2)
					String v2 = parts[i+1];
					stack.add(Server.getPluginManager().getPlugin(v2));
					i++;
					continue;
				}
			//}
			else {
				// no args
				if(v.endsWith("()")) {
					stack.add(new SubCall(v.substring(0, v.length()-2), new ArrayList<Integer>()));
				}
				
				// field
				else if (!v.endsWith(")")) {
					stack.add(new SubField(v));
				}
				
				// args
				else {
					// find the position of the args
					int startPos = v.lastIndexOf("(");
					
					// take the stuff in the ('s and )'s 
					String[] argParts = v.substring(startPos+1, v.length() - 1).split(",");

					ArrayList<Integer> argPos = new ArrayList<Integer>();
					
					// put all string 0, 1, 2, 3 etc into a int[]
					int multiplier = 0;
					
					for(int x = 0; x < argParts.length; x++) {
						if(argParts[x].trim().startsWith("\"") && argParts[x].trim().endsWith("\"")) {
							defaults.put(x, argParts[x].trim().substring(1, argParts[x].trim().length() - 1));
							
							ArrayList<Class<?>> cc = new ArrayList<Class<?>>(Arrays.asList(signature));
							cc.add(x, String.class);
							signature = cc.toArray(signature);
							
							if(argPos.size() > 0) {
								argPos.add(argPos.get(argPos.size()-1)+1);
							}
							else {
								argPos.add(0);
							}
							
							multiplier++;
						}
						else {
							try {
								argPos.add(Integer.parseInt(argParts[x].trim()) + (multiplier));
							}
							catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}
					}
					
					expectedArgs = argPos.size() - multiplier;
					
					// add this "method" onto the stack
					stack.add(new SubCall(v.substring(0, startPos), argPos));
				}
			}
		}
	}
	
	static class SubField {
		private String name;
		
		public SubField(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	static class SubCall {
		private String name;
		private ArrayList<Integer> argPos;
		
		public SubCall(String name, ArrayList<Integer> argPos) {
			this.name = name;
			this.argPos = argPos;
		}
		
		public ArrayList<Integer> requiresArgs () {
			return (argPos.size() > 0 ? argPos : null);
		}
		
		public String getName() {
			return name;
		}
	}
}
