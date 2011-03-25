package com.bukkit.alecgorge.jsonapi;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class JSONServer extends NanoHTTPD {
	Hashtable<String, Object> methods = new Hashtable<String, Object>();
	Hashtable<String, String> logins = new Hashtable<String, String>();
	private JSONAPI inst;
	private Logger outLog = Logger.getLogger("JSONAPI");

	public JSONServer(Hashtable<String, String> logins, JSONAPI plugin) throws IOException {
		super(plugin.port);
		inst = plugin;
		
		this.logins = logins;
	}
	
	public Object callMethod(String method, String[] signature, Object[] params) throws Exception {
		String[] parts = method.split("\\.");

		Class<?>[] ps = new Class<?>[signature.length];
		for(int i = 0; i< signature.length; i++) {
			try {
				ps[i] = Class.forName(signature[i]);
			}
			catch(ClassNotFoundException e) {
				ps[i] = Class.forName("java.lang."+signature[i]);
			}
		}

		Class c = Class.forName(parts[0]);
		Object lastResult = new Object();
		for(int i = 0; i < parts.length; i++) {
			if(i == 0) {
				lastResult = c.getMethod(parts[i+1], null).invoke(null, null);
			}
			else if(i == (parts.length - 1)) {
				return lastResult;
			}
			else {
				lastResult = lastResult.getClass().getMethod(parts[i+1], ps).invoke(lastResult, params);
			}
		}

		return lastResult;
	}
	
	public boolean testLogin (String method, String hash) {
		try {
			boolean valid = false;
			
			Enumeration<String> e = logins.keys();
			
			while(e.hasMoreElements()) {
				String user = e.nextElement();
				String pass = logins.get(user);
				
				String thishash = inst.SHA256(user+method+pass+inst.salt);
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
		if(callback == null) return json;
		return callback.concat("(").concat(json).concat(")");
	}
	
	public void info (final String log) {
		if(inst.logging || inst.logFile != "false") {
			outLog.info(log);
		}
	}	
	
	@Override
	public Response serve( String uri, String method, Properties header, Properties parms )	{
		String callback = parms.getProperty("callback");

		/*if(uri.equals("/api/subscribe")) {
			String source = parms.getProperty("source");
			String key = parms.getProperty("key");

			if(!testLogin(source, key)) {
				info("[Streaming API] "+header.get("X-REMOTE-ADDR")+": Invalid API Key.");
				return jsonRespone(returnAPIError("Invalid API key."), callback, HTTP_FORBIDDEN);
			}

			info("[Streaming API] "+header.get("X-REMOTE-ADDR")+": source="+ source);

			try {
				if(source == null)
					throw new Exception();

				// HttpStream.handler will now be non-null
				HttpStream out = new HttpStream(source, callback);

				return new NanoHTTPD.Response( HTTP_OK, MIME_PLAINTEXT, out);
			} catch (Exception e) {
				e.printStackTrace();
				return jsonRespone(returnAPIError("That source doesn't exist!"), callback, HTTP_NOTFOUND);
			}
		}

		if(!uri.equals("/api/call") && !uri.equals("/api/call-multiple")) {
			boolean valid = false;

			// use basic authentication for other file access
			// not the most secure but whatever...
			// IMPORTANT all headers are lowercase
			String authHeader = header.getProperty("authorization");

			if (authHeader != null && !authHeader.equals("")) {
				try {
					StringTokenizer st = new StringTokenizer(authHeader);
					if (st.hasMoreTokens()) {
						String basic = st.nextToken();

						// We only handle HTTP Basic authentication
						if (basic.equalsIgnoreCase("Basic")) {
							String credentials = st.nextToken();
							String userPass = new String(Base64Coder.decode(credentials));

							// The decoded string is in the form
							// "userID:password".
							int p = userPass.indexOf(":");
							if (p != -1) {
								String authU = userPass.substring(0, p).trim();
								String authP = userPass.substring(p + 1).trim();

								try {
									if(logins.get(authU).equals(authP)) {
										valid = true;
									}
								} catch (Exception e) {
									valid = false;
								}
							}
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}

			if(!valid) {
				NanoHTTPD.Response r = new NanoHTTPD.Response(HTTP_UNAUTHORIZED, MIME_PLAINTEXT, "Use a WebUI username & password combination.");
				r.addHeader("WWW-Authenticate", "Basic realm=\"hMod Server Login\"");
				return r;
			}

			info("[WebUI] Serving file: "+uri);

			return serveFile(uri, header, new File("www/"), true);
		}*/
		//System.out.println()

		Object args = parms.getProperty("args","[]");
		Object sig = parms.getProperty("signature","[]");
		String calledMethod = (String)parms.getProperty("method");

		if(calledMethod == null) {
			info("[API Call] "+header.get("X-REMOTE-ADDR")+": Method doesn't exist.");
			return jsonRespone(returnAPIError("Method doesn't exist!"), callback, HTTP_NOTFOUND);
		}

		String key = parms.getProperty("key");
		if(!testLogin(calledMethod, key)) {
			info("[API Call] "+header.get("X-REMOTE-ADDR")+": Invalid API Key.");
			return jsonRespone(returnAPIError("Invalid API key."), callback, HTTP_FORBIDDEN);
		}


		info("[API Call] "+header.get("X-REMOTE-ADDR")+": method="+ parms.getProperty("method").concat("?args=").concat((String) args));

		if(args == null || calledMethod == null) {
			return jsonRespone(returnAPIError("You need to pass a method and an array of arguments."), callback, HTTP_NOTFOUND);
		}
		else {
			try {
				JSONParser parse = new JSONParser();
				args = parse.parse((String) args);
				sig = parse.parse((String)sig);

				if(uri.equals("/api/call-multiple")) {
					List<String> methods = new ArrayList<String>();
					List<Object> arguments = new ArrayList<Object>();
					List<Object> signatures = new ArrayList<Object>();
					Object o = parse.parse(calledMethod);
					if (o instanceof List && args instanceof List && sig instanceof List) {
						methods = (List<String>)o;
						arguments = (List<Object>)args;
						signatures = (List<Object>)sig;
					}
					else {
						return jsonRespone(returnAPIException(new Exception("method, args and signature all need to be arrays for /api/call-multiple"), callback), callback);
					}

					int size = methods.size();
					JSONArray arr = new JSONArray();
					for(int i = 0; i < size; i++) {
						arr.add(serveAPICall(methods.get(i), (signatures.size()-1 >= i ? signatures.get(i) : new ArrayList<String>()), (arguments.size()-1 >= i ? arguments.get(i) : new ArrayList<Object>()), callback));
					}

					return jsonRespone(returnAPISuccess(o, arr), callback);
				}
				else {
					return jsonRespone(serveAPICall(calledMethod, sig, args, callback), callback);
				}
			}
			catch (Exception e) {
				return jsonRespone(returnAPIException(e, callback), callback);
			}
		}
	}

	public JSONObject returnAPIException (Exception e, String callback) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
		StringWriter pw = new StringWriter();
		e.printStackTrace(new PrintWriter( pw ));
		e.printStackTrace();
		r.put("error", "Caught exception: "+pw.toString());
		return r;
	}

	public JSONObject returnAPIError (String error) {
		JSONObject r = new JSONObject();
		r.put("result", "error");
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

	public JSONObject serveAPICall(String calledMethod, Object sig, Object args, String callback) {
		try {
			if(args.getClass().getCanonicalName().endsWith("JSONArray")) {
				Object result = callMethod(calledMethod,
						// TODO Make this suck less.
						// ick, this is why I hate Java. maybe I am just doing it wrong...
						(String[]) ((ArrayList) sig).toArray(new String[((ArrayList) sig).size()]),
						(Object[]) ((ArrayList) args).toArray(new Object[((ArrayList) args).size()]));
				return returnAPISuccess(calledMethod, result);
			}
		}
		catch (Exception e) {
			return returnAPIException(e, callback);
		}

		return returnAPIError("You need to pass a method and an array of arguments.");
	}
}
