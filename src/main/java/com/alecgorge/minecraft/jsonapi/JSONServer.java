package com.alecgorge.minecraft.jsonapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;
import org.json.simpleForBukkit.parser.ParseException;

import com.alecgorge.java.http.MutableHttpRequest;
import com.alecgorge.minecraft.jsonapi.api.v2.APIv2Handler;
import com.alecgorge.minecraft.jsonapi.api.v2.EssentialsAPIMethods;
import com.alecgorge.minecraft.jsonapi.api.v2.StandardAPIMethods;
import com.alecgorge.minecraft.jsonapi.config.UsersConfig;
import com.alecgorge.minecraft.jsonapi.dynamic.APIWrapperMethods;
import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.streams.ChatMessage;
import com.alecgorge.minecraft.jsonapi.streams.ChatStream;
import com.alecgorge.minecraft.jsonapi.streams.EggMessage;
import com.alecgorge.minecraft.jsonapi.streams.EggStream;
import com.alecgorge.minecraft.jsonapi.streams.ConnectionMessage;
import com.alecgorge.minecraft.jsonapi.streams.ConnectionStream;
import com.alecgorge.minecraft.jsonapi.streams.ConsoleMessage;
import com.alecgorge.minecraft.jsonapi.streams.ConsoleStream;
import com.alecgorge.minecraft.jsonapi.streams.FormattedChatMessage;
import com.alecgorge.minecraft.jsonapi.streams.FormattedChatStream;
import com.alecgorge.minecraft.jsonapi.streams.PerformanceStream;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class JSONServer extends NanoHTTPD {
	public UsersConfig logins;
	private JSONAPI inst;
	private Logger outLog = JSONAPI.instance.outLog;
	private Caller caller;

	public ChatStream chat = new ChatStream("chat");
	public EggStream eggStream = new EggStream("egg");
	public FormattedChatStream formattedChat = new FormattedChatStream("formatted_chat");
	public ConsoleStream console = new ConsoleStream("console");
	public ConnectionStream connections = new ConnectionStream("connections");
	public PerformanceStream performance = new PerformanceStream("performance");

	private static boolean initted = false;

	public JSONServer(UsersConfig auth, final JSONAPI plugin, final long startupDelay) throws IOException {
		super(plugin.port, plugin.bindAddress);
		inst = plugin;

		caller = new Caller(inst);
		caller.loadFile(new File(inst.getDataFolder() + File.separator + "methods.json"), false);
		
		outLog.info("[JSONAPI] Loaded methods.json.");

		(new Thread(new Runnable() {
			@Override
			public void run() {
				float seconds = startupDelay / 1000;
				outLog.info("[JSONAPI] Waiting " + String.format("%2.3f", seconds) + " seconds to load methods so that all the other plugins load...");
				outLog.info("[JSONAPI] Any requests in this time will not work...");

				try {
					if (!initted) {
						Thread.sleep(startupDelay);
						initted = true;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				File[] files = (new File(inst.getDataFolder() + File.separator + "methods" + File.separator)).listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".json");
					}
				});

				if (files != null && files.length > 0) {
					for (File f : files) {
						caller.loadFile(f, false);
					}
				}
				
				String[] methodsFiles = new String[] { "chat.json", "dynmap.json", "econ.json", "fs.json", "permissions.json",
													   "players.json", "plugins.json", "remotetoolkit.json", "server.json",
													   "streams.json", "system.json", "worlds.json", "jsonapi.json" };
				
				for(String m : methodsFiles) {
					caller.loadInputStream(inst.getResource("jsonapi4/methods/" + m), true);
				}
				
				caller.registerMethods(APIWrapperMethods.getInstance());
				new EssentialsAPIMethods(inst);
				new StandardAPIMethods(inst);
				
				outLog.info("[JSONAPI] " + caller.methodCount + " methods loaded in " + caller.methods.size() + " namespaces.");
				
				connectionInfo();
			}
		})).start();

		this.logins = auth;
	}

	
	void connectionInfo() {
        outLog.info("[JSONAPI] ------[Connection information]-------");
		outLog.info("[JSONAPI] JSON Server listening on " + inst.port);
		outLog.info("[JSONAPI] JSON Stream Server listening on " + (inst.port + 1));
		outLog.info("[JSONAPI] JSON WebSocket Stream Server listening on " + (inst.port + 2));
		
		if(inst.sslJsonWebSocketServer != null){
			outLog.info("[JSONAPI] JSON WebSocket Secure Stream Server listening on " + (inst.port + 3));			
		}
		
		outLog.info("[JSONAPI] Active and listening for requests.");

		try {
			URL whatismyip = new URL("http://tools.alecgorge.com/ip.php");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

            String ip = in.readLine();
            
            URL checkURL = new URL("http://tools.alecgorge.com/port_check.php");
            
            outLog.info("[JSONAPI] External IP: " + ip);

            for(int i : new int[] { inst.port, inst.port + 1, inst.port + 2 }) {
            	MutableHttpRequest reqReg = new MutableHttpRequest(checkURL);
	            reqReg.addGetValue("host", ip);
	            reqReg.addGetValue("port", String.valueOf(i));
	            
	            if(reqReg.get().getStatusCode() == 200) {
	            	outLog.info("[JSONAPI] Port " + i + " is properly forwarded and is externally accessible.");
	            }
	            else {
	            	outLog.info("[JSONAPI] Port " + i + " is not properly forwarded.");
	            }
            }
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        outLog.info("[JSONAPI] -------------------------------------");
	}
	
	
	public UsersConfig getLogins() {
		return logins;
	}

	public Caller getCaller() {
		return caller;
	}

	public JSONAPI getInstance() {
		return inst;
	}

	public void logChat(AsyncPlayerChatEvent e) {
		if(inst.isEnabled()) {
			chat.addMessage(new ChatMessage(e));
			formattedChat.addMessage(new FormattedChatMessage(e));
		}
	}

	public void logChat(String player, String message) {
		if(inst.isEnabled()) {
			chat.addMessage(new ChatMessage(player, message));
		}
	}

	public void logConsole(String line) {
		if(inst.isEnabled()) {
			console.addMessage(new ConsoleMessage(line));
		}
	}

	public void logConnected(String player) {
		if(inst.isEnabled()) {
			connections.addMessage(new ConnectionMessage(player, true));
		}
	}

	public void logDisconnected(String player) {
		if(inst.isEnabled()) {
			connections.addMessage(new ConnectionMessage(player, false));
		}
	}

        public void logEggThrow(PlayerEggThrowEvent e) {
		if(inst.isEnabled()) {
			eggStream.addMessage(new EggMessage(e));
		}
        }

	public boolean testLogin(String method, String hash) {
		try {
			boolean valid = false;
			for (Map<String, Object> user : logins.getUsers()) {
				String thishash = JSONAPI.SHA256(user.get("username") + method + user.get("password") + inst.salt);
				String saltless = JSONAPI.SHA256(user.get("username") + method + user.get("password"));

				if (thishash.equals(hash) || saltless.equals(hash)) {
					valid = true;
					break;
				}
			}

			return valid;
		} catch (Exception e) {
			return false;
		}
	}

	public static String callback(String callback, String json) {
		if (callback == null || callback.equals(""))
			return json;
		return callback.concat("(").concat(json).concat(")");
	}

	public void info(final String log) {
		if (inst.logging || !inst.logFile.equals("false")) {
			outLog.info("[JSONAPI] " + log);
		}
	}

	public void warning(final String log) {
		if (inst.logging || !inst.logFile.equals("false")) {
			outLog.warning("[JSONAPI] " + log);
		}
	}

	private void setLastRequestParms(Properties parms) {
		synchronized (parms) {
			this.lastRequestParms = parms;
		}
	}

	private Properties getLastRequestParms() {
		synchronized (lastRequestParms) {
			return lastRequestParms;
		}
	}

	private Properties lastRequestParms = null;

	@SuppressWarnings("unchecked")
	@Override
	public Response serve(String uri, String method, Properties header, Properties parms) {
		if(method.equals("OPTIONS")) {
			Response r = new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "");
			
			r.addHeader("Access-Control-Allow-Origin", "*");
			r.addHeader("Access-Control-Allow-Methods", "GET, POST");
			
			return r;
		}
		
		if(uri.startsWith("/api/2/") || inst.useGroups) {
			APIv2Handler handler = new APIv2Handler(uri, method, header, parms, this);
			return handler.serve();
		}
		String callback = parms.getProperty("callback");
		setLastRequestParms(parms);

		if (inst.whitelist.size() > 0 && !inst.whitelist.contains(header.get("X-REMOTE-ADDR"))) {
			outLog.warning("[JSONAPI] An API call from " + header.get("X-REMOTE-ADDR") + " was blocked because " + header.get("X-REMOTE-ADDR") + " is not on the whitelist.");
			return jsonRespone(returnAPIError("", "You are not allowed to make API calls."), callback, HTTP_FORBIDDEN);
		}

		if (uri.equals("/api/subscribe")) {
			String source = parms.getProperty("source");
			String sources = parms.getProperty("sources");
			String key = parms.getProperty("key");

			Object prev = parms.getProperty("show_previous");
			boolean showOlder;
			if (prev == null) {
				showOlder = true;
			} else {
				if (prev.equals("false")) {
					showOlder = false;
				} else {
					showOlder = true;
				}
			}

			List<String> sourceList = new ArrayList<String>();
			if (source != null) {
				if (!testLogin(source, key)) {
					info("[Streaming API] " + header.get("X-REMOTE-ADDR") + ": Invalid API Key.");
					return jsonRespone(returnAPIError(source, "Invalid API key."), callback, HTTP_FORBIDDEN);
				}

				if (source.equals("all")) {
					sourceList = new ArrayList<String>(JSONAPI.instance.getStreamManager().getStreams().keySet());
				} else {
					sourceList.add(source);
				}
			} else if (sources != null) {
				if (!testLogin(sources, key)) {
					info("[Streaming API] " + header.get("X-REMOTE-ADDR") + ": Invalid API Key.");
					return jsonRespone(returnAPIError(source, "Invalid API key."), callback, HTTP_FORBIDDEN);
				}
				JSONParser p = new JSONParser();
				try {
					for (Object o : (JSONArray) p.parse(sources)) {
						sourceList.add(o.toString());
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			info("[Streaming API] " + header.get("X-REMOTE-ADDR") + ": source=" + sourceList.toString());
			StreamingResponse out = new StreamingResponse(inst, sourceList, callback, showOlder, parms.containsKey("tag") ? parms.getProperty("tag") : null);

			Response r = new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, out);
			r.addHeader("Access-Control-Allow-Origin", "*");
			return r;
		} else if (!uri.equals("/api/call") && !uri.equals("/api/call-multiple")) {
			Response r = new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "File not found.");
			r.addHeader("Access-Control-Allow-Origin", "*");
			return r;
		}

		Object args = parms.getProperty("args", "[]");
		String calledMethod = (String) parms.getProperty("method");

		if (calledMethod == null) {
			info("[API Call] " + header.get("X-REMOTE-ADDR") + ": Parameter 'method' was not defined.");
			return jsonRespone(returnAPIError("", "Parameter 'method' was not defined."), callback, HTTP_NOTFOUND);
		}

		String key = parms.getProperty("key");
		if (!inst.method_noauth_whitelist.contains(calledMethod) && !testLogin(calledMethod, key)) {
			info("[API Call] " + header.get("X-REMOTE-ADDR") + ": Invalid API Key.");
			return jsonRespone(returnAPIError(calledMethod, "Invalid API key."), callback, HTTP_FORBIDDEN);
		}

		info("[API Call] " + header.get("X-REMOTE-ADDR") + ": method=" + parms.getProperty("method").concat("?args=").concat((String) args));

		if (args == null || calledMethod == null) {
			return jsonRespone(returnAPIError(calledMethod, "You need to pass a method and an array of arguments."), callback, HTTP_NOTFOUND);
		} else {
			try {
				JSONParser parse = new JSONParser();
				args = parse.parse((String) args);

				if (uri.equals("/api/call-multiple")) {
					List<String> methods = new ArrayList<String>();
					List<Object> arguments = new ArrayList<Object>();
					Object o = parse.parse(calledMethod);
					if (o instanceof List<?> && args instanceof List<?>) {
						methods = (List<String>) o;
						arguments = (List<Object>) args;
					} else {
						return jsonRespone(returnAPIException(calledMethod, new Exception("method and args both need to be arrays for /api/call-multiple")), callback);
					}

					int size = methods.size();
					JSONArray arr = new JSONArray();
					for (int i = 0; i < size; i++) {
						arr.add(serveAPICall(methods.get(i), (arguments.size() - 1 >= i ? arguments.get(i) : new ArrayList<Object>())));
					}

					return jsonRespone(returnAPISuccess(o, arr), callback);
				} else {
					// work around because Adminium 2.1.1 doesn't parse the version correctly
					// it says that 4.0.1 < 3.4.5. Always return 3.6.7 for that.
					// :sadface:
					if(calledMethod.equals("getPluginVersion")
					&& args instanceof List<?>
					&& ((List<Object>) args).get(0).toString().equals("JSONAPI")
					&& header.getProperty("user-agent").startsWith("Adminium 2.1.1")) {
						calledMethod = "polyfill_getPluginVersion";
					}
					return jsonRespone(serveAPICall(calledMethod, args), callback);
				}
			} catch (Exception e) {
				return jsonRespone(returnAPIException(calledMethod, e), callback);
			}
		}
	}

	public JSONObject returnAPIException(Object calledMethod, Throwable e) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		StringWriter pw = new StringWriter();
		e.printStackTrace(new PrintWriter(pw));
		e.printStackTrace();
		r.put("source", calledMethod);
		r.put("error", "Caught exception: " + pw.toString().replaceAll("\\n", "\n").replaceAll("\\r", "\r"));
		return r;
	}

	public JSONObject returnAPIError(Object calledMethod, String error) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		r.put("source", calledMethod);
		r.put("error", error);
		return r;
	}

	public JSONObject returnAPISuccess(Object calledMethod, Object result) {
		JSONObject r = new JSONObject();
		r.put("result", "success");
		r.put("source", calledMethod);
		r.put("success", result);
		return r;
	}

	public NanoHTTPD.Response jsonRespone(JSONObject o, String callback, String code) {
		Properties p = getLastRequestParms();
		if (p != null && p.containsKey("tag")) {
			o.put("tag", p.get("tag"));
		}

		NanoHTTPD.Response r = new NanoHTTPD.Response(code, MIME_JSON, callback(callback, o.toJSONString()));
		r.addHeader("Access-Control-Allow-Origin", "*");
		return r;
	}

	public NanoHTTPD.Response jsonRespone(JSONObject o, String callback) {
		return jsonRespone(o, callback, HTTP_OK);
	}

	@SuppressWarnings("unchecked")
	public JSONObject serveAPICall(String calledMethod, Object args) {
		try {
			if (caller.methodExists(calledMethod)) {
				if (!(args instanceof JSONArray)) {
					args = new JSONArray();
				}
				Object result = caller.call(calledMethod, (Object[]) ((ArrayList<Object>) args).toArray(new Object[((ArrayList<Object>) args).size()]));
				return returnAPISuccess(calledMethod, result);
			} else {
				warning("The method '" + calledMethod + "' does not exist!");
				return returnAPIError(calledMethod, "The method '" + calledMethod + "' does not exist!");
			}
		} catch (APIException e) {
			return returnAPIError(calledMethod, e.getMessage());
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof APIException) {
				return returnAPIError(calledMethod, e.getCause().getMessage());
			}
			return returnAPIException(calledMethod, e);
		} catch (NullPointerException e) {
			return returnAPIError(calledMethod, "The server is offline right now. Try again in 2 seconds.");
		} catch (Throwable e) {
			return returnAPIException(calledMethod, e);
		}
	}
}
