package com.alecgorge.minecraft.jsonapi;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.simpleForBukkit.JSONArray;
import org.json.simpleForBukkit.JSONObject;
import org.json.simpleForBukkit.parser.JSONParser;
import org.json.simpleForBukkit.parser.ParseException;

import com.alecgorge.minecraft.jsonapi.dynamic.Caller;
import com.alecgorge.minecraft.jsonapi.event.JSONAPIAuthEvent;
import com.alecgorge.minecraft.jsonapi.streams.ChatMessage;
import com.alecgorge.minecraft.jsonapi.streams.ChatStream;
import com.alecgorge.minecraft.jsonapi.streams.ConnectionMessage;
import com.alecgorge.minecraft.jsonapi.streams.ConnectionStream;
import com.alecgorge.minecraft.jsonapi.streams.ConsoleMessage;
import com.alecgorge.minecraft.jsonapi.streams.ConsoleStream;
import com.alecgorge.minecraft.jsonapi.streams.StreamingResponse;

public class JSONServer extends NanoHTTPD {
	HashMap<String, String> logins = new HashMap<String, String>();
	private JSONAPI inst;
	private Logger outLog = Logger.getLogger("JSONAPI");
	private Caller caller;

	public ChatStream chat = new ChatStream();
	public ConsoleStream console = new ConsoleStream();
	public ConnectionStream connections = new ConnectionStream();

	private static boolean initted = false;

	public JSONServer(HashMap<String, String> auth, final JSONAPI plugin, final long startupDelay) throws IOException {
		super(plugin.port, plugin.bindAddress);
		inst = plugin;

		caller = new Caller(inst);
		caller.loadFile(new File(inst.getDataFolder() + File.separator + "methods.json"));

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
						caller.loadFile(f);
					}
				}
				outLog.info("[JSONAPI] " + caller.methodCount + " methods loaded in " + caller.methods.size() + " namespaces.");
				outLog.info("[JSONAPI] JSON Server listening on " + plugin.port);
				outLog.info("[JSONAPI] JSON Stream Server listening on " + (plugin.port + 1));
				outLog.info("[JSONAPI] JSON WebSocket Stream Server listening on " + (plugin.port + 2));
				outLog.info("[JSONAPI] Active and listening for requests.");
			}
		})).start();

		this.logins = auth;
	}

	public Caller getCaller() {
		return caller;
	}

	public JSONAPI getInstance() {
		return inst;
	}

	public void logChat(String player, String message) {
		chat.addMessage(new ChatMessage(player, message));
	}

	public void logConsole(String line) {
		console.addMessage(new ConsoleMessage(line));
	}

	public void logConnected(String player) {
		connections.addMessage(new ConnectionMessage(player, true));
	}

	public void logDisconnected(String player) {
		connections.addMessage(new ConnectionMessage(player, false));
	}

	public boolean testLogin(String method, String hash) {
		try {
			boolean valid = false;

			for (String user : logins.keySet()) {
				String pass = logins.get(user);

				String thishash = JSONAPI.SHA256(user + method + pass + inst.salt);

				if (thishash.equals(hash)) {
					valid = true;
					break;
				}
			}

			JSONAPIAuthEvent e = new JSONAPIAuthEvent(valid, method, hash, logins);
			inst.getServer().getPluginManager().callEvent(e);

			return e.getValid();
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

			return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, out);
		} else if (!uri.equals("/api/call") && !uri.equals("/api/call-multiple")) {
			return new NanoHTTPD.Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "File not found.");
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
					return jsonRespone(serveAPICall(calledMethod, args), callback);
				}
			} catch (Exception e) {
				return jsonRespone(returnAPIException(calledMethod, e), callback);
			}
		}
	}

	public JSONObject returnAPIException(Object calledMethod, Exception e) {
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

		return new NanoHTTPD.Response(code, MIME_JSON, callback(callback, o.toJSONString()));
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
		} catch (Exception e) {
			return returnAPIException(calledMethod, e);
		}

		// return returnAPIError(calledMethod,
		// "You need to pass a method and an array of arguments.");
	}
}
