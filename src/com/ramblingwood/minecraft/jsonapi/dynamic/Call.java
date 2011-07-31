package com.ramblingwood.minecraft.jsonapi.dynamic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import com.ramblingwood.minecraft.jsonapi.JSONAPI;

public class Call {
	private static Server Server = JSONAPI.instance.getServer();
	private static APIWrapperMethods APIInstance = APIWrapperMethods.getInstance();
	
	private ArrayList<Class<?>> signature;
	private ArrayList<Object> stack = new ArrayList<Object>();
	private ArrayList<String> flags = new ArrayList<String>();
	private HashMap<Integer, Object> defaults = new HashMap<Integer, Object>();
	public static boolean debug = false;
	private int expectedArgs = 0;

	public Call (String input, ArgumentList args, ArrayList<String> flags) {
		this(input, new ArrayList<Class<?>>(Arrays.asList(args.getTypes())), flags);
	}

	public Call (String input,  ArrayList<Class<?>> sig, ArrayList<String> flags) {
		signature = sig;
		this.flags = flags;
		debug("INPUT: "+input);
		parseString(input);
	}
	
	private void debug(Object in) {
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
		
		debug("defaults:"+defaults);
		debug("oParams:"+oParams);
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
				debug("Last result:"+lastResult == null ? null : lastResult.toString());
				debug("Invoking method: '"+obj.getName()+"' with args: '"+Arrays.asList(indicies(oParams, obj.requiresArgs()))+"'.");
				debug("Requires arg: "+obj.requiresArgs());
				
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
				
				debug("CLASS: "+lastResult.getClass());
				debug("SUBCALL NAME: "+obj.getName());
				debug("Sig:"+Arrays.toString(sig));
				debug("Args:"+Arrays.toString(args));
				
				int count = 0;
				for(Class<?> s : sig) {
					if(s.equals(Call.class)) {
						Object res = ((Call)args[count]).call(params);
						sig[count] = res.getClass();
						args[count] = res;
					}
					count++;
				}
				debug("Sig:"+Arrays.toString(sig));
				debug("Args:"+Arrays.toString(args));
				java.lang.reflect.Method thisMethod = lastResult.getClass().getMethod(obj.getName(), sig);
				if(flags.contains("NO_EXCEPTIONS") || flags.contains("FALSE_ON_EXCEPTION")) {
					try {
						lastResult = thisMethod.invoke(lastResult, args);
					}
					catch (Exception e) {
						if(flags.contains("FALSE_ON_EXCEPTION")) {
							return false;
						}
					}
				}
				else {
					lastResult = thisMethod.invoke(lastResult, args);
				}
				
				debug("New value:"+lastResult);
			}
			else if(v instanceof Call) {
				Call c = (Call)v;
				
				lastResult = c.call(params);
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
			ret[y] = signature.get(i.get(y));
		}
		
		return ret;
	}
	
	public void parseString (String input) {
		int parOpen = 0;
		List<String> parts = new ArrayList<String>();
		int xx = 0;
		int lastIndex = 0;
		boolean isOpen = false;
		for(char c : input.toCharArray()) {
			if(c == '(') {
				parOpen++;
				isOpen = true;
			}
			else if(c == ')') {
				parOpen--;
				
				if(parOpen == 0) {
					isOpen = false;
					//System.out.println(input.length());
					//System.out.println(xx);
					if(input.length()-1 == xx) {
						parts.add(input.substring(lastIndex == 0 ? lastIndex : lastIndex+1, xx+1));
						lastIndex = xx;
					}
				}
			}
			else if(!isOpen && c == '.') {
				parts.add(input.substring(lastIndex == 0 ? lastIndex : lastIndex+1, xx));
				lastIndex = xx;
			}
				
			xx++;
		}

		debug("PARTS: "+parts);
		int size = parts.size();
		int argcnt = 0;
		for(int i = 0; i < size; i++) {
			String v = parts.get(i);
			
			if(v.equals("Server")) {
				stack.add(Server);
			}
			else if(v.equals("this")) {
				stack.add(APIInstance);
			}
			else if(v.equals("Plugins")) { // handles Plugins.PLUGINNAME.pluginMethod(0,1,2)
				String v2 = parts.get(i+1);
				stack.add(Server.getPluginManager().getPlugin(v2));
				i++;
				continue;
			}
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
					int startPos = v.indexOf("(");
					
					// take the stuff in the ('s and )'s 
					String[] argParts = v.substring(startPos+1, v.length() - 1).split(",");
					debug("ARG PARTS: "+Arrays.toString(argParts));

					ArrayList<Integer> argPos = new ArrayList<Integer>();
					
					// put all string 0, 1, 2, 3 etc into a int[]
					
					// multiplyer is the number of args to skip due to string hard codes
					int multiplier = 0;
					
					for(int x = 0; x < argParts.length; x++, argcnt++) {
						String arg = argParts[x].trim();
						debug("x: ("+String.valueOf(x)+")");
						try {
							// if arg is a number (user supplied argument) then this will go just peachy
							// otherwise we can handle thing in the exception
							argPos.add(Integer.parseInt(arg) + multiplier);
						}
						catch (NumberFormatException e) {
							// default placeholder string
							if(arg.startsWith("\"") && arg.endsWith("\"")) {
								defaults.put(argcnt, arg.substring(1, arg.length() - 1));
								signature.add(argcnt, String.class);
								
								if(argPos.size() > 0) {
									argPos.add(argPos.get(argPos.size()-1)+1);
								}
								else {
									argPos.add(0);
								}
								
								multiplier++;
							}
							else if(arg.startsWith("\\")) {
								try {
									debug("ISNUMBERARG:"+argcnt);
									defaults.put(argcnt, Integer.parseInt(arg.substring(1)));
									signature.add(argcnt, Integer.TYPE);
									
									if(argPos.size() > 0) {
										argPos.add(argPos.get(argPos.size()-1)+1);
									}
									else {
										argPos.add(0);
									}
									
									multiplier++;
								}
								catch (Exception e1) {
									e1.printStackTrace();
									System.err.println("Expected number after the \\");
								}
							}
							// assume a subcall
							else {
								debug("MAKING NEW CALL");
								defaults.put(argcnt, new Call(arg, signature, flags));
								signature.add(argcnt, Call.class);
								
								if(argPos.size() > 0) {
									argPos.add(argPos.get(argPos.size()-1)+1);
								}
								else {
									argPos.add(0);
								}
								
								multiplier++;
							}
						}
					}
					
					expectedArgs = argPos.size() - multiplier;
					
					// add this "method" onto the stack
					debug("ARG PART: "+v);
					stack.add(new SubCall(v, argPos));
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
			int i = name.indexOf("(");
			this.name = name.substring(0, i == -1 ? name.length() : i+1);
			
			if(this.name.endsWith("(")) {
				this.name = this.name.substring(0, this.name.length()-1);
			}
			
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
