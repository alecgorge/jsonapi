package com.ramblingwood.minecraft.jsonapi;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONAware;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;

import com.ramblingwood.minecraft.jsonapi.dynamic.Caller;
import com.ramblingwood.minecraft.jsonapi.streams.ChatMessage;
import com.ramblingwood.minecraft.jsonapi.streams.ConnectionMessage;
import com.ramblingwood.minecraft.jsonapi.streams.ConsoleMessage;
import com.ramblingwood.minecraft.jsonapi.streams.JSONAPIStream;
import com.ramblingwood.minecraft.jsonapi.streams.StreamingResponse;


public class JSONServer extends NanoHTTPD {
	Hashtable<String, String> logins = new Hashtable<String, String>();
	private JSONAPI inst;
	private Logger outLog = Logger.getLogger("JSONAPI");
	private Caller caller;

	public ArrayList<ChatMessage> chat = new ArrayList<ChatMessage>(); 
	public ArrayList<ConsoleMessage> console = new ArrayList<ConsoleMessage>(); 
	public ArrayList<ConnectionMessage> connections = new ArrayList<ConnectionMessage>();
	public ArrayList<JSONAPIStream> all = new ArrayList<JSONAPIStream>();
	
	private static boolean initted = false;

	
	public JSONServer(Hashtable<String, String> logins, final JSONAPI plugin) throws IOException {
		super(plugin.port);
		inst = plugin;
		
		(new Thread(new Runnable() {
			@Override
			public void run() {
				outLog.info("[JSONAPI] Waiting 2 seconds to load methods so that all the other plugins load...");
				outLog.info("[JSONAPI] Any requests in this time will not work...");
				
				try {
					if(!initted) {
						Thread.sleep(2000);
						initted = true;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				caller = new Caller(inst);
				caller.loadFile(new File(inst.getDataFolder()+File.separator+"methods.json"));
				
				File[] files = (new File(inst.getDataFolder()+File.separator+"methods"+File.separator)).listFiles(new FilenameFilter() {
				    @Override
				    public boolean accept(File dir, String name) {
				        return name.endsWith(".json");
				    }
				});
				
				if(files != null && files.length > 0) {
					for(File f : files) {
						caller.loadFile(f);
					}
				}
				outLog.info("[JSONAPI] "+caller.methodCount+" methods loaded in "+caller.methods.size()+" namespaces.");
				outLog.info("[JSONAPI] JSON Server listening on "+plugin.port);
				outLog.info("[JSONAPI] JSON Stream Server listening on "+(plugin.port+1));
				outLog.info("[JSONAPI] JSON WebSocket Stream Server listening on "+(plugin.port+2));
				outLog.info("[JSONAPI] Active and listening for requests.");
			}
		})).start();
		
		this.logins = logins;
	}
	
	public Caller getCaller() {
		return caller;
	}
	
	public JSONAPI getInstance () {
		return inst;
	}
	
	public void logChat(String player, String message) {
		ChatMessage c = new ChatMessage(player, message);
		chat.add(c);
		all.add(c);
		trimLists();
	}
	
	private void trimLists () {
		if(chat.size() > 50) {
			chat.remove(0);
			chat.trimToSize();
		}		
		if(connections.size() > 50) {
			connections.remove(0);
			connections.trimToSize();
		}		
		if(console.size() > 50) {
			console.remove(0);
			console.trimToSize();
		}		
		if(all.size() > 50) {
			all.remove(0);
			all.trimToSize();
		}		
	}
	
	public void logConsole(String line) {
		ConsoleMessage c = new ConsoleMessage(line);
		console.add(c);
		all.add(c);
		trimLists();
	}
	
	public void logConnected(String player) {
		ConnectionMessage c = new ConnectionMessage(player, true);
		connections.add(c);
		all.add(c);
		trimLists();
	}
	
	public void logDisconnected(String player) {
		ConnectionMessage c = new ConnectionMessage(player, false);
		connections.add(c);
		all.add(c);
		trimLists();
	}	
	
	public boolean testLogin (String method, String hash) {
		try {
			boolean valid = false;
			
			Enumeration<String> e = logins.keys();
			
			while(e.hasMoreElements()) {
				String user = e.nextElement();
				String pass = logins.get(user);

				String thishash = JSONAPI.SHA256(user+method+pass+inst.salt);
				
				if(thishash.equals(hash)) {
					valid = true;
					break;
				}
			}
			
			return valid;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static String callback (String callback, String json) {
		if(callback == null || callback.equals("")) return json;
		return callback.concat("(").concat(json).concat(")");
	}
	
	public void info (final String log) {
		if(inst.logging || !inst.logFile.equals("false")) {
			outLog.info("[JSONAPI] " +log);
		}
	}	
	
	public void warning (final String log) {
		if(inst.logging || !inst.logFile.equals("false")) {
			outLog.warning("[JSONAPI] " +log);
		}
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public Response serve( String uri, String method, Properties header, Properties parms )	{
		String callback = parms.getProperty("callback");
		
		if(inst.whitelist.size() > 0 && !inst.whitelist.contains(header.get("X-REMOTE-ADDR"))) {
			outLog.warning("[JSONAPI] An API call from "+ header.get("X-REMOTE-ADDR") +" was blocked because "+header.get("X-REMOTE-ADDR")+" is not on the whitelist.");
			return jsonRespone(returnAPIError("", "You are not allowed to make API calls."), callback, HTTP_FORBIDDEN);			
		}

		if(uri.equals("/api/subscribe")) {
			String source = parms.getProperty("source");
			String key = parms.getProperty("key");

			if(!testLogin(source, key)) {
				info("[Streaming API] "+header.get("X-REMOTE-ADDR")+": Invalid API Key.");
				return jsonRespone(returnAPIError(source, "Invalid API key."), callback, HTTP_FORBIDDEN);
			}

			info("[Streaming API] "+header.get("X-REMOTE-ADDR")+": source="+ source);

			try {
				if(source == null) {
					throw new Exception();
				}
				
				ArrayList<? extends JSONAPIStream> arr;
				
				if(source.equals("chat")) {
					arr = chat;
				}
				else if(source.equals("connections")) {
					arr = connections;
				}
				else if(source.equals("console")) {
					arr = console;
				}
				else if(source.equals("all")) {
					arr = all;
				}
				else {
					throw new Exception();
				}

				StreamingResponse out = new StreamingResponse(arr, callback);

				return new NanoHTTPD.Response( HTTP_OK, MIME_PLAINTEXT, out);
			} catch (Exception e) {
				e.printStackTrace();
				return jsonRespone(returnAPIError(source, "'"+source+"' is not a valid stream source!"), callback, HTTP_NOTFOUND);
			}
		}
		
		if(!uri.equals("/api/call") && !uri.equals("/api/call-multiple")) {
			return new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "File not found.");
		}

		Object args = parms.getProperty("args","[]");
		String calledMethod = (String)parms.getProperty("method");

		if(calledMethod == null) {
			info("[API Call] "+header.get("X-REMOTE-ADDR")+": Parameter 'method' was not defined.");
			return jsonRespone(returnAPIError("", "Parameter 'method' was not defined."), callback, HTTP_NOTFOUND);
		}

		String key = parms.getProperty("key");
		if(!inst.method_noauth_whitelist.contains(calledMethod) && !testLogin(calledMethod, key)) {
			info("[API Call] "+header.get("X-REMOTE-ADDR")+": Invalid API Key.");
			return jsonRespone(returnAPIError("", "Invalid API key."), callback, HTTP_FORBIDDEN);
		}

		info("[API Call] "+header.get("X-REMOTE-ADDR")+": method="+ parms.getProperty("method").concat("?args=").concat((String) args));

		if(args == null || calledMethod == null) {
			return jsonRespone(returnAPIError(calledMethod, "You need to pass a method and an array of arguments."), callback, HTTP_NOTFOUND);
		}
		else {
			try {
				JSONParser parse = new JSONParser();
				args = parse.parse((String) args);

				if(uri.equals("/api/call-multiple")) {
					List<String> methods = new ArrayList<String>();
					List<Object> arguments = new ArrayList<Object>();
					Object o = parse.parse(calledMethod);
					if (o instanceof List<?> && args instanceof List<?>) {
						methods = (List<String>)o;
						arguments = (List<Object>)args;
					}
					else {
						return jsonRespone(returnAPIException(calledMethod, new Exception("method and args both need to be arrays for /api/call-multiple")), callback);
					}

					int size = methods.size();
					JSONArray arr = new JSONArray();
					for(int i = 0; i < size; i++) {
						arr.add(serveAPICall(methods.get(i), (arguments.size()-1 >= i ? arguments.get(i) : new ArrayList<Object>())));
					}

					return jsonRespone(returnAPISuccess(o, arr), callback);
				}
				else {
					return jsonRespone(serveAPICall(calledMethod, args), callback);
				}
			}
			catch (Exception e) {
				return jsonRespone(returnAPIException(calledMethod, e), callback);
			}
		}
	}

	public JSONObject returnAPIException (Object calledMethod, Exception e) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		StringWriter pw = new StringWriter();
		e.printStackTrace(new PrintWriter( pw ));
		e.printStackTrace();
		r.put("source", calledMethod);
		r.put("error", "Caught exception: "+pw.toString().replaceAll("\\n", "\n").replaceAll("\\r", "\r"));
		return r;
	}

	public JSONObject returnAPIError (Object calledMethod, String error) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		r.put("source", calledMethod);
		r.put("error", error);
		return r;
	}

	public JSONObject returnAPISuccess (Object calledMethod, Object result) {
		JSONObject r = new JSONObject();
		r.put("result", "success");
		r.put("source", calledMethod);
		r.put("success", result);
		return r;
	}

	public NanoHTTPD.Response jsonRespone (JSONAware o, String callback, String code) {
		return new NanoHTTPD.Response(code, MIME_JSON, callback(callback, o.toJSONString()));
	}

	public NanoHTTPD.Response jsonRespone (JSONAware o, String callback) {
		return jsonRespone(o, callback, HTTP_OK);
	}

	@SuppressWarnings("unchecked")
	public JSONObject serveAPICall(String calledMethod, Object args) {
		try {
			if(caller.methodExists(calledMethod)) {
				if(args instanceof JSONArray) {
					Object result = caller.call(calledMethod,
							(Object[]) ((ArrayList) args).toArray(new Object[((ArrayList) args).size()]));
					return returnAPISuccess(calledMethod, result);
				}
			}
			else {
				warning("The method '"+calledMethod+"' does not exist!");
				return returnAPIError(calledMethod, "The method '"+calledMethod+"' does not exist!");
			}
		}
		catch (NullPointerException e) {
			return returnAPIError(calledMethod, "The server is offline right now. Try again in 2 seconds.");
		}
		catch (Exception e) {
			return returnAPIException(calledMethod, e);
		}

		return returnAPIError(calledMethod, "You need to pass a method and an array of arguments.");
	}
}
